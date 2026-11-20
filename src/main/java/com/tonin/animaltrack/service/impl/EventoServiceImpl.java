package com.tonin.animaltrack.service.impl;

import java.sql.Connection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tonin.animaltrack.dao.EventoDAO;
import com.tonin.animaltrack.dao.Results;
import com.tonin.animaltrack.dao.criteria.EventoCriteria;
import com.tonin.animaltrack.dao.utils.JDBCUtils;
import com.tonin.animaltrack.model.Evento;
import com.tonin.animaltrack.model.dto.EventoDTO;
import com.tonin.animaltrack.service.EventoService;

public class EventoServiceImpl implements EventoService {

    private static Logger logger = LogManager.getLogger(EventoServiceImpl.class.getName());

    private EventoDAO eventoDAO = null;

    public EventoServiceImpl() {
        this.eventoDAO = new EventoDAO();
    }

    @Override
    public EventoDTO findById(Long id) throws Exception {
        Connection c = null;
        boolean commit = false;
        try {
            c = JDBCUtils.getConnection();
            c.setAutoCommit(false);
            EventoDTO result = eventoDAO.findById(c, id);
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
    public List<EventoDTO> findByCriteria(EventoCriteria criteria) throws Exception {
        Results<EventoDTO> results = findByCriteria(criteria, 1, Integer.MAX_VALUE);
        return results == null ? null : results.getPageResults();
    }

    @Override
    public Results<EventoDTO> findByCriteria(EventoCriteria criteria, int from, int pageSize) throws Exception {
        Connection c = null;
        boolean commit = false;
        try {
            c = JDBCUtils.getConnection();
            c.setAutoCommit(false);
            Results<EventoDTO> result = eventoDAO.findBy(c, criteria, from, pageSize);
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
    public List<EventoDTO> findByAnimalId(Long animalId) throws Exception {
        Connection c = null;
        boolean commit = false;
        try {
            c = JDBCUtils.getConnection();
            c.setAutoCommit(false);
            List<EventoDTO> result = eventoDAO.findByAnimalId(c, animalId);
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
    public List<EventoDTO> findAll() throws Exception {
        Connection c = null;
        boolean commit = false;
        try {
            c = JDBCUtils.getConnection();
            c.setAutoCommit(false);
            List<EventoDTO> result = eventoDAO.getAll(c);
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
    public EventoDTO create(Evento evento) throws Exception {
        return create(evento, null);
    }

    public EventoDTO create(Evento evento, Long granjaId) throws Exception {
        Connection c = null;
        try {
            c = JDBCUtils.getConnection();
            c.setAutoCommit(false);
            if (evento == null || evento.getAnimalId() == null || evento.getTipoEventoId() == null) {
                return null;
            }
            Long id = eventoDAO.create(c, evento, granjaId);
            EventoDTO result = id == null ? null : eventoDAO.findById(c, id);
            if (result == null) throw new IllegalArgumentException("No se pudo guardar el evento en esta granja.");
            c.commit();
            return result;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        } finally {
            JDBCUtils.close(c, false);
        }
    }

    @Override
    public void update(Evento evento) throws Exception {
        update(evento, null);
    }

    public void update(Evento evento, Long granjaId) throws Exception {
        Connection c = null;
        try {
            c = JDBCUtils.getConnection();
            c.setAutoCommit(false);
            if (evento != null && evento.getId() != null) {
                boolean updated = eventoDAO.update(c, evento, granjaId);
                if (granjaId != null && !updated) {
                    throw new IllegalArgumentException("El evento ya no está disponible en esta granja.");
                }
            }
            c.commit();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        } finally {
            JDBCUtils.close(c, false);
        }
    }

    @Override
    public void delete(Long id) throws Exception {
        delete(id, null);
    }

    public void delete(Long id, Long granjaId) throws Exception {
        Connection c = null;
        try {
            c = JDBCUtils.getConnection();
            c.setAutoCommit(false);
            boolean deleted = eventoDAO.delete(c, id, granjaId);
            if (granjaId != null && !deleted) {
                throw new IllegalArgumentException("El evento ya no está disponible en esta granja.");
            }
            c.commit();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        } finally {
            JDBCUtils.close(c, false);
        }
    }
}
