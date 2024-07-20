package com.diplom.faces_recognition.entity;

import com.diplom.faces_recognition.entity.netmodel.INetFrame;

public interface IModelFactory {
    IModel create( String name, String content);
    INetFrame createNetModel( String windowName, String content );
}
