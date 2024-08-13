package com.diplom.faces_recognition.api;

import com.diplom.faces_recognition.entity.IModelFactory;
import com.diplom.faces_recognition.models.ModelResponse;
import com.diplom.faces_recognition.usecases.ModelInputBoundary;
import com.diplom.faces_recognition.models.ModelRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @Autowired
    private ModelInputBoundary modelInputBoundary;
    @Autowired
    private IModelFactory modelFactory;

    @PostMapping("/auth/signup")
    ModelResponse authenticate(@RequestBody ModelRequest request){
        return modelInputBoundary.authenticate(modelFactory.create(request.getName(), request.getContent()));
    }

    @PostMapping("/auth/signin")
    ModelResponse signIn(@RequestBody ModelRequest request){
        return modelInputBoundary.signIn(modelFactory.create(request.getName(), request.getContent()));
    }
}
