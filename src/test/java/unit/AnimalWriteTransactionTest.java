package unit;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import org.junit.Test;
import static org.junit.Assert.*;
import com.tonin.animaltrack.dao.AnimalDAO;
import com.tonin.animaltrack.model.Animal;
import com.tonin.animaltrack.model.dto.AnimalDTO;
import com.tonin.animaltrack.service.impl.AnimalServiceImpl;

public class AnimalWriteTransactionTest {
    @Test public void successfulWritesCommitOnceBeforeReturning() throws Exception {
        for (String action : new String[] {"create", "update", "delete"}) {
            Transaction transaction = new Transaction(false);
            AnimalServiceImpl service = new AnimalServiceImpl(new FakeDAO(), () -> transaction.connection());
            execute(service, action);
            assertEquals(action, 1, transaction.commits);
            assertEquals(action, 0, transaction.rollbacks);
            assertEquals(action, 1, transaction.closes);
        }
    }

    @Test public void failedCommitIsPropagatedAndNeverReportedAsSuccess() throws Exception {
        for (String action : new String[] {"create", "update", "delete"}) {
            Transaction transaction = new Transaction(true);
            AnimalServiceImpl service = new AnimalServiceImpl(new FakeDAO(), () -> transaction.connection());
            try { execute(service, action); fail("Commit failure hidden in " + action); }
            catch (SQLException expected) { assertEquals("simulated commit failure", expected.getMessage()); }
            assertEquals(1, transaction.commits);
            assertEquals(1, transaction.rollbacks);
            assertEquals(1, transaction.closes);
        }
    }

    @Test public void missingUpdatedAnimalReturnsFalse() throws Exception {
        Transaction transaction = new Transaction(false);
        AnimalDAO dao = new FakeDAO() {
            @Override public boolean update(Connection connection, Animal animal) { return false; }
        };
        assertFalse(new AnimalServiceImpl(dao, () -> transaction.connection()).update(animal()));
    }

    @Test public void missingCreatedAnimalRollsBackRatherThanReturningSuccess() throws Exception {
        Transaction transaction = new Transaction(false);
        AnimalDAO dao = new FakeDAO() {
            @Override public AnimalDTO findById(Connection connection, Long id) { return null; }
        };
        try {
            new AnimalServiceImpl(dao, () -> transaction.connection()).create(animal());
            fail("Incomplete creation must fail");
        } catch (IllegalStateException expected) { assertEquals(1, transaction.rollbacks); }
        assertEquals(0, transaction.commits);
    }

    private static void execute(AnimalServiceImpl service, String action) throws Exception {
        if ("create".equals(action)) assertEquals(Long.valueOf(123), service.create(animal()).getId());
        else if ("update".equals(action)) assertTrue(service.update(animal()));
        else assertTrue(service.delete(123L));
    }

    private static Animal animal() {
        Animal animal = new Animal(); animal.setId(123L); animal.setCrotal("TEST-123");
        animal.setGranjaId(1L); animal.setSexoId(1L); return animal;
    }

    private static class FakeDAO extends AnimalDAO {
        @Override public AnimalDTO findByCrotal(Connection connection, String crotal) { return null; }
        @Override public Long create(Connection connection, Animal animal) { return 123L; }
        @Override public AnimalDTO findById(Connection connection, Long id) {
            AnimalDTO dto = new AnimalDTO(); dto.setId(id); return dto;
        }
        @Override public boolean update(Connection connection, Animal animal) { return true; }
        @Override public boolean delete(Connection connection, Long id) { return true; }
    }

    private static class Transaction {
        int commits, rollbacks, closes;
        private final boolean failCommit;
        Transaction(boolean failCommit) { this.failCommit = failCommit; }
        Connection connection() {
            return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(),
                    new Class<?>[] {Connection.class}, (proxy, method, args) -> {
                if ("commit".equals(method.getName())) {
                    commits++;
                    if (failCommit) throw new SQLException("simulated commit failure");
                } else if ("rollback".equals(method.getName())) rollbacks++;
                else if ("close".equals(method.getName())) closes++;
                else if ("isClosed".equals(method.getName())) return false;
                return null;
            });
        }
    }
}
