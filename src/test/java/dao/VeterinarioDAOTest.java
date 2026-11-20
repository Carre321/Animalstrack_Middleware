package dao;

import java.sql.Connection;
import java.util.List;

import com.tonin.animaltrack.dao.VeterinarioDAO;
import com.tonin.animaltrack.dao.utils.JDBCUtils;
import com.tonin.animaltrack.dao.criteria.VeterinarioCriteria;
import com.tonin.animaltrack.model.Veterinario;
import com.tonin.animaltrack.model.dto.VeterinarioDTO;

public class VeterinarioDAOTest {

	private static VeterinarioDAO dao = new VeterinarioDAO();
	private static Connection c = null;
	private static Long createdId = null;

	public static final void testFindById() throws Exception {
		if (c == null) {
			 c = openConnection();
		}
		VeterinarioDTO dto = dao.findById(c, 1L);
		System.out.println(dto);
	}

	public static final void testFindBy() throws Exception {
		if (c == null) {
			 c = openConnection();
		}
		VeterinarioCriteria criteria = new VeterinarioCriteria();
		criteria.setMunicipioId(1L);
		List<VeterinarioDTO> resultados = dao.findBy(c, criteria);
		for (VeterinarioDTO dto : resultados) {
			System.out.println(dto);
		}
	}

	public static final void testCreate() throws Exception {
		if (c == null) {
			 c = openConnection();
		}
		Veterinario v = new Veterinario();
		v.setCodigo("VET-T-" + System.currentTimeMillis());
		v.setDni("88888888W");
		v.setNombre("TEST");
		v.setApellidos("VETERINARIO");
		v.setTelefono("622222222");
		v.setEmail("test.vet@local");
		v.setMunicipioId(1L);
		createdId = dao.create(c, v);
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
		VeterinarioDTO dto = dao.findById(c, createdId);
		if (dto == null) {
			System.out.println("No existe");
			return;
		}
		Veterinario v = new Veterinario();
		v.setId(dto.getId());
		v.setCodigo(dto.getCodigo());
		v.setDni(dto.getDni());
		v.setNombre(dto.getNombre());
		v.setApellidos(dto.getApellidos());
		v.setTelefono("633333333");
		v.setEmail(dto.getEmail());
		v.setMunicipioId(dto.getMunicipioId());
		dao.update(c, v);
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
