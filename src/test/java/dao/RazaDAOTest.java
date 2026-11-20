package dao;

import java.sql.Connection;
import java.util.List;

import com.tonin.animaltrack.dao.RazaDAO;
import com.tonin.animaltrack.dao.utils.JDBCUtils;
import com.tonin.animaltrack.model.Raza;

public class RazaDAOTest {

	private static RazaDAO dao = new RazaDAO();
	private static Connection c = null;

	public static final void testFindById() throws Exception {
		if (c == null) {
			 c = openConnection();
		}
		Raza e = dao.findById(c, 1L);
		System.out.println(e);
	}

	public static final void testFindBy() throws Exception {
		if (c == null) {
			 c = openConnection();
		}
		List<Raza> resultados = dao.getAll(c);
		for (Raza e : resultados) {
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
