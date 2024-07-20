package com.diplom.faces_recognition.models;

import java.io.Serializable;

public class GenericResponse implements Serializable {
    private int errorCode;
    private String errorMessage;

    public GenericResponse() {
    }

    public GenericResponse(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
