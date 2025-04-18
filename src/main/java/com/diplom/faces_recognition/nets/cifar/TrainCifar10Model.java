package com.diplom.faces_recognition.nets.cifar;

import com.diplom.faces_recognition.Constants;
import com.diplom.faces_recognition.nets.cifar.contract.AbstractCifarNetModel;
import com.diplom.faces_recognition.utils.ImageUtils;
import com.diplom.faces_recognition.utils.log.ILog;
import com.diplom.faces_recognition.utils.log.LoggerImpl;
import com.diplom.faces_recognition.utils.yolo.Speed;
import org.bytedeco.opencv.opencv_core.Mat;
import org.deeplearning4j.core.storage.StatsStorage;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.WorkspaceMode;
import org.deeplearning4j.nn.conf.graph.L2NormalizeVertex;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.CenterLossOutputLayer;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.layers.objdetect.DetectedObject;
import org.deeplearning4j.nn.transferlearning.FineTuneConfiguration;
import org.deeplearning4j.nn.transferlearning.TransferLearning;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.model.stats.StatsListener;
import org.deeplearning4j.ui.model.storage.InMemoryStatsStorage;
import org.deeplearning4j.util.ModelSerializer;
import org.deeplearning4j.zoo.ZooModel;
import org.deeplearning4j.zoo.model.VGG16;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.IUpdater;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

public class TrainCifar10Model extends AbstractCifarNetModel implements Serializable {

    @Autowired
    private ILog logger;

    public static void main(String[] args) throws IOException {
        TrainCifar10Model trainCifar10Model = new TrainCifar10Model();
        trainCifar10Model.train();
    }

