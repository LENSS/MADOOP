package edu.tamu.lenss.madoop4;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
//import android.support.design.widget.FloatingActionButton;
//import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.protobuf.InvalidProtocolBufferException;
import com.my.jni.dlib.DLibLandmarks68Detector;
import com.my.jni.dlib.IDLibFaceDetector;
import com.my.jni.dlib.data.DLibFace;
import com.pl.sphelper.SPHelper;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.objdetect.CascadeClassifier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import edu.tamu.cse.lenss.gnsService.client.GnsServiceClient;
import edu.tamu.cse.lenss.gnsService.server.GNSServiceUtils;
import edu.tamu.lenss.util.DFSFiles;
import edu.tamu.lenss.util.IPToolBox;
import edu.tamu.lenss.util.MyFiles;

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
import java.util.Map;

import org.bytedeco.javacpp.opencv_face.FaceRecognizer;


import com.daimajia.numberprogressbar.NumberProgressBar;
import com.daimajia.numberprogressbar.OnProgressBarListener;

import edu.tamu.lenss.R;


import static org.bytedeco.javacpp.opencv_core.CV_32SC1;



public class Main2Activity extends AppCompatActivity implements MadoopConstants {

    private static final String FACE68_FILE = "shape_predictor_68_face_landmarks.dat";
    private final SparseArray<DLibFace> mDetFaces = new SparseArray<>();
    //private final File docu = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
    //private final File down = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    IDLibFaceDetector mLandmarksDetector;
    private Detector<Face> faceDetector;

    private int DLIB = 0;
    private int GOOGLE = 1;
    private int DETCTOR = DLIB;

    private File input_dir;
    private File faces_dir;
    private File output_dir;
    private File temp_dir;
    private File show_dir;
    private boolean tasktracker_on = false;
    private boolean tasktracker_changestatus = false;
    private boolean datanode_on = false;
    private boolean datanode_changestatus = false;
    private boolean purge_inputfaces = false;

//  private static final String TAG = MainActivity.class.getSimpleName();

    //    private static final MyLogger LOG = MyLogger.getLogger(TAG);
    private static final ILogger LOG = LogFactory.getLog(MainActivity.class);


    private static final boolean TEST_WORDCOUNT = false;
    private static final boolean TEST_FACERECO = !TEST_WORDCOUNT;

    private static String device_name = "";

    private File mCascadeFile;
    private CascadeClassifier mJavaDetector;

    private Button btn_facereco;
    private Button btn_upload;
    private NumberProgressBar  mProgress_allOther;
    private NumberProgressBar  mProgress_reducer;

    private TextView textView_allOther;
    private TextView textView_reducer;

    private int mProgressStatus = 0;
    private Handler mHandler = new Handler();

    private GridView mGridView;

    private GridViewAdapter mGridAdapter;
    private ArrayList<GridItem> mGridData;

    private Context getContext() {
        return this;
    }

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
            final List<Rect> faceBounds;
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

