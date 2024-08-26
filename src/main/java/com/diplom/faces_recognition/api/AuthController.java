package com.diplom.faces_recognition.api;

import com.diplom.faces_recognition.entity.IModelFactory;
import com.diplom.faces_recognition.models.ModelResponse;
import com.diplom.faces_recognition.usecases.ModelInputBoundary;
import com.diplom.faces_recognition.models.ModelRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
@Tag(name="Авторизация пользователя", description="авторизует и аутентифицирует")
@RestController
public class AuthController {

    @Autowired
    private ModelInputBoundary modelInputBoundary;
    @Autowired
    private IModelFactory modelFactory;

    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Зарегистрировать нового пользователя"
    )
    @PostMapping("/auth/signup")
    ModelResponse authenticate(@RequestBody ModelRequest request){
        return modelInputBoundary.authenticate(modelFactory.create(request.getName(), request.getContent()));
    }
    @Operation(
            summary = "Авторизация",
            description = "Авторизовать нового пользователя"
    )
    @PostMapping("/auth/signin")
    ModelResponse signIn(@RequestBody ModelRequest request){
        return modelInputBoundary.signIn(modelFactory.create(request.getName(), request.getContent()));
    }
}
