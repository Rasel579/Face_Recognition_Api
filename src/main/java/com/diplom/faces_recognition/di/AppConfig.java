package com.diplom.faces_recognition.di;

import com.diplom.faces_recognition.dao.JpaModel;
import com.diplom.faces_recognition.entity.IModelFactory;
import com.diplom.faces_recognition.entity.ModelFactoryImpl;
import com.diplom.faces_recognition.presenters.ModelPresenter;
import com.diplom.faces_recognition.presenters.ModelResponseFormatter;
import com.diplom.faces_recognition.usecases.ModelDataSource;
import com.diplom.faces_recognition.usecases.ModelInputBoundary;
import com.diplom.faces_recognition.usecases.ModelInteractor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean
    public ModelInputBoundary modelInputBoundary() {
        return new ModelInteractor();
    }

    @Bean
    public ModelDataSource modelDataSource(){
        return  new JpaModel();
    }

    @Bean
    public ModelPresenter modelPresenter(){
        return new ModelResponseFormatter();
    }

    @Bean
    public IModelFactory modelFactory(){
        return new ModelFactoryImpl();
    }
}
