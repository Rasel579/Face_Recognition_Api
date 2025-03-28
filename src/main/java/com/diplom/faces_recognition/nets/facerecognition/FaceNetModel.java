package com.diplom.faces_recognition.nets.facerecognition;

import com.diplom.faces_recognition.Constants;
import com.diplom.faces_recognition.nets.facerecognition.contract.AbstractFaceNetModel;
import com.diplom.faces_recognition.utils.log.LoggerImpl;
import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.split.FileSplit;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.recordreader.ImageRecordReader;
import org.deeplearning4j.core.storage.StatsStorage;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.*;
import org.deeplearning4j.nn.conf.graph.L2NormalizeVertex;
import org.deeplearning4j.nn.conf.graph.MergeVertex;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.CenterLossOutputLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.conf.layers.ZeroPaddingLayer;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.model.stats.StatsListener;
import org.deeplearning4j.ui.model.storage.InMemoryStatsStorage;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import static com.diplom.faces_recognition.nets.facerecognition.FaceNetUtils.convolution;

public class FaceNetModel extends AbstractFaceNetModel {

    public ComputationGraphConfiguration conf() {

        ComputationGraphConfiguration.GraphBuilder graph = new NeuralNetConfiguration
                .Builder().seed(seed)
                .activation(Activation.IDENTITY)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(updater)
                .weightInit(WeightInit.RELU)
                .l2(5e-5)
                .miniBatch(true)
                .graphBuilder();

        graph.addInputs("input1")
                .addLayer("pad1",
                        FaceNetUtils.zeroPadding(3), "input1")
                .addLayer("conv1",
                        convolution(7, inputShape[2], 64, 2),
                        "pad1")
                .addLayer("bn1", FaceNetUtils.batchNorm(64),
                        "conv1")
                .addLayer(FaceNetUtils.nextReluId(), FaceNetUtils.relu(),
                        "bn1")
                .addLayer("pad2",
                        FaceNetUtils.zeroPadding(1), FaceNetUtils.lastReluId())
                // pool -> norm
                .addLayer("pool1",
                        new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX, new int[]{3, 3},
                                new int[]{2, 2})
                                .convolutionMode(ConvolutionMode.Truncate)
                                .build(),
                        "pad2")
                // Inception 2
                .addLayer("conv2",
                        convolution(1, 64, 64),
                        "pool1")
                .addLayer("bn2", FaceNetUtils.batchNorm(64),
                        "conv2")
                .addLayer(FaceNetUtils.nextReluId(),
                        FaceNetUtils.relu(),
                        "bn2")

                .addLayer("pad3",
                        FaceNetUtils.zeroPadding(1), FaceNetUtils.lastReluId())

                .addLayer("conv3",
                        convolution(3, 64, 192),
                        "pad3")
                .addLayer("bn3",
                        FaceNetUtils.batchNorm(192),
                        "conv3")
                .addLayer(FaceNetUtils.nextReluId(),
                        FaceNetUtils.relu(),
                        "bn3")

