package dao;

import java.sql.Connection;
import java.util.List;

import com.tonin.animaltrack.dao.TratamientoDAO;
import com.tonin.animaltrack.dao.utils.JDBCUtils;
import com.tonin.animaltrack.model.Tratamiento;

public class TratamientoDAOTest {

	private static Long createdId = null;
	private static TratamientoDAO dao = new TratamientoDAO();
	private static Connection c = null;

	public static final void testFindById() throws Exception {
		if (c == null) {
			 c = openConnection();
		}
		Tratamiento e = dao.findById(c, 1L);
		System.out.println(e);
	}

	public static final void testFindBy() throws Exception {
		if (c == null) {
			 c = openConnection();
		}
		List<Tratamiento> resultados = dao.getAll(c);
		for (Tratamiento e : resultados) {
			System.out.println(e);
		}
	}

	public static final void testCreate() throws Exception {
		if (c == null) {
			 c = openConnection();
		}
		Tratamiento e = new Tratamiento();
		e.setNombre("TEST-TRATAMIENTO-" + System.currentTimeMillis());
		createdId = dao.create(c, e);
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
		Tratamiento e = dao.findById(c, createdId);
		if (e == null) {
			System.out.println("No existe");
			return;
		}
		e.setNombre(e.getNombre() + "-UPD");
		dao.update(c, e);
		System.out.println(dao.findById(c, createdId));
	}

	public static final void deleteTest() throws Exception {
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