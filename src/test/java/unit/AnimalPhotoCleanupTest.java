package unit;

import java.lang.reflect.Proxy;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;
import com.tonin.animaltrack.model.ContentData;
import com.tonin.animaltrack.model.ContentMetadata;
import com.tonin.animaltrack.model.dto.AnimalDTO;
import com.tonin.animaltrack.service.AnimalService;
import com.tonin.animaltrack.service.impl.ContentServiceFileImpl;
import com.tonin.animaltrack.tools.AnimalPhotoCleanup;

public class AnimalPhotoCleanupTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    @Test public void previewFindsOnlyOrphanPhotosWithoutDeletingAnything() throws Exception {
        ContentServiceFileImpl contents = new ContentServiceFileImpl(temporary.getRoot().toPath());
        save(contents, "1", "foto"); save(contents, "2", "foto"); save(contents, "2", "documento");
        AnimalService animals = lookup(id -> id == 1L ? new AnimalDTO() : null);
        List<ContentMetadata> orphans = AnimalPhotoCleanup.findOrphans(animals, contents);
        assertEquals(1, orphans.size()); assertEquals("2", orphans.get(0).getEntityId());
        assertEquals(2, contents.get("animal", "2").size());
    }

    @Test public void logicallyDeactivatedAnimalsKeepTheirPhotos() throws Exception {
        ContentServiceFileImpl contents = new ContentServiceFileImpl(temporary.getRoot().toPath());
        save(contents, "1", "foto");
        AnimalDTO deactivated = new AnimalDTO(); deactivated.setFechaBaja(java.sql.Date.valueOf("2026-01-01"));
        assertTrue(AnimalPhotoCleanup.findOrphans(lookup(id -> deactivated), contents).isEmpty());
    }

    @Test public void databaseErrorAbortsPreviewRatherThanTreatingAllAnimalsAsDeleted() throws Exception {
        ContentServiceFileImpl contents = new ContentServiceFileImpl(temporary.getRoot().toPath());
        save(contents, "1", "foto");
        try {
            AnimalPhotoCleanup.findOrphans(lookup(id -> { throw new java.sql.SQLException("simulated failure"); }), contents);
            fail("A failed lookup must not be treated as a missing animal");
        } catch (java.sql.SQLException expected) { assertEquals(1, contents.get("animal", "1").size()); }
    }

    private static AnimalService lookup(Lookup lookup) {
        return (AnimalService) Proxy.newProxyInstance(AnimalService.class.getClassLoader(),
                new Class<?>[] {AnimalService.class}, (proxy, method, args) -> {
            assertEquals("findById", method.getName()); return lookup.find((Long) args[0]);
        });
    }
    private interface Lookup { AnimalDTO find(Long id) throws Exception; }
    private static void save(ContentServiceFileImpl service, String id, String resource) throws Exception {
        ContentMetadata metadata = new ContentMetadata(); metadata.setEntityType("animal");
        metadata.setEntityId(id); metadata.setResourceId(resource); metadata.setFormat("png");
        ContentData data = new ContentData(); data.setMetadata(metadata); data.setRaw(new byte[] {1}); service.save(data);
    }
}
