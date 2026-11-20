package com.tonin.animaltrack.utils;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

public final class FileUtils {
    private FileUtils() { }

    public static byte[] readFully(String filePath) throws IOException {
        return Files.readAllBytes(Paths.get(filePath));
    }

    /** false sustituye el contenido; nunca se trunca el destino antes de escribir. */
    public static void write(byte[] raw, String filePath, boolean append) throws IOException {
        Path destination = Paths.get(filePath).toAbsolutePath().normalize();
        Files.createDirectories(destination.getParent());
        if (append) {
            Files.write(destination, raw, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return;
        }
        Path temporary = Files.createTempFile(destination.getParent(), ".content-", ".tmp");
        try {
            Files.write(temporary, raw);
            try {
                Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
