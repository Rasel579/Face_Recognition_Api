package com.diplom.faces_recognition.nets.facerecognition.contract;

import com.diplom.faces_recognition.utils.log.ILog;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.learning.config.IUpdater;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractFaceNetModel implements IFaceNet {

    @Autowired
    protected static ILog logger;
    protected final int numClasses = 31;
    protected final long seed = 1234;
    protected final int[] inputShape = new int[]{3, 96, 96};
    protected final IUpdater updater = new Adam(0.1, 0.9, 0.999, 0.01);
    protected final int encodings = 128;
    public static int reluIndex = 1;
    public static int paddingIndex = 1;

    protected static final int BATCH_SIZE = 10;
    protected static final int EPOCH_NUM = 25;
    protected static final String SAVE_PATH = "./src/main/resources/facenet_model/trained_face_recon_model.zip";
}
