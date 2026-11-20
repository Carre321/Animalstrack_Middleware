package service;

import java.util.List;

import com.tonin.animaltrack.model.Municipio;
import com.tonin.animaltrack.service.MunicipioService;
import com.tonin.animaltrack.service.impl.MunicipioServiceImpl;

public class MunicipioServiceTest {

    private MunicipioService service = null;

    public MunicipioServiceTest() {
        this.service = new MunicipioServiceImpl();
    }

    public void testFindById(Long id) throws Exception {
        Municipio dto = service.findById(id);
        System.out.println(dto);
    }

    public void testFindByProvinciaId() throws Exception {
        List<Municipio> resultados = service.findByProvinciaId(1L);
        for (Municipio dto : resultados) {
            System.out.println(dto);
        }
    }

    public static void main(String[] args) throws Exception {
        MunicipioServiceTest test = new MunicipioServiceTest();
        test.testFindById(1L);
//      test.testFindByProvinciaId();
    }
}
