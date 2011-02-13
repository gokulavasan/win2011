import java.io.*;
import java.util.*;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.mapreduce.lib.output.*;
import org.apache.hadoop.util.*;

public class Ngram extends Configured implements Tool {

   public static class Map
       extends Mapper<LongWritable, Text, Text, IntWritable> {

     static enum Counters { INPUT_WORDS }

     private final static IntWritable one = new IntWritable(1);
     private Text word = new Text();

     private int ngram_num = 0;
     private Set<String> queryNgram = new HashSet<String>();

     private long numRecords = 0;
     private String inputFile;

     public void setup(Context context) {
       Configuration conf = context.getConfiguration();
       ngram_num = conf.getInt("ngram.num", 0);
       inputFile = conf.get("mapreduce.map.input.file");

       Path[] queryFiles = new Path[0];
       try {
         queryFiles = DistributedCache.getLocalCacheFiles(conf);
       } catch (IOException ioe) {
         System.err.println("Caught exception while getting cached files: "
             + StringUtils.stringifyException(ioe));
       }
       for (Path queryFile : queryFiles) {
         parseQueryFile(queryFile);
       }
     }

     private void parseQueryFile(Path queryFile) {
       try {
         BufferedReader fis = new BufferedReader(new FileReader(
             queryFile.toString()));
         String pattern = null;
         while ((pattern = fis.readLine()) != null) {
           queryNgram.add(pattern);
         }
       } catch (IOException ioe) {
         System.err.println("Caught exception while parsing the cached file '"
             + queryFile + "' : " + StringUtils.stringifyException(ioe));
       }
     }

     public void map(LongWritable key, Text value, Context context)
         throws IOException, InterruptedException {
       String line = value.toString();

       StringTokenizer tokenizer = new StringTokenizer(line);
       while (tokenizer.hasMoreTokens()) {
         word.set(tokenizer.nextToken());
         context.write(word, one);
         context.getCounter(Counters.INPUT_WORDS).increment(1);
       }

       if ((++numRecords % 100) == 0) {
         context.setStatus("Finished processing " + numRecords
             + " records " + "from the input file: " + inputFile);
       }
     }
   }

   public static class Reduce
       extends Reducer<Text, IntWritable, Text, IntWritable> {
     public void reduce(Text key, Iterable<IntWritable> values,
         Context context) throws IOException, InterruptedException {

       int sum = 0;
       for (IntWritable val : values) {
         sum += val.get();
       }
       context.write(key, new IntWritable(sum));
     }
   }

public static class NonSplittableTextInputFormat
    extends TextInputFormat {
  protected boolean isSplitable(org.apache.hadoop.fs.FileSystem fs, org.apache.hadoop.fs.Path filename) { return false; }
}

   public int run(String[] args) throws Exception {
     //Args format: <#Ngram> <query_file> <input_dir> <output_dir>
     Job job = new Job(getConf());
     job.setJarByClass(Ngram.class);
     job.setJobName("Ngram");

     job.setOutputKeyClass(Text.class);
     job.setOutputValueClass(IntWritable.class);

     job.setMapperClass(Map.class);
     job.setCombinerClass(Reduce.class);
     job.setReducerClass(Reduce.class);

     // Note that these are the default.
     job.setInputFormatClass(NonSplittableTextInputFormat.class);
     job.setOutputFormatClass(TextOutputFormat.class);

     int ngram_num = Integer.parseInt(args[0]);
     job.getConfiguration().setInt("ngram.num", ngram_num);
     
     DistributedCache.addCacheFile(new Path(args[1]).toUri(),
             job.getConfiguration());

     FileInputFormat.setInputPaths(job, new Path(args[2]));
     FileOutputFormat.setOutputPath(job, new Path(args[3]));

     boolean success = job.waitForCompletion(true);
     return success ? 0 : 1;
   }

   public static void main(String[] args) throws Exception {
     int res = ToolRunner.run(new Configuration(), new Ngram(), args);
     System.exit(res);
   }
}