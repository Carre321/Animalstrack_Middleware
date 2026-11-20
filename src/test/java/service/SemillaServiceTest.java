package service;

import com.tonin.animaltrack.model.Semilla;
import com.tonin.animaltrack.service.SemillaService;
import com.tonin.animaltrack.service.impl.SemillaServiceImpl;

public class SemillaServiceTest {

    private SemillaService service = null;
    private Semilla created = null;

    public SemillaServiceTest() {
        this.service = new SemillaServiceImpl();
    }

    public void testFindById(Long id) throws Exception {
        Semilla dto = service.findById(id);
        System.out.println(dto);
    }

    public void testFindByCodigo(String codigo) throws Exception {
        Semilla dto = service.findByCodigo(codigo);
        System.out.println(dto);
    }

    public void testCreate() throws Exception {
        Semilla s = new Semilla();
        s.setCodigo("TEST-" + System.currentTimeMillis());
        s.setNombre("Semilla test");
        s.setDescripcion("Semilla test");
        created = service.create(s);
        System.out.println("Created " + created);
    }

    public void testUpdate() throws Exception {
        if (created == null || created.getId() == null) {
            System.out.println("No hay ID creado");
            return;
        }
        created.setDescripcion("Semilla editada");
        service.update(created);
        System.out.println(service.findById(created.getId()));
    }

    public void deleteTest() throws Exception {
        if (created == null || created.getId() == null) {
            System.out.println("No hay ID creado");
            return;
        }
        service.delete(created.getId());
        System.out.println("Deleted " + created.getId());
        created = null;
    }

    public static void main(String[] args) throws Exception {
        SemillaServiceTest test = new SemillaServiceTest();
//      test.testFindById(1L);
        test.testFindByCodigo("LIM-2025-01");
//      test.testCreate();
//      test.testUpdate();
//      test.deleteTest();
    }
}
