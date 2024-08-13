package com.diplom.faces_recognition.entity;

import com.diplom.faces_recognition.entity.netmodel.INetFrame;
import com.diplom.faces_recognition.entity.netmodel.NetFrameModel;
import org.apache.commons.codec.binary.Base64;

public class ModelFactoryImpl implements IModelFactory {
    @Override
    public IModel create(String name, String content) {
        return  new ModelImpl( name, content );
    }

    @Override
    public INetFrame createNetModel(String windowName, String content) {
        return new NetFrameModel( windowName, Base64.decodeBase64(content) );
    }
}
