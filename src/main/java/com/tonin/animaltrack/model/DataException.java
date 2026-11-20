package com.tonin.animaltrack.model;

/** Se conserva la excepcion de borrado del contrato del ejemplo. */
public class DataException extends Exception {
    private static final long serialVersionUID = 1L;
    public DataException(String message) { super(message); }
    public DataException(String message, Throwable cause) { super(message, cause); }
}
