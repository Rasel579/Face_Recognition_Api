package com.diplom.faces_recognition.nets.yolo.contract;

import com.diplom.faces_recognition.entity.netmodel.INetFrame;
import com.diplom.faces_recognition.models.GenericResponse;
import com.diplom.faces_recognition.utils.yolo.Speed;
import org.bytedeco.opencv.opencv_core.Mat;

import java.io.IOException;

public interface IObjDetectionNet {
    void initialize() throws Exception;
    void push(Mat frame, String windowName);
    GenericResponse drawBoundingBoxesRectangle(Mat matFrame, String windowName);
    Speed getSelectedSpeed();
    void predictBoundingBoxes(String windowName) throws IOException;
    GenericResponse feedNet(INetFrame frame);

}
