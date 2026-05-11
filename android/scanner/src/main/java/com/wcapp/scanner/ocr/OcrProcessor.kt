package com.wcapp.scanner.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.wcapp.scanner.data.model.ScannedCard

/**
 * Procesador de texto usando Google ML Kit.
 * Reconoce tarjetas/cartas con formato como "N° 123" o "FWC-001".
 */
class OcrProcessor {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    // Patrones para identificar cartas
    private val cardCodePattern = Regex("""[A-Z]{3}[- ]?\d{3}""")
    private val cardNumberPattern = Regex("""N[°º]\s*(\d{1,3})""")
    private val quantityPattern = Regex("""(\d+)\s*x|Cantidad[:]\s*(\d+)|x\s*(\d+)""")
    private val duplicatePattern = Regex("""(?i)(duplicado|repetido|repetida|\bx2\b|\bx3\b|\bx4\b)""")

    /**
     * Procesa un bitmap y extrae información de cartas.
     */
    fun recognizeText(bitmap: Bitmap, onComplete: (List<ScannedCard>) -> Unit) {
        val image = InputImage.fromBitmap(bitmap, 0)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val cards = extractCards(visionText.text)
                onComplete(cards)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                onComplete(emptyList())
            }
    }

    /**
     * Extrae cartas del texto reconocido.
     */
    fun extractCards(text: String): List<ScannedCard> {
        val cards = mutableListOf<ScannedCard>()
        val lines = text.lines()

        var currentQuantity = 1
        var isDuplicate = false

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            // Detectar cantidad y duplicados
            quantityPattern.find(trimmed)?.let { match ->
                currentQuantity = (match.groupValues[1].toIntOrNull()
                    ?: match.groupValues[2].toIntOrNull()
                    ?: match.groupValues[3].toIntOrNull()) ?: 1
                isDuplicate = currentQuantity > 1
            }

            if (duplicatePattern.containsMatchIn(trimmed)) {
                isDuplicate = true
                currentQuantity = maxOf(currentQuantity, 2)
            }

            // Buscar código de carta (FWC-001)
            cardCodePattern.find(trimmed)?.let { match ->
                val code = match.value.replace(" ", "")
                cards.add(ScannedCard(
                    cardCode = code,
                    cardName = extractCardName(lines, trimmed),
                    quantity = currentQuantity,
                    isDuplicate = isDuplicate || currentQuantity > 1
                ))
                // Resetear para la siguiente carta
                currentQuantity = 1
                isDuplicate = false
            }

            // Buscar número de carta (N° 123)
            cardNumberPattern.find(trimmed)?.let { match ->
                val num = match.groupValues[1]
                cards.add(ScannedCard(
                    cardCode = "FWC-$num",
                    cardName = extractCardName(lines, trimmed),
                    quantity = currentQuantity,
                    isDuplicate = isDuplicate || currentQuantity > 1
                ))
                currentQuantity = 1
                isDuplicate = false
            }
        }

        return cards.distinctBy { it.cardCode }
    }

    private fun extractCardName(lines: List<String>, currentLine: String): String? {
        // Intentar obtener el nombre de la línea anterior o siguiente
        val idx = lines.indexOf(currentLine)
        if (idx > 0 && lines[idx - 1].trim().length in 3..50) {
            return lines[idx - 1].trim()
        }
        if (idx < lines.size - 1 && lines[idx + 1].trim().length in 3..50) {
            return lines[idx + 1].trim()
        }
        return null
    }
}