        int cur = 0;
        for (File image : imageFiles) {
            if (image.getName().contains("eig")) continue;
            GenerateEigen(device_name, input + File.separator + image.getName(), output);
            mProgressStatus = ++cur*100/imageFiles.length;
            LOG.info(String.format("mProgressStatus: %d", mProgressStatus));
            mHandler.post(new Runnable() {
                public void run() {
                    LOG.info(String.format("mProgressStatus: %d", mProgressStatus));
                    mProgress_allOther.setProgress(mProgressStatus);
                }
            });
        }

    }


    private void preProcessingForUpload() {

        preProcessing(StreamInput, faces_dir.getAbsolutePath());

    }


    private void preProcessingForTrain() {

        preProcessing(faceOriginalDir, faceTrainingDir);

    }


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


    private void setProgress(final NumberProgressBar pb, final int val){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pb.setProgress(val);
            }
        });

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

        if (true) {
            File log_dir = new File(madoopDir + File.separator + "log");

            if (log_dir.exists()) {
                LOG.info("Deleting the log directory");
                MyFiles.deleteDir(log_dir);
            }
        }

    }


    private boolean PrepareLocalDir(String input, String faces, String output, String temp, String show) {
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
            LOG.info("Create input directory!!!");
            if (!input_dir.mkdirs()) {
                LOG.error("Failed to create input directory");
                return false;
            }
        }
        else{
            LOG.info("Deleting files in input directory");
            MyFiles.emptyDir(input_dir);
        }

        if (TEST_FACERECO) {
            faces_dir = new File(madoop_doc_dir.getAbsolutePath() + File.separator + faces);
            LOG.info(faces_dir.getAbsolutePath());

            if (!faces_dir.exists()) {
                LOG.info("Create faces directory!!!");
                if (!faces_dir.mkdirs()) {
                    LOG.error("Failed to create faces directory");
                    return false;
                }
            }
            else{
                LOG.info("Deleting files in faces directory");
                MyFiles.emptyDir(faces_dir);
            }
        }

        output_dir = new File(madoop_doc_dir.getAbsolutePath() + File.separator + output);
        LOG.info(output_dir.getAbsolutePath());

        if (output_dir.exists()) {
            LOG.info("Delete output directory");
            MyFiles.deleteDir(output_dir);
        }

        temp_dir = new File(madoop_doc_dir.getAbsolutePath() + File.separator + temp);
        LOG.info(temp_dir.getAbsolutePath());

        if (!temp_dir.exists()) {
            LOG.info("Create temp directory!!!");
            if (!temp_dir.mkdirs()) {
                LOG.error("Failed to create temp directory");
                return false;
            }
        }
        else{
            LOG.info("Deleting files in temp directory");
            MyFiles.emptyDir(temp_dir);
        }

        show_dir = new File(madoop_doc_dir.getAbsolutePath() + File.separator + show);
        LOG.info(show_dir.getAbsolutePath());

        if (show_dir.exists()) {
            LOG.info("Deleting show directory");
            MyFiles.deleteDir(show_dir);
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

                        //Suman's edit
                        //File face68ModelPath = new File(docu.getAbsolutePath() + File.separator + AppName + File.separator + FACE68_FILE);
                        File face68ModelPath = new File(madoopDocuments + File.separator + FACE68_FILE);


                        if (!face68ModelPath.exists()) {

                            setText(textView_allOther, "face68Model file is missing!");
                            return;
                        }

                        mLandmarksDetector = new DLibLandmarks68Detector();

                        if (!mLandmarksDetector.isFaceDetectorReady()) {
                            mLandmarksDetector.prepareFaceDetector();
                        }

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

            AlertDialog alertDialog = new AlertDialog.Builder(Main2Activity.this).create();
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

        // First, get own IP addresses
        List<InetAddress> ips= new ArrayList<InetAddress>();
        List<String> madoopServerList = new ArrayList<String>();
        try {
            ips = GNSServiceUtils.getOwnIPv4s();
            LOG.debug("Got own IPS: "+ips.toString());

            GnsServiceClient gnsServiceClient = new GnsServiceClient();
            madoopServerList = gnsServiceClient.getPeerNames("madoop", "server");
            LOG.debug("Got the Madoop Server: "+madoopServerList.toString());


        } catch (SocketException e) {
            LOG.fatal("Problem getting either own IPs or GNS server ", e);
        }

        if ( ips.isEmpty() || madoopServerList.isEmpty()){
            LOG.fatal("could not retrieve own IP list or connecting to GNS");
            Toast.makeText(getContext(), "could not retrieve own IP list or find server",Toast.LENGTH_LONG).show();
            onBackPressed();
            return;
        }

        // Set the madoop server name in the configuration file
        modifyHadoopConfig(madoopServerList.get(0));

        // Now, check for own IPs
        final String[] ownIPs = new String[ips.size()];
        for (int i =0; i <ips.size(); i++)
            ownIPs[i] = ips.get(i).toString();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select the IP you want to use as own IP");
        builder.setCancelable(false);

        builder.setSingleChoiceItems(ownIPs, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // user checked an item
                String ownIP = ownIPs[which];
                LOG.info("You selected IP:" + ownIP );

                SPHelper.save("theMainIP", ownIP);
                device_name = IPToolBox.RDNS.get(SPHelper.getString("theMainIP", ""));
                LOG.info("device_name :" + device_name);

                dialog.dismiss();
            }
        });
        builder.show();

        /*
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
                device_name = IPToolBox.RDNS.get(SPHelper.getString("theMainIP", ""));
                LOG.info("device_name :" + device_name);
            }
        });

        builder.show();
        */
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            LOG.info("Got message: " + message);
            String[] array = message.split(":");
            int mapping_prog = Integer.valueOf(array[0]);
            int reducing_prog = Integer.valueOf(array[1]);
            setProgress(mProgress_allOther, mapping_prog); setProgress(mProgress_reducer, reducing_prog);
            if (mapping_prog==100) setText(textView_allOther, "Mapping is done!");
            if (reducing_prog==100) setText(textView_reducer, "Reducing is done!");
        }
    };



    void showResults(String img_dir, String output_dir){

        Map<String, String> mp = new HashMap<>();

        try(BufferedReader br = new BufferedReader(new FileReader(new File(output_dir + File.separator + "part-r-00000")))) {
            for(String line; (line = br.readLine()) != null; ) {
                // process the line.
                LOG.info(line);
                String[] yy = line.split("\t");
                LOG.info(yy[0]);
//                LOG.info(yy[1]);
                String[] y = yy[0].split(":");
                mp.put(y[0], y[1]);
            }
        }
        catch(Exception e){
            LOG.error(e);
        }

        File show = new File(img_dir);

        FilenameFilter imgFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                name = name.toLowerCase();
                return name.endsWith(".jpg") || name.endsWith(".pgm") || name.endsWith(".png");
            }
        };

        File[] imageFiles = show.listFiles(imgFilter);

