package dao;

import java.time.LocalDateTime;
import java.sql.Connection;
import java.util.List;

import com.tonin.animaltrack.dao.NotificacionDAO;
import com.tonin.animaltrack.dao.utils.JDBCUtils;
import com.tonin.animaltrack.dao.criteria.NotificacionCriteria;
import com.tonin.animaltrack.model.Notificacion;
import com.tonin.animaltrack.model.dto.NotificacionDTO;

public class NotificacionDAOTest {

	private static NotificacionDAO dao = new NotificacionDAO();
	private static Connection c = null;
	private static Long createdId = null;

	public static final void testFindById() throws Exception {
		if (c == null) {
			 c = openConnection();
		}
		NotificacionDTO dto = dao.findById(c, 1L);
		System.out.println(dto);
	}

	public static final void testFindBy() throws Exception {
		if (c == null) {
			 c = openConnection();
		}
		NotificacionCriteria criteria = new NotificacionCriteria();
		criteria.setEventoId(1L);
		List<NotificacionDTO> resultados = dao.findBy(c, criteria);
		for (NotificacionDTO dto : resultados) {
			System.out.println(dto);
		}
	}

	public static final void testCreate() throws Exception {
		if (c == null) {
			 c = openConnection();
		}
		Notificacion n = new Notificacion();
		n.setEventoId(1L);
		n.setTipo("INFO");
		n.setFechaEmision(LocalDateTime.now());
		n.setDescripcion("Notificacion de prueba");
		n.setTipoNotificacionId(1L);
		createdId = dao.create(c, n);
		System.out.println("ID " + createdId);
	}

	public static final void testUpdate() throws Exception {
		if (c == null) {
			 c = openConnection();
		}
		if (createdId == null) {
			System.out.println("No hay ID creado");
			return;
		}
		NotificacionDTO dto = dao.findById(c, createdId);
		if (dto == null) {
			System.out.println("No existe");
			return;
		}
		Notificacion n = new Notificacion();
		n.setId(dto.getId());
		n.setEventoId(dto.getEventoId());
		n.setTipo(dto.getTipo());
		n.setFechaEmision(dto.getFechaEmision());
		n.setDescripcion("Notificacion editada");
		n.setTipoNotificacionId(dto.getTipoNotificacionId());
		dao.update(c, n);
		System.out.println(dao.findById(c, dto.getId()));
	}

	public static void deleteTest() throws Exception {
		if (c == null) {
			 c = openConnection();
		}
		if (createdId == null) {
			System.out.println("No hay ID creado");
			return;
		}
		dao.delete(c, createdId);
		System.out.println("Deleted " + createdId);
		createdId = null;
	}

	private static Connection openConnection() throws Exception {
		Connection c = JDBCUtils.getConnection();
		c.setAutoCommit(false);
		return c;
	}

	public static void main(String[] args) throws Exception {
		//testFindById();
		//testFindBy();
		testCreate();
		//testUpdate();
		//deleteTest();
	}
}
