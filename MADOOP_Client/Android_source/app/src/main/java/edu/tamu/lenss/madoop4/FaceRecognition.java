
package edu.tamu.lenss.madoop4;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import edu.tamu.lenss.util.DFSFiles;
import edu.tamu.lenss.util.ILogger;
import edu.tamu.lenss.util.LogFactory;

public class FaceRecognition
{
//  private static final String TAG = FaceRecognition.class.getSimpleName();

//    private static final MyLogger LOG = MyLogger.getLogger(TAG);
  private static final ILogger LOG = LogFactory.getLog(FaceRecognition.class);
  private static Configuration conf = new Configuration();

  public static class TokenizerMapper
    extends Mapper<Object, Text, Text, IntWritable> implements MadoopConstants
  {

    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();

    public void map(Object key, Text value, Context context
                   ) throws IOException, InterruptedException
    {

//      try {
//        DFSFiles.copyfromDFS("faces" + File.separator + value.toString(),
//                madoopDocuments + File.separator + "temp" + File.separator + value.toString());
//      } catch (Exception e) {
//        LOG.error(e);
//      }
      FileSystem dfs = FileSystem.get(conf);
      Path src = new Path("faces" + File.separator + value.toString());
      Path dst = new Path(madoopDocuments + File.separator + "temp" + File.separator + value.toString());
      dfs.copyToLocalFile(src, dst);
      
      String temp = OpenCVFaceRecognizer.predictFromModel2(madoopDocuments + File.separator + "facemodel.xml", madoopDocuments + File.separator + "temp" + File.separator + value.toString());
      LOG.info(value.toString() + " is predicted as: " + temp);
      word.set(value.toString() + ":" + temp);
      context.write(word, one);
    }
  }

  public static class IntSumReducer
    extends Reducer<Text,IntWritable,Text,IntWritable>
  {
    private IntWritable result = new IntWritable();

    public void reduce(Text key, Iterable<IntWritable> values,
                       Context context
                      ) throws IOException, InterruptedException
    {
      int sum = 0;
      for (IntWritable val : values) {
        sum += val.get();
      }
      result.set(sum);
      context.write(key, result);
    }
  }


  public static Boolean main(Context _ctx, String input_dir, String output_dir) throws Exception {

//    conf = new Configuration();
//    try
//    {
      Job job = new Job(conf, "face recognition");
      job.ctx = _ctx;
      //        LOG.info(job.getLocalPath().toString());
      job.setJarByClass(FaceRecognition.class);
      job.setMapperClass(FaceRecognition.TokenizerMapper.class);
//      job.setCombinerClass(FaceRecognition.IntSumReducer.class);
      job.setReducerClass(FaceRecognition.IntSumReducer.class);
      job.setOutputKeyClass(Text.class);
      job.setOutputValueClass(IntWritable.class);


      FileInputFormat.addInputPath(job, new Path(input_dir));
      FileOutputFormat.setOutputPath(job, new Path(output_dir));
      return job.waitForCompletion(true);

//    } catch (Exception e)
//    {
//      LOG.error(e);
//    }

  }

}

