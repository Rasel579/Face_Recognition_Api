package com.diplom.faces_recognition.nets.yolo.contract;

import com.diplom.faces_recognition.models.MarkedObject;
import com.diplom.faces_recognition.nets.cifar.contract.AbstractCifarNetModel;
import com.diplom.faces_recognition.nets.facerecognition.contract.IFaceRecognize;
import com.diplom.faces_recognition.utils.log.ILog;
import com.diplom.faces_recognition.utils.yolo.Speed;
import com.diplom.faces_recognition.utils.yolo.Strategy;
import org.bytedeco.opencv.opencv_core.Mat;
import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.threadly.concurrent.collections.ConcurrentArrayList;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractObjDetectionNet implements IObjDetectionNet {

    protected ILog logger;
    protected IFaceRecognize faceRecognition;
    protected AbstractCifarNetModel trainCifar10Model;

    public AbstractObjDetectionNet(ILog logger, IFaceRecognize faceRecognition, AbstractCifarNetModel trainCifar10Model) {
        this.logger = logger;
        this.faceRecognition = faceRecognition;
        this.trainCifar10Model = trainCifar10Model;
        try {
            initialize();
        } catch (Exception e) {

            logger.error(e.getMessage());
        }
    }

    protected static final double YOLO_DETECTION_THRESHOLD = 0.75;
    protected final String[] COCO_CLASSES = {"person", "bicycle", "car", "motorbike", "aeroplane", "bus", "train", "truck", "boat", "traffic light", "fire hydrant", "stop sign", "parking meter", "bench", "bird", "cat", "dog", "horse", "sheep", "cow", "elephant", "bear", "zebra", "giraffe", "backpack", "umbrella", "handbag", "tie", "suitcase", "frisbee", "skis", "snowboard", "sports ball", "kite", "baseball bat", "baseball glove", "skateboard", "surfboard", "tennis racket", "bottle", "wine glass", "cup", "fork", "knife", "spoon", "bowl", "banana", "apple", "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza", "donut", "cake", "chair", "sofa", "pottedplant", "bed", "diningtable", "toilet", "tvmonitor", "laptop", "mouse", "remote", "keyboard", "cell phone", "microwave", "oven", "toaster", "sink", "refrigerator", "book", "clock", "vase", "scissors", "teddy bear", "hair drier", "toothbrush"};
    protected final Map<String, Stack<Mat>> stackMap = new ConcurrentHashMap<>();
    protected final Speed selectedSpeed = Speed.MEDIUM;
    protected boolean outputFrames = false;
    protected double trackingThreshold = 0.2;
    protected String pretrainedCifarModel = "631_epoch_data_e512_b256_600.zip";
    protected Strategy strategy = Strategy.IoU_PLUS_ENCODINGS;
    protected volatile List<MarkedObject> predictedObjects = new ConcurrentArrayList<>();
    protected final Set<MarkedObject> previousPredictedObjects = new TreeSet<>();
    protected HashMap<Integer, String> map;
    protected HashMap<String, String> groupMap;
    protected final Map<String, ComputationGraph> modelsMap = new ConcurrentHashMap<>();
    protected NativeImageLoader loader;

    protected static final String BASE = "./src/main/resources/";

}
