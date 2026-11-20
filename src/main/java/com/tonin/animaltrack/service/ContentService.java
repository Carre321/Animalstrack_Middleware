package com.tonin.animaltrack.service;

import java.util.List;
import com.tonin.animaltrack.model.ContentData;
import com.tonin.animaltrack.model.ContentException;
import com.tonin.animaltrack.model.ContentMetadata;
import com.tonin.animaltrack.model.DataException;

/** Servicio comun de contenidos, con las mismas operaciones que el ZIP. */
public interface ContentService {
    /** Devuelve null si no existe; los errores de lectura se propagan. */
    ContentData get(ContentMetadata metadata) throws ContentException;
    List<ContentData> get(String entityType, String entityId) throws ContentException;
    /** Lista los metadatos sin cargar los bytes de los archivos. */
    List<ContentMetadata> listMetadata(String entityType, String entityId) throws ContentException;
    void save(ContentData data) throws ContentException;
    void remove(ContentMetadata metadata) throws DataException;
    /** Identificador relativo: tipoEntidad/idEntidad/recurso.formato. */
    void remove(String id) throws DataException;
}
