package com.tonin.animaltrack.service.impl;

import com.tonin.animaltrack.dao.GanaderoDAO;
import com.tonin.animaltrack.dao.UsuarioLoginDAO;
import com.tonin.animaltrack.dao.VeterinarioDAO;
import com.tonin.animaltrack.dao.utils.JDBCUtils;
import com.tonin.animaltrack.model.Ganadero;
import com.tonin.animaltrack.model.Veterinario;
import com.tonin.animaltrack.model.dto.UsuarioLoginDTO;
import java.sql.Connection;

public class AdministracionUsuarioService {
    private final GanaderoDAO ganaderos = new GanaderoDAO();
    private final VeterinarioDAO veterinarios = new VeterinarioDAO();
    private final UsuarioLoginDAO accesos = new UsuarioLoginDAO();
    private final EncryptionServiceBCryptImpl encryption = new EncryptionServiceBCryptImpl();

    public void save(Ganadero ganadero, Veterinario veterinario, String password) throws Exception {
        boolean vet = veterinario != null;
        Long id = vet ? veterinario.getId() : ganadero.getId();
        String email = vet ? veterinario.getEmail() : ganadero.getEmail();
        String hash = password == null ? null : encryption.encrypt(password);
        Connection connection = JDBCUtils.getConnection();
        try {
            connection.setAutoCommit(false);
            UsuarioLoginDTO login = id == null ? null : accesos.findByPerson(connection, id, vet);
            if (login == null && hash == null) {
                throw new IllegalArgumentException("Introduce una contraseña para crear el acceso.");
            }
            UsuarioLoginDTO duplicate = accesos.findByEmail(connection, email);
            if (duplicate != null && (login == null || !duplicate.getId().equals(login.getId()))) {
                throw new IllegalArgumentException("Ya existe una cuenta con ese correo.");
            }

            // Guarda la ficha y su acceso en la misma transacción.
            if (id == null) {
                id = vet ? veterinarios.create(connection, veterinario) : ganaderos.create(connection, ganadero);
                if (id == null) throw new IllegalStateException("No se pudo crear el usuario.");
            } else if (!(vet ? veterinarios.update(connection, veterinario) : ganaderos.update(connection, ganadero))) {
                if ((vet ? veterinarios.findById(connection, id) : ganaderos.findById(connection, id)) == null) {
                    throw new IllegalArgumentException("El usuario ya no existe.");
                }
            }
            if (login == null) {
                login = new UsuarioLoginDTO();
                login.setActivo(Boolean.TRUE);
            }
            login.setEmail(email);
            login.setRol(vet ? "VETERINARIO" : "GANADERO");
            login.setGanaderoId(vet ? null : id);
            login.setVeterinarioId(vet ? id : null);
            if (hash != null) login.setPasswordHash(hash);
            if (login.getId() == null) accesos.create(connection, login);
            else accesos.update(connection, login);
            connection.commit();
        } catch (Exception exception) {
            connection.rollback();
            throw exception;
        } finally {
            JDBCUtils.close(connection);
        }
    }

    public void delete(Long id, boolean vet) throws Exception {
        Connection connection = JDBCUtils.getConnection();
        try {
            connection.setAutoCommit(false);
            UsuarioLoginDTO login = accesos.findByPerson(connection, id, vet);
            if (login != null) accesos.delete(connection, login.getId());
            boolean deleted = vet ? veterinarios.delete(connection, id) : ganaderos.delete(connection, id);
            if (!deleted) throw new IllegalArgumentException("El usuario ya no existe.");
            connection.commit();
        } catch (Exception exception) {
            connection.rollback();
            throw exception;
        } finally {
            JDBCUtils.close(connection);
        }
    }
}
