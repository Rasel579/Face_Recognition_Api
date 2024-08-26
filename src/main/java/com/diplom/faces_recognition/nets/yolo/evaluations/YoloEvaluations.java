package com.diplom.faces_recognition.nets.yolo.evaluations;

import org.datavec.api.records.metadata.RecordMetaDataImageURI;
import org.datavec.image.recordreader.objdetect.ImageObject;
import org.datavec.image.recordreader.objdetect.impl.VocLabelProvider;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.layers.objdetect.DetectedObject;
import org.deeplearning4j.nn.layers.objdetect.Yolo2OutputLayer;
import org.nd4j.common.primitives.Counter;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.DataSet;
import java.util.LinkedList;
import java.util.List;

public class YoloEvaluations {

    private final double ioUThreshold;
    private final double confidenceThreshold;

    private final int width;
    private final int height;

    private final int gridWidth;
    private final int gridHeight;
    private List<String> labelsList;

    protected Counter<String> truePositives = new Counter<>();
    protected Counter<String> falsePositives = new Counter<>();
    protected Counter<String> falseNegatives = new Counter<>();

    public YoloEvaluations(ComputationGraph yoloModel, RecordReaderDataSetIterator dataSetIterator, VocLabelProvider labelProvider, int width, int height, int gridWidth, int gridHeight) {
        this(yoloModel, dataSetIterator, labelProvider, 0.5, 0.4, width, height, gridWidth, gridHeight);
    }

    public YoloEvaluations(ComputationGraph yoloModel, RecordReaderDataSetIterator dataSetIterator, VocLabelProvider labelProvider, double ioUThreshold, double confidenceThreshold, int width, int height, int gridWidth, int gridHeight) {
        this.ioUThreshold = ioUThreshold;
        this.confidenceThreshold = confidenceThreshold;
        this.gridHeight = gridHeight;
        this.gridWidth = gridWidth;
        this.width = width;
        this.height = height;

        dataSetIterator.reset();
        labelsList = dataSetIterator.getLabels();
        Yolo2OutputLayer yout = (Yolo2OutputLayer) yoloModel.getOutputLayer(0);

        while (dataSetIterator.hasNext()) {
            DataSet ds = dataSetIterator.next();

            INDArray features = ds.getFeatures();
            INDArray results = yoloModel.outputSingle(features);
            RecordMetaDataImageURI metadata = (RecordMetaDataImageURI) ds.getExampleMetaData().get(0);
            List<ImageObject> expectedObjects = labelProvider.getImageObjectsForPath(metadata.getURI());
            LinkedList<DetectedObject> detectedObjects = new LinkedList<>(yout.getPredictedObjects(results, confidenceThreshold));

            for (ImageObject expected : expectedObjects) {

                double bestIou = 0.0;
                int detectedObjectIndex = -1;

                if (!detectedObjects.isEmpty()) {
                    for (DetectedObject detectedObj : detectedObjects) {
                        if (detectedObj == null)
                            continue;
                        if (labelsList.size() <=  detectedObj.getPredictedClass() || !labelsList.get(detectedObj.getPredictedClass()).equals(expected.getLabel())) {
                            continue;
                        }

                        if (calcIou(expected, detectedObj) > bestIou && (detectedObj.getConfidence() > confidenceThreshold)) {
                            bestIou = calcIou(expected, detectedObj);
                            System.out.println("best IoU " + bestIou);
                            detectedObjectIndex = detectedObjects.indexOf(detectedObj);
                        }
                    }

                    if (bestIou >= ioUThreshold) {
                        truePositives.incrementCount(expected.getLabel(), 1); //True Positives
                    } else {
                        falsePositives.incrementCount(expected.getLabel(), 1); //False Positive
                    }

                    if (detectedObjectIndex != -1)
                        detectedObjects.remove(detectedObjectIndex); //removing detected object to avoid repetition
                } else {
                    falseNegatives.incrementCount(expected.getLabel(), 1); //False Negative
                }
            }
            features.close();
            results.close();
        }

    }

    private double calcIou(ImageObject expected, DetectedObject detectedObj) {
        assert expected != null;
        assert detectedObj != null;

        double[] xy1 = detectedObj.getTopLeftXY();
        double[] xy2 = detectedObj.getBottomRightXY();

        int x1 = (int) Math.round(width * xy1[0] / gridWidth);
        int y1 = (int) Math.round(height * xy1[1] / gridHeight);

        int x2 = (int) Math.round(width * xy2[0] / gridWidth);
        int y2 = (int) Math.round(height * xy2[1] / gridHeight);


        //if the GT bbox and predcited BBox do not overlap then iou=0
        // If bottom right of x-coordinate  GT  bbox is less than or above the top left of x coordinate of  the predicted BBox
        if (expected.getX2() < x1) {
            return 0.0;
        }

        // If bottom right of y-coordinate  GT  bbox is less than or above the top left of y coordinate of  the predicted BBox
        if (expected.getY2() < y1) {
            return 0.0;
        }

        // If bottom right of x-coordinate  GT  bbox is greater than or below the bottom right  of x coordinate of  the predcited BBox
        if (expected.getX1() > x2) {
            return 0.0;
        }

        // If bottom right of y-coordinate  GT  bbox is greater than or below the bottom right  of y coordinate of  the predcited BBox
        if (expected.getY1() > y2) {
            return 0.0;
        }

        double expectedBboxArea = (expected.getX2() - expected.getX1() + 1) * (expected.getY2() - expected.getY1() + 1);
        double predictedBboxArea = (x2 - x1 + 1) * (y2 - y1 + 1);

        double xTopLeft = Math.max(expected.getX1(), x1);
        double yTopLeft = Math.max(expected.getY1(), y1);
        double xBottomRight = Math.min(expected.getX2(), x2);
        double yBottomRight = Math.min(expected.getY2(), y2);

        double intersectionArea = (xBottomRight - xTopLeft + 1) * (yBottomRight - yTopLeft + 1);
        double unionArea = (expectedBboxArea + predictedBboxArea - intersectionArea);

        return intersectionArea / unionArea;
    }

    public double getPrecision() {
        return (truePositives.totalCount()) / (truePositives.totalCount() + falsePositives.totalCount());
    }

    public double getRecall() {
        return (truePositives.totalCount()) / (truePositives.totalCount() + falseNegatives.totalCount());
    }

    public double getF1() {
        return Double.isNaN((0.5) * (getPrecision() + getRecall())) ? 0 : (0.5) * (getPrecision() + getRecall());
    }

    public List<String> getLabelsList() {
        return labelsList;
    }
}
