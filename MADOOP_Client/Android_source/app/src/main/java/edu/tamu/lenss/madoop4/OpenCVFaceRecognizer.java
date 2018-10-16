package edu.tamu.lenss.madoop4;
//import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.IntBuffer;

import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_core.CV_8UC1;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.opencv_face.FaceRecognizer;
import org.bytedeco.javacpp.opencv_face.FisherFaceRecognizer;
import org.bytedeco.javacpp.opencv_face.EigenFaceRecognizer;
import org.bytedeco.javacpp.opencv_face.LBPHFaceRecognizer;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;

import edu.tamu.lenss.util.ILogger;
import edu.tamu.lenss.util.LogFactory;

/**
 * Created by root on 3/12/2018.
 */

public class OpenCVFaceRecognizer implements MadoopConstants {
    private static final ILogger LOG = LogFactory.getLog(OpenCVFaceRecognizer.class);

    private static int getLabel(String name) {

//        LOG.info(name + " " + Integer.parseInt(name.substring(1, 3)));
      return Integer.parseInt(name.substring(1, 3));
    }

//    public static FaceRecognizer train(String args) {
//
//        String trainingDir = args;
//
//        File root = new File(trainingDir);
//
//        FilenameFilter imgFilter = new FilenameFilter() {
//            public boolean accept(File dir, String name) {
//                name = name.toLowerCase();
//                return name.endsWith("eig.jpg") || name.endsWith("eig.pgm") || name.endsWith("eig.png");
//            }
//        };
//
//        File[] imageFiles = root.listFiles(imgFilter);
//
//        MatVector images = new MatVector(imageFiles.length);
//
//        Mat labels = new Mat(imageFiles.length, 1, CV_32SC1);
//        IntBuffer labelsBuf = labels.createBuffer();
//
//        int counter = 0;
//
//        for (File image : imageFiles) {
//            Mat img = imread(image.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
//            if (img.empty()) {
//                LOG.error( "--(!)Error loading image: " + image.getAbsolutePath());
//                return null;
//            }
//
////            int label = Integer.parseInt(image.getName().split("\\-")[0]);
//
//            images.put(counter, img);
//
////            labelsBuf.put(counter, label);
//            labelsBuf.put(counter, getLabel(image.getName()));
//
//            counter++;
//        }
//
//        FaceRecognizer faceRecognizer = FisherFaceRecognizer.create();
////         FaceRecognizer faceRecognizer = EigenFaceRecognizer.create();
////        FaceRecognizer faceRecognizer = LBPHFaceRecognizer.create();
//
//        faceRecognizer.train(images, labels);
////        faceRecognizer.save(rootDir + File.separator + "facemodel.xml");
//
//        return faceRecognizer;
//
//    }
//
//    public static void predict(FaceRecognizer faceRecognizer, String args) {
//        Mat testImage = imread(args, CV_LOAD_IMAGE_GRAYSCALE);
//        if (testImage.empty()) {
//            LOG.error( "--(!)Error loading " + args);
//            return;
//        }
//        IntPointer label = new IntPointer(1);
//        DoublePointer confidence = new DoublePointer(1);
//
////        FaceRecognizer faceRecognizer = FisherFaceRecognizer.create();
////         FaceRecognizer faceRecognizer = EigenFaceRecognizer.create();
////        FaceRecognizer faceRecognizer = LBPHFaceRecognizer.create();
//
////        faceRecognizer.read(rootDir + File.separator + "facemodel.xml");
////        faceRecognizer.load(rootDir + File.separator + "facemodel.xml");
//        faceRecognizer.predict(testImage, label, confidence);
//
//        LOG.info( "Confidence: " + String.valueOf(confidence.get()));
//        int predictedLabel = label.get(0);
//
//        System.out.println("Predicted label: " + predictedLabel + ", name: " + FACES.get(predictedLabel));
//    }


    public static void trainToModel(String input, String output) {

        String trainingDir = input;

        File root = new File(trainingDir);

        FilenameFilter imgFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                name = name.toLowerCase();
                return name.endsWith("eig.jpg") || name.endsWith("eig.pgm") || name.endsWith("eig.png");
            }
        };

        File[] imageFiles = root.listFiles(imgFilter);

        MatVector images = new MatVector(imageFiles.length);

        Mat labels = new Mat(imageFiles.length, 1, CV_32SC1);
        IntBuffer labelsBuf = labels.createBuffer();

        int counter = 0;

        for (File image : imageFiles) {
            Mat img = imread(image.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
            if (img.empty()) {
                LOG.error( "--(!)Error loading image: " + image.getAbsolutePath());
                return;
            }

//            int label = Integer.parseInt(image.getName().split("\\-")[0]);

            LOG.debug(image.getName());

            images.put(counter, img);

//            labelsBuf.put(counter, label);
            LOG.info(String.format("getLabel: %d", getLabel(image.getName())));
            labelsBuf.put(counter, getLabel(image.getName()));

            counter++;
        }

        FaceRecognizer faceRecognizer = FisherFaceRecognizer.create();
//         FaceRecognizer faceRecognizer = EigenFaceRecognizer.create();
//        FaceRecognizer faceRecognizer = LBPHFaceRecognizer.create();

        faceRecognizer.train(images, labels);
        faceRecognizer.save(output + File.separator + "facemodel.xml");

    }

    public static void predictFromModel(String model, String args) {
        Mat testImage = imread(args, CV_LOAD_IMAGE_GRAYSCALE);
        if (testImage.empty()) {
            LOG.error( "--(!)Error loading " + args);
            return;
        }
        IntPointer label = new IntPointer(1);
        DoublePointer confidence = new DoublePointer(1);

        FaceRecognizer faceRecognizer = FisherFaceRecognizer.create();
//         FaceRecognizer faceRecognizer = EigenFaceRecognizer.create();
//        FaceRecognizer faceRecognizer = LBPHFaceRecognizer.create();

        faceRecognizer.read(model);
//        faceRecognizer.load(rootDir + File.separator + "facemodel.xml");
        faceRecognizer.predict(testImage, label, confidence);

        LOG.debug( "Confidence: " + String.valueOf(confidence.get()));
        int predictedLabel = label.get(0);

        LOG.info("Predicted label: " + predictedLabel + ", name: s" + predictedLabel);
    }

    //This version returns the label recognized
    public static String predictFromModel2(String model, String args) {
        Mat testImage = imread(args, CV_LOAD_IMAGE_GRAYSCALE);
        if (testImage.empty()) {
            LOG.error( "--(!)Error loading " + args);
            return null;
        }
        IntPointer label = new IntPointer(1);
        DoublePointer confidence = new DoublePointer(1);

        FaceRecognizer faceRecognizer = FisherFaceRecognizer.create();
//         FaceRecognizer faceRecognizer = EigenFaceRecognizer.create();
//        FaceRecognizer faceRecognizer = LBPHFaceRecognizer.create();

        faceRecognizer.read(model);
//        faceRecognizer.load(model);
        faceRecognizer.predict(testImage, label, confidence);

        LOG.debug( "Confidence: " + String.valueOf(confidence.get()));
        int predictedLabel = label.get(0);

        LOG.info("Predicted label: " + predictedLabel + ", name: s" + predictedLabel);
        return "s" + predictedLabel;
    }

    
}
