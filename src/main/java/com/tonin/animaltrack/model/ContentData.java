package com.tonin.animaltrack.model;

public class ContentData {
    private byte[] raw;
    private ContentMetadata metadata;

    public byte[] getRaw() { return raw; }
    public void setRaw(byte[] raw) { this.raw = raw; }
    public ContentMetadata getMetadata() { return metadata; }
    public void setMetadata(ContentMetadata metadata) { this.metadata = metadata; }
}
