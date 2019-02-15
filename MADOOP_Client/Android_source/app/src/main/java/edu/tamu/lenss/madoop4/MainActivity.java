package edu.tamu.lenss.madoop4;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.protobuf.InvalidProtocolBufferException;
import com.my.jni.dlib.DLibLandmarks68Detector;
import com.my.jni.dlib.IDLibFaceDetector;
import com.my.jni.dlib.data.DLibFace;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FsShell;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.server.datanode.DataNode;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.TaskTracker;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.net.DNS;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.StringUtils;
import org.bytedeco.javacpp.opencv_core;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.URL;
import java.nio.IntBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.pl.sphelper.SPHelper;

import edu.tamu.lenss.util.AndroidLogger;
import edu.tamu.lenss.util.DFSFiles;
import edu.tamu.lenss.util.MyFiles;
import edu.tamu.lenss.util.ILogger;
import edu.tamu.lenss.util.LogFactory;
import edu.tamu.lenss.util.IPToolBox;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import java.net.URI;
import java.util.List;

import org.bytedeco.javacpp.opencv_face.FaceRecognizer;

import edu.tamu.lenss.R;

import static org.bytedeco.javacpp.opencv_core.CV_32SC1;

public class MainActivity extends AppCompatActivity implements MadoopConstants {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    //  private Button btn_quit;
    private Button btn_upload;
    private Button btn_delout;
    private Button btn_delin;
    private Button btn_run;
    private Button btn_tasktracker;
    private Button btn_datanode;
    private Button btn_clearlog;
    private TextView tv;
    private File input_dir;
    private File faces_dir;
    private File output_dir;
//    private MenuItem menu_tasktracker;
//    private MenuItem menu_datanode;
    private boolean tasktracker_on = false;
    private boolean tasktracker_changestatus = false;
    private boolean datanode_on = false;
    private boolean datanode_changestatus = false;
    private boolean reset_log = false;

//  private static final String TAG = MainActivity.class.getSimpleName();

    //    private static final MyLogger LOG = MyLogger.getLogger(TAG);
    private static final ILogger LOG = LogFactory.getLog(MainActivity.class);


    private static final boolean TEST_WORDCOUNT = false;
    private static final boolean TEST_FACERECO = !TEST_WORDCOUNT;

    private static String device_name = "";




//    private static final int REQUEST_CODE_WRITE_SETTINGS = 1;
//
//    private void requestWriteSettings() {
//        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
//        intent.setData(Uri.parse("package:" + getPackageName()));
//        startActivityForResult(intent, REQUEST_CODE_WRITE_SETTINGS);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_CODE_WRITE_SETTINGS) {
////            if (Settings.Global.canWrite(this)) {
//            LOG.info("onActivityResult write settings granted");
////            }
//        }
//    }


    private File mCascadeFile;
    private CascadeClassifier mJavaDetector;


