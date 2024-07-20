package com.diplom.faces_recognition.nets.facerecognition.contract;

import org.bytedeco.opencv.opencv_core.Mat;
import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.graph.ComputationGraph;

import java.io.IOException;

public interface IFaceNet {
    ComputationGraphConfiguration conf();
    ComputationGraph initSavedModel() throws IOException;

}
