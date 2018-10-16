
package edu.tamu.lenss.madoop4;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class WordCount
{

  public static class TokenizerMapper
    extends Mapper<Object, Text, Text, IntWritable>
  {

    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();

    public void map(Object key, Text value, Context context
                   ) throws IOException, InterruptedException
    {
      StringTokenizer itr = new StringTokenizer(value.toString());
      while (itr.hasMoreTokens()) {
        word.set(itr.nextToken());
        context.write(word, one);
      }
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


  public static Boolean main(String input_dir, String output_dir) throws Exception {

    Configuration conf = new Configuration();
//    try
//    {
      Job job = new Job(conf, "word count");
      //        LOG.info(job.getLocalPath().toString());
      job.setJarByClass(WordCount.class);
      job.setMapperClass(WordCount.TokenizerMapper.class);
      job.setCombinerClass(WordCount.IntSumReducer.class);
      job.setReducerClass(WordCount.IntSumReducer.class);
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
