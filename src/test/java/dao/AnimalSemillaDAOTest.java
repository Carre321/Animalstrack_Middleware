package dao;

import java.sql.Connection;
import java.util.List;

import com.tonin.animaltrack.dao.AnimalSemillaDAO;
import com.tonin.animaltrack.dao.utils.JDBCUtils;
import com.tonin.animaltrack.model.AnimalSemilla;

public class AnimalSemillaDAOTest {

	private static Long oldAnimalId = null;
	private static Long oldSemillaId = null;
	private static AnimalSemillaDAO dao = new AnimalSemillaDAO();
	private static Connection c = null;

	public static final void testFindById() throws Exception {
		if (c == null) {
			 c = openConnection();
		}
		List<AnimalSemilla> resultados = dao.findByAnimalId(c, 1L);
		for (AnimalSemilla e : resultados) {
			System.out.println(e);
		}
	}

	public static final void testFindBy() throws Exception {
		if (c == null) {
			 c = openConnection();
		}
		List<AnimalSemilla> resultados = dao.findBySemillaId(c, 1L);
		for (AnimalSemilla e : resultados) {
			System.out.println(e);
		}
	}

	public static final void testCreate() throws Exception {
		if (c == null) {
			 c = openConnection();
		}
		AnimalSemilla e = new AnimalSemilla();
		e.setAnimalId(1L);
		e.setSemillaId(2L);
		dao.create(c, e);
		oldAnimalId = e.getAnimalId();
		oldSemillaId = e.getSemillaId();
		System.out.println("Created " + oldAnimalId + "-" + oldSemillaId);
	}

	public static final void testUpdate() throws Exception {
		if (c == null) {
			 c = openConnection();
		}
		if (oldAnimalId == null || oldSemillaId == null) {
			System.out.println("No hay clave creada");
			return;
		}
		AnimalSemilla e = new AnimalSemilla();
		e.setAnimalId(oldAnimalId);
		e.setSemillaId(3L);
		dao.update(c, oldAnimalId, oldSemillaId, e);
		oldSemillaId = 3L;
		System.out.println("Updated " + oldAnimalId + "-" + oldSemillaId);
	}

	public static void deleteTest() throws Exception {
		if (c == null) {
			 c = openConnection();
		}
		if (oldAnimalId == null || oldSemillaId == null) {
			System.out.println("No hay clave creada");
			return;
		}
		dao.delete(c, oldAnimalId, oldSemillaId);
		System.out.println("Deleted " + oldAnimalId + "-" + oldSemillaId);
		oldAnimalId = null;
		oldSemillaId = null;
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