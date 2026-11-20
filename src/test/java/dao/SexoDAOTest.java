package dao;

import java.sql.Connection;
import java.util.List;

import com.tonin.animaltrack.dao.SexoDAO;
import com.tonin.animaltrack.dao.utils.JDBCUtils;
import com.tonin.animaltrack.model.Sexo;

public class SexoDAOTest {

	private static SexoDAO dao = new SexoDAO();
	private static Connection c = null;

	public static final void testFindById() throws Exception {
		if (c == null) {
			 c = openConnection();
		}
		Sexo e = dao.findById(c, 1L);
		System.out.println(e);
	}

	public static final void testFindBy() throws Exception {
		if (c == null) {
			 c = openConnection();
		}
		List<Sexo> resultados = dao.getAll(c);
		for (Sexo e : resultados) {
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
