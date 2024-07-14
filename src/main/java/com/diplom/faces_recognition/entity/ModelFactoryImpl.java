package com.diplom.faces_recognition.entity;

public class ModelFactoryImpl implements IModelFactory {
    @Override
    public IModel create(String name, String content) {
        return  new ModelImpl( name, content );
    }
}