    /**
     * 提取特征
     *
     * @param fileName 文件名
     * @return 特征图片
     */
    public Bitmap getImage(String fileName) {
        try {
            return BitmapFactory.decodeFile(faceRootDir + File.separator + fileName);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 特征保存
     *
     * @param image    Mat
     * @param rect     人脸信息
     * @param fileName 文件名字
     * @return 保存是否成功
     */
    public boolean saveImage(Mat image, Rect rect, String fileName) {
        try {
            String PATH = fileName;
            // 把检测到的人脸重新定义大小后保存成文件
            Mat sub = image.submat(rect);
            Mat mat = new Mat();
            Size size = new Size(100, 100);
            Imgproc.resize(sub, mat, size);
            Imgcodecs.imwrite(PATH, mat);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public String getBaseName(String fileName) {
        int index = fileName.lastIndexOf('.');
//    LOG.info("" + index);
        int index2 = fileName.lastIndexOf('/');
//    LOG.info("" + index2);
        if (index == -1) {
            return fileName.substring(index2 + 1);
        } else {
            return fileName.substring(index2 + 1, index);
        }
    }

    public String getBasePath(String fileName) {
        int index = fileName.lastIndexOf('/');
        if (index == -1) {
            return fileName;
        } else {
            return fileName.substring(0, index);
        }
    }

    private Context getContext() {
        return this;
    }

    private static final String FACE68_FILE = "shape_predictor_68_face_landmarks.dat";
    private final SparseArray<DLibFace> mDetFaces = new SparseArray<>();
    private final File docu = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
    private final File down = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    IDLibFaceDetector mLandmarksDetector;
    private Detector<Face> faceDetector;

    private int DLIB = 0;
    private int GOOGLE = 1;
    private int DETCTOR = DLIB;

    private Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    private boolean GenerateEigen(String subject, String fullname, String output) {
        LOG.info(fullname);
        Bitmap bMap = BitmapFactory.decodeFile(fullname);

        if (DETCTOR == GOOGLE) {

            Frame outputFrame = new Frame.Builder().setBitmap(bMap).build();

            final SparseArray<Face> faces;
            final List<android.graphics.Rect> faceBounds;
            try {
                faces = faceDetector.detect(outputFrame);
                LOG.debug(String.format("Detect %d faces",
                        faces.size()));
                if (faces.size() == 0) return false;

                // Translate the face bounds into something that DLib detector knows.
                faceBounds = new ArrayList<>();
                for (int i = 0; i < faces.size(); ++i) {
                    final Face face = faces.get(faces.keyAt(i));

                    final float x = face.getPosition().x;
                    final float y = face.getPosition().y;
                    final float w = face.getWidth();
                    final float h = face.getHeight();
                    final android.graphics.Rect bound = new android.graphics.Rect((int) (x),
                            (int) (y),
                            (int) (x + w),
                            (int) (y + h));

                    // The face bound that DLib landmarks algorithm needs is slightly
                    // different from the one given by Google Vision API, so I change
                    // it a bit from the experience of try-and-error.
                    bound.inset(bound.width() / 10,
                            bound.height() / 6);
                    bound.offset(0, bound.height() / 4);

                    LOG.debug(String.format("#%d face x=%f, y=%f, w=%f, h=%f",
                            faces.keyAt(i),
                            face.getPosition().x,
                            face.getPosition().y,
                            face.getWidth(),
                            face.getHeight()));
                    faceBounds.add(bound);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }


            // Detect faces and landmarks.
            try {
                int ret = mLandmarksDetector.storeFaces(bMap, faceBounds, fullname, output + File.separator);
                //ret is not used for now
                return true;
            } catch (InvalidProtocolBufferException err) {
                err.printStackTrace();
                return false;
            }
        } else {
            bMap = getResizedBitmap(bMap, 120, 120);

            // Detect faces and landmarks.
            try {
                List<DLibFace> detFaces = mLandmarksDetector.findFacesAndLandmarks(bMap);
                mDetFaces.clear();
                for (int i = 0; i < detFaces.size(); ++i) {
                    mDetFaces.put(i, detFaces.get(i));
                }
                LOG.debug(String.format("Detect %d face with landmarks", detFaces.size()));

                LOG.debug(String.format("Process of detecting faces and landmarks done"));

            } catch (InvalidProtocolBufferException err) {
                err.printStackTrace();
                return false;
            }

            // Detect faces and landmarks.
            try {
                int ret = mLandmarksDetector.getFaces(bMap, fullname, output + File.separator);
                //ret is not used for now
                return true;
            } catch (InvalidProtocolBufferException err) {
                err.printStackTrace();
                return false;
            }

        }


//    LOG.info("Picture:" + fullname);
//    Mat image = Imgcodecs.imread(fullname);
//    if (image.empty()) {
//      LOG.error("--(!)Error loading image");
//      return false;
//    }
//
//    // Detect faces in the image.
//    // MatOfRect is a special container class for Rect.
//    MatOfRect faceDetections = new MatOfRect();
//    if (mJavaDetector != null)
//      mJavaDetector.detectMultiScale(image, faceDetections);
//
//    LOG.info(String.format("Detected %s faces", faceDetections.toArray().length));
//    if (faceDetections.toArray().length != 1) return true;
//
//    // Draw a bounding box around each face.
//    for (Rect rect : faceDetections.toArray()) {
////                        Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
////                                new Scalar(0, 255, 0), 2);
//      saveImage(image, rect, output + File.separator + subject + "_" + getBaseName(fullname) + "_eig.jpg");
//    }
//
//    return true;
    }


    private void preProcessing(String input, String output) {

        while (!faceDetector_initialized){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        File root = new File(input);

        FilenameFilter imgFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                name = name.toLowerCase();
                return name.endsWith(".jpg") || name.endsWith(".pgm") || name.endsWith(".png");
            }
        };


        File[] imageFiles = root.listFiles(imgFilter);


        for (File image : imageFiles) {
            if (image.getName().contains("eig")) continue;
            GenerateEigen(device_name, input + File.separator + image.getName(), output);

        }

    }


    private void preProcessingForUpload() {

        preProcessing(StreamInput, faces_dir.getAbsolutePath());

    }


//  private void preProcessing() {
//
//    File root = new File(faceOriginalDir);
//
//    FilenameFilter imgFilter = new FilenameFilter() {
//      public boolean accept(File dir, String name) {
//        name = name.toLowerCase();
//        return name.endsWith(".jpg") || name.endsWith(".pgm") || name.endsWith(".png");
//      }
//    };
//
//    File[] imageDirs = root.listFiles();
//    for (File imageDir : imageDirs) {
//
//      File[] imageFiles = imageDir.listFiles(imgFilter);
//
//      for (File image : imageFiles) {
//
//        LOG.info(MadoopConstants.madoopDocuments + File.separator + "test" +
//                File.separator + imageDir.getName()+"_"+image.getName());
//        File dst = new File(MadoopConstants.madoopDocuments + File.separator + "test" +
//                File.separator + imageDir.getName()+"_"+image.getName());
//        try {
//          MyFiles.copy(image, dst);
//        } catch (IOException e) {
//          LOG.error(e);
//        }
//
//      }
//
//    }
//  }


    private void preProcessingForTrain() {

        preProcessing(faceOriginalDir, faceTrainingDir);

//    File root = new File(faceOriginalDir);
//
//    FilenameFilter imgFilter = new FilenameFilter() {
//      public boolean accept(File dir, String name) {
//        name = name.toLowerCase();
//        return name.endsWith(".jpg") || name.endsWith(".pgm") || name.endsWith(".png");
//      }
//    };
//
//    File[] imageDirs = root.listFiles();
//    for (File imageDir : imageDirs) {
//
//      File[] imageFiles = imageDir.listFiles(imgFilter);
//
//
//      for (File image : imageFiles) {
//        if (image.getName().contains("eig")) continue;
//        if (!GenerateEigen(imageDir.getName(),
//                          faceOriginalDir + File.separator + imageDir.getName() + File.separator + image.getName(),
//                          faceTrainingDir)) return;
//
//      }
//
//    }

    }


//  private void faceRecoTrain()
//  {
//
//    File root = new File(faceTrainingDir);
//
//    FilenameFilter imgFilter = new FilenameFilter() {
//      public boolean accept(File dir, String name) {
//        name = name.toLowerCase();
//        return name.endsWith(".jpg") || name.endsWith(".pgm") || name.endsWith(".png");
//      }
//    };
//
//    if (!new File(faceRootDir + File.separator + "facemodel.xml").exists()) {
//
////      File[] imageFiles = root.listFiles(imgFilter);
////
////
////      for (File image : imageFiles) {
////        if (image.getName().contains("eig")) continue;
////        if (!GenerateEigen(faceTrainingDir + File.separator + image.getName())) return;
////
////      }
//
//      OpenCVFaceRecognizer.trainToModel(faceTrainingDir);
//    }
//
//
////    File faceRoot = new File(faceRootDir);
////    File[] filesList = faceRoot.listFiles();
////    for(File f : filesList){
////
////      if(f.isFile() && f.getName().endsWith(".jpg") && !f.getName().contains("eig")){
////
////        if (!GenerateEigen(f.getAbsolutePath())) return;
//////      OpenCVFaceRecognizer.predictFromModel(faceRootDir + File.separator + device_name + "_brad_eig.jpg");
////      }
////
////    }
//
////    LOG.info(device_name);
//
//  }
//


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    LOG.info("OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
//                    System.loadLibrary("detection_based_tracker");

                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            LOG.error("Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            LOG.info("Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

//                        mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);

                        cascadeDir.delete();

//          faceRecoTrain();

//          LOG.info(getBasePath(faceRootDir + File.separator + "liu.jpg"));
//          LOG.info(getBaseName(faceRootDir + File.separator + "liu.jpg"));


                    } catch (IOException e) {
                        e.printStackTrace();
                        LOG.error("Failed to load cascade. Exception thrown: " + e);
                    }

                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    private void setText(final TextView text, final String value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(value);
            }
        });
    }

    private void append(final TextView text, final String value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.append(value);
            }
        });
    }

    private void setEnabled(final Button btn, final Boolean value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btn.setEnabled(value);
            }
        });
    }

    private static final Boolean DFS = true;


    private void purgeMadoopDir() {

        File tmp_dir = new File(madoopDir + File.separator + "tmp");
//    LOG.info(tmp_dir.getAbsolutePath());

        if (tmp_dir.exists()) {
            LOG.info("Deleting the tmp directory");
            MyFiles.deleteDir(tmp_dir);
        }

        if (reset_log) {
            File log_dir = new File(madoopDir + File.separator + "log");

            if (log_dir.exists()) {
                LOG.info("Deleting the log directory");
                MyFiles.deleteDir(log_dir);
            }
        }

    }

    private static final boolean AUTOTEST_PURGEDIR = true;
    private static final boolean AUTOTEST_STARTDATANODE = false;
    private static final boolean AUTOTEST_STARTTASKTRACKER = false;
    private static final boolean AUTOTEST_STARTJOB = false;

    private void do_autotest() {
        if (AUTOTEST_PURGEDIR) {
            reset_log = true;
            purgeMadoopDir();
        }
        if (AUTOTEST_STARTDATANODE) {
            try {
                startService(new Intent(MainActivity.this, datanodeService.class));
                datanode_on = true;
            } catch (Exception e) {
                LOG.error(e);
                datanode_on = false;
            }
        }
    }


    private boolean PrepareLocalDir(String input, String faces, String output) {
        LOG.info(Environment.getExternalStorageDirectory().getAbsolutePath());

        File madoop_doc_dir = new File(madoopDocuments);

        LOG.info(madoop_doc_dir.getAbsolutePath());

        if (!madoop_doc_dir.exists()) {
            if (!madoop_doc_dir.mkdirs()) {
                LOG.error("Failed to create Documents directory");
                return false;
            }
        }

        LOG.info(madoop_doc_dir.getAbsolutePath());

        input_dir = new File(madoop_doc_dir.getAbsolutePath() + File.separator + input);
        LOG.info(input_dir.getAbsolutePath());

        if (!input_dir.exists()) {
            LOG.error("Input directory does not exist!!!");
            if (!input_dir.mkdirs()) {
                LOG.error("Failed to create input directory");
                return false;
            }
            return false;
        }

//    if (MyFiles.isDirEmpty(input_dir)) {
//      LOG.error("Input directory cannot be empty!!!");
//      return false;
//    }

        if (TEST_FACERECO) {
            faces_dir = new File(madoop_doc_dir.getAbsolutePath() + File.separator + faces);
            LOG.info(faces_dir.getAbsolutePath());

            if (!faces_dir.exists()) {
                LOG.error("Faces directory does not exist!!!");
                if (!faces_dir.mkdirs()) {
                    LOG.error("Failed to create faces directory");
                    return false;
                }
                return false;
            }
        }

//    if (MyFiles.isDirEmpty(faces_dir)) {
//      LOG.error("Faces directory cannot be empty!!!");
//      return false;
//    }


        output_dir = new File(madoop_doc_dir.getAbsolutePath() + File.separator + output);
        LOG.info(output_dir.getAbsolutePath());

        if (output_dir.exists()) {
            LOG.info("Deleting files in output directory");
            MyFiles.deleteDir(output_dir);
        }

        return true;
    }

    private void uploadFolder(String local_input, String dfs_input) {

//    try {
//        DFSFiles.upload2DFS(local_faces.getAbsolutePath() + File.separator + "*", dfs_faces);
////            LOG.info(f.getAbsolutePath());
////            LOG.info(dfs_input + File.separator + f.getName());
//    }
//    catch (Exception e){
//      LOG.error(e);
//    }

        try {
            DFSFiles.upload2DFS(local_input, dfs_input);
//            LOG.info(f.getAbsolutePath());
//            LOG.info(dfs_input + File.separator + f.getName());
        } catch (Exception e) {
            LOG.error(e);
        }
    }


    private void uploadFaceFiles(File local_input, File local_faces, String dfs_faces, String dfs_input) {

        File[] filesList = local_faces.listFiles();
        File facelist = new File(local_input.getAbsolutePath() + File.separator + device_name + "_facelist.txt");
        if (facelist.exists() && !facelist.isDirectory()) {
            facelist.delete();
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(facelist);
        } catch (FileNotFoundException e) {
            LOG.error(e);
        }

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

        for (File f : filesList) {
//          if(f.isDirectory())
//              uploadAllFiles(f, dfs_input);
            if (f.isFile() && f.getName().contains("eig.jpg")) {
                try {
                    DFSFiles.upload2DFS(f.getAbsolutePath(), dfs_faces + File.separator + f.getName());
                    bw.write(f.getName());
                    bw.newLine();
//            LOG.info(f.getAbsolutePath());
//            LOG.info(dfs_input + File.separator + f.getName());
                } catch (Exception e) {
                    LOG.error(e);
                }
            }
        }

        try {
            bw.close();
        } catch (IOException e) {
            LOG.error(e);
        }

//    try {
//        DFSFiles.upload2DFS(local_faces.getAbsolutePath() + File.separator + "*", dfs_faces);
////            LOG.info(f.getAbsolutePath());
////            LOG.info(dfs_input + File.separator + f.getName());
//    }
//    catch (Exception e){
//      LOG.error(e);
//    }

        try {
            DFSFiles.upload2DFS(facelist.getAbsolutePath(), dfs_input + File.separator + facelist.getName());
//            LOG.info(f.getAbsolutePath());
//            LOG.info(dfs_input + File.separator + f.getName());
        } catch (Exception e) {
            LOG.error(e);
        }
    }


