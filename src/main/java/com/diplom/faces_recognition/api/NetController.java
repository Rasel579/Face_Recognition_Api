package com.diplom.faces_recognition.api;

import com.diplom.faces_recognition.entity.IModelFactory;
import com.diplom.faces_recognition.models.GenericResponse;
import com.diplom.faces_recognition.models.ModelRequest;
import com.diplom.faces_recognition.models.ModelResponse;
import com.diplom.faces_recognition.models.NetRequest;
import com.diplom.faces_recognition.usecases.ModelInputBoundary;
import com.diplom.faces_recognition.usecases.net.NetUsecase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NetController {

    @Autowired
    private NetUsecase usecase;
    @Autowired
    private IModelFactory factory;
    @PostMapping("/net/detect")
    GenericResponse detectObjects(@RequestBody NetRequest request){
        return usecase.predictObjets( factory.createNetModel(request.getName(), request.getImageByteArray()));
    }
}
