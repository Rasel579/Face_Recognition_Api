package com.diplom.faces_recognition.presenters;

import com.diplom.faces_recognition.entity.IModel;
import com.diplom.faces_recognition.models.ModelResponse;

import java.util.List;

public class ModelResponseFormatter implements ModelPresenter {
    @Override
    public List<ModelResponse> prepareSuccessView( List<IModel> modelResponse) {
        return modelResponse.stream().map( user -> new ModelResponse( user.getName(), user.getContent()) ).toList();
    }

    @Override
    public ModelResponse prepareSuccessView(ModelResponse response) {
        return response;
    }

    @Override
    public ModelResponse prepareFailureView(String error) {

        return new ModelResponse("error", error);
    }
}
