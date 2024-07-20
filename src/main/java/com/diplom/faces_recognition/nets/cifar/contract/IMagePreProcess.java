package com.diplom.faces_recognition.nets.cifar.contract;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;

public interface IMagePreProcess extends DataNormalization {
     void preProcess(INDArray features);
}
