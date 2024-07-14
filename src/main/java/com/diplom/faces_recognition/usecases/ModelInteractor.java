package com.diplom.faces_recognition.usecases;

import com.diplom.faces_recognition.entity.IModel;
import com.diplom.faces_recognition.entity.IModelFactory;
import com.diplom.faces_recognition.models.ModelRequest;
import com.diplom.faces_recognition.models.ModelResponse;
import com.diplom.faces_recognition.presenters.ModelPresenter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ModelInteractor implements ModelInputBoundary {

    @Autowired
    private ModelDataSource dataSource;
    @Autowired
    private ModelPresenter presenter;
    @Autowired
    private IModelFactory modelFactory;

    @Override
    public List<ModelResponse> create(ModelRequest modelRequest) {
        List<IModel> users = dataSource.get().stream().map(user -> modelFactory.create(user.getName(), user.getContent()) ).toList();
        return presenter.prepareSuccessView( users );
    }

    @Override
    public ModelResponse authenticate(IModel newUser) {
        if ( newUser.getName().isEmpty() || newUser.getContent().isEmpty() ){
            return presenter.prepareFailureView( "Не введен логин или пароль");
        }
        dataSource.saveUser( newUser );
        return presenter.prepareSuccessView( new ModelResponse("SUCCESS", "USER ADDED"));
    }

    @Override
    public ModelResponse signIn(IModel user) {
        return dataSource.verifiedUser(user) ?  presenter.prepareSuccessView(new ModelResponse("SUCCESS", "USER SIGN IN")) : presenter.prepareFailureView( "FAILED WRONG_PASSWORD" );
    }
}
