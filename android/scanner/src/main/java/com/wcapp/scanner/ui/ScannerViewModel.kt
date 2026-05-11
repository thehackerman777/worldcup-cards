package com.wcapp.scanner.ui

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.media.projection.MediaProjectionManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wcapp.scanner.data.local.ScanDatabase
import com.wcapp.scanner.data.model.ScannedCard
import com.wcapp.scanner.service.ScannerOverlayService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ScannerUiState(
    val isOverlayActive: Boolean = false,
    val totalScanned: Int = 0,
    val totalDuplicates: Int = 0,
    val recentScans: List<ScannedCard> = emptyList(),
    val error: String? = null
)

class ScannerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = ScanDatabase.getInstance(application)
    private val dao = db.scanDao()

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    init {
        // Observe scan totals
        viewModelScope.launch {
            dao.totalScanned().collect { count ->
                _uiState.update { it.copy(totalScanned = count) }
            }
        }
        viewModelScope.launch {
            dao.totalDuplicates().collect { count ->
                _uiState.update { it.copy(totalDuplicates = count) }
            }
        }
        viewModelScope.launch {
            dao.getAllScans().collect { scans ->
                _uiState.update { it.copy(recentScans = scans.take(50)) }
            }
        }
    }

    /**
     * Solicita permiso para capturar pantalla y arranca el overlay.
     */
    fun requestOverlay(activity: Activity) {
        val projectionManager = activity.getSystemService(Activity.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val intent = projectionManager.createScreenCaptureIntent()
        activity.startActivityForResult(intent, OVERLAY_REQUEST_CODE)
    }

    /**
     * Maneja el resultado del permiso de captura de pantalla.
     */
    fun handleProjectionResult(resultCode: Int, data: Intent?, activity: Activity) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            val serviceIntent = Intent(activity, ScannerOverlayService::class.java).apply {
                putExtra(ScannerOverlayService.EXTRA_RESULT_CODE, resultCode)
                putExtra(ScannerOverlayService.EXTRA_PROJECTION_DATA, data)
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                activity.startForegroundService(serviceIntent)
            } else {
                activity.startService(serviceIntent)
            }
            _uiState.update { it.copy(isOverlayActive = true) }
        } else {
            _uiState.update { it.copy(error = "Permiso de captura denegado") }
        }
    }

    /**
     * Detiene el overlay.
     */
    fun stopOverlay(activity: Activity) {
        activity.stopService(Intent(activity, ScannerOverlayService::class.java))
        _uiState.update { it.copy(isOverlayActive = false) }
    }

    /**
     * Obtiene scans no sincronizados.
     */
    suspend fun getUnsyncedScans(): List<ScannedCard> = dao.getUnsyncedScans()

    /**
     * Marca scans como sincronizados.
     */
    suspend fun markSynced(ids: List<Long>) = dao.markSynced(ids)

    /**
     * Limpia scans ya sincronizados.
     */
    suspend fun clearSynced() = dao.clearSynced()

    companion object {
        const val OVERLAY_REQUEST_CODE = 9001
    }
}