//  public void copyFilesFassets(Context context, String oldPath, String newPath)
//  {
//    try {
//      String fileNames[] = context.getAssets().list(oldPath);
//      if (fileNames.length > 0) {
//        File dir = new File(newPath);
//        if (!dir.exists()) dir.mkdirs();
//        for (String fileName : fileNames) {
//          copyFilesFassets(context,oldPath + "/" + fileName,newPath+"/"+fileName);
//        }
//      } else {
//        InputStream is = context.getAssets().open(oldPath);
//        FileOutputStream fos = new FileOutputStream(new File(newPath));
//        byte[] buffer = new byte[1024];
//        int byteCount=0;
//        while((byteCount=is.read(buffer))!=-1) {
//          fos.write(buffer, 0, byteCount);
//        }
//        fos.flush();
//        is.close();
//        fos.close();
//      }
//    }
//    catch (Exception e) {
//
//      LOG.info(e);
//
//    }
//  }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {

            Intent backHome = new Intent(Intent.ACTION_MAIN);
            backHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            backHome.addCategory(Intent.CATEGORY_HOME);
            startActivity(backHome);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    private Object lock = new Object();
    private boolean can_unlock = false;

    private boolean faceDetector_initialized = false;

    void initialize_faceDetector(){

        if (TEST_FACERECO) {

            new Thread(new Runnable() {

                @Override
                public void run() {

                    if (DETCTOR==GOOGLE) {
                        faceDetector = new FaceDetector.Builder(getContext())
                                .setClassificationType(FaceDetector.FAST_MODE)
                                .setLandmarkType(FaceDetector.NO_LANDMARKS)
                                .build();
                    }
                    else {
                        // Init the face detector.
                        mLandmarksDetector = new DLibLandmarks68Detector();

                        if (!mLandmarksDetector.isFaceDetectorReady()) {
                            mLandmarksDetector.prepareFaceDetector();
                        }

                        File face68ModelPath = new File(docu.getAbsolutePath() + File.separator + AppName + File.separator + FACE68_FILE);
                        if (!mLandmarksDetector.isFaceLandmarksDetectorReady()) {
                            mLandmarksDetector.prepareFaceLandmarksDetector(
                                    face68ModelPath.getAbsolutePath());
                        }
                    }

                    faceDetector_initialized = true;
                    LOG.info("faceDetector initialization done");

                }
            }).start();

        }

    }

    private static final int REQEUST_PERMISSION_MADOOP = 1;


    private void doPermission(String perm) {

        if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, perm)) {

            } else {
                ActivityCompat.requestPermissions(this, new String[]{perm}, REQEUST_PERMISSION_MADOOP);
            }
        }
        else{

            initialize_faceDetector();

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case REQEUST_PERMISSION_MADOOP:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    LOG.info("WRITE_EXTERNAL_STORAGE permission granted");
                    initialize_faceDetector();
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
//                synchronized (lock) {
//                    can_unlock = true;
//                    lock.notify();
//                }

                return;
        }
    }


    private void showSettingIP() {

        if (tasktracker_on || datanode_on) {

            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle("Note");
            alertDialog.setMessage("Cannot change IP when tasktracker/datanode running, shut down first!");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();

            return;
        }

//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//                try {
//
//                    String[] ips = DNS.getIPs();
//
//                    for (int ctr = 0; ctr < ips.length; ctr++) {
//                        LOG.info("ips :" + ips[ctr]);
//                    }
//
//                } catch (Exception e) {
//
//                    LOG.error(e);
//                    synchronized (lock) {
//                        can_unlock = true;
//                        lock.notify();
//                    }
//                }
//
//                synchronized (lock) {
//                    can_unlock = true;
//                    lock.notify();
//                }
//
//            }
//        }).start();
//
//        try {
//            synchronized (lock) {
//                if (!can_unlock)
//                    lock.wait();
//            }
//        } catch (InterruptedException e) {
//            LOG.error(e);
//        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Input Local IP address");
        builder.setMessage("(Please check your local IP in Android Settings)");
        builder.setCancelable(false);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(SPHelper.getString("theMainIP", "").substring(1, SPHelper.getString("theMainIP", "").length()));
        builder.setView(input);

        builder.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String m_Text = input.getText().toString();
                SPHelper.save("theMainIP", "/" + m_Text);
                LOG.info("theMainIP :" + SPHelper.getString("theMainIP", "").substring(1, SPHelper.getString("theMainIP", "").length()));
                //device_name = IPToolBox.RDNS.get(SPHelper.getString("theMainIP", ""));
                LOG.info("device_name :" + device_name);
            }
        });

