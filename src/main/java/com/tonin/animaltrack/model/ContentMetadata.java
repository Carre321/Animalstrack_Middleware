package com.tonin.animaltrack.model;

/** Identifica un contenido dentro de entidad/tipo/id/recurso.formato. */
public class ContentMetadata {
    private String entityType;
    private String entityId;
    private String resourceId;
    private String format;

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }
    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
}
