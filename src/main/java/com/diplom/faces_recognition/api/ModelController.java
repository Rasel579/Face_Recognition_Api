package com.diplom.faces_recognition.api;

import com.diplom.faces_recognition.entity.IModelFactory;
import com.diplom.faces_recognition.models.ModelResponse;
import com.diplom.faces_recognition.usecases.ModelInputBoundary;
import com.diplom.faces_recognition.models.ModelRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ModelController {

    @Autowired
    private ModelInputBoundary modelInputBoundary;
    @Autowired
    private IModelFactory modelFactory;

    @GetMapping("/model")
    List<ModelResponse> create(@RequestBody ModelRequest request){
        return modelInputBoundary.create(request);
    }

    @PostMapping("/model/save")
    ModelResponse authenticate(@RequestBody ModelRequest request){
        return modelInputBoundary.authenticate(modelFactory.create(request.getName(), request.getContent()));
    }

    @PostMapping("/model/signin")
    ModelResponse signIn(@RequestBody ModelRequest request){
        return modelInputBoundary.signIn(modelFactory.create(request.getName(), request.getContent()));
    }
}
