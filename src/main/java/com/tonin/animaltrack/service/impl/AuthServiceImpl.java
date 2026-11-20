package com.tonin.animaltrack.service.impl;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.tonin.animaltrack.dao.GanaderoDAO;
import com.tonin.animaltrack.dao.MunicipioDAO;
import com.tonin.animaltrack.dao.UsuarioLoginDAO;
import com.tonin.animaltrack.dao.criteria.GanaderoCriteria;
import com.tonin.animaltrack.dao.utils.JDBCUtils;
import com.tonin.animaltrack.model.Ganadero;
import com.tonin.animaltrack.model.dto.UsuarioLoginDTO;
import com.tonin.animaltrack.service.AuthService;
import com.tonin.animaltrack.service.EncryptionService;

public class AuthServiceImpl implements AuthService {

    private static Logger logger = LogManager.getLogger(AuthServiceImpl.class.getName());

    private UsuarioLoginDAO usuarioLoginDAO = null;
    private EncryptionService encryptionService = null;

    public AuthServiceImpl() {
        this.usuarioLoginDAO = new UsuarioLoginDAO();
        this.encryptionService = new EncryptionServiceBCryptImpl();
    }

    @Override
    public UsuarioLoginDTO register(Ganadero ganadero, String password) throws Exception {
        validateRegistration(ganadero, password);
        String hash = encryptionService.encrypt(password);
        GanaderoDAO ganaderoDAO = new GanaderoDAO();
        Connection connection = null;
        boolean committed = false;
        try {
            connection = JDBCUtils.getConnection();
            if (connection == null) throw new IllegalStateException("No se pudo conectar con la base de datos.");
            connection.setAutoCommit(false);
            if (usuarioLoginDAO.findByEmail(connection, ganadero.getEmail()) != null) {
                throw new IllegalArgumentException("Ya existe una cuenta con ese correo electrónico.");
            }
            GanaderoCriteria criteria = new GanaderoCriteria();
            criteria.setDni(ganadero.getDni());
            if (!ganaderoDAO.findBy(connection, criteria).isEmpty()) {
                throw new IllegalArgumentException("Ya existe un ganadero con ese DNI o NIE.");
            }
            if (new MunicipioDAO().findById(connection, ganadero.getMunicipioId()) == null) {
                throw new IllegalArgumentException("Selecciona un municipio válido.");
            }
            Long ganaderoId = ganaderoDAO.create(connection, ganadero);
            if (ganaderoId == null) throw new IllegalStateException("No se pudo crear el perfil del ganadero.");
            UsuarioLoginDTO usuario = new UsuarioLoginDTO();
            usuario.setEmail(ganadero.getEmail());
            usuario.setPasswordHash(hash);
            usuario.setRol("GANADERO");
            usuario.setGanaderoId(ganaderoId);
            usuario.setActivo(true);
            Long usuarioId = usuarioLoginDAO.create(connection, usuario);
            if (usuarioId == null) throw new IllegalStateException("No se pudo crear la cuenta de acceso.");
            UsuarioLoginDTO result = usuarioLoginDAO.findById(connection, usuarioId);
            if (result == null) throw new IllegalStateException("No se pudo recuperar la cuenta creada.");
            connection.commit();
            committed = true;
            result.setPasswordHash(null);
            return result;
        } catch (SQLIntegrityConstraintViolationException exception) {
            if (exception.getErrorCode() == 1062) {
                throw new IllegalArgumentException("Ya existe una cuenta con ese correo o DNI.");
            }
            throw exception;
        } finally {
            if (committed) JDBCUtils.close(connection);
            else JDBCUtils.close(connection, false);
        }
    }

    private void validateRegistration(Ganadero ganadero, String password) {
        if (ganadero == null) throw new IllegalArgumentException("Completa los datos del registro.");
        String nombre = trimToNull(ganadero.getNombre());
        String apellidos = trimToNull(ganadero.getApellidos());
        String dni = trimToNull(ganadero.getDni());
        String email = trimToNull(ganadero.getEmail());
        if (nombre == null || nombre.length() > 60 || apellidos == null || apellidos.length() > 80) {
            throw new IllegalArgumentException("Introduce tu nombre y apellidos con una longitud válida.");
        }
        if (dni == null || !dni.toUpperCase(Locale.ROOT).matches("(?:[0-9]{8}|[XYZ][0-9]{7})[A-Z]")) {
            throw new IllegalArgumentException("Introduce un DNI o NIE con formato válido.");
        }
        if (email == null || email.length() > 200 || !email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
            throw new IllegalArgumentException("Introduce un correo electrónico válido.");
        }
        if (ganadero.getMunicipioId() == null || ganadero.getMunicipioId() <= 0) {
            throw new IllegalArgumentException("Selecciona un municipio.");
        }
        if (password == null || password.length() < 8 || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres y no ser demasiado larga.");
        }
        ganadero.setNombre(nombre);
        ganadero.setApellidos(apellidos);
        ganadero.setDni(dni.toUpperCase(Locale.ROOT));
        ganadero.setEmail(email.toLowerCase(Locale.ROOT));
    }

    @Override
    public UsuarioLoginDTO login(String email, String password) throws Exception {
        Connection c = null;
        boolean commit = false;
        try {
            c = JDBCUtils.getConnection();
            c.setAutoCommit(false);
            String safeEmail = trimToNull(email);
            if (safeEmail == null || password == null || password.isEmpty()) {
                commit = true;
                return null;
            }
            UsuarioLoginDTO usuario = usuarioLoginDAO.findByEmail(c, safeEmail);
            if (usuario == null || !Boolean.TRUE.equals(usuario.getActivo())) {
                commit = true;
                return null;
            }
            UsuarioLoginDTO result = encryptionService.checkEncryption(password, usuario.getPasswordHash()) ? usuario : null;
            commit = true;
            return result;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        } finally {
            JDBCUtils.close(c, commit);
        }
    }

    @Override
    public UsuarioLoginDTO findByEmail(String email) throws Exception {
        Connection c = null;
        boolean commit = false;
        try {
            c = JDBCUtils.getConnection();
            c.setAutoCommit(false);
            String safeEmail = trimToNull(email);
            UsuarioLoginDTO result = safeEmail == null ? null : usuarioLoginDAO.findByEmail(c, safeEmail);
            commit = true;
            return result;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        } finally {
            JDBCUtils.close(c, commit);
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
