package com.diplom.faces_recognition.usecases;

import com.diplom.faces_recognition.entity.IModel;
import com.diplom.faces_recognition.models.ModelRequest;
import com.diplom.faces_recognition.models.ModelResponse;

import java.util.List;

public interface ModelInputBoundary {
    List<ModelResponse> create(ModelRequest modelRequest);

    ModelResponse authenticate(IModel newUser);

    ModelResponse signIn(IModel newUser);
}
