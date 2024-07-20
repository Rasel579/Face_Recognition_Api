package com.diplom.faces_recognition.nets.cifar.contract;

import com.diplom.faces_recognition.utils.yolo.Speed;
import org.bytedeco.opencv.opencv_core.Mat;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.layers.objdetect.DetectedObject;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.IOException;

public interface ICifarNetModel {
    ComputationGraph getCifar10Transfer();
    void loadTrainedModel(String pretrainedCifarModel) throws IOException;
    INDArray getEmbeddings(Mat file, DetectedObject object, Speed selectedSpeed) throws IOException;

}