//    builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
//      @Override
//      public void onClick(DialogInterface dialog, int which) {
//        dialog.cancel();
//      }
//    });

        builder.show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        doPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        try {
//            synchronized (lock) {
//                if (!can_unlock)
//                    lock.wait();
//            }
//        } catch (InterruptedException e) {
//            LOG.error(e);
//        }

        SPHelper.init(getApplication());

        String temp = SPHelper.getString("theMainIP", "");
        if (temp=="") SPHelper.save("theMainIP", "/192.168.x.y");

        showSettingIP();

        setContentView(R.layout.activity_main);

        AndroidLogger.SetLog2UI(true, INFO, this);

        tv = (TextView) findViewById(R.id.textView);
        // Example of a call to a native method
//        tv.setText(stringFromJNI());
        tv.setHorizontallyScrolling(true);
        tv.setMovementMethod(new ScrollingMovementMethod());

        if (isMyServiceRunning(tasktrackerService.class)) {
            tasktracker_on = true;
            tasktracker_changestatus = true;
        }

        if (isMyServiceRunning(datanodeService.class)) {
            datanode_on = true;
            datanode_changestatus = true;
        }

//        if (TEST_FACERECO) {
//
//            if (DETCTOR==GOOGLE) {
//                faceDetector = new FaceDetector.Builder(getContext())
//                        .setClassificationType(FaceDetector.FAST_MODE)
//                        .setLandmarkType(FaceDetector.NO_LANDMARKS)
//                        .build();
//            }
//            else {
//
//                // Init the face detector.
//                mLandmarksDetector = new DLibLandmarks68Detector();
//
//                if (!mLandmarksDetector.isFaceDetectorReady()) {
//                    mLandmarksDetector.prepareFaceDetector();
//                }
//
////                LOG.info("Begin to read face68Model file");
//                File face68ModelPath = new File(docu.getAbsolutePath() + File.separator + AppName + File.separator + FACE68_FILE);
////                LOG.info("Done with reading face68Model file");
//                if (!mLandmarksDetector.isFaceLandmarksDetectorReady()) {
////                    LOG.info("Begin to prepareFaceLandmarksDetector");
//                    mLandmarksDetector.prepareFaceLandmarksDetector(
//                            face68ModelPath.getAbsolutePath());
////                    LOG.info("Done with prepareFaceLandmarksDetector");
//                }
//            }
//        }

        btn_upload = (Button) findViewById(R.id.btn_upload);
        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                btn_upload.setEnabled(false);
//        btn_quit.setEnabled(false);

                AndroidLogger.ClearUILog();

                new Thread(new Runnable() {

                    public void cleanup() {
                        setEnabled(btn_upload, true);
//            setEnabled(btn_quit, true);
                    }

                    @Override
                    public void run() {
                        try {

                            if (!PrepareLocalDir("input", "faces", "output")) {
                                cleanup();
                                return;
                            }

                            if (!DFS) {

                                if (WordCount.main(input_dir.getAbsolutePath(), output_dir.getAbsolutePath())) {
                                    append(tv, "   WordCount Succeed!!!");
                                } else {
                                    append(tv, "   WordCount Failed!!!");
                                }
                            } else {

                                if (TEST_WORDCOUNT) {

                                    uploadFolder(input_dir.getAbsolutePath(), "input");
                                    DFSFiles.RemoveDFSOutput("output");

                                }

                                if (TEST_FACERECO) {
                                    preProcessingForUpload();
                                    uploadFaceFiles(input_dir, faces_dir, "faces", "input");
                                    DFSFiles.RemoveDFSOutput("output");
                                }

                            }


                            cleanup();

                        } catch (Exception e) {
                            cleanup();
                            LOG.error(e);
                        }

                    }
                }).start();

            }
        });


        btn_delin = (Button) findViewById(R.id.btn_delin);
        btn_delin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                btn_delin.setEnabled(false);
