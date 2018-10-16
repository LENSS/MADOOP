

package edu.tamu.lenss.util;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FsShell;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import edu.tamu.lenss.util.ILogger;
import edu.tamu.lenss.util.LogFactory;

public class DFSFiles
{

  private static final ILogger LOG = LogFactory.getLog(DFSFiles.class);


  public static void copyfromDFS(String dfs_dir, String local_dir) throws Exception
  {

    LOG.info("dfs_dir: " + dfs_dir);
    LOG.info("local_dir: " + local_dir);
//    if (!CheckExistence(dfs_input)) {
    String argv [] = {"-get", dfs_dir, local_dir};
    FsShell.main(argv);
//    }

  }



  public static void upload2DFS(String local_input, String dfs_input) throws Exception
  {

      LOG.info("local_input: " + local_input);
//    if (!CheckExistence(dfs_input)) {
      String argv [] = {"-put", local_input, dfs_input};
      FsShell.main(argv);
//    }

  }


  public static void RemoveDFSOutput(String dfs_output) throws Exception
  {

    if (CheckExistence(dfs_output)) {
      String argv [] = {"-rmr", dfs_output};
      FsShell.main(argv);
    }

  }

  public static void RemoveDFSInput(String dfs_input) throws Exception
  {

    if (CheckExistence(dfs_input)) {
      String argv [] = {"-rmr", dfs_input};
      FsShell.main(argv);
    }

  }

  public static boolean CheckExistence(String dfs_input) throws Exception
  {

    String argv [] = {"-test", "-e", dfs_input};
    int res = FsShell.main(argv);
    return res==0?true:false;

  }
}

