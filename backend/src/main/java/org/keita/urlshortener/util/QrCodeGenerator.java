package org.keita.urlshortener.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Утилита для генерации QR-кодов в формате PNG.
 */
public class QrCodeGenerator {

    /**
     * Генерирует QR-код для указанного текста.
     *
     * @param text текст, который нужно закодировать
     * @param size размер картинки (ширина и высота в пикселях)
     * @return массив байт PNG-изображения
     */
    public static byte[] generatePng(String text, int size) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, size, size);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", os);
            return os.toByteArray();
        }
    }
}
