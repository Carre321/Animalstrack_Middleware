package com.tonin.animaltrack.service.impl;

import java.sql.Connection;
import java.util.List;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tonin.animaltrack.dao.AnimalDAO;
import com.tonin.animaltrack.dao.Results;
import com.tonin.animaltrack.dao.criteria.AnimalCriteria;
import com.tonin.animaltrack.dao.utils.JDBCUtils;
import com.tonin.animaltrack.model.Animal;
import com.tonin.animaltrack.model.dto.AnimalDTO;
import com.tonin.animaltrack.service.AnimalService;

public class AnimalServiceImpl implements AnimalService {

    private static Logger logger = LogManager.getLogger(AnimalServiceImpl.class.getName());

    private static final String REQUIRED_DATA_MESSAGE = "Faltan datos obligatorios. Revisa los datos introducidos.";

    private AnimalDAO animalDAO = null;
    private final Supplier<Connection> connections;

    public AnimalServiceImpl() {
        this(new AnimalDAO(), JDBCUtils::getConnection);
    }

    public AnimalServiceImpl(AnimalDAO animalDAO, Supplier<Connection> connections) {
        this.animalDAO = animalDAO;
        this.connections = connections;
    }

    private Connection connection() {
        Connection connection = connections.get();
        if (connection == null) throw new IllegalStateException("No se pudo conectar con la base de datos.");
        return connection;
    }

    @Override
    public AnimalDTO findById(Long id) throws Exception {
        if (id == null || id <= 0) {
            return null;
        }
        Connection c = null;
        boolean commit = false;
        try {
            c = connection();
            c.setAutoCommit(false);
            AnimalDTO result = animalDAO.findById(c, id);
            commit = true;
            return result;
        } catch (Exception e) {
            logger.error("Buscando animal {}: {}", id, e.getMessage(), e);
            throw e;
        } finally {
            JDBCUtils.close(c, commit);
        }
    }

    @Override
    public AnimalDTO findByCrotal(String crotal) throws Exception {
        Connection c = null;
        boolean commit = false;
        try {
            c = connection();
            c.setAutoCommit(false);
            AnimalDTO result = animalDAO.findByCrotal(c, crotal);
            commit = true;
            return result;
        } catch (Exception e) {
            logger.error("Buscando animal por crotal {}: {}", crotal, e.getMessage(), e);
            throw e;
        } finally {
            JDBCUtils.close(c, commit);
        }
    }

    @Override
    public Results<AnimalDTO> findByCriteria(AnimalCriteria criteria, int from, int pageSize) throws Exception {
        Connection c = null;
        boolean commit = false;
        try {
            c = connection();
            c.setAutoCommit(false);
            Results<AnimalDTO> result = animalDAO.findBy(c, criteria, from, pageSize);
            commit = true;
            return result;
        } catch (Exception e) {
            logger.error("Buscando animales {}: {}", criteria, e.getMessage(), e);
            throw e;
        } finally {
            JDBCUtils.close(c, commit);
        }
    }

    @Override
    public List<AnimalDTO> findAll() throws Exception {
        Results<AnimalDTO> results = findByCriteria(new AnimalCriteria(), 1, 10);
        return results == null ? null : results.getPageResults();
    }

    @Override
    public AnimalDTO create(Animal animal) throws Exception {
        Connection c = null;
        boolean commit = false;
        try {
            c = connection();
            c.setAutoCommit(false);
            validateForSave(c, animal);
            Long id = animalDAO.create(c, animal);
            AnimalDTO result = id == null ? null : animalDAO.findById(c, id);
            if (result == null) throw new IllegalStateException("No se pudo recuperar el animal creado.");
            c.commit();
            commit = true;
            return result;
        } catch (Exception e) {
            logger.error("Creando animal {}: {}", animal, e.getMessage(), e);
            throw e;
        } finally {
            if (commit) JDBCUtils.close(c);
            else JDBCUtils.close(c, false);
        }
    }

    @Override
    public boolean update(Animal animal) throws Exception {
        if (animal == null || animal.getId() == null || animal.getId() <= 0) {
            return false;
        }
        Connection c = null;
        boolean commit = false;
        try {
            c = connection();
            c.setAutoCommit(false);
            validateForSave(c, animal);
            boolean result = animalDAO.update(c, animal);
            c.commit();
            commit = true;
            return result;
        } catch (Exception e) {
            logger.error("Actualizando animal {}: {}", animal, e.getMessage(), e);
            throw e;
        } finally {
            if (commit) JDBCUtils.close(c);
            else JDBCUtils.close(c, false);
        }
    }

    @Override
    public boolean delete(Long id) throws Exception {
        if (id == null || id <= 0) {
            return false;
        }
        Connection c = null;
        boolean commit = false;
        try {
            c = connection();
            c.setAutoCommit(false);
            boolean result = animalDAO.delete(c, id);
            c.commit();
            commit = true;
            return result;
        } catch (Exception e) {
            logger.error("Eliminando animal {}: {}", id, e.getMessage(), e);
            throw e;
        } finally {
            if (commit) JDBCUtils.close(c);
            else JDBCUtils.close(c, false);
        }
    }

    @Override
    public List<AnimalDTO> findByCriteria(AnimalCriteria criteria) throws Exception {
        Results<AnimalDTO> results = findByCriteria(criteria, 1, Integer.MAX_VALUE);
        return results == null ? null : results.getPageResults();
    }

    private void validateForSave(Connection c, Animal animal) throws Exception {
        if (animal == null) {
            throw new IllegalArgumentException(REQUIRED_DATA_MESSAGE);
        }
        if (isBlank(animal.getCrotal())) {
            throw new IllegalArgumentException(REQUIRED_DATA_MESSAGE);
        }
        String crotal = animal.getCrotal().trim().toUpperCase();
        animal.setCrotal(crotal);
        AnimalDTO existing = animalDAO.findByCrotal(c, crotal);
        if (existing != null && existing.getId() != null && !existing.getId().equals(animal.getId())) {
            throw new IllegalArgumentException("No se pudo guardar el animal. El crotal ya existe.");
        }

        if (animal.getSexoId() == null) {
            throw new IllegalArgumentException(REQUIRED_DATA_MESSAGE);
        }
        if (animal.getGranjaId() == null) {
            throw new IllegalArgumentException(REQUIRED_DATA_MESSAGE);
        }
        if (animal.getMadreInternaId() != null && !isBlank(animal.getMadreExternaCrotal())) {
            throw new IllegalArgumentException("Solo puedes indicar madre interna o madre externa.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
