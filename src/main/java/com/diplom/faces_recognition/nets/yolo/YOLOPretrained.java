package com.diplom.faces_recognition.nets.yolo;

import com.diplom.faces_recognition.nets.yolo.evaluations.YoloEvaluations;
import com.diplom.faces_recognition.utils.yolo.Speed;
import org.datavec.api.split.FileSplit;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.recordreader.objdetect.ObjectDetectionRecordReader;
import org.datavec.image.recordreader.objdetect.impl.VocLabelProvider;
import org.deeplearning4j.core.storage.StatsStorage;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.CNN2DFormat;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.WorkspaceMode;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.objdetect.Yolo2OutputLayer;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.modelimport.keras.KerasLayer;
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.modelimport.keras.exceptions.InvalidKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.exceptions.UnsupportedKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.layers.convolutional.KerasSpaceToDepth;
import org.deeplearning4j.nn.transferlearning.FineTuneConfiguration;
import org.deeplearning4j.nn.transferlearning.TransferLearning;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.model.stats.StatsListener;
import org.deeplearning4j.ui.model.storage.InMemoryStatsStorage;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class YOLOPretrained {
    private static final long seed = 12345;
    private static final String TRAIN_PATH = "./src/main/resources/static/yolo2_dl4j_inference.v1.zip";
    private static final String TRAINED_PATH = "./src/main/resources/static/yolo2_dl4j_inference.v1";
    private static final String TRAIN_DATA_VOC = "D:/downloads/Microsoft_COCO.v2-raw-voc/train";
    private static final String TEST_DATA_VOC = "D:/downloads/Microsoft_COCO.v2-raw-voc/valid";

    private static final double[][] DEFAULT_PRIOR_BOXES = {{0.57273, 0.677385}, {1.87446, 2.06253}, {3.33843, 5.47434}, {7.88282, 3.52778}, {9.77052, 9.16828}};
    private static final double[][] priorBoxes = DEFAULT_PRIOR_BOXES;

    public static ComputationGraph initPretrained() throws IOException, UnsupportedKerasConfigurationException, InvalidKerasConfigurationException {
        String filename = "./src/main/resources/static/yolo.h5";
        KerasLayer.registerCustomLayer("Lambda", KerasSpaceToDepth.class);
        ComputationGraph graph = KerasModelImport.importKerasModelAndWeights(filename, false);
        INDArray priors = Nd4j.create(priorBoxes).castTo(DataType.FLOAT);
        FineTuneConfiguration fineTuneConf = new FineTuneConfiguration.Builder()
                .seed(seed)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .gradientNormalization(GradientNormalization.RenormalizeL2PerLayer)
                .gradientNormalizationThreshold(1.0)
                .updater(new Adam.Builder().learningRate(1e-3).build())
                .l2(0.00001)
                .activation(Activation.IDENTITY)
                .trainingWorkspaceMode(WorkspaceMode.NONE)
                .build();

        System.out.println("Configuration : " + fineTuneConf.toJson());

        ComputationGraph model = new TransferLearning.GraphBuilder(graph)
                .fineTuneConfiguration(fineTuneConf)
                .addLayer("outputs", new Yolo2OutputLayer.Builder()
                        .boundingBoxPriors(priors)
                        .build(), "conv2d_23")
                .setOutputs("outputs")
                .setInputTypes(InputType.convolutional(Speed.MEDIUM.height, Speed.MEDIUM.width, 3, CNN2DFormat.NCHW))
                .build();
        System.out.println("YOLO " + model.summary(InputType.convolutional(Speed.MEDIUM.height, Speed.MEDIUM.width, 3)));
        ModelSerializer.writeModel(model, TRAIN_PATH, false);

        return ComputationGraph.load(new File(TRAIN_PATH), false);
    }

    public static void main(String[] args) {
        try {
            trainYolo();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void trainYolo() throws IOException, UnsupportedKerasConfigurationException, InvalidKerasConfigurationException {
        int nEpochs = 50;
        int width = 416;
        int height = 416;
        int channels = 3;
        int gridHeight = 13;
        int gridWidth = 13;
        ComputationGraph yolo = initPretrained();
        DataSetIterator trainDataSet = prepareData(TRAIN_DATA_VOC, height, width, channels, gridHeight, gridWidth);
        RecordReaderDataSetIterator testDataSet = prepareData(TEST_DATA_VOC, height, width, channels, gridHeight, gridWidth);
        testDataSet.setCollectMetaData(true);
        double bestScore = new YoloEvaluations(yolo, testDataSet, new VocLabelProvider(TEST_DATA_VOC), width, height, gridWidth, gridHeight).getF1();
        System.out.println("Current F1 score :" + bestScore);

        UIServer uiServer = UIServer.getInstance();
        StatsStorage statsStorage = new InMemoryStatsStorage();

        uiServer.attach(statsStorage);

        yolo.setListeners(new StatsListener(statsStorage));

        for (int i = 0; i < nEpochs; i++) {
            yolo.fit(trainDataSet);
            System.out.println("Evaluating model...");
            double tempScore = new YoloEvaluations(yolo, testDataSet, new VocLabelProvider(TEST_DATA_VOC), width, height, gridWidth, gridHeight).getF1();

            if (tempScore > bestScore) {
                bestScore = tempScore;
                System.out.println("New best F1 score: " + bestScore + " reached at Epoch: " + i);
                System.out.println("Saving model...");
                ModelSerializer.writeModel(yolo, TRAINED_PATH + System.currentTimeMillis() + ".zip", true);
            } else
                System.out.println("Current F1 score :" + bestScore);
        }
    }

    private static RecordReaderDataSetIterator prepareData(String path, int height, int width, int chanels, int gridHeight, int gridWidth) throws IOException {

        int batchSize = 10;

        File data = new File(path);

        VocLabelProvider labelProvider = new VocLabelProvider(path);

        ObjectDetectionRecordReader recordReaderTrain = new ObjectDetectionRecordReader(height, width, chanels, gridHeight, gridWidth, labelProvider);

        FileSplit trainData = new FileSplit(data, NativeImageLoader.ALLOWED_FORMATS, new Random(12345));

        recordReaderTrain.initialize(trainData);

        // ObjectDetectionRecordReader performs regression, so we need to specify it here
        RecordReaderDataSetIterator train = new RecordReaderDataSetIterator(recordReaderTrain, batchSize, 1, 1, true);
        train.setPreProcessor(new ImagePreProcessingScaler(0, 1));
        return train;

    }
}

