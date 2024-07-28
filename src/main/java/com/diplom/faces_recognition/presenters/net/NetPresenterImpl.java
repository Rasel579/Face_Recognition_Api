package com.diplom.faces_recognition.presenters.net;

import com.diplom.faces_recognition.models.GenericResponse;

public class NetPresenterImpl implements NetPresenter {
    @Override
    public GenericResponse prepareSuccessView(GenericResponse response) {
        return response;
    }

    @Override
    public GenericResponse prepareFailureView(GenericResponse error) {
        return error;
    }
}