//        btn_quit.setEnabled(false);

                AndroidLogger.ClearUILog();

                new Thread(new Runnable() {

                    public void cleanup() {
                        setEnabled(btn_delin, true);
//            setEnabled(btn_quit, true);
                    }

                    @Override
                    public void run() {
                        try {

                            if (DFS) {
                                DFSFiles.RemoveDFSInput("input");
                            }
                            cleanup();

                        } catch (Exception e) {
                            cleanup();
                            LOG.error(e);
                        }

                    }
                }).start();

            }
        });


        btn_delout = (Button) findViewById(R.id.btn_delout);
        btn_delout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                btn_delout.setEnabled(false);
//        btn_quit.setEnabled(false);

                AndroidLogger.ClearUILog();

                new Thread(new Runnable() {

                    public void cleanup() {
                        setEnabled(btn_delout, true);
//            setEnabled(btn_quit, true);
                    }

                    @Override
                    public void run() {
                        try {

                            if (DFS) {
                                DFSFiles.RemoveDFSOutput("output");
                            }
                            cleanup();

                        } catch (Exception e) {
                            cleanup();
                            LOG.error(e);
                        }

                    }
                }).start();

            }
        });


        btn_run = (Button) findViewById(R.id.btn_run);
        btn_run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                btn_run.setEnabled(false);
