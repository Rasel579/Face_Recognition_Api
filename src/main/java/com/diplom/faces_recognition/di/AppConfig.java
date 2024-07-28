package com.diplom.faces_recognition.di;

import com.diplom.faces_recognition.dao.JpaModel;
import com.diplom.faces_recognition.entity.IModelFactory;
import com.diplom.faces_recognition.entity.ModelFactoryImpl;
import com.diplom.faces_recognition.nets.cifar.CifarImagePreProcessor;
import com.diplom.faces_recognition.nets.cifar.TrainCifar10Model;
import com.diplom.faces_recognition.nets.cifar.contract.AbstractCifarNetModel;
import com.diplom.faces_recognition.nets.cifar.contract.IMagePreProcess;
import com.diplom.faces_recognition.nets.facerecognition.FaceNetModel;
import com.diplom.faces_recognition.nets.facerecognition.FaceRecognition;
import com.diplom.faces_recognition.nets.facerecognition.contract.AbstractFaceNetModel;
import com.diplom.faces_recognition.nets.facerecognition.contract.IFaceRecognize;
import com.diplom.faces_recognition.nets.yolo.Yolo;
import com.diplom.faces_recognition.nets.yolo.contract.AbstractObjDetectionNet;
import com.diplom.faces_recognition.presenters.ModelPresenter;
import com.diplom.faces_recognition.presenters.ModelResponseFormatter;
import com.diplom.faces_recognition.presenters.net.NetPresenter;
import com.diplom.faces_recognition.presenters.net.NetPresenterImpl;
import com.diplom.faces_recognition.usecases.ModelDataSource;
import com.diplom.faces_recognition.usecases.ModelInputBoundary;
import com.diplom.faces_recognition.usecases.ModelInteractor;
import com.diplom.faces_recognition.usecases.net.NetIteractor;
import com.diplom.faces_recognition.usecases.net.NetUsecase;
import com.diplom.faces_recognition.utils.log.ILog;
import com.diplom.faces_recognition.utils.log.LoggerImpl;
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
    public NetPresenter netPresenter(){
        return new NetPresenterImpl();
    }

    @Bean
    public IModelFactory modelFactory(){
        return new ModelFactoryImpl();
    }

    @Bean
    public ILog logger() { return new LoggerImpl(); }

    @Bean
    public IMagePreProcess dataNormalization() { return new CifarImagePreProcessor(); }

    @Bean
    public AbstractCifarNetModel cifarNet() { return new TrainCifar10Model(); }

    @Bean
    public AbstractFaceNetModel faceNet() { return new FaceNetModel(); }

    @Bean
    public IFaceRecognize faceRecognize() { return new FaceRecognition(); }

    @Bean
    public AbstractObjDetectionNet objDetectionNet() { return new Yolo(); }

    @Bean
    public NetUsecase netUsecase() { return new NetIteractor(); }
}
