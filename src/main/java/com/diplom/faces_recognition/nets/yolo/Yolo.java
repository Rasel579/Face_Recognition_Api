package com.diplom.faces_recognition.nets.yolo;

import com.diplom.faces_recognition.entity.netmodel.INetFrame;
import com.diplom.faces_recognition.models.GenericResponse;
import com.diplom.faces_recognition.models.MarkedObject;
import com.diplom.faces_recognition.models.NetModelResponse;
import com.diplom.faces_recognition.nets.cifar.contract.AbstractCifarNetModel;
import com.diplom.faces_recognition.nets.facerecognition.contract.IFaceRecognize;
import com.diplom.faces_recognition.nets.yolo.contract.AbstractObjDetectionNet;
import com.diplom.faces_recognition.utils.ImageUtils;
import com.diplom.faces_recognition.utils.log.ILog;
import com.diplom.faces_recognition.utils.yolo.Speed;
import com.diplom.faces_recognition.utils.yolo.Strategy;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.layers.objdetect.DetectedObject;
import org.deeplearning4j.nn.layers.objdetect.Yolo2OutputLayer;
import org.deeplearning4j.nn.layers.objdetect.YoloUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.threadly.concurrent.collections.ConcurrentArrayList;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.bytedeco.opencv.global.opencv_imgproc.*;


public class Yolo extends AbstractObjDetectionNet {

    private ComputationGraph yolo;
    private final OpenCVFrameConverter.ToMat toMatConverter = new OpenCVFrameConverter.ToMat();
    private Java2DFrameConverter imageConverter = new Java2DFrameConverter();

    public Yolo(ILog logger, IFaceRecognize faceRecognition, AbstractCifarNetModel trainCifar10Model) {
        super(logger, faceRecognition, trainCifar10Model);
    }

    @Override
    public void initialize() throws Exception {

        faceRecognition.loadModel();

        File[] files = new File(BASE + "/images").listFiles();
        for (File file : Objects.requireNonNull(files)) {
            File[] images = file.listFiles();
            addPhoto(Objects.requireNonNull(images)[0].getAbsolutePath(), file.getName());
        }

        yolo = YOLOPretrained.initSavedModel();
        prepareYOLOLabels();

        trainCifar10Model.loadTrainedModel(pretrainedCifarModel);
        loader = new NativeImageLoader(selectedSpeed.height, selectedSpeed.width, 3);
        warmUp(yolo);
    }

    @Override
    public GenericResponse feedNet(INetFrame frame) {
        try {

            if (yolo == null) {
                initialize();
            }

            modelsMap.put(frame.getWindowName(), yolo);
            stackMap.computeIfAbsent(frame.getWindowName(), k -> new Stack<>());

            BufferedImage img = ImageIO.read(new ByteArrayInputStream(frame.getBytesImage()));
            Mat mat = new Mat(toMatConverter.convert(imageConverter.convert(img)));
            Mat resizeMat = new Mat(getSelectedSpeed().height, getSelectedSpeed().width, mat.type());
            push(resizeMat, frame.getWindowName());
            org.bytedeco.opencv.global.opencv_imgproc.resize(mat, resizeMat, resizeMat.size());
            predictBoundingBoxes(frame.getWindowName());

            return drawBoundingBoxesRectangle(resizeMat, frame.getWindowName());
        } catch (Exception exception) {
            logger.error(exception.getMessage());
            return new GenericResponse(404, exception.getMessage());
        }

    }

    private void warmUp(ComputationGraph model) throws IOException {
        Yolo2OutputLayer outputLayer = (Yolo2OutputLayer) model.getOutputLayer(0);
        BufferedImage read = ImageIO.read(new File("./src/main/resources/sample.jpg"));
        INDArray indArray = loader.asMatrix(read);
        indArray = prepareImage(indArray);
        INDArray results = model.outputSingle(indArray);
        outputLayer.getPredictedObjects(results, YOLO_DETECTION_THRESHOLD);
    }

    @Override
    public void push(Mat frame, String windowName) {
        stackMap.get(windowName).push(frame);
    }

    @Override
    public GenericResponse drawBoundingBoxesRectangle(Mat matFrame, String windowName) {
        if (invalidData(matFrame) || outputFrames) {
            return null;
        }

        if (previousPredictedObjects == null) {
            previousPredictedObjects.addAll(predictedObjects);
        }

        ArrayList<MarkedObject> detectedObjects = predictedObjects == null ? new ArrayList<>() : new ArrayList<>(predictedObjects);

        for (MarkedObject markedObject : detectedObjects) {
            try {
                NetModelResponse netModelResponse = new NetModelResponse();
                createBoundingBoxRectangle(matFrame, markedObject, netModelResponse);
                previousPredictedObjects.add(markedObject);
                return netModelResponse;
            } catch (Exception e) {
                logger.error("Problem with out of the bounds image");
            }
        }
        return new GenericResponse(404, "Don't have predicted objects");
    }

