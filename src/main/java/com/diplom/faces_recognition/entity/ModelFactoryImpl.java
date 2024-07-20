package com.diplom.faces_recognition.entity;

import com.diplom.faces_recognition.entity.netmodel.INetFrame;
import com.diplom.faces_recognition.entity.netmodel.NetFrameModel;

import java.nio.charset.StandardCharsets;

public class ModelFactoryImpl implements IModelFactory {
    @Override
    public IModel create(String name, String content) {
        return  new ModelImpl( name, content );
    }

    @Override
    public INetFrame createNetModel(String windowName, String content) {
        return new NetFrameModel( windowName, content.getBytes(StandardCharsets.UTF_8) );
    }
}
