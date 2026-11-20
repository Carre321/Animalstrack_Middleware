package dao;

import java.sql.Connection;
import java.util.List;

import com.tonin.animaltrack.dao.SemillaDAO;
import com.tonin.animaltrack.dao.utils.JDBCUtils;
import com.tonin.animaltrack.model.Semilla;

public class SemillaDAOTest {

	private static Long createdId = null;
	private static SemillaDAO dao = new SemillaDAO();
	private static Connection c = null;

	public static final void testFindById() throws Exception {
		if (c == null) {
			 c = openConnection();
		}
		Semilla s = dao.findById(c, 1L);
		System.out.println(s);
	}

	public static final void testFindBy() throws Exception {
		if (c == null) {
			 c = openConnection();
		}
		Semilla s = dao.findByCodigo(c, "LIM-2025-01");
		System.out.println(s);
		List<Semilla> all = dao.getAll(c);
		for (Semilla item : all) {
			System.out.println(item);
		}
	}

	public static final void testCreate() throws Exception {
		if (c == null) {
			 c = openConnection();
		}
		Semilla s = new Semilla();
		s.setCodigo("TEST-" + System.currentTimeMillis());
		s.setNombre("Semilla test");
		s.setDescripcion("Semilla test");
		createdId = dao.create(c, s);
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
		Semilla s = dao.findById(c, createdId);
		if (s == null) {
			System.out.println("No existe");
			return;
		}
		s.setDescripcion("Semilla editada");
		dao.update(c, s);
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
