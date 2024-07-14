package com.diplom.faces_recognition.presenters;

import com.diplom.faces_recognition.entity.IModel;
import com.diplom.faces_recognition.models.ModelResponse;

import java.util.List;

public interface ModelPresenter {
  List<ModelResponse>  prepareSuccessView( List<IModel> modelResponse);
  ModelResponse prepareSuccessView( ModelResponse response);
  ModelResponse prepareFailureView( String error );
}
