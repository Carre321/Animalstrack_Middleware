package unit;

import com.tonin.animaltrack.dao.utils.JDBCUtils;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class JDBCConfigurationTest {
    @Rule
    public TemporaryFolder temporary = new TemporaryFolder();

    private Object configured(String environment, String property, Properties values) throws Exception {
        Method resolver = JDBCUtils.class.getDeclaredMethod("value", String.class, String.class, Properties.class);
        resolver.setAccessible(true);
        return resolver.invoke(null, environment, property, values);
    }

    @Test
    public void readsPropertyWithoutDatabaseAccess() throws Exception {
        Properties values = new Properties();
        values.setProperty("db.user", "unit-only");
        assertEquals("unit-only", configured("ANIMALSTRACK_UNIT_UNSET", "db.user", values));
    }

    @Test
    public void rejectsMissingConfigurationWithoutDatabaseAccess() throws Exception {
        try {
            configured("ANIMALSTRACK_UNIT_UNSET", "missing", new Properties());
            fail("Missing values must be rejected");
        } catch (InvocationTargetException exception) {
            assertTrue(exception.getCause() instanceof IllegalStateException);
        }
    }

    @Test
    public void acceptsExplicitEmptyPasswordWithoutDatabaseAccess() throws Exception {
        Properties values = new Properties();
        values.setProperty("db.password", "");
        assertEquals("", configured("ANIMALSTRACK_UNIT_UNSET", "db.password", values));
    }

    @Test
    public void bundledConfigurationSupportsStandaloneWarDeployment() throws Exception {
        assertEquals("jdbc:unit-test:bundled", configuration(null).getProperty("db.url"));
    }

    @Test
    public void explicitLocalConfigurationTakesPriorityOverBundledConfiguration() throws Exception {
        Path local = temporary.newFile().toPath();
        Files.write(local, "db.url=jdbc:unit-test:local\ndb.user=local-only\n".getBytes(StandardCharsets.UTF_8));
        Properties properties = configuration(local.toString());
        assertEquals("jdbc:unit-test:local", properties.getProperty("db.url"));
        assertEquals("local-only", properties.getProperty("db.user"));
    }

    @Test
    public void missingExplicitLocalFileDoesNotFallBackToHostingCredentials() throws Exception {
        Properties properties = configuration(temporary.getRoot().toPath().resolve("missing.properties").toString());
        assertTrue(properties.isEmpty());
    }

    private Properties configuration(String location) throws Exception {
        String previousLocation = System.getProperty("animalstrack.config");
        String previousHome = System.getProperty("user.home");
        try {
            System.setProperty("user.home", temporary.getRoot().getAbsolutePath());
            if (location == null) System.clearProperty("animalstrack.config");
            else System.setProperty("animalstrack.config", location);
            Method loader = JDBCUtils.class.getDeclaredMethod("configuration");
            loader.setAccessible(true);
            return (Properties) loader.invoke(null);
        } finally {
            if (previousLocation == null) System.clearProperty("animalstrack.config");
            else System.setProperty("animalstrack.config", previousLocation);
            System.setProperty("user.home", previousHome);
        }
    }
}
