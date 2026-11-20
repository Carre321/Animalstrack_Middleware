package dao;

import java.sql.Connection;
import java.util.List;

import com.tonin.animaltrack.dao.VeterinarioGranjaDAO;
import com.tonin.animaltrack.dao.utils.JDBCUtils;
import com.tonin.animaltrack.model.VeterinarioGranja;

public class VeterinarioGranjaDAOTest {

	private static Long oldVetId = null;
	private static Long oldGranjaId = null;
	private static VeterinarioGranjaDAO dao = new VeterinarioGranjaDAO();
	private static Connection c = null;

	public static final void testFindById() throws Exception {
		if (c == null) {
			 c = openConnection();
		}
		List<VeterinarioGranja> resultados = dao.findByVeterinarioId(c, 1L);
		for (VeterinarioGranja e : resultados) {
			System.out.println(e);
		}
	}

	public static final void testFindBy() throws Exception {
		if (c == null) {
			 c = openConnection();
		}
		List<VeterinarioGranja> resultados = dao.findByGranjaId(c, 1L);
		for (VeterinarioGranja e : resultados) {
			System.out.println(e);
		}
	}

	public static final void testCreate() throws Exception {
		if (c == null) {
			 c = openConnection();
		}
		VeterinarioGranja e = new VeterinarioGranja();
		e.setVeterinarioId(1L);
		e.setGranjaId(1L);
		dao.create(c, e);
		oldVetId = e.getVeterinarioId();
		oldGranjaId = e.getGranjaId();
		System.out.println("Created " + oldVetId + "-" + oldGranjaId);
	}

	public static final void testUpdate() throws Exception {
		if (c == null) {
			 c = openConnection();
		}
		if (oldVetId == null || oldGranjaId == null) {
			System.out.println("No hay clave creada");
			return;
		}
		VeterinarioGranja e = new VeterinarioGranja();
		e.setVeterinarioId(2L);
		e.setGranjaId(oldGranjaId);
		dao.update(c, oldVetId, oldGranjaId, e);
		oldVetId = 2L;
		System.out.println("Updated " + oldVetId + "-" + oldGranjaId);
	}

	public static void deleteTest() throws Exception {
		if (c == null) {
			 c = openConnection();
		}
		if (oldVetId == null || oldGranjaId == null) {
			System.out.println("No hay clave creada");
			return;
		}
		dao.delete(c, oldVetId, oldGranjaId);
		System.out.println("Deleted " + oldVetId + "-" + oldGranjaId);
		oldVetId = null;
		oldGranjaId = null;
	}

	private static Connection openConnection() throws Exception {
		Connection c = JDBCUtils.getConnection();
		c.setAutoCommit(false);
		return c;
	}

	public static void main(String[] args) throws Exception {
		testFindById();
		//testFindBy();
		//testCreate();
		//testUpdate();
		//deleteTest();
	}
}
