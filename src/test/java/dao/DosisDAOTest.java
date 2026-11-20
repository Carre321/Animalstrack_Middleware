package dao;

import java.sql.Connection;
import java.util.List;

import com.tonin.animaltrack.dao.DosisDAO;
import com.tonin.animaltrack.dao.utils.JDBCUtils;
import com.tonin.animaltrack.model.Dosis;

public class DosisDAOTest {

	private static Long createdId = null;
	private static DosisDAO dao = new DosisDAO();
	private static Connection c = null;

	public static final void testFindById() throws Exception {
		if (c == null) {
			 c = openConnection();
		}
		Dosis d = dao.findById(c, 1L);
		System.out.println(d);
	}

	public static final void testFindBy() throws Exception {
		if (c == null) {
			 c = openConnection();
		}
		List<Dosis> resultados = dao.findByTratamientoId(c, 1L);
		for (Dosis d : resultados) {
			System.out.println(d);
		}
	}

	public static final void testCreate() throws Exception {
		if (c == null) {
			 c = openConnection();
		}
		Dosis d = new Dosis();
		d.setPlazoSiguiente(30);
		d.setNumOrdenDosis(9);
		d.setTratamientoId(1L);
		createdId = dao.create(c, d);
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
		Dosis d = dao.findById(c, createdId);
		if (d == null) {
			System.out.println("No existe");
			return;
		}
		d.setPlazoSiguiente(45);
		dao.update(c, d);
		System.out.println(dao.findById(c, createdId));
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