package com.diplom.faces_recognition.usecases;

import com.diplom.faces_recognition.entity.IModel;

import java.util.List;

public interface ModelDataSource {
    List<IModel> get();

    void saveUser(IModel entity);

    boolean verifiedUser( IModel entity);
}
