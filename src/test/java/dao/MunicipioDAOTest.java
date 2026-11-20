package dao;

import java.sql.Connection;
import java.util.List;

import com.tonin.animaltrack.dao.MunicipioDAO;
import com.tonin.animaltrack.dao.utils.JDBCUtils;
import com.tonin.animaltrack.model.Municipio;

public class MunicipioDAOTest {

	private static MunicipioDAO dao = new MunicipioDAO();
	private static Connection c = null;

	public static final void testFindById() throws Exception {
		if (c == null) {
			 c = openConnection();
		}
		Municipio m = dao.findById(c, 1L);
		System.out.println(m);
	}

	public static final void testFindBy() throws Exception {
		if (c == null) {
			 c = openConnection();
		}
		List<Municipio> resultados = dao.findByProvinciaId(c, 1L);
		for (Municipio m : resultados) {
			System.out.println(m);
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
