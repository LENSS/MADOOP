
  package edu.tamu.lenss.madoop4;

  import android.os.Environment;

  import java.io.File;
  import java.util.Collections;
  import java.util.HashMap;
  import java.util.Map;

  /*******************************
 * Some handy constants
 * 
 *******************************/
public interface MadoopConstants {

  public static final String AppName = "Madoop4";
//  public static final String webappsDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Madoop4/tasktracker/webapps";
  public static final String madoopDir = Environment.getExternalStorageDirectory().getAbsolutePath() +
        File.separator + "distressnet"+File.separator+AppName;
  /*public static final String madoopDocuments = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() +
          File.separator + AppName;
  */
  public static final String madoopDocuments =  madoopDir + File.separator + "Madoop4_documents";

    /*public static final String faceRootDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() +
          File.separator + "FaceReco";
*/
    public static final String faceRootDir = madoopDir + File.separator + "FaceReco";


    public static final String faceTrainingDir = faceRootDir + File.separator + "Train";
  public static final String faceOriginalDir = faceRootDir + File.separator + "Orig";

  
  //public static final String StreamInput = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "StreamFDPic";
  public static final String StreamInput = Environment.getExternalStorageDirectory().getAbsolutePath() +
          File.separator + "distressnet"+File.separator + "MStorm"+File.separator + "StreamFDPic";



//  public static Map<String, String> RDNS;

//  public static final Map<String, String> RDNS =
//      Collections.unmodifiableMap(new HashMap<String, String>()
//      {
//        {
//          put("/192.168.0.30", "phone-30");
//          put("/192.168.0.31", "phone-31");
//          put("/192.168.0.32", "phone-32");
//          put("/192.168.0.33", "phone-33");
//          put("/192.168.0.34", "phone-34");
//          put("/192.168.0.35", "phone-35");
//          put("/192.168.0.36", "phone-36");
//          put("/192.168.0.37", "phone-37");
//          put("/192.168.0.38", "phone-38");
//          put("/192.168.0.39", "phone-39");
//          put("/192.168.0.40", "phone-40");
//          put("/192.168.0.41", "phone-41");
//          put("/192.168.0.42", "phone-42");
//          put("/192.168.0.43", "phone-43");
//          put("/192.168.0.44", "phone-44");
//          put("/192.168.0.45", "phone-45");
//          put("/192.168.0.46", "phone-46");
//          put("/192.168.0.47", "phone-47");
//          put("/192.168.0.48", "phone-48");
//          put("/192.168.0.49", "phone-49");
//          put("/192.168.0.50", "phone-50");
//          put("/192.168.0.51", "phone-51");
//          put("/192.168.0.52", "phone-52");
//          put("/192.168.0.53", "phone-53");
//          put("/192.168.0.54", "phone-54");
//          put("/192.168.0.55", "phone-55");
//          put("/192.168.0.56", "phone-56");
//          put("/192.168.0.57", "phone-57");
//          put("/192.168.0.58", "phone-58");
//          put("/192.168.0.59", "phone-59");
//          put("/192.168.0.60", "phone-60");
//          put("/192.168.0.61", "phone-61");
//          put("/192.168.0.62", "phone-62");
//          put("/192.168.0.63", "phone-63");
//          put("/192.168.0.64", "phone-64");
//          put("/192.168.0.65", "phone-65");
//          put("/192.168.0.66", "phone-66");
//          put("/192.168.0.67", "phone-67");
//          put("/192.168.0.68", "phone-68");
//          put("/192.168.0.69", "phone-69");
//        }
//      });


  public static final int VERBOSE = 0;
  public static final int DEBUG = 1;
  public static final int INFO = 2;
  public static final int WARN = 3;
  public static final int ERROR = 4;

//  public static final int BRAD = 0;
//  public static final int SCARLETT = 1;
//  public static final int LIU = 2;
//  public static final int ZHAO = 3;

//  public static final Map<Integer, String> FACES =
//          Collections.unmodifiableMap(new HashMap<Integer, String>()
//          {
//            {
//              put(BRAD, "Brad Pitt");
//              put(SCARLETT, "Scarlett Johansson");
//              put(LIU, "Dehua Liu");
//              put(ZHAO, "Liying Zhao");
//            }
//          });
}

