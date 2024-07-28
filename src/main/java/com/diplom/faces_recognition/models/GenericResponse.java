package com.diplom.faces_recognition.models;

import java.io.Serializable;

public class GenericResponse implements Serializable {
    private Integer errorCode;
    private String errorMessage;

    public GenericResponse() {
    }

    public GenericResponse(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
