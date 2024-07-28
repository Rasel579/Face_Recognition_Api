package com.diplom.faces_recognition.presenters.net;

import com.diplom.faces_recognition.models.GenericResponse;
import com.diplom.faces_recognition.models.ModelResponse;

public interface NetPresenter {
    GenericResponse prepareSuccessView(GenericResponse response);
    GenericResponse prepareFailureView( GenericResponse error );
}
