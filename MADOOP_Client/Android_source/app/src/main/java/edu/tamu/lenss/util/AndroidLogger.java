package edu.tamu.lenss.util;

import android.app.Activity;
import android.text.Layout;
import android.util.Log;
import android.widget.TextView;

import org.apache.hadoop.log.LogLevel;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Exception;
import java.lang.Throwable;
import edu.tamu.lenss.madoop4.MadoopConstants;
import edu.tamu.lenss.R;


public final class AndroidLogger extends ILogger implements MadoopConstants
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


  private String default_tag = "";
  public static Boolean Log2UI = false;
  public static int LogLevel = INFO;
  public static Activity Act;



    AndroidLogger(String name)
    {
        default_tag = name;
    }
    /** Gets a MyLogger and, as a side effect, installs this as the default
     * formatter. */
    public static AndroidLogger getLogger(String name)
    {
        // just referencing this class installs it
        return new AndroidLogger(name);
    }

//  private void Log2TV(){
//
//  }

  public static void SetLog2UI(final Boolean L, final int LL, final Activity A)
  {
    Act = A;
    Log2UI = L;
    LogLevel = LL;
  }

  public static void ClearUILog()
  {

    final TextView tv = (TextView) Act.findViewById(R.id.textView);

    if (tv != null) {

      Act.runOnUiThread(new Runnable() {
        @Override
        public void run() {

          tv.setText("");
          tv.scrollTo(0, 0);

        }
      });
    }


  }

  private void UILog(final int level, final String tag, final String msg)
  {

    if (Log2UI) {
      if (level<LogLevel) return;

      final TextView tv = (TextView) Act.findViewById(R.id.textView);

      if (tv != null) {

        final String Level;

        if (level==VERBOSE)  Level = "V";
        else if (level==DEBUG)  Level = "D";
        else if (level==INFO)  Level = "I";
        else if (level==WARN)  Level = "W";
        else   Level = "E";

        Act.runOnUiThread(new Runnable() {
          @Override
          public void run() {

            tv.append(" " + Level + "/" + (tag.equals("")?tag:(tag+ ": ")) + msg + " \n");

            final Layout layout = tv.getLayout();
            if(layout != null) {
              int scrollDelta = layout.getLineBottom(tv.getLineCount() - 1)
                                - tv.getScrollY() - tv.getHeight();
              if(scrollDelta > 0)
                tv.scrollBy(0, scrollDelta);
            }

          }
        });
      }
    }

  }

  private void UILog(final int level, Throwable e)
  {
    StringWriter errors = new StringWriter();
    e.printStackTrace(new PrintWriter(errors));
    UILog(level, "", errors.toString());

  }

  public final void v(String tag, String msg)
  {
    Log.v(tag, msg);
    //JMyLogger.log(Level.FINEST, msg);
    //System.out.println(tag + ": " + msg);
  }

  public final void trace(String msg)
  {
    Log.v(default_tag, msg);
    //JMyLogger.log(Level.INFO, msg);
    //System.out.println(tag + ": " + msg);
  }

//  public final void trace(String msg, Exception e)
//  {
//    Log.v(default_tag, msg);
//    e.printStackTrace();
//    //JMyLogger.log(Level.INFO, msg);
//    //System.out.println(tag + ": " + msg);
//  }

  public final void trace(String msg, Throwable e)
  {
    Log.v(default_tag, msg);
    e.printStackTrace();
    //JMyLogger.log(Level.INFO, msg);
    //System.out.println(tag + ": " + msg);
  }

  public final boolean isTraceEnabled()
  {
    return true;
    //JMyLogger.log(Level.INFO, msg);
    //System.out.println(tag + ": " + msg);
  }


  public final void d(String tag, String msg)
  {
    Log.d(tag, msg);
    //JMyLogger.log(Level.FINEST, msg);
    //System.out.println(tag + ": " + msg);
  }

  public final void debug(String msg)
  {
    Log.d(default_tag, msg);
    UILog(DEBUG, default_tag, msg);
    //JMyLogger.log(Level.INFO, msg);
    //System.out.println(tag + ": " + msg);
  }

  public final void debug(StringBuilder msg)
  {
    Log.d(default_tag, msg.toString());
    UILog(DEBUG, default_tag, msg.toString());
    //JMyLogger.log(Level.INFO, msg);
    //System.out.println(tag + ": " + msg);
  }

//  public final void debug(String msg, Exception e)
//  {
//    Log.d(default_tag, msg);
//    UILog(DEBUG, default_tag, msg);
//    e.printStackTrace();
//    UILog(DEBUG, e);
//    //JMyLogger.log(Level.INFO, msg);
//    //System.out.println(tag + ": " + msg);
//  }

  public final void debug(Throwable e)
  {
    e.printStackTrace();
    UILog(DEBUG, e);
    //JMyLogger.log(Level.INFO, msg);
    //System.out.println(tag + ": " + msg);
  }

  public final void debug(String msg, Throwable e)
  {
    Log.d(default_tag, msg);
    UILog(DEBUG, default_tag, msg);
    e.printStackTrace();
    UILog(DEBUG, e);
    //JMyLogger.log(Level.INFO, msg);
    //System.out.println(tag + ": " + msg);
  }

  public final boolean isDebugEnabled()
  {
    return true;
    //JMyLogger.log(Level.INFO, msg);
    //System.out.println(tag + ": " + msg);
  }

  public final void i(String tag, String msg)
  {
    Log.i(tag, msg);
    //JMyLogger.log(Level.INFO, msg);
    //System.out.println(tag + ": " + msg);
  }

  public final void info(String msg)
  {
    Log.i(default_tag, msg);
    UILog(INFO, default_tag, msg);
    //JMyLogger.log(Level.INFO, msg);
    //System.out.println(tag + ": " + msg);
  }
  public final void info(StringBuilder msg)
  {
    Log.i(default_tag, msg.toString());
    UILog(INFO, default_tag, msg.toString());
    //JMyLogger.log(Level.WARNING, msg);
    //System.err.println(tag + ": " + msg);
  }

