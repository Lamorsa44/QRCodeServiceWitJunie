package com.example.qrservice

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.client.j2se.MatrixToImageWriter
import java.io.File

@RestController
class QrController {

    @GetMapping("/health", produces = [MediaType.TEXT_PLAIN_VALUE])
    fun health(): ResponseEntity<String> {
        return ResponseEntity("Service is running", HttpStatus.OK)
    }

    // Dynamic content type based on 'type' parameter
    @GetMapping("/qr")
    fun qr(
        @RequestParam(required = false) size: Int?,
        @RequestParam(required = false) type: String?,
        @RequestParam(required = false) contents: String?
    ): ResponseEntity<ByteArray> {
        val (params, error) = parseParams(size, type, contents)
        if (error != null) return badRequest(error)

        return try {
            val bytes = generateQrBytes(params!!)

            val headers = HttpHeaders()
            headers.contentType = if (params.type == "png") MediaType.IMAGE_PNG else MediaType.IMAGE_JPEG
            headers.contentLength = bytes.size.toLong()

            ResponseEntity(bytes, headers, HttpStatus.OK)
        } catch (e: Exception) {
            badRequest("Failed to generate QR code: ${e.message}")
        }
    }

    // New endpoint to save the QR code image to disk and return a plain text confirmation
    @GetMapping("/qr/save", produces = [MediaType.TEXT_PLAIN_VALUE])
    fun qrSave(
        @RequestParam(required = false) size: Int?,
        @RequestParam(required = false) type: String?,
        @RequestParam(required = false) contents: String?
    ): ResponseEntity<String> {
        val (params, error) = parseParams(size, type, contents)
        if (error != null) return badRequestText(error)

        return try {
            val bytes = generateQrBytes(params!!)

            // Use system temp directory to be portable; create a subfolder "qr"
            val dir: Path = Paths.get(System.getProperty("java.io.tmpdir"), "qr")
            Files.createDirectories(dir)

            val timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now())
            val extension = if (params.type == "png") "png" else "jpeg"
            val filename = "qr_${timestamp}.${extension}"
            val filePath = dir.resolve(filename)

            Files.write(filePath, bytes)

            ResponseEntity("Saved QR code to $filePath", HttpStatus.OK)
        } catch (e: Exception) {
            ResponseEntity("Failed to save QR code: ${e.message}", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    // Shared helpers
    private data class QrParams(val size: Int, val type: String, val contents: String)

    private fun parseParams(size: Int?, type: String?, contents: String?): Pair<QrParams?, String?> {
        val finalSize = size ?: 250
        if (finalSize !in 100..1000) {
            return Pair(null, "Invalid 'size' parameter. Allowed range is 100..1000.")
        }

        val finalType = (type ?: "png").lowercase()
        val allowedTypes = setOf("png", "jpeg")
        if (finalType !in allowedTypes) {
            return Pair(null, "Invalid 'type' parameter. Allowed values are 'png' or 'jpeg'.")
        }

        val finalContents = contents ?: ""
        return Pair(QrParams(finalSize, finalType, finalContents), null)
    }

    private fun generateQrBytes(params: QrParams): ByteArray {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(params.contents, BarcodeFormat.QR_CODE, params.size, params.size)
        val baos = ByteArrayOutputStream()
        MatrixToImageWriter.writeToStream(bitMatrix, params.type, baos)
        return baos.toByteArray()
    }

    private fun badRequest(message: String): ResponseEntity<ByteArray> {
        val bytes = message.toByteArray(StandardCharsets.UTF_8)
        val headers = HttpHeaders()
        headers.contentType = MediaType.TEXT_PLAIN
        headers.contentLength = bytes.size.toLong()
        return ResponseEntity(bytes, headers, HttpStatus.BAD_REQUEST)
    }

    private fun badRequestText(message: String): ResponseEntity<String> {
        return ResponseEntity(message, HttpStatus.BAD_REQUEST)
    }
}