//        btn_quit.setEnabled(false);

                AndroidLogger.ClearUILog();

                new Thread(new Runnable() {

                    public void cleanup() {
                        setEnabled(btn_run, true);
//            setEnabled(btn_quit, true);
                    }

                    @Override
                    public void run() {
                        try {

                            if (!DFS) {

                                if (!PrepareLocalDir("input", "faces", "output")) {
                                    cleanup();
                                    return;
                                }

                                if (WordCount.main(input_dir.getAbsolutePath(), output_dir.getAbsolutePath())) {
                                    append(tv, "   WordCount Succeed!!!");
                                } else {
                                    append(tv, "   WordCount Failed!!!");
                                }
                            } else {

                                if (TEST_WORDCOUNT) {
                                    if (WordCount.main("input", "output")) {
                                        append(tv, "   WordCount Succeed!!!");
                                    } else {
                                        append(tv, "   WordCount Failed!!!");
                                    }
                                }

                                if (TEST_FACERECO) {
                                    if (FaceRecognition.main(MainActivity.this,"input", "output")) {
                                        append(tv, "   FaceRecognition Succeed!!!");
                                    } else {
                                        append(tv, "   FaceRecognition Failed!!!");
                                    }
                                }

                            }

                            cleanup();

                        } catch (Exception e) {
                            cleanup();
                            LOG.error(e);
                        }

                    }
                }).start();

            }
        });


        if (TEST_FACERECO) {
//      do_autotest();
//      if (!PrepareLocalDir("input", "faces", "output")) {
//        return;
//      }
//      preProcessingForUpload();


//      String input = madoopDocuments + File.separator + "faces";
//      File root = new File(input);
//
//      FilenameFilter imgFilter = new FilenameFilter() {
//        public boolean accept(File dir, String name) {
//          name = name.toLowerCase();
//          return name.endsWith(".jpg") || name.endsWith(".pgm") || name.endsWith(".png");
//        }
//      };
//
//
//      File[] imageFiles = root.listFiles(imgFilter);
//
//
//      for (File image : imageFiles) {
//        String temp = OpenCVFaceRecognizer.predictFromModel2(madoopDocuments + File.separator + "facemodel.xml",image.getAbsolutePath());
//        LOG.info(String.format("%s is predicted as: %s", image.getName(), temp));
//
//      }

//      OpenCVFaceRecognizer.trainToModel(faceTrainingDir, faceRootDir);

        }


