package com.diplom.faces_recognition;

import com.diplom.faces_recognition.entity.IModel;
import com.diplom.faces_recognition.entity.ModelFactoryImpl;
import com.diplom.faces_recognition.presenters.ModelPresenter;
import com.diplom.faces_recognition.usecases.ModelInputBoundary;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;


@RunWith(JUnit4.class)
public class UseCasesTests {

    private final ModelInputBoundary inputBoundary = Mockito.mock(ModelInputBoundary.class);
    private final ModelPresenter presenter = Mockito.mock(ModelPresenter.class);

    @Test
    void assertAuthenticate(){
        IModel model = new ModelFactoryImpl().create("Hello", "World");
        Assertions.assertEquals(inputBoundary.authenticate(model), presenter.prepareFailureView(""));
    }
}