    @Override
    protected void train() throws IOException {
        ZooModel zooModel = VGG16.builder().build();
        ComputationGraph vgg16 = (ComputationGraph) zooModel.initPretrained();
        if (logger == null) {
            logger = new LoggerImpl();
        }
        logger.info(vgg16.summary());

        IUpdater iUpdaterWithDefaultConfig = Updater.ADAM.getIUpdaterWithDefaultConfig();
        iUpdaterWithDefaultConfig.setLrAndSchedule(0.1, null);
        FineTuneConfiguration fineTuneConfiguration = new FineTuneConfiguration.Builder()
                .seed(1234)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .activation(Activation.RELU)
                .updater(iUpdaterWithDefaultConfig)
                .cudnnAlgoMode(ConvolutionLayer.AlgoMode.NO_WORKSPACE)
                .miniBatch(true)
                .inferenceWorkspaceMode(WorkspaceMode.ENABLED)
                .trainingWorkspaceMode(WorkspaceMode.ENABLED)
                .pretrain(true)
                .backprop(true)
                .build();

        ComputationGraph cifar10Model = new TransferLearning.GraphBuilder(vgg16)
                .setWorkspaceMode(WorkspaceMode.ENABLED)
                .fineTuneConfiguration(fineTuneConfiguration)
                .setInputTypes(InputType.convolutionalFlat(ImageUtils.HEIGHT, ImageUtils.WIDTH, 3))
                .removeVertexAndConnections("dense_2_loss")
                .removeVertexAndConnections("dense_2")
                .removeVertexAndConnections("dense_1")
                .removeVertexAndConnections("dropout_1")
                .removeVertexAndConnections("embeddings")
                .removeVertexAndConnections("flatten_1")
                .removeVertexAndConnections("fc1")
                .removeVertexAndConnections("fc2")
                .removeVertexAndConnections("predictions")
                .removeVertexAndConnections("flatten")
                .addLayer("dense_1", new DenseLayer.Builder()
                        .nIn(1000)
                        .nOut(EMBEDDINGS)
                        .activation(Activation.RELU)
                        .build(), "block5_pool")
                .addVertex("embeddings", new L2NormalizeVertex(new int[]{}, 1e-12), "dense_1")
                .addLayer("lossLayers", new CenterLossOutputLayer.Builder()
                        .lossFunction(LossFunctions.LossFunction.SQUARED_LOSS)
                        .nOut(EMBEDDINGS)
                        .activation(Activation.SOFTMAX)
                        .lambda(LAMBDA)
                        .alpha(0.9)
                        .gradientNormalization(GradientNormalization.RenormalizeL2PerLayer)
                        .build(), "embeddings")
                .setOutputs("lossLayers")
                .build();
        logger.info(cifar10Model.summary());

        File rootDir = new File(System.getenv(Constants.RESOURCES_ENV) + "train_from_video_" + NUM_POSSIBLE_LABELS);
        if (!rootDir.exists() && !rootDir.mkdir()) {
            logger.error(rootDir.getName() + " Not created");
        }
        DataSetIterator dataSetIterator = ImageUtils.createDataSetIterator(new File("D:/datasets/images_category/images_category/train"), NUM_POSSIBLE_LABELS, BATCH_SIZE);
        DataSetIterator testSetIterator = ImageUtils.createDataSetIterator(new File("D:/datasets/images_category/images_category/valid"), NUM_POSSIBLE_LABELS, BATCH_SIZE);

        UIServer uiServer = UIServer.getInstance();

        StatsStorage statsStorage = new InMemoryStatsStorage();

        uiServer.attach(statsStorage);

        cifar10Model.setListeners(new StatsListener(statsStorage));

        int iEpoch = I_EPOCH;
        while (iEpoch < EPOCH_INTERVAL) {
            while (dataSetIterator.hasNext()) {
                DataSet trainMiniBatchData = null;
                try {
                    trainMiniBatchData = dataSetIterator.next();

                } catch (Exception e) {
                }
                if (trainMiniBatchData != null) {
                    cifar10Model.fit(trainMiniBatchData);
                }
            }
            iEpoch++;

            String modelName = PREFIX + NUM_POSSIBLE_LABELS + "_epoch_data_e" + EMBEDDINGS + "_b" + BATCH_SIZE + "_" + iEpoch + ".zip";
            saveProgress(cifar10Model, iEpoch, modelName);
            testResult(cifar10Model, testSetIterator, iEpoch, modelName);
            dataSetIterator.reset();
            logger.info("iEpoch" + iEpoch);
        }

    }

    @Override
    protected void testResult(ComputationGraph cifar10Model, DataSetIterator testSetIterator, int iEpoch, String modelName) {
        Evaluation evaluation = cifar10Model.evaluate(testSetIterator);
        logger.info(evaluation.stats());
        testSetIterator.reset();
    }

    @Override
    protected void saveProgress(ComputationGraph cifar10Model, int iEpoch, String modelName) throws IOException {
        if (iEpoch % SAVE_INTERVAL == 0) {
            ModelSerializer.writeModel(cifar10Model,
                    new File(MODEL_SAVE_PATH + modelName), true);
        }
    }

    @Override
    public void loadTrainedModel(String pretrainedCifarModel) throws IOException {
        File file = new File(MODEL_SAVE_PATH + pretrainedCifarModel);
        logger.info("loading model " + file);

        cifar10Transfer = ModelSerializer.restoreComputationGraph(file);
        logger.info(cifar10Transfer.summary());

    }

    @Override
    public INDArray getEmbeddings(Mat file, DetectedObject object, Speed selectedSpeed) throws IOException {
        BufferedImage croppedImage = ImageUtils.cropImageWithYOLO(selectedSpeed, file, object, false);
        INDArray croppedArray = LOADER.asMatrix(croppedImage);
        IMAGE_PRE_PROCESSOR.transform(croppedArray);
        Map<String, INDArray> stringINDArrayMap = getCifar10Transfer().feedForward(croppedArray, false);
        INDArray indArray = stringINDArrayMap.get(CONTENT_LAYER_NAME);
        return indArray;
    }


}