    private void createBoundingBoxRectangle(Mat file, @NotNull MarkedObject markedObject, NetModelResponse netModelResponse) {
        double[] xy1 = markedObject.getDetectedObject().getTopLeftXY();
        double[] xy2 = markedObject.getDetectedObject().getBottomRightXY();

        MarkedObject minDistanceMarkedObject = findExistanceObjMatch(markedObject);

        if (minDistanceMarkedObject == null) {
            minDistanceMarkedObject = markedObject;
        }
        String id = minDistanceMarkedObject.getId();
        int predictedClass = minDistanceMarkedObject.getDetectedObject().getPredictedClass();

        int w = selectedSpeed.width;
        int h = selectedSpeed.height;
        double x1 = (double) Math.round(w * xy1[0] / selectedSpeed.gridWidth);
        double y1 = (double) Math.round(h * xy1[1] / selectedSpeed.gridHeight);
        double x2 = (double) Math.round(w * xy2[0] / selectedSpeed.gridWidth);
        double y2 = (double) Math.round(h * xy2[1] / selectedSpeed.gridHeight);

        netModelResponse.setTopX(x1);
        netModelResponse.setTopY(y1);
        netModelResponse.setBottomX(x2);
        netModelResponse.setBottomY(y2);

        rectangle(file, new Point((int)x1, (int)y1), new Point((int)x2, (int)y2), Scalar.BLUE);
        String name = "не знаю";
        try {
            name = faceRecognition.whoIs(file);
            netModelResponse.setName(name);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        putText(file, groupMap.get(map.get(predictedClass)) + "-" + name, new Point((int)x1 + 2, (int)y1 - 2), FONT_HERSHEY_DUPLEX, 1, Scalar.GREEN);
    }

    @Nullable
    private MarkedObject findExistanceObjMatch(MarkedObject markedObject) {
        MarkedObject minDistanceMarkedObject = null;
        for (MarkedObject predictedObject : previousPredictedObjects) {
            double distance = predictedObject.getL2Norm().distance2(markedObject.getL2Norm());
            if (strategy == Strategy.IoU_PLUS_ENCODINGS) {
                if (YoloUtils.iou(markedObject.getDetectedObject(), predictedObject.getDetectedObject()) >= 0.5 && distance <= trackingThreshold) {
                    minDistanceMarkedObject = predictedObject;
                    markedObject.setId(minDistanceMarkedObject.getId());
                    break;
                } else if (strategy == Strategy.ONLY_ENCODINGS) {
                    if (distance < trackingThreshold) {
                        minDistanceMarkedObject = predictedObject;
                        markedObject.setId(minDistanceMarkedObject.getId());
                        break;
                    }
                } else if (strategy == Strategy.ONLY_IoU) {
                    if (YoloUtils.iou(markedObject.getDetectedObject(), predictedObject.getDetectedObject()) >= 0.4) {
                        minDistanceMarkedObject = predictedObject;
                        markedObject.setId(minDistanceMarkedObject.getId());
                        break;
                    }
                }
            }
        }
        return minDistanceMarkedObject;
    }

    private boolean invalidData(Mat matFrame) {
        return predictedObjects == null || matFrame == null;
    }

    private INDArray prepareImage(Mat frame) throws IOException {
        if (frame == null) {
            return null;
        }
        ImagePreProcessingScaler imagePreProcessingScaler = new ImagePreProcessingScaler(0, 1);
        INDArray indArray = loader.asMatrix(frame);
        if (indArray == null) {
            return null;
        }
        imagePreProcessingScaler.transform(indArray);
        return indArray;
    }

    private INDArray prepareImage(INDArray indArray) {
        if (indArray == null) {
            return null;
        }

        ImagePreProcessingScaler imagePreProcessingScaler = new ImagePreProcessingScaler(0, 1);
        imagePreProcessingScaler.transform(indArray);
        return indArray;

    }

    private void prepareYOLOLabels() {
        prepareLabels(COCO_CLASSES);
    }

    private void prepareLabels(String[] coco_classes) {
        if (map == null) {
            groupMap = new HashMap<>();
            groupMap.put("car", "car");
            groupMap.put("bus", "bus");
            groupMap.put("truck", "truck");
            groupMap.put("person", "person");
            groupMap.put("cat", "cat");
            groupMap.put("dog", "dog");
            int i = 0;
            map = new HashMap<>();
            for (String s1 : coco_classes) {
                map.put(i++, s1);
            }
        }
    }

    @Override
    public Speed getSelectedSpeed() {
        return selectedSpeed;
    }

    @Override
    public void predictBoundingBoxes(String windowName) throws IOException {
        long start = System.currentTimeMillis();

        Yolo2OutputLayer outputLayer = (Yolo2OutputLayer) modelsMap.get(windowName).getOutputLayer(0);
        Mat matFrame = stackMap.get(windowName).pop();
        INDArray indArray = prepareImage(matFrame);
        logger.info("stack of frame size " + stackMap.get(windowName).size());

        if (indArray == null) {
            return;
        }

        INDArray results = modelsMap.get(windowName).outputSingle(indArray);
        if (results == null) {
            return;
        }
        List<DetectedObject> predictedObjects = outputLayer.getPredictedObjects(results, YOLO_DETECTION_THRESHOLD);
        YoloUtils.nms(predictedObjects, 0.5);
        List<MarkedObject> markedObjects = predictedObjects.stream().filter(e -> groupMap.get(map.get(e.getPredictedClass())) != null).map(e -> {
            try {
                return new MarkedObject(e, trainCifar10Model.getEmbeddings(matFrame, e, selectedSpeed), System.currentTimeMillis(), matFrame);
            } catch (Exception e1) {
            }
            return null;
        }).collect(Collectors.toCollection(ConcurrentArrayList::new));

        if (outputFrames) {
            for (MarkedObject markedObject : markedObjects) {
                ImageUtils.cropImageWithYOLO(selectedSpeed, markedObject.getFrame(), markedObject.getDetectedObject(), outputFrames);
            }
        }
        this.predictedObjects = markedObjects;
        logger.info("stack of predictions size " + this.predictedObjects.size());
        logger.info("Prediction time " + (System.currentTimeMillis() - start) / 1000d);
    }

    private void addPhoto(String path, String name) throws IOException {
        faceRecognition.registerNewMember(name, path);
    }
}
