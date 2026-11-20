package service;

import com.tonin.animaltrack.model.TipoEvento;
import com.tonin.animaltrack.service.TipoEventoService;
import com.tonin.animaltrack.service.impl.TipoEventoServiceImpl;

public class TipoEventoServiceTest {

    private TipoEventoService service = null;

    public TipoEventoServiceTest() {
        this.service = new TipoEventoServiceImpl();
    }

    public void testFindById(Long id) throws Exception {
        TipoEvento dto = service.findById(id);
        System.out.println(dto);
    }

    public static void main(String[] args) throws Exception {
        TipoEventoServiceTest test = new TipoEventoServiceTest();
        test.testFindById(1L);
    }
}
