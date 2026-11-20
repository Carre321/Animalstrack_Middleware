package dao;

import java.sql.Connection;
import java.util.List;

import com.tonin.animaltrack.dao.TipoNotificacionDAO;
import com.tonin.animaltrack.dao.utils.JDBCUtils;
import com.tonin.animaltrack.model.TipoNotificacion;

public class TipoNotificacionDAOTest {

	private static TipoNotificacionDAO dao = new TipoNotificacionDAO();
	private static Connection c = null;

	public static final void testFindById() throws Exception {
		if (c == null) {
			 c = openConnection();
		}
		TipoNotificacion e = dao.findById(c, 1L);
		System.out.println(e);
	}

	public static final void testFindBy() throws Exception {
		if (c == null) {
			 c = openConnection();
		}
		List<TipoNotificacion> resultados = dao.getAll(c);
		for (TipoNotificacion e : resultados) {
			System.out.println(e);
		}
	}

	private static Connection openConnection() throws Exception {
		Connection c = JDBCUtils.getConnection();
		c.setAutoCommit(false);
		return c;
	}

	public static void main(String[] args) throws Exception {
		testFindById();
		//testFindBy();
	}
}
