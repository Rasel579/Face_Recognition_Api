package com.diplom.faces_recognition;

import com.diplom.faces_recognition.entity.IModel;
import com.diplom.faces_recognition.entity.IModelFactory;
import com.diplom.faces_recognition.entity.ModelFactoryImpl;
import com.diplom.faces_recognition.entity.ModelImpl;
import com.diplom.faces_recognition.models.ModelRequest;
import com.diplom.faces_recognition.usecases.ModelInputBoundary;
import com.diplom.faces_recognition.usecases.ModelInteractor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@SpringBootTest
class FacesRecognitionApplicationTests {

    private IModelFactory factory = mock(ModelFactoryImpl.class);
    private ModelInputBoundary interactor = new ModelInteractor();

    @Test
    void assertModel() {
        IModel model = new ModelFactoryImpl().create("Hello", "World");
        Assertions.assertEquals(model.getName(), "Hello");
    }

    @Test
    void assetUseCase() {
        IModel model = new ModelFactoryImpl().create("Hello", "World");
        when(factory.create("Hello", "World")).thenReturn(model);
    }
}
