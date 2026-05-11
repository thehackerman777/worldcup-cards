package com.wcapp.scanner.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.DisplayMetrics
import android.view.*
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.wcapp.scanner.data.local.ScanDatabase
import com.wcapp.scanner.data.model.ScannedCard
import com.wcapp.scanner.ocr.OcrProcessor

/**
 * Overlay service que muestra un botón flotante sobre otras apps.
 * Al presionarlo, captura la pantalla y procesa el texto con ML Kit.
 */
class ScannerOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayButton: View
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private val ocrProcessor = OcrProcessor()
    private val mainHandler = Handler(Looper.getMainLooper())

    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "scanner_overlay"
        const val EXTRA_PROJECTION_DATA = "projection_data"
        const val EXTRA_RESULT_CODE = "result_code"
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        showOverlayButton()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        // Restore MediaProjection if we have the data
        intent?.let {
            val resultCode = it.getIntExtra(EXTRA_RESULT_CODE, -1)
            val data = it.getParcelableExtra(EXTRA_PROJECTION_DATA, Intent::class.java)
            if (resultCode != -1 && data != null) {
                startProjection(resultCode, data)
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopProjection()
        removeOverlayButton()
        super.onDestroy()
    }

    // ── Overlay Button ────────────────────────────────────

    private fun showOverlayButton() {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 50
            y = 200
        }

        overlayButton = View(this).apply {
            setBackgroundColor(Color.parseColor("#1A6B52"))
            setOnClickListener { captureScreen() }

            // Circular button 56dp
            val size = (56 * resources.displayMetrics.density).toInt()
            layoutParams = ViewGroup.LayoutParams(size, size)
            setOnApplyWindowInsetsListener(null)

            // Round shape
            background = android.graphics.drawable.GradientDrawable().apply {
                setShape(android.graphics.drawable.GradientDrawable.OVAL)
                setColor(Color.parseColor("#1A6B52"))
                setStroke(3, Color.WHITE)
            }

            contentDescription = "Scan cards"
        }

        windowManager.addView(overlayButton, params)
    }

    private fun removeOverlayButton() {
        try {
            if (::overlayButton.isInitialized) {
                windowManager.removeView(overlayButton)
            }
        } catch (e: Exception) {
            // View already removed
        }
    }

    // ── MediaProjection ───────────────────────────────────

    fun startProjection(resultCode: Int, data: Intent) {
        val projectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = projectionManager.getMediaProjection(resultCode, data)

        val metrics = DisplayMetrics().also {
            windowManager.defaultDisplay.getMetrics(it)
        }

        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val density = metrics.densityDpi

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)

        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScannerCapture",
            width, height, density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface, null, null
        )
    }

    private fun stopProjection() {
        virtualDisplay?.release()
        imageReader?.close()
        mediaProjection?.stop()
        virtualDisplay = null
        imageReader = null
        mediaProjection = null
    }

    // ── Screen Capture ────────────────────────────────────

    private fun captureScreen() {
        val reader = imageReader ?: return

        val image = reader.acquireLatestImage() ?: return
        val bitmap = imageToBitmap(image)
        image.close()

        if (bitmap != null) {
            processBitmap(bitmap)
        }
    }

    private fun imageToBitmap(image: android.media.Image): Bitmap? {
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * image.width

        val bitmap = Bitmap.createBitmap(
            image.width + rowPadding / pixelStride,
            image.height, Bitmap.Config.ARGB_8888
        )
        bitmap.copyPixelsFromBuffer(buffer)
        return Bitmap.createBitmap(bitmap, 0, 0, image.width, image.height)
    }

    // ── OCR Processing ────────────────────────────────────

    private fun processBitmap(bitmap: Bitmap) {
        ocrProcessor.recognizeText(bitmap) { results ->
            results.forEach { scanResult ->
                saveScanResult(scanResult)
            }
            // Auto-hide overlay briefly to show feedback
            flashFeedback(results.size)
        }
    }

    private fun saveScanResult(card: ScannedCard) {
        Thread {
            try {
                val db = ScanDatabase.getInstance(applicationContext)
                val existing = db.scanDao().getByCardCode(card.cardCode)
                if (existing != null) {
                    db.scanDao().insert(existing.copy(quantity = existing.quantity + 1, isDuplicate = true))
                } else {
                    db.scanDao().insert(card)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun flashFeedback(count: Int) {
        val originalColor = Color.parseColor("#1A6B52")
        overlayButton.setBackgroundColor(if (count > 0) Color.GREEN else Color.RED)
        mainHandler.postDelayed({
            overlayButton.setBackgroundColor(originalColor)
        }, 500)
    }

    // ── Notification ──────────────────────────────────────

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Scanner Overlay",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificación del escáner de cartas"
                setShowBadge(false)
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Scanner activo")
            .setContentText("Toca el botón flotante para escanear")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
