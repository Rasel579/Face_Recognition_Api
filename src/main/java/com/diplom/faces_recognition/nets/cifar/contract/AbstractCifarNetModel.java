package com.diplom.faces_recognition.nets.cifar.contract;

import com.diplom.faces_recognition.utils.ImageUtils;
import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

public abstract class AbstractCifarNetModel implements ICifarNetModel {

    @Autowired
    protected IMagePreProcess IMAGE_PRE_PROCESSOR;
    protected static final int NUM_POSSIBLE_LABELS = 611;
    protected static final int BATCH_SIZE = 256;
    protected static final int E_BATCH_SIZE = 256;
    protected static final NativeImageLoader LOADER = new NativeImageLoader(ImageUtils.HEIGHT, ImageUtils.WIDTH, 3);
    protected static final String CONTENT_LAYER_NAME = "embeddings";
    protected static final String MODEL_SAVE_PATH = "./src/main/resources/static/models/";
    protected static final int SAVE_INTERVAL = 50;
    protected static final int TEST_INTERVAL = 5;
    protected static final int EPOCH_INTERVAL = 2400;
    protected static final int EMBEDDINGS = 512;
    protected static final int I_EPOCH = 0;
    protected static final double LAMBDA = 5e-4;
    protected static final String PREFIX = "EXP";

    protected ComputationGraph cifar10Transfer;
    protected static final String FREEZE_UNTIL_LAYER = "fc2";

    public ComputationGraph getCifar10Transfer() {
        return cifar10Transfer;
    }

    protected abstract void train() throws IOException;

    protected abstract void testResult(ComputationGraph cifar10Model, DataSetIterator testSetIterator, int iEpoch, String modelName);

    protected abstract void saveProgress(ComputationGraph cifar10Model, int iEpoch, String modelName) throws IOException;

}
