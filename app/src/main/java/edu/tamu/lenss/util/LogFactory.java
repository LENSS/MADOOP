package edu.tamu.lenss.util;

//import android.util.Log;
//import edu.tamu.lenss.MyLogger;
import java.lang.Object;


public final class LogFactory
{

//  private static java.util.logging.MyLogger JMyLogger = java.util.logging.MyLogger.getMyLogger("InfoLogging");
  static
  {
    /*try {
      JMyLogger.addHandler(new FileHandler("MDFSLog.log"));
    } catch (IOException e) {
      e.printStackTrace();
    }*/
    //JMyLogger.addHandler(new ConsoleHandler());
  }

    public static boolean AndroidLogcat = true;
    public static String LogFile = "file.log";

    LogFactory()
    {

    }
    /** Gets a MyLogger and, as a side effect, installs this as the default
     * formatter. */
    public static ILogger getLog(Class cls)
    {
        // just referencing this class installs it
//        return new AndroidLogger(name.getSimpleName());
        if (AndroidLogcat)
            return AndroidLogger.getLogger(cls.getSimpleName());
        else
            return JournalLogger.getLogger(cls, LogFile);
    }

    public static ILogger getLog(String name)
    {
        // just referencing this class installs it
//        return new AndroidLogger(name);
        if (AndroidLogcat)
            return AndroidLogger.getLogger(name);
        else
            return JournalLogger.getLogger(name, LogFile);
    }



}