//  public final void info(String msg, Exception e)
//  {
//    Log.i(default_tag, msg);
//    e.printStackTrace();
//    UILog(INFO, default_tag, msg.toString());
//    UILog(INFO, e);
//
//
//    //JMyLogger.log(Level.INFO, msg);
//    //System.out.println(tag + ": " + msg);
//  }

  public final void info(Throwable e)
  {
    e.printStackTrace();
    UILog(INFO, e);

    //JMyLogger.log(Level.INFO, msg);
    //System.out.println(tag + ": " + msg);
  }

  public final void info(String msg, Throwable e)
  {
    Log.i(default_tag, msg);
    e.printStackTrace();
    UILog(INFO, default_tag, msg);
    UILog(INFO, e);

    //JMyLogger.log(Level.INFO, msg);
    //System.out.println(tag + ": " + msg);
  }

  public final boolean isInfoEnabled()
  {
    return true;
    //JMyLogger.log(Level.INFO, msg);
    //System.out.println(tag + ": " + msg);
  }

  public final void e(String tag, String msg)
  {
    Log.e(tag, msg);

    //JMyLogger.log(Level.SEVERE, msg);
    //System.err.println(tag + ": " + msg);
  }

  public final void error(String msg)
  {
    Log.e(default_tag, msg);
    UILog(ERROR, default_tag, msg);

    //JMyLogger.log(Level.SEVERE, msg);
    //System.err.println(tag + ": " + msg);
  }

  public final void error(StringBuilder msg)
  {
    Log.e(default_tag, msg.toString());
    UILog(ERROR, default_tag, msg.toString());
    //JMyLogger.log(Level.WARNING, msg);
    //System.err.println(tag + ": " + msg);
  }

//  public final void error(String msg, Exception e)
//  {
//    Log.e(default_tag, msg);
//    UILog(ERROR, default_tag, msg);
//    e.printStackTrace();
//    UILog(ERROR, e);
//    //JMyLogger.log(Level.INFO, msg);
//    //System.out.println(tag + ": " + msg);
//  }

  public final void error(Throwable e)
  {
    e.printStackTrace();
    UILog(ERROR, e);
    //JMyLogger.log(Level.INFO, msg);
    //System.out.println(tag + ": " + msg);
  }

  public final void error(String msg, Throwable e)
  {
    Log.e(default_tag, msg);
    UILog(ERROR, default_tag, msg);
    e.printStackTrace();
    UILog(ERROR, e);
    //JMyLogger.log(Level.INFO, msg);
    //System.out.println(tag + ": " + msg);
  }

  public final void fatal(String msg)
  {
    Log.e(default_tag, msg);
    UILog(ERROR, default_tag, msg);
    //JMyLogger.log(Level.SEVERE, msg);
    //System.err.println(tag + ": " + msg);
  }

  public final void fatal(StringBuilder msg)
  {
    Log.e(default_tag, msg.toString());
    UILog(ERROR, default_tag, msg.toString());
    //JMyLogger.log(Level.WARNING, msg);
    //System.err.println(tag + ": " + msg);
  }

//  public final void fatal(String msg, Exception e)
//  {
//    Log.e(default_tag, msg);
//    UILog(ERROR, default_tag, msg);
//    e.printStackTrace();
//    UILog(ERROR, e);
//    //JMyLogger.log(Level.INFO, msg);
//    //System.out.println(tag + ": " + msg);
//  }

  public final void fatal( Throwable e)
  {
    e.printStackTrace();
    UILog(ERROR, e);
    //JMyLogger.log(Level.INFO, msg);
    //System.out.println(tag + ": " + msg);
  }

  public final void fatal(String msg, Throwable e)
  {
    Log.e(default_tag, msg);
    UILog(ERROR, default_tag, msg);
    e.printStackTrace();
    UILog(ERROR, e);
    //JMyLogger.log(Level.INFO, msg);
    //System.out.println(tag + ": " + msg);
  }


  public final void w(String tag, String msg)
  {
    Log.w(tag, msg);
    //JMyLogger.log(Level.WARNING, msg);
    //System.err.println(tag + ": " + msg);
  }

  public final void warn(String msg)
  {
    Log.w(default_tag, msg);
    UILog(WARN, default_tag, msg);
    //JMyLogger.log(Level.WARNING, msg);
    //System.err.println(tag + ": " + msg);
  }
  public final void warn(StringBuilder msg)
  {
    Log.w(default_tag, msg.toString());
    UILog(WARN, default_tag, msg.toString());
    //JMyLogger.log(Level.WARNING, msg);
    //System.err.println(tag + ": " + msg);
  }

//  public final void warn(String msg, Exception e)
//  {
//    Log.w(default_tag, msg);
//    UILog(WARN, default_tag, msg);
//    e.printStackTrace();
//    UILog(WARN, e);
//    //JMyLogger.log(Level.INFO, msg);
//    //System.out.println(tag + ": " + msg);
//  }

  public final void warn(Throwable e)
  {
    e.printStackTrace();
    UILog(WARN, e);
    //JMyLogger.log(Level.INFO, msg);
    //System.out.println(tag + ": " + msg);
  }

  public final void warn(String msg, Throwable e)
  {
    Log.w(default_tag, msg);
    UILog(WARN, default_tag, msg);
    e.printStackTrace();
    UILog(WARN, e);
    //JMyLogger.log(Level.INFO, msg);
    //System.out.println(tag + ": " + msg);
  }

}
