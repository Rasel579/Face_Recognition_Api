package com.diplom.faces_recognition;

import com.diplom.faces_recognition.entity.IModel;
import com.diplom.faces_recognition.entity.IModelFactory;
import com.diplom.faces_recognition.entity.ModelFactoryImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import static org.mockito.Mockito.mock;



@SpringBootTest
class FacesRecognitionApplicationTests {

    private IModelFactory factory = mock(ModelFactoryImpl.class);

    @Test
    void assertModel() {
        IModel model = new ModelFactoryImpl().create("Hello", "World");
        Assertions.assertEquals("Hello", model.getName());
    }

    @Test
    void assetUseCase() {
        IModel model = new ModelFactoryImpl().create("Hello", "World");
        Mockito.when(factory.create("Hello", "World")).thenReturn(model);
    }
}
