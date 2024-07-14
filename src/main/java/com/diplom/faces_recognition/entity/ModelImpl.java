package com.diplom.faces_recognition.entity;

public class ModelImpl implements IModel {
    private String name;
    private String content;

    public ModelImpl(String name, String content) {
        this.name = name;
        this.content = content;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getContent() {
        return content;
    }
}
