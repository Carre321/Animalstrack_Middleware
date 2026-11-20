package unit;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;
import com.tonin.animaltrack.model.ContentData;
import com.tonin.animaltrack.model.ContentException;
import com.tonin.animaltrack.model.ContentMetadata;
import com.tonin.animaltrack.model.DataException;
import com.tonin.animaltrack.service.impl.ContentServiceFileImpl;

public class ContentServiceFileTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    @Test public void binaryRoundTripCreatesFoldersAndSurvivesNewService() throws Exception {
        Path root = temporary.getRoot().toPath().resolve("cms");
        ContentData data = data("1", "foto", "png", new byte[] {0, 1, -1, 42});
        new ContentServiceFileImpl(root).save(data);
        assertArrayEquals(data.getRaw(), new ContentServiceFileImpl(root).get(data.getMetadata()).getRaw());
        assertTrue(Files.isRegularFile(root.resolve("animal/1/foto.png")));
    }

    @Test public void listingIsScopedAndIgnoresTemporaryFiles() throws Exception {
        Path root = temporary.getRoot().toPath();
        ContentServiceFileImpl service = new ContentServiceFileImpl(root);
        service.save(data("1", "foto", "png", new byte[] {1}));
        service.save(data("1", "documento", "txt", new byte[] {2}));
        service.save(data("2", "foto", "jpg", new byte[] {3}));
        Files.write(root.resolve("animal/1/.content-pending.tmp"), new byte[] {4});
        List<ContentData> results = service.get("animal", "1");
        assertEquals(2, results.size());
        assertEquals("documento", results.get(0).getMetadata().getResourceId());
        assertEquals("foto", results.get(1).getMetadata().getResourceId());
        assertEquals("png", results.get(1).getMetadata().getFormat());
        assertEquals("1", results.get(1).getMetadata().getEntityId());
    }

    @Test public void missingPhotoAndEntityAreNormalAbsence() throws Exception {
        ContentServiceFileImpl service = new ContentServiceFileImpl(temporary.getRoot().toPath().resolve("new"));
        assertNull(service.get(data("1", "foto", "png", null).getMetadata()));
        assertTrue(service.get("animal", "1").isEmpty());
    }

    @Test public void replacementDoesNotAppendAndLeavesNoTemporaryContent() throws Exception {
        Path root = temporary.getRoot().toPath();
        ContentServiceFileImpl service = new ContentServiceFileImpl(root);
        ContentData data = data("1", "foto", "png", new byte[] {1, 2, 3});
        service.save(data);
        data.setRaw(new byte[] {9});
        service.save(data);
        assertArrayEquals(new byte[] {9}, service.get(data.getMetadata()).getRaw());
        try (java.util.stream.Stream<Path> files = Files.list(root.resolve("animal/1"))) {
            assertEquals(1, files.count());
        }
    }

    @Test public void invalidReplacementPreservesPreviousFile() throws Exception {
        ContentServiceFileImpl service = new ContentServiceFileImpl(temporary.getRoot().toPath());
        ContentData data = data("1", "foto", "png", new byte[] {1, 2});
        service.save(data);
        data.setRaw(null);
        try { service.save(data); fail("Missing bytes must fail"); }
        catch (ContentException expected) { assertNotNull(expected.getCause()); }
        assertArrayEquals(new byte[] {1, 2}, service.get(data.getMetadata()).getRaw());
    }

    @Test public void bothDeleteOverloadsAreIdempotentAndScoped() throws Exception {
        ContentServiceFileImpl service = new ContentServiceFileImpl(temporary.getRoot().toPath());
        ContentData one = data("1", "foto", "png", new byte[] {1});
        ContentData two = data("2", "foto", "png", new byte[] {2});
        service.save(one);
        service.save(two);
        service.remove(one.getMetadata());
        service.remove(one.getMetadata());
        assertNull(service.get(one.getMetadata()));
        assertNotNull(service.get(two.getMetadata()));
        service.remove("animal/2/foto.png");
        service.remove("animal/2/foto.png");
        assertNull(service.get(two.getMetadata()));
    }

    @Test public void pathTraversalAndAbsoluteIdsAreRejected() throws Exception {
        ContentServiceFileImpl service = new ContentServiceFileImpl(temporary.getRoot().toPath());
        for (String id : new String[] {"../1/foto.png", "animal/../foto.png", "/animal/1/foto.png",
                "C:/animal/1/foto.png", "animal/1/../foto.png", "animal/1"}) {
            try { service.remove(id); fail("Unsafe id accepted: " + id); }
            catch (DataException expected) { assertNotNull(expected.getMessage()); }
        }
        ContentData invalid = data("1", "../outside", "png", new byte[] {1});
        try { service.save(invalid); fail("Unsafe resource accepted"); }
        catch (ContentException expected) { assertNotNull(expected.getCause()); }
    }

    @Test public void unavailableStorageIsNotReportedAsMissingPhoto() throws Exception {
        Path rootFile = temporary.newFile("not-a-directory").toPath();
        ContentServiceFileImpl service = new ContentServiceFileImpl(rootFile);
        try { service.get("animal", "1"); fail("Storage error was hidden"); }
        catch (ContentException expected) { assertNotNull(expected.getCause()); }
        try { service.save(data("1", "foto", "png", new byte[] {1})); fail("Invalid storage was accepted"); }
        catch (ContentException expected) { assertNotNull(expected.getCause()); }
    }

    @Test public void deletingContentCannotRemoveADirectory() throws Exception {
        Path root = temporary.getRoot().toPath();
        Path directory = Files.createDirectories(root.resolve("animal/1/foto.png"));
        ContentServiceFileImpl service = new ContentServiceFileImpl(root);
        try { service.remove("animal/1/foto.png"); fail("A directory must not be deleted"); }
        catch (DataException expected) { assertTrue(Files.isDirectory(directory)); }
    }

    @Test public void databaseConfigurationLocationsHaveIndependentDefaultStorage() throws Exception {
        String previousFolder = System.getProperty("animalstrack.cms.folder");
        String previousConfig = System.getProperty("animalstrack.config");
        org.junit.Assume.assumeTrue(System.getenv("ANIMALSTRACK_CMS_FOLDER") == null);
        try {
            System.clearProperty("animalstrack.cms.folder");
            Path configA = temporary.getRoot().toPath().resolve("real/database.properties");
            Path configB = temporary.getRoot().toPath().resolve("test/database.properties");
            System.setProperty("animalstrack.config", configA.toString());
            new ContentServiceFileImpl().save(data("1", "foto", "png", new byte[] {1}));
            System.setProperty("animalstrack.config", configB.toString());
            assertTrue(new ContentServiceFileImpl().get("animal", "1").isEmpty());
            assertTrue(Files.exists(configA.getParent().resolve("cms/animal/1/foto.png")));
        } finally {
            restore("animalstrack.cms.folder", previousFolder);
            restore("animalstrack.config", previousConfig);
        }
    }

    private static void restore(String name, String value) {
        if (value == null) System.clearProperty(name); else System.setProperty(name, value);
    }

    private static ContentData data(String id, String resource, String format, byte[] bytes) {
        ContentMetadata metadata = new ContentMetadata();
        metadata.setEntityType("animal"); metadata.setEntityId(id);
        metadata.setResourceId(resource); metadata.setFormat(format);
        ContentData data = new ContentData(); data.setMetadata(metadata); data.setRaw(bytes);
        return data;
    }
}
