package unit;

import com.tonin.animaltrack.model.Ganadero;
import com.tonin.animaltrack.service.impl.AuthServiceImpl;
import org.junit.Test;
import static org.junit.Assert.*;

public class AuthRegistrationValidationTest {
    @Test
    public void missingProfileIsRejectedBeforeConnecting() throws Exception {
        rejected(null, "test-only-password");
    }

    @Test
    public void missingNameIsRejectedBeforeConnecting() throws Exception {
        Ganadero ganadero = profile();
        ganadero.setNombre(" ");
        rejected(ganadero, "test-only-password");
    }

    @Test
    public void invalidEmailIsRejectedBeforeConnecting() throws Exception {
        Ganadero ganadero = profile();
        ganadero.setEmail("invalid-email");
        rejected(ganadero, "test-only-password");
    }

    @Test
    public void invalidDniIsRejectedBeforeConnecting() throws Exception {
        Ganadero ganadero = profile();
        ganadero.setDni("not-a-dni");
        rejected(ganadero, "test-only-password");
    }

    @Test
    public void missingMunicipalityIsRejectedBeforeConnecting() throws Exception {
        Ganadero ganadero = profile();
        ganadero.setMunicipioId(null);
        rejected(ganadero, "test-only-password");
    }

    @Test
    public void shortPasswordIsRejectedBeforeConnecting() throws Exception {
        rejected(profile(), "short");
    }

    @Test
    public void bcryptLengthLimitCountsUtf8BytesBeforeConnecting() throws Exception {
        rejected(profile(), new String(new char[40]).replace('\0', 'é'));
    }

    private Ganadero profile() {
        Ganadero ganadero = new Ganadero();
        ganadero.setNombre("Persona");
        ganadero.setApellidos("De prueba");
        ganadero.setDni("12345678Z");
        ganadero.setEmail("persona@example.test");
        ganadero.setMunicipioId(900001L);
        return ganadero;
    }

    private void rejected(Ganadero ganadero, String password) throws Exception {
        try {
            new AuthServiceImpl().register(ganadero, password);
            fail("Invalid registration must be rejected before accessing a database");
        } catch (IllegalArgumentException expected) {
            assertNotNull(expected.getMessage());
            assertFalse(expected.getMessage().isEmpty());
        }
    }
}
