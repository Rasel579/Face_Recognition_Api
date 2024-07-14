package com.diplom.faces_recognition.dao;

import com.diplom.faces_recognition.entity.IModel;
import com.diplom.faces_recognition.entity.ModelFactoryImpl;
import com.diplom.faces_recognition.usecases.ModelDataSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

public class JpaModel implements ModelDataSource {

    @Autowired
    private JPAModelRepository repository;

    @Override
    public List<IModel> get() {
        return repository.getAllUsers().stream().map(user -> new ModelFactoryImpl().create(user.getName(), user.getPassword()) ).collect(Collectors.toList());
    }

    @Override
    public void saveUser(IModel entity) {
        repository.saveUser(entity.getName(), entity.getContent());
    }

    @Override
    public boolean verifiedUser(IModel entity) {
        return repository.verifiedUser( entity.getName(), entity.getContent() );
    }


}