//    try {
//      startService(new Intent(MainActivity.this, tasktrackerService.class));
//    } catch (Exception e) {
//      LOG.error(e);
//    }

    }


    @Override
    public void onResume() {
        super.onResume();
        if (TEST_FACERECO) {
            if (!OpenCVLoader.initDebug()) {
                LOG.debug("Internal OpenCV library not found. Using OpenCV Manager for initialization");
                OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
            } else {
                LOG.debug("OpenCV library found inside package. Using it!");
                mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//    LOG.info("onCreateOptionsMenu get called");
//    getMenuInflater();
//		menu.add(0, 0, 0, "Debugging Pages ...");
//    menu.add(0, 1, 0, "About");
//    menu.add(0, 2, 0, "Quit");
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
//    LOG.info("****<onPrepareOptionsMenu>");
//    menu.clear();
//    getMenuInflater().inflate(R.menu.main_menu, menu);
//    LOG.info("****<onPrepareOptionsMenu> tasktracker_changestatus: " + tasktracker_changestatus);
//    LOG.info("****<onPrepareOptionsMenu> tasktracker_on: " + tasktracker_on);

        {
            MenuItem item = menu.findItem(R.id.menu_tasktracker);
            item.setChecked(tasktracker_on);
        }
        {
            MenuItem item = menu.findItem(R.id.menu_datanode);
            item.setChecked(datanode_on);
        }


        if (tasktracker_changestatus) {
            if (tasktracker_on) {
                MenuItem item = menu.findItem(R.id.menu_tasktracker);
                if (item != null) {
                    item.setChecked(true);
//          LOG.info("****<onPrepareOptionsMenu> tasktracker is ON");
                    try {
                        startService(new Intent(MainActivity.this, tasktrackerService.class));
                    } catch (Exception e) {
                        LOG.error(e);
                        tasktracker_on = false;

                    }
                    tasktracker_changestatus = false;
                }
            } else {
                MenuItem item = menu.findItem(R.id.menu_tasktracker);
                if (item != null) {
                    item.setChecked(false);
//          LOG.info("****<onPrepareOptionsMenu> tasktracker is ON");
                    try {
                        stopService(new Intent(MainActivity.this, tasktrackerService.class));
                    } catch (Exception e) {
                        LOG.error(e);
                    }
                    tasktracker_changestatus = false;
                }
            }
        }
//    LOG.info("****<onPrepareOptionsMenu> tasktracker_changestatus: " + tasktracker_changestatus);

//    LOG.info("****<onPrepareOptionsMenu> datanode_changestatus: " + datanode_changestatus);
//    LOG.info("****<onPrepareOptionsMenu> datanode_on: " + datanode_on);
        if (datanode_changestatus) {
            if (datanode_on) {
                MenuItem item = menu.findItem(R.id.menu_datanode);
                if (item != null) {
                    item.setChecked(true);
//          LOG.info("****<onPrepareOptionsMenu> datanode is ON: ");
                    try {
                        startService(new Intent(MainActivity.this, datanodeService.class));
                    } catch (Exception e) {
                        LOG.error(e);
                        datanode_on = false;
                    }
                    datanode_changestatus = false;
                }
            } else {
                MenuItem item = menu.findItem(R.id.menu_datanode);
                if (item != null) {
                    item.setChecked(false);
//          LOG.info("****<onPrepareOptionsMenu> datanode is OFF: ");
                    try {
                        stopService(new Intent(MainActivity.this, datanodeService.class));
                    } catch (Exception e) {
                        LOG.error(e);
                    }
                    datanode_changestatus = false;
                }
            }
        }
//    LOG.info("****<onPrepareOptionsMenu> datanode_changestatus: " + datanode_changestatus);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//		case 0:
//			startActivity(new Intent(getBaseContext(), MDFSSetting.class));
//			break;
            case R.id.menu_setip:

                showSettingIP();
                return true;
            case R.id.menu_tasktracker:
                //        LOG.info("****<onOptionsItemSelected> menu_tasktracker is selected");
                if (item.isChecked()) {

                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    //Yes button clicked
                                    tasktracker_on = false;
                                    tasktracker_changestatus = true;
                                    //                  LOG.info("****<onOptionsItemSelected> 1 tasktracker_on: " + tasktracker_on);
                                    //                  LOG.info("****<onOptionsItemSelected> 1 tasktracker_changestatus: " + tasktracker_changestatus);
                                    supportInvalidateOptionsMenu();
                                    break;

                                //                case DialogInterface.BUTTON_NEGATIVE:
                                //                  //No button clicked
                                //                  tasktracker_on = true;
                                //                  tasktracker_changestatus = false;
                                //                  break;
                            }
                        }
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("TaskTracker");
                    builder.setMessage("Are you sure to turn off TaskTracker?").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener);

                    AlertDialog alertDialog = builder.create();
                    if (!alertDialog.isShowing())
                        alertDialog.show();

                } else {
                    tasktracker_on = true;
                    tasktracker_changestatus = true;
                    //          LOG.info("****<onOptionsItemSelected> 2 tasktracker_on: " + tasktracker_on);
                    //          LOG.info("****<onOptionsItemSelected> 2 tasktracker_changestatus: " + tasktracker_changestatus);
                    //          item.setChecked(true);
                    supportInvalidateOptionsMenu();
                }
                return true;
            case R.id.menu_datanode:
                //        LOG.info("****<onOptionsItemSelected> menu_datanode is selected");
                if (item.isChecked()) {

                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    //Yes button clicked
                                    datanode_on = false;
                                    datanode_changestatus = true;
                                    //                  LOG.info("****<onOptionsItemSelected> 1 tasktracker_on: " + tasktracker_on);
                                    //                  LOG.info("****<onOptionsItemSelected> 1 tasktracker_changestatus: " + tasktracker_changestatus);
                                    supportInvalidateOptionsMenu();
                                    break;

                                //                case DialogInterface.BUTTON_NEGATIVE:
                                //                  //No button clicked
                                //                  tasktracker_on = true;
                                //                  tasktracker_changestatus = false;
                                //                  break;
                            }
                        }
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("DataNode");
                    builder.setMessage("Are you sure to turn off DataNode?").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener);

                    AlertDialog alertDialog = builder.create();
                    if (!alertDialog.isShowing())
                        alertDialog.show();

                } else {
                    datanode_on = true;
                    datanode_changestatus = true;
                    //          LOG.info("****<onOptionsItemSelected> 2 tasktracker_on: " + tasktracker_on);
                    //          LOG.info("****<onOptionsItemSelected> 2 tasktracker_changestatus: " + tasktracker_changestatus);
                    //          item.setChecked(true);
                    supportInvalidateOptionsMenu();
                }
                return true;

            case R.id.menu_reset: {
                reset_log = true;
                View checkBoxView = View.inflate(this, R.layout.checkbox, null);
                CheckBox checkBox = (CheckBox) checkBoxView.findViewById(R.id.checkbox);
                checkBox.setChecked(true);
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        reset_log = isChecked;
                        // Save to shared preferences
                    }
                });
                checkBox.setText("Also delete all log files");

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Reset");
                builder.setMessage("Are you sure about this?")
                        .setView(checkBoxView)
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                tv.setText("");
                                tv.scrollTo(0, 0);
                                purgeMadoopDir();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }).show();


                //          DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                //            @Override
                //            public void onClick(DialogInterface dialog, int which) {
                //              switch (which) {
                //                case DialogInterface.BUTTON_POSITIVE:
                //
                //                  break;
                //
                //  //                case DialogInterface.BUTTON_NEGATIVE:
                //  //                  //No button clicked
                //  //                  tasktracker_on = true;
                //  //                  tasktracker_changestatus = false;
                //  //                  break;
                //              }
                //            }
                //          };
                //          AlertDialog.Builder builder = new AlertDialog.Builder(this);
                //          builder.setMessage("Are you sure to reset Madoop?").setPositiveButton("Yes", dialogClickListener)
                //                  .setNegativeButton("No", dialogClickListener);
                //
                //          AlertDialog alertDialog = builder.create();
                //          if (!alertDialog.isShowing())
                //            alertDialog.show();
            }
            return true;

            case R.id.menu_about: {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSSSSS");
                //			LOG.warn(TAG, simpleDateFormat.format(new Date(BuildConfig.TIMESTAMP)));
                final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setCancelable(true);
                alertDialog.setTitle("About Madoop4");
                alertDialog.setMessage(
                        "Author: Wei Zhang\n" +
                                "Email: charleyhuman@tamu.edu\n"); //+
//                                "Build time: " + simpleDateFormat.format(new Date(BuildConfig.TIMESTAMP)));

                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                if (!alertDialog.isShowing())
                    alertDialog.show();
            }
            return true;
            //      case R.id.menu_quit:
            //        finish();
            //        return true;
            case R.id.menu_test:
                preProcessingForTrain();
                //      faceRecoTrain();
                //      File root = new File(madoopDocuments + File.separator + "faces");
                //
                //      FilenameFilter imgFilter = new FilenameFilter() {
                //        public boolean accept(File dir, String name) {
                //          name = name.toLowerCase();
                //          return name.endsWith(".jpg") || name.endsWith(".pgm") || name.endsWith(".png");
                //        }
                //      };
                //
                //      File[] imageFiles = root.listFiles(imgFilter);
                //
                //      for (File image : imageFiles) {
                //
                //        String temp = OpenCVFaceRecognizer.predictFromModel2(image.getAbsolutePath());
                //        LOG.info(image.getName() + " is predicted as: " + temp);
                //
                //      }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }


    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
//    public native String stringFromJNI();
}