//        int cur = 0;
        GridItem item;
        for (File image : imageFiles) {
            item = new GridItem();
            if (mp.get(image.getName())==null)
                item.setTitle("Unknown");
            else
                item.setTitle(mp.get(image.getName()));
            item.setImage(image.getAbsolutePath());
            mGridData.add(item);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mGridAdapter.setGridData(mGridData);
            }
        });

    }


    @Override
    protected void onStop() {
        // Unregister since the activity is paused.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
                mMessageReceiver);
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mProgress_allOther = (NumberProgressBar ) findViewById(R.id.progressBar_allOther);
        mProgress_reducer = (NumberProgressBar ) findViewById(R.id.progressBar_reducer);

        textView_allOther = (TextView) findViewById(R.id.textView_allOther);
        textView_allOther.setText("");
        textView_reducer = (TextView) findViewById(R.id.textView_reducer);
        textView_reducer.setText("");

        mGridView = (GridView) findViewById(R.id.gridView);

        //Initialize with empty data
        mGridData = new ArrayList<>();
        mGridAdapter = new GridViewAdapter(this, R.layout.grid_item_layout, mGridData);
        mGridView.setAdapter(mGridAdapter);

        //Grid view click event
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                //Get item at position
                GridItem item = (GridItem) parent.getItemAtPosition(position);

                Intent intent = new Intent(Main2Activity.this, DetailsActivity.class);
                ImageView imageView = (ImageView) v.findViewById(R.id.grid_item_image);

                // Interesting data to pass across are the thumbnail size/location, the
                // resourceId of the source bitmap, the picture description, and the
                // orientation (to avoid returning back to an obsolete configuration if
                // the device rotates again in the meantime)

                int[] screenLocation = new int[2];
                imageView.getLocationOnScreen(screenLocation);

                //Pass the image title and url to DetailsActivity
                intent.putExtra("left", screenLocation[0]).
                        putExtra("top", screenLocation[1]).
                        putExtra("width", imageView.getWidth()).
                        putExtra("height", imageView.getHeight()).
                        putExtra("title", item.getTitle()).
                        putExtra("image", item.getImage());

                //Start details activity
                startActivity(intent);
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("custom-event-name"));

