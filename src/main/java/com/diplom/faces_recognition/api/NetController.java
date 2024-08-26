package com.diplom.faces_recognition.api;

import com.diplom.faces_recognition.entity.IModelFactory;
import com.diplom.faces_recognition.models.GenericResponse;
import com.diplom.faces_recognition.models.ModelRequest;
import com.diplom.faces_recognition.models.ModelResponse;
import com.diplom.faces_recognition.models.NetRequest;
import com.diplom.faces_recognition.usecases.ModelInputBoundary;
import com.diplom.faces_recognition.usecases.net.NetUsecase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name="Работа с нейронной моделью", description="распознает образы на изображении")
@RestController
public class NetController {

    @Autowired
    private NetUsecase usecase;
    @Autowired
    private IModelFactory factory;

    @Operation(
            summary = "Распознавание образов",
            description = "Распознавание образов по переданному изображению"
    )
    @PostMapping("/net/detect")
    GenericResponse detectObjects(@RequestBody NetRequest request){
        return usecase.predictObjets( factory.createNetModel(request.getName(), request.getImageByteArray()));
    }
}
