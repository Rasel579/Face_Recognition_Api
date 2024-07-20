package com.diplom.faces_recognition.nets.facerecognition.contract;

import org.bytedeco.opencv.opencv_core.Mat;

import java.io.IOException;

public interface IFaceRecognize {
    void loadModel() throws Exception;
    void registerNewMember(String memberId, String imagePath) throws IOException;
    String whoIs(String imagePath) throws IOException;
    String whoIs(Mat imageMat) throws IOException;

}
