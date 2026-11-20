package com.tonin.animaltrack.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/** Validación de las fotos de animales y usuarios. */
public final class PhotoUtils {
    public static final int MAX_BYTES = 20 * 1024 * 1024;
    private static final long MAX_PIXELS = 40_000_000L;

    private PhotoUtils() { }

    /** Valida los bytes de la imagen y devuelve el formato que usa el CMS. */
    public static String format(byte[] bytes) {
        if (bytes == null || bytes.length == 0 || bytes.length > MAX_BYTES) {
            throw new IllegalArgumentException("La imagen está vacía o supera los 20 MB.");
        }
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw new IllegalArgumentException("Selecciona una imagen JPG, PNG o GIF válida.");
            ImageReader reader = readers.next();
            try {
                reader.setInput(input);
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                if ("jpeg".equals(format)) format = "jpg";
                if (!"jpg".equals(format) && !"png".equals(format) && !"gif".equals(format)) {
                    throw new IllegalArgumentException("Selecciona una imagen JPG, PNG o GIF.");
                }
                if ((long) reader.getWidth(0) * reader.getHeight(0) > MAX_PIXELS) {
                    throw new IllegalArgumentException("La imagen supera los 40 millones de píxeles.");
                }
                if (reader.read(0) == null) throw new IllegalArgumentException("No se pudo leer la imagen.");
                return format;
            } finally {
                reader.dispose();
            }
        } catch (IOException exception) {
            throw new IllegalArgumentException("El archivo no contiene una imagen válida.", exception);
        }
    }
}
