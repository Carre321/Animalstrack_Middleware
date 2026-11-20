package com.tonin.animaltrack.service.impl;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.tonin.animaltrack.model.ContentData;
import com.tonin.animaltrack.model.ContentException;
import com.tonin.animaltrack.model.ContentMetadata;
import com.tonin.animaltrack.model.DataException;
import com.tonin.animaltrack.service.ContentService;
import com.tonin.animaltrack.utils.FileUtils;

public class ContentServiceFileImpl implements ContentService {
    private final Path baseFolder;

    public ContentServiceFileImpl() {
        this(configuredFolder());
    }

    public ContentServiceFileImpl(Path baseFolder) {
        this.baseFolder = baseFolder.toAbsolutePath().normalize();
    }

    public Path getBaseFolder() {
        return baseFolder;
    }

    private static Path configuredFolder() {
        String folder = System.getProperty("animalstrack.cms.folder");
        if (folder == null || folder.trim().isEmpty()) folder = System.getenv("ANIMALSTRACK_CMS_FOLDER");
        if (folder != null && !folder.trim().isEmpty()) return Paths.get(folder);
        // Cada configuracion de base de datos tiene su almacen hermano.
        String configuration = System.getProperty("animalstrack.config");
        if (configuration != null) return Paths.get(configuration).toAbsolutePath().getParent().resolve("cms");
        return Paths.get(System.getProperty("user.home"), ".animalstrack", "cms");
    }

    protected String getPath(ContentMetadata metadata) {
        if (metadata == null) throw new IllegalArgumentException("Faltan los metadatos del contenido.");
        return entityFolder(metadata.getEntityType(), metadata.getEntityId())
                .resolve(segment(metadata.getResourceId()) + "." + format(metadata.getFormat())).toString();
    }

    private Path entityFolder(String entityType, String entityId) {
        return baseFolder.resolve(segment(entityType)).resolve(segment(entityId));
    }

    private static String segment(String value) {
        if (value == null || !value.matches("[A-Za-z0-9][A-Za-z0-9_-]*")) {
            throw new IllegalArgumentException("Identificador de contenido no valido.");
        }
        return value;
    }

    private static String format(String value) {
        if (value == null || !value.matches("[A-Za-z0-9]+")) {
            throw new IllegalArgumentException("Formato de contenido no valido.");
        }
        return value.toLowerCase(Locale.ROOT);
    }

    /** No seguir enlaces que puedan llevar las operaciones fuera del almacen. */
    private void checkPath(Path path) throws IOException {
        if (!path.startsWith(baseFolder)) throw new IOException("Ruta fuera del almacen de contenidos.");
        for (Path part = path; part != null; part = part.getParent()) {
            try {
                BasicFileAttributes attributes = Files.readAttributes(part, BasicFileAttributes.class,
                        LinkOption.NOFOLLOW_LINKS);
                if (attributes.isSymbolicLink()) throw new IOException("El almacen no admite enlaces simbolicos.");
                if (!part.equals(path) && !attributes.isDirectory()) {
                    throw new IOException("Una carpeta del almacen es un fichero.");
                }
            } catch (NoSuchFileException exception) {
                // Una carpeta nueva es valida; seguir comprobando sus antecesores.
            }
        }
    }

    @Override
    public ContentData get(ContentMetadata metadata) throws ContentException {
        try {
            Path path = Paths.get(getPath(metadata));
            checkPath(path);
            ContentData data = new ContentData();
            data.setRaw(FileUtils.readFully(path.toString()));
            data.setMetadata(metadata);
            return data;
        } catch (NoSuchFileException exception) {
            return null;
        } catch (IOException | IllegalArgumentException exception) {
            throw new ContentException("No se pudo leer el contenido.", exception);
        }
    }

    @Override
    public List<ContentData> get(String entityType, String entityId) throws ContentException {
        List<ContentData> result = new ArrayList<ContentData>();
        for (ContentMetadata metadata : listMetadata(entityType, entityId)) {
            ContentData data = get(metadata);
            if (data != null) result.add(data);
        }
        return result;
    }

    @Override
    public List<ContentMetadata> listMetadata(String entityType, String entityId) throws ContentException {
        List<ContentMetadata> result = new ArrayList<>();
        try {
            Path folder = entityFolder(entityType, entityId);
            checkPath(folder);
            try (DirectoryStream<Path> entries = Files.newDirectoryStream(folder)) {
                List<Path> files = new ArrayList<Path>();
                for (Path path : entries) files.add(path);
                files.sort(Comparator.comparing(path -> path.getFileName().toString()));
                for (Path path : files) {
                    String name = path.getFileName().toString();
                    if (name.startsWith(".") || name.endsWith(".tmp")) continue;
                    int dot = name.lastIndexOf('.');
                    if (dot <= 0 || dot == name.length() - 1) continue;
                    checkPath(path);
                    BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class,
                            LinkOption.NOFOLLOW_LINKS);
                    if (!attributes.isRegularFile()) continue;
                    ContentMetadata metadata = new ContentMetadata();
                    metadata.setEntityType(entityType);
                    metadata.setEntityId(entityId);
                    metadata.setResourceId(name.substring(0, dot));
                    metadata.setFormat(name.substring(dot + 1));
                    // Los nombres externos que no cumplen la convencion no son contenidos del CMS.
                    if (!metadata.getResourceId().matches("[A-Za-z0-9][A-Za-z0-9_-]*")
                            || !metadata.getFormat().matches("[A-Za-z0-9]+")) continue;
                    result.add(metadata);
                }
            }
            return result;
        } catch (NoSuchFileException exception) {
            return result;
        } catch (IOException | IllegalArgumentException exception) {
            throw new ContentException("No se pudieron listar los contenidos.", exception);
        }
    }

    @Override
    public void save(ContentData data) throws ContentException {
        try {
            if (data == null || data.getRaw() == null) {
                throw new IllegalArgumentException("Falta el contenido que se quiere guardar.");
            }
            Path path = Paths.get(getPath(data.getMetadata()));
            checkPath(path);
            FileUtils.write(data.getRaw(), path.toString(), false);
        } catch (IOException | IllegalArgumentException exception) {
            throw new ContentException("No se pudo guardar el contenido.", exception);
        }
    }

    @Override
    public void remove(ContentMetadata metadata) throws DataException {
        try {
            Path path = Paths.get(getPath(metadata));
            checkPath(path);
            // Nunca borrar directorios mediante una operacion de contenido.
            try {
                if (!Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS).isRegularFile()) {
                    throw new IOException("El contenido no es un fichero normal.");
                }
                Files.delete(path);
            } catch (NoSuchFileException exception) {
                // Un borrado repetido ya esta satisfecho.
            }
        } catch (IOException | IllegalArgumentException exception) {
            throw new DataException("No se pudo eliminar el contenido.", exception);
        }
    }

    @Override
    public void remove(String id) throws DataException {
        if (id == null) throw new DataException("Falta el identificador del contenido.");
        String[] parts = id.replace('\\', '/').split("/", -1);
        if (parts.length != 3) throw new DataException("Usa tipoEntidad/idEntidad/recurso.formato.");
        int dot = parts[2].lastIndexOf('.');
        if (dot <= 0) throw new DataException("Falta el formato del contenido.");
        ContentMetadata metadata = new ContentMetadata();
        metadata.setEntityType(parts[0]);
        metadata.setEntityId(parts[1]);
        metadata.setResourceId(parts[2].substring(0, dot));
        metadata.setFormat(parts[2].substring(dot + 1));
        remove(metadata);
    }
}
