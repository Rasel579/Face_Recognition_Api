package com.diplom.faces_recognition.models;

import java.util.Objects;

public class ModelResponse {
    private String name;
    private String content;

    public ModelResponse(String name, String content) {
        this.name = name;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ModelResponse that = (ModelResponse) o;
        return Objects.equals(name, that.name) && Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, content);
    }
}
