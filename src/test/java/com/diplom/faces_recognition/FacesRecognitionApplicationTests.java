package com.diplom.faces_recognition;

import com.diplom.faces_recognition.entity.IModel;
import com.diplom.faces_recognition.entity.IModelFactory;
import com.diplom.faces_recognition.entity.ModelFactoryImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;



@RunWith(JUnit4.class)
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
