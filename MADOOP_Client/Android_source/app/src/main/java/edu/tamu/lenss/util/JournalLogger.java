package edu.tamu.lenss.util;

import android.os.Environment;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;

import de.mindpipe.android.logging.log4j.LogConfigurator;

/**
 * Created by root on 2/17/2018.
 */

public class JournalLogger extends ILogger
{
    private org.apache.log4j.Logger thelogger;

    private void _getLogger(Class clazz, String file) {
        final LogConfigurator logConfigurator = new LogConfigurator();
        logConfigurator.setFileName(Environment.getExternalStorageDirectory().toString() + File.separator+"distressnet"+File.separator + "Madoop4/log/" + file);
        logConfigurator.setRootLevel(Level.INFO);
//        logConfigurator.setLevel("edu.tamu.lenss", Level.INFO);
        logConfigurator.setUseFileAppender(true);
        logConfigurator.setFilePattern("%d %-5p [%c{2}]%m%n");
        logConfigurator.setMaxFileSize(1024 * 1024 * 5);
        logConfigurator.setImmediateFlush(true);
        logConfigurator.configure();
        thelogger = Logger.getLogger(clazz);
    }

    JournalLogger(Class cls, String file)
    {
        _getLogger(cls, file);
    }
    /** Gets a MyLogger and, as a side effect, installs this as the default
     * formatter. */
    public static JournalLogger getLogger(Class cls, String file)
    {
        // just referencing this class installs it
        return new JournalLogger(cls, file);
    }

    public static JournalLogger getLogger(String name, String file)
    {
        // just referencing this class installs it
//        return new JournalLogger(cls);
        try {
            Class<?> cls = Class.forName(name);
            return new JournalLogger(cls, file);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public  final void v(String tag, String msg)
    {
        thelogger.trace(tag + " " + msg);
    }

    public final void trace(String msg)
    {
        thelogger.trace(msg);
    }

    public final void trace(String msg, Throwable e)
    {
        thelogger.trace(msg, e);
    }

    public final boolean isTraceEnabled()
    {
        return thelogger.isTraceEnabled();
    }


    public  final void d(String tag, String msg)
    {
        thelogger.debug(tag + " " + msg);
    }

    public final void debug(String msg)
    {
        thelogger.debug(msg);
    }

    public final void debug(StringBuilder msg)
    {
        thelogger.debug(msg.toString());
    }

    public final void debug(Throwable e)
    {
        thelogger.debug("", e);
    }

    public final void debug(String msg, Throwable e)
    {
        thelogger.debug(msg, e);
    }

    public final boolean isDebugEnabled()
    {
        return thelogger.isDebugEnabled();
    }

    public  final void i(String tag, String msg)
    {
        thelogger.info(tag + " " + msg);
    }

    public final void info(String msg)
    {
        thelogger.info("-[" + ILogger.getlinenumber() + "]: " + msg);
    }
    public final void info(StringBuilder msg)
    {
        thelogger.info("-[" + ILogger.getlinenumber() + "]: " + msg.toString());
    }

    public final void info(Throwable e)
    {
        thelogger.info("-[" + ILogger.getlinenumber() + "]: ", e);
    }

    public final void info(String msg, Throwable e)
    {
        thelogger.info("-[" + ILogger.getlinenumber() + "]: " + msg, e);
    }

    public final boolean isInfoEnabled()
    {
        return thelogger.isInfoEnabled();
    }

    public  final void e(String tag, String msg)
    {
        thelogger.error(tag + "-[" + ILogger.getlinenumber() + "]: " +  msg);
    }

    public final void error(String msg)
    {
        thelogger.error("-[" + ILogger.getlinenumber() + "]: " + msg);
    }

    public final void error(StringBuilder msg)
    {
        thelogger.error("-[" + ILogger.getlinenumber() + "]: " + msg.toString());
    }

    public final void error(Throwable e)
    {
        thelogger.error("-[" + ILogger.getlinenumber() + "]: ", e);
    }

    public final void error(String msg, Throwable e)
    {
        thelogger.error("-[" + ILogger.getlinenumber() + "]: " + msg, e);
    }

    public final void fatal(String msg)
    {
        thelogger.error("-[" + ILogger.getlinenumber() + "]: " + msg);
    }

    public final void fatal(StringBuilder msg)
    {
        thelogger.error("-[" + ILogger.getlinenumber() + "]: " + msg.toString());
    }

    public final void fatal( Throwable e)
    {
        thelogger.error("-[" + ILogger.getlinenumber() + "]: ", e);
    }

    public final void fatal(String msg, Throwable e)
    {
        thelogger.error("-[" + ILogger.getlinenumber() + "]: " + msg, e);
    }


    public  final void w(String tag, String msg)
    {
        thelogger.warn(tag + "-[" + ILogger.getlinenumber() + "]: " +  msg);
    }

    public final void warn(String msg)
    {
        thelogger.warn("-[" + ILogger.getlinenumber() + "]: " + msg);
    }
    public final void warn(StringBuilder msg)
    {
        thelogger.warn("-[" + ILogger.getlinenumber() + "]: " + msg.toString());
    }


    public final void warn(Throwable e)
    {
        thelogger.warn("-[" + ILogger.getlinenumber() + "]: ", e);
    }

    public final void warn(String msg, Throwable e)
    {
        thelogger.warn("-[" + ILogger.getlinenumber() + "]: " + msg, e);
    }

}
