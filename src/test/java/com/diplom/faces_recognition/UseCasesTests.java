package com.diplom.faces_recognition;

import com.diplom.faces_recognition.entity.IModel;
import com.diplom.faces_recognition.entity.IModelFactory;
import com.diplom.faces_recognition.models.ModelResponse;
import com.diplom.faces_recognition.presenters.ModelPresenter;
import com.diplom.faces_recognition.usecases.ModelDataSource;
import com.diplom.faces_recognition.usecases.ModelInteractor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UseCasesTests {
    @Mock
    private ModelDataSource dataSource = Mockito.mock(ModelDataSource.class);
    @Mock
    private ModelPresenter presenter = Mockito.mock(ModelPresenter.class);
    @Mock
    private IModelFactory modelFactory = Mockito.mock(IModelFactory.class);

    @InjectMocks
    private ModelInteractor interactor;

    // Тестовые данные
    private final IModel validUser = Mockito.mock(IModel.class);
    private final IModel emptyUser = Mockito.mock(IModel.class);
    private final ModelResponse successResponse = new ModelResponse("SUCCESS", "USER ADDED");
    private final ModelResponse errorResponse = new ModelResponse("ERROR", "Не введен логин или пароль");

    @Test
    void authenticate_shouldReturnErrorWhenEmptyFields() {
        // Подготовка
        when(emptyUser.getName()).thenReturn("");
        when(emptyUser.getContent()).thenReturn("");
        when(presenter.prepareFailureView(anyString())).thenReturn(errorResponse);

        // Выполнение
        ModelResponse result = interactor.authenticate(emptyUser);

        // Проверки
        Assertions.assertEquals(errorResponse, result);
        verify(presenter).prepareFailureView("Не введен логин или пароль");
        verify(dataSource, never()).saveUser(any());
    }

    @Test
    void authenticate_shouldSaveUserWhenValid() {
        // Подготовка
        when(validUser.getName()).thenReturn("test");
        when(validUser.getContent()).thenReturn("password");
        when(presenter.prepareSuccessView(any(ModelResponse.class))).thenReturn(successResponse);

        // Выполнение
        ModelResponse result = interactor.authenticate(validUser);

        // Проверки
        Assertions.assertEquals(successResponse, result);
        verify(dataSource).saveUser(validUser);
        verify(presenter).prepareSuccessView(successResponse);
    }
}
