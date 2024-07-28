package com.diplom.faces_recognition.models;

import java.io.Serializable;

public class NetRequest implements Serializable {
    private String name;
    private String imageByteArray;

    public NetRequest(String name, String imageByteArray) {
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
