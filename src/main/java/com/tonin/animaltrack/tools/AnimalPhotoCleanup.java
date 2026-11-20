package com.tonin.animaltrack.tools;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import com.tonin.animaltrack.model.ContentData;
import com.tonin.animaltrack.model.ContentMetadata;
import com.tonin.animaltrack.service.AnimalService;
import com.tonin.animaltrack.service.impl.AnimalServiceImpl;
import com.tonin.animaltrack.service.impl.ContentServiceFileImpl;

/** Limpieza explicita desde el equipo que tiene las fotos, tras borrados externos. */
public final class AnimalPhotoCleanup {
    private AnimalPhotoCleanup() { }

    public static List<ContentMetadata> findOrphans(AnimalService animals, ContentServiceFileImpl contents)
            throws Exception {
        List<ContentMetadata> result = new ArrayList<ContentMetadata>();
        Path folder = contents.getBaseFolder().resolve("animal");
        // El CMS valida los caminos antes de acceder a cualquier contenido.
        if (Files.isSymbolicLink(folder)) throw new IllegalStateException("El almacen no admite enlaces simbolicos.");
        try (DirectoryStream<Path> directories = Files.newDirectoryStream(folder)) {
            for (Path directory : directories) {
                String id = directory.getFileName().toString();
                if (!id.matches("[1-9][0-9]*")) continue;
                BasicFileAttributes attributes = Files.readAttributes(directory, BasicFileAttributes.class,
                        LinkOption.NOFOLLOW_LINKS);
                if (!attributes.isDirectory()) continue;
                Long animalId = Long.valueOf(id);
                // findById incluye animales de baja y evita el limite de findAll.
                if (animals.findById(animalId) != null) continue;
                for (ContentData content : contents.get("animal", id)) {
                    if ("foto".equals(content.getMetadata().getResourceId())) result.add(content.getMetadata());
                }
            }
        } catch (NoSuchFileException exception) {
            return result;
        }
        return result;
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 1 || (args.length == 1 && !"--delete".equals(args[0]))) {
            throw new IllegalArgumentException("Sin argumentos: solo listar. --delete: borrar fotos huerfanas.");
        }
        boolean delete = args.length == 1;
        ContentServiceFileImpl contents = new ContentServiceFileImpl();
        AnimalService animals = new AnimalServiceImpl();
        List<ContentMetadata> orphans = findOrphans(animals, contents);
        System.out.println("Almacen: " + contents.getBaseFolder());
        for (ContentMetadata photo : orphans) {
            String id = photo.getEntityType() + "/" + photo.getEntityId() + "/"
                    + photo.getResourceId() + "." + photo.getFormat();
            if (delete && animals.findById(Long.valueOf(photo.getEntityId())) == null) {
                contents.remove(photo);
                System.out.println("Eliminada: " + id);
            } else System.out.println("Candidata: " + id);
        }
        System.out.println(delete ? "Limpieza terminada." : "Solo consulta: no se ha borrado ningun fichero.");
    }
}
