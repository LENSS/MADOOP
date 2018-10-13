package edu.tamu.lenss.util;

import android.util.Log;

public abstract class ILogger
{
    public abstract void v(String tag, String msg);
    public abstract void trace(String msg);
    public abstract void trace(String msg, Throwable e);
    public abstract boolean isTraceEnabled();

    public abstract void d(String tag, String msg);
    public abstract void debug(String msg);
    public abstract void debug(StringBuilder msg);
    public abstract void debug(Throwable e);
    public abstract  void debug(String msg, Throwable e);
    public abstract  boolean isDebugEnabled();

    public abstract  void i(String tag, String msg);
    public abstract  void info(String msg);
    public abstract  void info(StringBuilder msg);
    public abstract  void info(Throwable e);
    public abstract  void info(String msg, Throwable e);
    public abstract  boolean isInfoEnabled();

    public abstract  void e(String tag, String msg);
    public abstract  void error(String msg);
    public abstract  void error(StringBuilder msg);
    public abstract  void error(Throwable e);
    public abstract  void error(String msg, Throwable e);

    public abstract  void fatal(String msg);
    public abstract  void fatal(StringBuilder msg);
    public abstract  void fatal( Throwable e);
    public abstract  void fatal(String msg, Throwable e);

    public abstract  void w(String tag, String msg);
    public abstract  void warn(String msg);
    public abstract  void warn(StringBuilder msg);
    public abstract  void warn(Throwable e);
    public abstract  void warn(String msg, Throwable e);
    public static int getlinenumber()
    {

//        Log.i("dfds", new Exception().getStackTrace().toString());
//        new Exception().printStackTrace();
//        Log.i("dfds", "************ see?");


        return new Exception().getStackTrace()[2].getLineNumber();

    }
}
