package com.diplom.faces_recognition.models;

import java.io.Serializable;

public class NetModelResponse extends GenericResponse implements Serializable {
    private String name;
    private String imageByteArray;

    public NetModelResponse(String name, String imageByteArray) {
        super();
        this.name = name;
        this.imageByteArray = imageByteArray;
    }

    public String getName() {
        return name;
    }

    public String getImageByteArray() {
        return imageByteArray;
    }
}
