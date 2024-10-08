package com.diplom.faces_recognition.nets.facerecognition;

import com.diplom.faces_recognition.nets.facerecognition.contract.AbstractFaceNetModel;
import com.diplom.faces_recognition.nets.facerecognition.contract.IFaceRecognize;
import com.diplom.faces_recognition.utils.log.ILog;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.Mat;
import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.graph.vertex.GraphVertex;
import org.deeplearning4j.nn.workspace.LayerWorkspaceMgr;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FaceRecognition implements IFaceRecognize {

    @Autowired
    private ILog logger;
    @Autowired
    private AbstractFaceNetModel faceNetModel;
    private static final double THRESHOLD = 1.5;
    private ComputationGraph computationGraph;
    private static final NativeImageLoader LOADER = new NativeImageLoader(96, 96, 3);
    private final HashMap<String, INDArray> memberEncodingsMap = new HashMap<>();

    private INDArray transpose(INDArray indArray1) {
        INDArray one = Nd4j.create(new int[]{1, 96, 96});
        one.assign(indArray1.get(NDArrayIndex.point(0), NDArrayIndex.point(2)));
        INDArray two = Nd4j.create(new int[]{1, 96, 96});
        two.assign(indArray1.get(NDArrayIndex.point(0), NDArrayIndex.point(1)));
        INDArray three = Nd4j.create(new int[]{1, 96, 96});
        three.assign(indArray1.get(NDArrayIndex.point(0), NDArrayIndex.point(0)));
        return Nd4j.concat(0, one, two, three).reshape(new int[]{1, 3, 96, 96});
    }

    private INDArray read(String pathname) throws IOException {
        Mat imread = opencv_imgcodecs.imread(new File(pathname).getAbsolutePath(), 1);
        INDArray indArray = LOADER.asMatrix(imread);
        return transpose(indArray);
    }

    private INDArray forwardPass(INDArray indArray) {
        Map<String, INDArray> output = computationGraph.feedForward(indArray, false);
        GraphVertex embeddings = computationGraph.getVertex("encodings");
        INDArray dense = output.get("dense");
        embeddings.setInputs(dense);
        INDArray embeddingValues = embeddings.doForward(false, LayerWorkspaceMgr.builder().defaultNoWorkspace().build());
        logger.info("dense =                 " + dense);
        logger.info("encodingsValues =                 " + embeddingValues);
        return embeddingValues;
    }

    private double distance(INDArray a, INDArray b) {
        return a.distance2(b);
    }

    public void loadModel() throws Exception {
        computationGraph = faceNetModel.initSavedModel();
        logger.info(computationGraph.summary());
    }

    public void registerNewMember(String memberId, String imagePath) throws IOException {
        INDArray read = read(imagePath);
        memberEncodingsMap.put(memberId, forwardPass(normalize(read)));
    }

    private static INDArray normalize(INDArray read) {
        DataNormalization scaler = new ImagePreProcessingScaler(0, 1);
        scaler.transform(read);
        return read;
    }

    public String whoIs(String imagePath) throws IOException {
        INDArray read = read(imagePath);
        INDArray encodings = forwardPass(read);
        double minDistance = Double.MAX_VALUE;
        String foundUser = "";
        for (Map.Entry<String, INDArray> entry : memberEncodingsMap.entrySet()) {
            INDArray value = entry.getValue();
            double distance = distance(value, encodings);
            logger.info("distance of " + entry.getKey() + " with " + new File(imagePath).getName() + " is " + distance);
            if (distance < minDistance) {
                minDistance = distance;
                foundUser = entry.getKey();
            }
        }
        if (minDistance > THRESHOLD) {
            foundUser = "Unknown user";
        }
        logger.info(foundUser + " with distance " + minDistance);
        return foundUser;
    }

    public String whoIs(Mat imageMat) throws IOException {
        INDArray read = LOADER.asMatrix(imageMat);
        INDArray encodings = forwardPass(normalize(read));
        double minDistance = Double.MAX_VALUE;
        String foundUser = "";
        for (Map.Entry<String, INDArray> entry : memberEncodingsMap.entrySet()) {
            INDArray value = entry.getValue();
            double distance = distance(value, encodings);
            logger.info("distance of " + entry.getKey() + " with " + " is " + distance);
            if (distance < minDistance) {
                minDistance = distance;
                foundUser = entry.getKey();
            }
        }
        if (minDistance > THRESHOLD) {
            foundUser = "Unknown user";
        }
        logger.info(foundUser + " with distance " + minDistance);
        return foundUser;
    }
}
