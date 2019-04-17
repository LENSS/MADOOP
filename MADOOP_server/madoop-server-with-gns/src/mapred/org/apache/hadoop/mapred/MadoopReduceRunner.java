/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.mapred;

import java.io.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.hadoop.mapred.TaskTracker.TaskInProgress;

import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.filecache.*;
import org.apache.hadoop.util.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.net.URI;





/** Runs a map task. */
class MadoopReduceRunner extends TaskRunner {
  private static final Log LOG = LogFactory.getLog(MadoopReduceRunner.class);

  public MadoopReduceRunner(TaskInProgress task, TaskTracker tracker, JobConf conf) {
    super(task, tracker, conf);
  }




  
  @Override
  public void run() {
    try {
      
      //before preparing the job localize 
      //all the archives
      TaskAttemptID taskid = t.getTaskID();
      LocalDirAllocator lDirAlloc = new LocalDirAllocator("mapred.local.dir");
      File jobCacheDir = null;
      if (conf.getJar() != null) {
        jobCacheDir = new File(
                          new Path(conf.getJar()).getParent().toString());
      }
      File workDir = new File(lDirAlloc.getLocalPathToRead(
                                TaskTracker.getLocalTaskDir( 
                                  t.getJobID().toString(), 
                                  t.getTaskID().toString(),
                                  t.isTaskCleanupTask())
                                + Path.SEPARATOR + MRConstants.WORKDIR,
                                conf). toString());

      URI[] archives = DistributedCache.getCacheArchives(conf);
      URI[] files = DistributedCache.getCacheFiles(conf);
      FileStatus fileStatus;
      FileSystem fileSystem;
      Path localPath;
      String baseDir;

      if ((archives != null) || (files != null)) {
        if (archives != null) {
          String[] archivesTimestamps = 
                               DistributedCache.getArchiveTimestamps(conf);
          Path[] p = new Path[archives.length];
          for (int i = 0; i < archives.length;i++){
            fileSystem = FileSystem.get(archives[i], conf);
            fileStatus = fileSystem.getFileStatus(
                                      new Path(archives[i].getPath()));
            String cacheId = DistributedCache.makeRelative(archives[i],conf);
            String cachePath = TaskTracker.getCacheSubdir() + 
                                 Path.SEPARATOR + cacheId;
            
            localPath = lDirAlloc.getLocalPathForWrite(cachePath,
                                      fileStatus.getLen(), conf);
            baseDir = localPath.toString().replace(cacheId, "");
            p[i] = DistributedCache.getLocalCache(archives[i], conf, 
                                                  new Path(baseDir),
                                                  fileStatus,
                                                  true, Long.parseLong(
                                                        archivesTimestamps[i]),
                                                  new Path(workDir.
                                                        getAbsolutePath()), 
                                                  false);
            
          }
          DistributedCache.setLocalArchives(conf, stringifyPathArray(p));
        }
        if ((files != null)) {
          String[] fileTimestamps = DistributedCache.getFileTimestamps(conf);
          Path[] p = new Path[files.length];
          for (int i = 0; i < files.length;i++){
            fileSystem = FileSystem.get(files[i], conf);
            fileStatus = fileSystem.getFileStatus(
                                      new Path(files[i].getPath()));
            String cacheId = DistributedCache.makeRelative(files[i], conf);
            String cachePath = TaskTracker.getCacheSubdir() +
                                 Path.SEPARATOR + cacheId;
            
            localPath = lDirAlloc.getLocalPathForWrite(cachePath,
                                      fileStatus.getLen(), conf);
            baseDir = localPath.toString().replace(cacheId, "");
            p[i] = DistributedCache.getLocalCache(files[i], conf, 
                                                  new Path(baseDir),
                                                  fileStatus,
                                                  false, Long.parseLong(
                                                           fileTimestamps[i]),
                                                  new Path(workDir.
                                                        getAbsolutePath()), 
                                                  false);
          }
          DistributedCache.setLocalFiles(conf, stringifyPathArray(p));
        }
        Path localTaskFile = new Path(t.getJobFile());
        FileSystem localFs = FileSystem.getLocal(conf);
        localFs.delete(localTaskFile, true);
        OutputStream out = localFs.create(localTaskFile);
        try {
          conf.writeXml(out);
        } finally {
          out.close();
        }
      }
          
      if (!prepare()) {
        return;
      }

      String sep = System.getProperty("path.separator");
      StringBuffer classPath = new StringBuffer();
      // start with same classpath as parent process
      classPath.append(System.getProperty("java.class.path"));
      classPath.append(sep);
      if (!workDir.mkdirs()) {
        if (!workDir.isDirectory()) {
          LOG.fatal("Mkdirs failed to create " + workDir.toString());
        }
      }
    
      String jar = conf.getJar();
      if (jar != null) {       
        // if jar exists, it into workDir
        File[] libs = new File(jobCacheDir, "lib").listFiles();
        if (libs != null) {
          for (int i = 0; i < libs.length; i++) {
            classPath.append(sep);            // add libs from jar to classpath
            classPath.append(libs[i]);
          }
        }
        classPath.append(sep);
        classPath.append(new File(jobCacheDir, "classes"));
        classPath.append(sep);
        classPath.append(jobCacheDir);
       
      }

      // include the user specified classpath
      
      //archive paths
      Path[] archiveClasspaths = DistributedCache.getArchiveClassPaths(conf);
      if (archiveClasspaths != null && archives != null) {
        Path[] localArchives = DistributedCache
          .getLocalCacheArchives(conf);
        if (localArchives != null){
          for (int i=0;i<archives.length;i++){
            for(int j=0;j<archiveClasspaths.length;j++){
              if (archives[i].getPath().equals(
                                               archiveClasspaths[j].toString())){
                classPath.append(sep);
                classPath.append(localArchives[i]
                                 .toString());
              }
            }
          }
        }
      }
      //file paths
      Path[] fileClasspaths = DistributedCache.getFileClassPaths(conf);
      if (fileClasspaths!=null && files != null) {
        Path[] localFiles = DistributedCache
          .getLocalCacheFiles(conf);
        if (localFiles != null) {
          for (int i = 0; i < files.length; i++) {
            for (int j = 0; j < fileClasspaths.length; j++) {
              if (files[i].getPath().equals(
                                            fileClasspaths[j].toString())) {
                classPath.append(sep);
                classPath.append(localFiles[i].toString());
              }
            }
          }
        }
      }

      classPath.append(sep);
      classPath.append(workDir);
      //  Build exec child jmv args.
      Vector<String> vargs = new Vector<String>(8);
      File jvm =                                  // use same jvm as parent
        new File(new File(System.getProperty("java.home"), "bin"), "java");

      vargs.add(jvm.toString());

      // Add child (task) java-vm options.
      //
      // The following symbols if present in mapred.child.java.opts value are
      // replaced:
      // + @taskid@ is interpolated with value of TaskID.
      // Other occurrences of @ will not be altered.
      //
      // Example with multiple arguments and substitutions, showing
      // jvm GC logging, and start of a passwordless JVM JMX agent so can
      // connect with jconsole and the likes to watch child memory, threads
      // and get thread dumps.
      //
      //  <property>
      //    <name>mapred.child.java.opts</name>
      //    <value>-verbose:gc -Xloggc:/tmp/@taskid@.gc \
      //           -Dcom.sun.management.jmxremote.authenticate=false \
      //           -Dcom.sun.management.jmxremote.ssl=false \
      //    </value>
      //  </property>
      //
      String javaOpts = conf.get("mapred.child.java.opts", "-Xmx200m");
      javaOpts = javaOpts.replace("@taskid@", taskid.toString());
      String [] javaOptsSplit = javaOpts.split(" ");
      
      // Add java.library.path; necessary for loading native libraries.
      //
      // 1. To support native-hadoop library i.e. libhadoop.so, we add the 
      //    parent processes' java.library.path to the child. 
      // 2. We also add the 'cwd' of the task to it's java.library.path to help 
      //    users distribute native libraries via the DistributedCache.
      // 3. The user can also specify extra paths to be added to the 
      //    java.library.path via mapred.child.java.opts.
      //
      String libraryPath = System.getProperty("java.library.path");
      if (libraryPath == null) {
        libraryPath = workDir.getAbsolutePath();
      } else {
        libraryPath += sep + workDir;
      }
      boolean hasUserLDPath = false;
      for(int i=0; i<javaOptsSplit.length ;i++) { 
        if(javaOptsSplit[i].startsWith("-Djava.library.path=")) {
          javaOptsSplit[i] += sep + libraryPath;
          hasUserLDPath = true;
          break;
        }
      }
      if(!hasUserLDPath) {
        vargs.add("-Djava.library.path=" + libraryPath);
      }
      for (int i = 0; i < javaOptsSplit.length; i++) {
        vargs.add(javaOptsSplit[i]);
      }

      // add java.io.tmpdir given by mapred.child.tmp
      String tmp = conf.get("mapred.child.tmp", "./tmp");
      Path tmpDir = new Path(tmp);
      
      // if temp directory path is not absolute 
      // prepend it with workDir.
      if (!tmpDir.isAbsolute()) {
        tmpDir = new Path(workDir.toString(), tmp);
      }
      FileSystem localFs = FileSystem.getLocal(conf);
      if (!localFs.mkdirs(tmpDir) && !localFs.getFileStatus(tmpDir).isDir()) {
        throw new IOException("Mkdirs failed to create " + tmpDir.toString());
      }
      vargs.add("-Djava.io.tmpdir=" + tmpDir.toString());

      // Add classpath.
      vargs.add("-classpath");
      vargs.add(classPath.toString());

      // Setup the log4j prop
      long logSize = TaskLog.getTaskLogLength(conf);
      vargs.add("-Dhadoop.log.dir=" + 
          new File(System.getProperty("hadoop.log.dir")
          ).getAbsolutePath());
      vargs.add("-Dhadoop.root.logger=INFO,TLA");
      vargs.add("-Dhadoop.tasklog.taskid=" + taskid);
      vargs.add("-Dhadoop.tasklog.totalLogFileSize=" + logSize);

      if (conf.getProfileEnabled()) {
        if (conf.getProfileTaskRange(t.isMapTask()
                                     ).isIncluded(t.getPartition())) {
          File prof = TaskLog.getTaskLogFile(taskid, TaskLog.LogName.PROFILE);
          vargs.add(String.format(conf.getProfileParams(), prof.toString()));
        }
      }

      // Add main class and its arguments 
      vargs.add(Child.class.getName());  // main of Child
      // pass umbilical address
      InetSocketAddress address = tracker.getTaskTrackerReportAddress();
      vargs.add(address.getAddress().getHostAddress()); 
      vargs.add(Integer.toString(address.getPort())); 
      vargs.add(taskid.toString());                      // pass task identifier

//      LOG.info("===print vargs begins===");
//      for (String s : vargs) {
//        LOG.info(s);
//      }
//      LOG.info("===print vargs ends=====");

      String pidFile = lDirAlloc.getLocalPathForWrite(
            (TaskTracker.getPidFile(t.getJobID().toString(), 
             taskid.toString(), t.isTaskCleanupTask())),
            this.conf).toString();
      t.setPidFile(pidFile);
      tracker.addToMemoryManager(t.getTaskID(), t.isMapTask(), conf, pidFile);

      // set memory limit using ulimit if feasible and necessary ...
      String[] ulimitCmd = Shell.getUlimitMemoryCommand(conf);
      List<String> setup = null;
      if (ulimitCmd != null) {
        setup = new ArrayList<String>();
        for (String arg : ulimitCmd) {
          setup.add(arg);
        }
      }

      // Set up the redirection of the task's stdout and stderr streams
      File stdout = TaskLog.getTaskLogFile(taskid, TaskLog.LogName.STDOUT);
      File stderr = TaskLog.getTaskLogFile(taskid, TaskLog.LogName.STDERR);
      stdout.getParentFile().mkdirs();
      tracker.getTaskTrackerInstrumentation().reportTaskLaunch(taskid, stdout, stderr);

      Map<String, String> env = new HashMap<String, String>();
      StringBuffer ldLibraryPath = new StringBuffer();
      ldLibraryPath.append(workDir.toString());
      String oldLdLibraryPath = null;
      oldLdLibraryPath = System.getenv("LD_LIBRARY_PATH");
      if (oldLdLibraryPath != null) {
        ldLibraryPath.append(sep);
        ldLibraryPath.append(oldLdLibraryPath);
      }
      env.put("LD_LIBRARY_PATH", ldLibraryPath.toString());
//      jvmManager.launchJvm(this, 
//          jvmManager.constructJvmEnv(setup,vargs,stdout,stderr,logSize, 
//              workDir, env, pidFile, conf));

      Child.innerChild(this, workDir);
      synchronized (lock) {
        while (!done) {
          lock.wait();
        }
      }
      tracker.getTaskTrackerInstrumentation().reportTaskEnd(t.getTaskID());
      if (exitCodeSet) {
        if (!killed && exitCode != 0) {
          if (exitCode == 65) {
            tracker.getTaskTrackerInstrumentation().taskFailedPing(t.getTaskID());
          }
          throw new IOException("Task process exit with nonzero status of " +
              exitCode + ".");
        }
      }
    } catch (FSError e) {
      LOG.fatal("FSError", e);
      try {
        tracker.fsError(t.getTaskID(), e.getMessage());
      } catch (IOException ie) {
        LOG.fatal(t.getTaskID()+" reporting FSError", ie);
      }
    } catch (Throwable throwable) {
      LOG.warn(t.getTaskID()+" Child Error", throwable);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      throwable.printStackTrace(new PrintStream(baos));
      try {
        tracker.reportDiagnosticInfo(t.getTaskID(), baos.toString());
      } catch (IOException e) {
        LOG.warn(t.getTaskID()+" Reporting Diagnostics", e);
      }
    } finally {
      try{
        URI[] archives = DistributedCache.getCacheArchives(conf);
        URI[] files = DistributedCache.getCacheFiles(conf);
        if (archives != null){
          for (int i = 0; i < archives.length; i++){
            DistributedCache.releaseCache(archives[i], conf);
          }
        }
        if (files != null){
          for(int i = 0; i < files.length; i++){
            DistributedCache.releaseCache(files[i], conf);
          }
        }
      }catch(IOException ie){
        LOG.warn("Error releasing caches : Cache files might not have been cleaned up");
      }
      tip.reportTaskFinished();
    }
  }


  
  /** Delete any temporary files from previous failed attempts. */
  public boolean prepare() throws IOException {
    if (!super.prepare()) {
      return false;
    }
    
    mapOutputFile.removeAll(getTask().getTaskID());
    return true;
  }

  /** Delete all of the temporary map output files. */
  public void close() throws IOException {
    LOG.info(getTask()+" done; removing files.");
    mapOutputFile.removeAll(getTask().getTaskID());
  }
}