                .addLayer("pad4",
                        FaceNetUtils.zeroPadding(1), FaceNetUtils.lastReluId())
                .addLayer("pool2",
                        new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX, new int[]{3, 3},
                                new int[]{2, 2})
                                .convolutionMode(ConvolutionMode.Truncate)
                                .build(),
                        "pad4");


        buildBlock3a(graph);
        buildBlock3b(graph);
        buildBlock3c(graph);

        buildBlock4a(graph);
        buildBlock4e(graph);

        buildBlock5a(graph);
        buildBlock5b(graph);

        graph.addLayer("avgpool",
                        new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.AVG, new int[]{3, 3},
                                new int[]{1, 1})
                                .convolutionMode(ConvolutionMode.Truncate)
                                .build(),
                        "inception_5b")
                .addLayer("dense", new DenseLayer.Builder().nIn(736).nOut(encodings)
                        .activation(Activation.IDENTITY).build(), "avgpool")
                .addVertex("encodings", new L2NormalizeVertex(new int[]{}, 1e-12), "dense")
                // .setInputTypes(InputType.convolutional(96, 96, inputShape[2]));

                // Uncomment in case of training the network, graph.setOutputs should be lossLayer then
                .addLayer("lossLayer", new CenterLossOutputLayer.Builder()
                                .lossFunction(LossFunctions.LossFunction.SQUARED_LOSS)
                                .activation(Activation.SOFTMAX).nIn(128).nOut(numClasses).lambda(1e-4).alpha(0.9)
                                .gradientNormalization(GradientNormalization.RenormalizeL2PerLayer).build(),
                        "encodings")
                .setInputTypes(InputType.convolutional(96, 96, inputShape[2]));
        graph.setOutputs("lossLayer");
        // graph.setOutputs("encodings");

        return graph.backpropType(BackpropType.Standard).build();
    }

    private void buildBlock3a(ComputationGraphConfiguration.GraphBuilder graph) {
        graph.addLayer("inception_3a_3x3_conv1", convolution(1, 192, 96),
                        "pool2")
                .addLayer("inception_3a_3x3_bn1",
                        FaceNetUtils.batchNorm(96), "inception_3a_3x3_conv1")
                .addLayer(FaceNetUtils.nextReluId(),
                        FaceNetUtils.relu(), "inception_3a_3x3_bn1")
                .addLayer(FaceNetUtils.nextPaddingId(),
                        FaceNetUtils.zeroPadding(1), FaceNetUtils.lastReluId())
                .addLayer("inception_3a_3x3_conv2", convolution(3, 96, 128), FaceNetUtils.lastPaddingId())
                .addLayer("inception_3a_3x3_bn2",
                        FaceNetUtils.batchNorm(128),
                        "inception_3a_3x3_conv2")
                .addLayer(FaceNetUtils.nextReluId(),
                        FaceNetUtils.relu(), "inception_3a_3x3_bn2")

                .addLayer("inception_3a_5x5_conv1", convolution(1, 192, 16),
                        "pool2")
                .addLayer("inception_3a_5x5_bn1",
                        FaceNetUtils.batchNorm(16),
                        "inception_3a_5x5_conv1")
                .addLayer(FaceNetUtils.nextReluId(),
                        FaceNetUtils.relu(), "inception_3a_5x5_bn1")
                .addLayer(FaceNetUtils.nextPaddingId(),
                        FaceNetUtils.zeroPadding(2), FaceNetUtils.lastReluId())
                .addLayer("inception_3a_5x5_conv2", convolution(5, 16, 32), FaceNetUtils.lastPaddingId())
                .addLayer("inception_3a_5x5_bn2",
                        FaceNetUtils.batchNorm(32),
                        "inception_3a_5x5_conv2")
                .addLayer(FaceNetUtils.nextReluId(),
                        FaceNetUtils.relu(), "inception_3a_5x5_bn2")

                .addLayer("pool3",
                        new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX, new int[]{3, 3},
                                new int[]{2, 2})
                                .convolutionMode(ConvolutionMode.Truncate)
                                .build(),
                        "pool2")
                .addLayer("inception_3a_pool_conv", convolution(1, 192, 32), "pool3")
                .addLayer("inception_3a_pool_bn",
                        FaceNetUtils.batchNorm(32),
                        "inception_3a_pool_conv")
                .addLayer(FaceNetUtils.nextReluId(),
                        FaceNetUtils.relu(),
                        "inception_3a_pool_bn")

                .addLayer(FaceNetUtils.nextPaddingId(),
                        new ZeroPaddingLayer.Builder(new int[]{3, 4, 3, 4})
                                .build(), FaceNetUtils.lastReluId())

                .addLayer("inception_3a_1x1_conv", convolution(1, 192, 64),
                        "pool2")
                .addLayer("inception_3a_1x1_bn",
                        FaceNetUtils.batchNorm(64),
                        "inception_3a_1x1_conv")
                .addLayer(FaceNetUtils.nextReluId(),
                        FaceNetUtils.relu(),
                        "inception_3a_1x1_bn")
                .addVertex("inception_3a", new MergeVertex(), "relu5", "relu7", FaceNetUtils.lastPaddingId(), "relu9");

    }


    private void buildBlock3b(ComputationGraphConfiguration.GraphBuilder graph) {
        graph.addLayer("inception_3b_3x3_conv1",
                        convolution(1, 256, 96),
                        "inception_3a")

                .addLayer("inception_3b_3x3_bn1",
                        FaceNetUtils.batchNorm(96),
                        "inception_3b_3x3_conv1")

                .addLayer(FaceNetUtils.nextReluId(),
                        FaceNetUtils.relu(),
                        "inception_3b_3x3_bn1")

                .addLayer(FaceNetUtils.nextPaddingId(),
                        FaceNetUtils.zeroPadding(1), FaceNetUtils.lastReluId())

                .addLayer("inception_3b_3x3_conv2",
                        convolution(3, 96, 128),
                        FaceNetUtils.lastPaddingId())

                .addLayer("inception_3b_3x3_bn2",
                        FaceNetUtils.batchNorm(128),
                        "inception_3b_3x3_conv2")

                .addLayer(FaceNetUtils.nextReluId(),
                        FaceNetUtils.relu(),
                        "inception_3b_3x3_bn2");


        graph.addLayer("inception_3b_5x5_conv1",
                        convolution(1, 256, 32),
                        "inception_3a")

                .addLayer("inception_3b_5x5_bn1",
                        FaceNetUtils.batchNorm(32),
                        "inception_3b_5x5_conv1")

                .addLayer(FaceNetUtils.nextReluId(),
                        FaceNetUtils.relu(),
                        "inception_3b_5x5_bn1")
                .addLayer(FaceNetUtils.nextPaddingId(),
                        FaceNetUtils.zeroPadding(2), FaceNetUtils.lastReluId())

                .addLayer("inception_3b_5x5_conv2",
                        convolution(5, 32, 64),
                        FaceNetUtils.lastPaddingId())

                .addLayer("inception_3b_5x5_bn2",
                        FaceNetUtils.batchNorm(64),
                        "inception_3b_5x5_conv2")
                .addLayer(FaceNetUtils.nextReluId(),
                        FaceNetUtils.relu(),
                        "inception_3b_5x5_bn2");

        graph.addLayer("avg1",
                        new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.AVG, new int[]{3, 3},
                                new int[]{3, 3})
                                .convolutionMode(ConvolutionMode.Truncate)
                                .build(),
                        "inception_3a")
                .addLayer("inception_3b_pool_conv",
                        convolution(1, 256, 64),
                        "avg1")

                .addLayer("inception_3b_pool_bn",
                        FaceNetUtils.batchNorm(64),
                        "inception_3b_pool_conv")

                .addLayer(FaceNetUtils.nextReluId(),
                        FaceNetUtils.relu(),
                        "inception_3b_pool_bn")
                .addLayer(FaceNetUtils.nextPaddingId(),
                        FaceNetUtils.zeroPadding(4), FaceNetUtils.lastReluId())

                .addLayer("inception_3b_1x1_conv",
                        convolution(1, 256, 64),
                        "inception_3a")
                .addLayer("inception_3b_1x1_bn",
                        FaceNetUtils.batchNorm(64),
                        "inception_3b_1x1_conv")

                .addLayer(FaceNetUtils.nextReluId(),
                        FaceNetUtils.relu(),
                        "inception_3b_1x1_bn")
                .addVertex("inception_3b", new MergeVertex(), "relu11", "relu13", FaceNetUtils.lastPaddingId(), "relu15");

    }

    private void buildBlock3c(ComputationGraphConfiguration.GraphBuilder graph) {
        FaceNetUtils.convolution2dAndBN(graph, "inception_3c_3x3",
                128, 320, new int[]{1, 1}, new int[]{1, 1},
                256, 128, new int[]{3, 3}, new int[]{2, 2},
                new int[]{1, 1, 1, 1}, "inception_3b");
        String rel1 = FaceNetUtils.lastReluId();

        FaceNetUtils.convolution2dAndBN(graph, "inception_3c_5x5",
                32, 320, new int[]{1, 1}, new int[]{1, 1},
                64, 32, new int[]{5, 5}, new int[]{2, 2},
                new int[]{2, 2, 2, 2}, "inception_3b");
        String rel2 = FaceNetUtils.lastReluId();

        graph.addLayer("pool7",
                new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX, new int[]{3, 3},
                        new int[]{2, 2})
                        .convolutionMode(ConvolutionMode.Truncate)
                        .build(),
                "inception_3b");

        graph.addLayer(FaceNetUtils.nextPaddingId(),
                new ZeroPaddingLayer.Builder(new int[]{0, 1, 0, 1})
                        .build(), "pool7");
        String pad1 = FaceNetUtils.lastPaddingId();

        graph.addVertex("inception_3c", new MergeVertex(), rel1, rel2, pad1);
    }

    private void buildBlock4a(ComputationGraphConfiguration.GraphBuilder graph) {
        FaceNetUtils.convolution2dAndBN(graph, "inception_4a_3x3",
                96, 640, new int[]{1, 1}, new int[]{1, 1},
                192, 96, new int[]{3, 3}, new int[]{1, 1}
                , new int[]{1, 1, 1, 1}, "inception_3c");
        String rel1 = FaceNetUtils.lastReluId();

        FaceNetUtils.convolution2dAndBN(graph, "inception_4a_5x5",
                32, 640, new int[]{1, 1}, new int[]{1, 1},
                64, 32, new int[]{5, 5}, new int[]{1, 1}
                , new int[]{2, 2, 2, 2}, "inception_3c");
        String rel2 = FaceNetUtils.lastReluId();

        graph.addLayer("avg7",
                new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.AVG, new int[]{3, 3},
                        new int[]{3, 3})
                        .convolutionMode(ConvolutionMode.Truncate)
                        .build(),
                "inception_3c");
        FaceNetUtils.convolution2dAndBN(graph, "inception_4a_pool",
                128, 640, new int[]{1, 1}, new int[]{1, 1},
                null, null, null, null
                , new int[]{2, 2, 2, 2}, "avg7");
        String pad1 = FaceNetUtils.lastPaddingId();

        FaceNetUtils.convolution2dAndBN(graph, "inception_4a_1x1",
                256, 640, new int[]{1, 1}, new int[]{1, 1},
                null, null, null, null
                , null, "inception_3c");
        String rel4 = FaceNetUtils.lastReluId();
        graph.addVertex("inception_4a", new MergeVertex(), rel1, rel2, rel4, pad1);

    }

    private void buildBlock4e(ComputationGraphConfiguration.GraphBuilder graph) {
        FaceNetUtils.convolution2dAndBN(graph, "inception_4e_3x3",
                160, 640, new int[]{1, 1}, new int[]{1, 1},
                256, 160, new int[]{3, 3}, new int[]{2, 2},
                new int[]{1, 1, 1, 1}, "inception_4a");
        String rel1 = FaceNetUtils.lastReluId();

        FaceNetUtils.convolution2dAndBN(graph, "inception_4e_5x5",
                64, 640, new int[]{1, 1}, new int[]{1, 1},
                128, 64, new int[]{5, 5}, new int[]{2, 2},
                new int[]{2, 2, 2, 2}, "inception_4a");
        String rel2 = FaceNetUtils.lastReluId();

        graph.addLayer("pool8",
                new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX, new int[]{3, 3},
                        new int[]{2, 2})
                        .convolutionMode(ConvolutionMode.Truncate)
                        .build(),
                "inception_4a");
        graph.addLayer(FaceNetUtils.nextPaddingId(),
                new ZeroPaddingLayer.Builder(new int[]{0, 1, 0, 1})
                        .build(), "pool8");
        String pad1 = FaceNetUtils.lastPaddingId();

        graph.addVertex("inception_4e", new MergeVertex(), rel1, rel2, pad1);
    }

    private void buildBlock5a(ComputationGraphConfiguration.GraphBuilder graph) {
        FaceNetUtils.convolution2dAndBN(graph, "inception_5a_3x3",
                96, 1024, new int[]{1, 1}, new int[]{1, 1},
                384, 96, new int[]{3, 3}, new int[]{1, 1},
                new int[]{1, 1, 1, 1}, "inception_4e");
        String relu1 = FaceNetUtils.lastReluId();

        graph.addLayer("avg9",
                new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.AVG, new int[]{3, 3},
                        new int[]{3, 3})
                        .convolutionMode(ConvolutionMode.Truncate)
                        .build(),
                "inception_4e");
        FaceNetUtils.convolution2dAndBN(graph, "inception_5a_pool",
                96, 1024, new int[]{1, 1}, new int[]{1, 1},
                null, null, null, null,
                new int[]{1, 1, 1, 1}, "avg9");
        String pad1 = FaceNetUtils.lastPaddingId();

        FaceNetUtils.convolution2dAndBN(graph, "inception_5a_1x1",
                256, 1024, new int[]{1, 1}, new int[]{1, 1},
                null, null, null, null,
                null, "inception_4e");
        String rel3 = FaceNetUtils.lastReluId();

        graph.addVertex("inception_5a", new MergeVertex(), relu1, pad1, rel3);
    }

    private void buildBlock5b(ComputationGraphConfiguration.GraphBuilder graph) {
        FaceNetUtils.convolution2dAndBN(graph, "inception_5b_3x3",
                96, 736, new int[]{1, 1}, new int[]{1, 1},
                384, 96, new int[]{3, 3}, new int[]{1, 1},
                new int[]{1, 1, 1, 1}, "inception_5a");
        String rel1 = FaceNetUtils.lastReluId();

        graph.addLayer("max2",
                new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX, new int[]{3, 3},
                        new int[]{2, 2})
                        .convolutionMode(ConvolutionMode.Truncate)
                        .build(),
                "inception_5a");
        FaceNetUtils.convolution2dAndBN(graph, "inception_5b_pool",
                96, 736, new int[]{1, 1}, new int[]{1, 1},
                null, null, null, null,
                null, "max2");
        graph.addLayer(FaceNetUtils.nextPaddingId(),
                FaceNetUtils.zeroPadding(1), FaceNetUtils.lastReluId());
        String pad1 = FaceNetUtils.lastPaddingId();

        FaceNetUtils.convolution2dAndBN(graph, "inception_5b_1x1",
                256, 736, new int[]{1, 1}, new int[]{1, 1},
                null, null, null, null,
                null, "inception_5a");
        String rel2 = FaceNetUtils.lastReluId();

        graph.addVertex("inception_5b", new MergeVertex(), rel1, pad1, rel2);
    }

    public ComputationGraph init() throws IOException {
        resetIndexes();
        ComputationGraph computationGraph = new ComputationGraph(conf());
        computationGraph.init();
        FaceNetUtils.loadWeights(computationGraph);
        return computationGraph;
    }

    public ComputationGraph initSavedModel() throws IOException {
        resetIndexes();
        ComputationGraph computationGraph = ModelSerializer.restoreComputationGraph(SAVE_PATH);
        computationGraph.init();
        return computationGraph;
    }

    public static void main(String[] args) {
        try {
            FaceNetModel model = new FaceNetModel();
            model.init();
            model.trainModel();
            model.testData();


        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void trainModel() throws IOException {

        File trainData = new File(System.getenv(Constants.RESOURCES_ENV) + "train_data");

        FileSplit train = new FileSplit(trainData, NativeImageLoader.ALLOWED_FORMATS, new Random(12345));

        ParentPathLabelGenerator labelMarker = new ParentPathLabelGenerator();

        ImageRecordReader recordReader = new ImageRecordReader(inputShape[0], inputShape[1], inputShape[2], labelMarker);

        recordReader.initialize(train);

        DataSetIterator dataIterator = new RecordReaderDataSetIterator(recordReader, BATCH_SIZE, 1, numClasses);

        DataNormalization scaler = new ImagePreProcessingScaler(0, 1);
        scaler.fit(dataIterator);

        dataIterator.setPreProcessor(scaler);

        ComputationGraph model = this.init();

        UIServer uiServer = UIServer.getInstance();

        StatsStorage statsStorage = new InMemoryStatsStorage();

        uiServer.attach(statsStorage);

        model.setListeners(new StatsListener(statsStorage));

        for (int i = 0; i < EPOCH_NUM; i++) {
            model.fit(dataIterator);
        }

        if (logger == null ){
            logger = new LoggerImpl();
        }
        logger.info("******SAVE TRAINED MODEL*****");

        File saveLocation = new File(SAVE_PATH);

        boolean isSaveUpdater = false;

        ModelSerializer.writeModel(model, saveLocation, isSaveUpdater);

    }

    private void testData() throws IOException {

        ComputationGraph model = this.initSavedModel();

        File testData = new File(System.getenv(Constants.RESOURCES_ENV) + "test_data");
        FileSplit test = new FileSplit(testData, NativeImageLoader.ALLOWED_FORMATS, new Random(12345));

        ParentPathLabelGenerator labelMarker = new ParentPathLabelGenerator();

        ImageRecordReader recordReader = new ImageRecordReader(inputShape[0], inputShape[1], inputShape[2], labelMarker);

        recordReader.initialize(test);

        DataSetIterator testIterator = new RecordReaderDataSetIterator(recordReader, BATCH_SIZE);
        DataNormalization scaler = new ImagePreProcessingScaler(0, 1);

        scaler.fit(testIterator);
        testIterator.setPreProcessor(scaler);


        Evaluation eval = model.evaluate(testIterator);
        if (logger == null ){
            logger = new LoggerImpl();
        }
        logger.info(eval.stats(false,true));

    }

    private static void resetIndexes() {
        reluIndex = 1;
        paddingIndex = 1;
    }
}
