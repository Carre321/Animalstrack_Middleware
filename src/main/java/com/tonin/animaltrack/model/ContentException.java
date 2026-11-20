package com.tonin.animaltrack.model;

import com.tonin.animaltrack.service.ServiceException;

public class ContentException extends ServiceException {
    private static final long serialVersionUID = 1L;
    public ContentException(String message) { super(message); }
    public ContentException(String message, Throwable cause) { super(message, cause); }
}