//        // Start lengthy operation in a background thread
//        new Thread(new Runnable() {
//            public void run() {
//                int i = 0;
//                while (mProgressStatus < 100) {
//                    mProgressStatus = i+=2;
//                    try {
//                        Thread.sleep(100);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    // Update the progress bar
//                    mHandler.post(new Runnable() {
//                        public void run() {
//                            mProgress_allOther.setProgress(mProgressStatus);
//                        }
//                    });
//                }
//            }
//        }).start();

        purgeMadoopDir();
        doPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        SPHelper.init(getApplication());


        //Suman's edit starts here

        //String temp = SPHelper.getString("theMainIP", "");
        //if (temp=="") SPHelper.save("theMainIP", "/192.168.x.y");

        //Suman's edit ends here

        showSettingIP();

        if (isMyServiceRunning(tasktrackerService.class)) {
            tasktracker_on = true;
            tasktracker_changestatus = true;
        }

        if (isMyServiceRunning(datanodeService.class)) {
            datanode_on = true;
            datanode_changestatus = true;
        }

        btn_upload = (Button) findViewById(R.id.btn_up);
        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                btn_upload.setEnabled(false);

                new Thread(new Runnable() {

                    public void cleanup() {
                        setEnabled(btn_upload, true);
                    }

                    @Override
                    public void run() {
                        try {

                            if (!PrepareLocalDir("input", "faces", "output", "temp", "show")) {
                                cleanup();
                                return;
                            }

                            if (!DFS) {

                                WordCount.main(input_dir.getAbsolutePath(), output_dir.getAbsolutePath());

                            } else {

                                if (TEST_WORDCOUNT) {

                                    uploadFolder(input_dir.getAbsolutePath(), "input");
                                    DFSFiles.RemoveDFSOutput("output");

                                }

                                if (TEST_FACERECO) {
                                    setProgress(mProgress_allOther, 0); setProgress(mProgress_reducer, 0);
                                    setText(textView_allOther, "Emptying local input dirs ...");
                                    MyFiles.emptyDir(input_dir); MyFiles.emptyDir(faces_dir);
                                    setText(textView_allOther, "Pre-processing all pictures, it may take long time ...");
                                    preProcessingForUpload();
//                                    setText(textView_allOther, "Removing remote input and output dirs ...");
//                                    DFSFiles.RemoveDFSInput("input"); DFSFiles.RemoveDFSInput("faces");
//                                    DFSFiles.RemoveDFSOutput("output");
                                    setText(textView_allOther, "Uploading pictures to remote file systems ...");
                                    uploadFaceFiles(input_dir, faces_dir, "faces", "input");
                                    setText(textView_allOther, "Upload pictures to remote file systems, Done!");
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

        btn_facereco = (Button) findViewById(R.id.btn_face);
        btn_facereco.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                btn_facereco.setEnabled(false);

                new Thread(new Runnable() {

                    public void cleanup() {
                        setEnabled(btn_facereco, true);
                    }

                    @Override
                    public void run() {
                        try {

//                            if (!PrepareLocalDir("input", "faces", "output", "temp", "show")) {
//                                cleanup();
//                                return;
//                            }

                            if (!DFS) {

                                WordCount.main(input_dir.getAbsolutePath(), output_dir.getAbsolutePath());

                            } else {

                                if (TEST_WORDCOUNT) {

                                    uploadFolder(input_dir.getAbsolutePath(), "input");
                                    DFSFiles.RemoveDFSOutput("output");

                                }

                                if (TEST_FACERECO) {
                                    mGridData.clear();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mGridAdapter.setGridData(mGridData);
                                        }
                                    });
                                    setText(textView_allOther, "Mapping progress ...");
                                    setText(textView_reducer, "Reducing progress ...");
                                    setProgress(mProgress_allOther, 0); setProgress(mProgress_reducer, 0);
                                    if (FaceRecognition.main(Main2Activity.this, "input", "output")) {
                                        setText(textView_reducer, "");
                                        setText(textView_allOther, "FaceRecognition Succeed!!!");
                                        DFSFiles.copyfromDFS("output", output_dir.getAbsolutePath());
                                        DFSFiles.copyfromDFS("faces", show_dir.getAbsolutePath());
                                        showResults(show_dir.getAbsolutePath(), output_dir.getAbsolutePath());
                                    } else {
                                        setText(textView_reducer, "");
                                        setText(textView_allOther, "FaceRecognition Failed!!!");
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
        getMenuInflater().inflate(R.menu.main_menu2, menu);
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
                        startService(new Intent(Main2Activity.this, tasktrackerService.class));
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
                        stopService(new Intent(Main2Activity.this, tasktrackerService.class));
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
                        startService(new Intent(Main2Activity.this, datanodeService.class));
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
                        stopService(new Intent(Main2Activity.this, datanodeService.class));
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

            case R.id.menu_purgeDFS: {

                purge_inputfaces = false;
                View checkBoxView = View.inflate(this, R.layout.checkbox, null);
                CheckBox checkBox = (CheckBox) checkBoxView.findViewById(R.id.checkbox);
                checkBox.setChecked(purge_inputfaces);
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        purge_inputfaces = isChecked;
                        // Save to shared preferences
                    }
                });
                checkBox.setText("Also purge input and faces dir");

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Purge DFS");
                builder.setMessage("Are you sure to purge DFS (output, input, faces)?")
                        .setView(checkBoxView)
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                new Thread(new Runnable() {
                                    public void run() {
                                        try {
                                            setText(textView_allOther, "Purging DFS dirs ...");
                                            if (purge_inputfaces) {
                                                DFSFiles.RemoveDFSInput("input");
                                                DFSFiles.RemoveDFSInput("faces");
                                            }
                                            DFSFiles.RemoveDFSOutput("output");
                                            setText(textView_allOther, "Purge DFS dirs, Done!");

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }).start();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }).show();

//                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        switch (which) {
//                            case DialogInterface.BUTTON_POSITIVE:
//
//                                new Thread(new Runnable() {
//                                    public void run() {
//                                        try {
//                                            setText(textView_allOther, "Purging all DFS dirs ...");
//                                            DFSFiles.RemoveDFSInput("input");
//                                            DFSFiles.RemoveDFSInput("faces");
//                                            DFSFiles.RemoveDFSOutput("output");
//                                            setText(textView_allOther, "Purge all DFS dirs, Done!");
//
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                        }
//                                    }
//                                }).start();
//
//                                break;
//
//                        }
//                    }
//                };
//                AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                builder.setTitle("Purge DFS");
//                builder.setMessage("Are you sure to purge DFS?").setPositiveButton("Yes", dialogClickListener)
//                        .setNegativeButton("No", dialogClickListener);
//
//                AlertDialog alertDialog = builder.create();
//                if (!alertDialog.isShowing())
//                    alertDialog.show();

            }
            return true;

            case R.id.menu_trainPhotos: {
                System.out.println("Training selected");
                preProcessing(faceOriginalDir, faceTrainingDir);
                System.out.println("Preprocessing done");
                OpenCVFaceRecognizer.trainToModel(faceTrainingDir, faceRootDir);
                System.out.println("Training done");
                setText(textView_allOther, "Training done");

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
                                "Email: charleyhuman@tamu.edu\n");// +
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

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    void makeDirectories(String path){
        File directory = new File(path);
        if (! directory.exists()) // Create the directory if it does not exist
            directory.mkdir();
    }

    /**
     * This function modify the Madoop configuration and set the correct madoop server
     * hostname
     * @param madoopServerName
     */
    private void modifyHadoopConfig(String madoopServerName) {
        String distressnetDir = Environment.getExternalStorageDirectory().toString() + File.separator + "distressnet";
        makeDirectories(distressnetDir);

        String madoopDir = distressnetDir+"/Madoop4";
        makeDirectories(madoopDir);

        String configDir = madoopDir+"/config";
        makeDirectories(configDir);





        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        for(String filename : files) {
            if (!filename.endsWith(".xml")) // no need to copy anything other than xml files
                continue;
            LOG.debug("Trying to copy file: "+ filename);
            try {
                InputStream in = assetManager.open(filename);
                BufferedReader br  = new BufferedReader(new InputStreamReader(in));

                File outFile = new File(configDir, filename);

                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter( new FileOutputStream(outFile)));

                String line;
                while ((line = br.readLine()) != null) {
                    // The current configuration contains lenss-epc as the server. Replace it with the
                    // actual server discovered through service discovery
                    line=line.replace("lenss-epc", madoopServerName);
                    bw.append(line+"\n");
                }

                //copyFile(in, out);
                in.close();
                br.close();
                bw.flush();
                bw.close();
            } catch(IOException e) {
                Log.e("tag", "Failed to copy asset file: " + filename, e);
            }
        }
    }

}
