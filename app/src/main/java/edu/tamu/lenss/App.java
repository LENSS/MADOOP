package edu.tamu.lenss;


import android.content.Context;
import android.app.Application;


public class App extends Application
{
  private static App mApp = null;

  /* (non-Javadoc)
   * @see android.app.Application#onCreate()
   */
  @Override
  public void onCreate()
  {
    super.onCreate();
    mApp = this;
  }
  public static Context get_context()
  {
    return mApp.getApplicationContext();
  }
}

