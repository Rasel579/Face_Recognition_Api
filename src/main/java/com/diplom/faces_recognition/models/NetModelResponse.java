package com.diplom.faces_recognition.models;

import java.io.Serializable;

public class NetModelResponse extends GenericResponse implements Serializable {
    private String name;
    private String imageByteArray;
    private Double topX;
    private Double topY;
    private Double bottomX;
    private Double bottomY;

    public NetModelResponse(){
    }
    public String getName() {
        return name;
    }

    public String getImageByteArray() {
        return imageByteArray;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getTopX() {
        return topX;
    }

    public void setTopX(Double topX) {
        this.topX = topX;
    }

    public Double getTopY() {
        return topY;
    }

    public void setTopY(Double topY) {
        this.topY = topY;
    }

    public Double getBottomX() {
        return bottomX;
    }

    public void setBottomX(Double bottomX) {
        this.bottomX = bottomX;
    }

    public Double getBottomY() {
        return bottomY;
    }

    public void setBottomY(Double bottomY) {
        this.bottomY = bottomY;
    }
}
