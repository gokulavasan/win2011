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

     private boolean caseSensitive = true;
     private Set<String> patternsToSkip = new HashSet<String>();

     private long numRecords = 0;
     private String inputFile;

     public void setup(Context context) {
       Configuration conf = context.getConfiguration();
       caseSensitive = conf.getBoolean("wordcount.case.sensitive", true);
       inputFile = conf.get("mapreduce.map.input.file");

       if (conf.getBoolean("wordcount.skip.patterns", false)) {
         Path[] patternsFiles = new Path[0];
         try {
           patternsFiles = DistributedCache.getLocalCacheFiles(conf);
         } catch (IOException ioe) {
           System.err.println("Caught exception while getting cached files: "
               + StringUtils.stringifyException(ioe));
         }
         for (Path patternsFile : patternsFiles) {
           parseSkipFile(patternsFile);
         }
       }
     }

     private void parseSkipFile(Path patternsFile) {
       try {
         BufferedReader fis = new BufferedReader(new FileReader(
             patternsFile.toString()));
         String pattern = null;
         while ((pattern = fis.readLine()) != null) {
           patternsToSkip.add(pattern);
         }
       } catch (IOException ioe) {
         System.err.println("Caught exception while parsing the cached file '"
             + patternsFile + "' : " + StringUtils.stringifyException(ioe));
       }
     }

     public void map(LongWritable key, Text value, Context context)
         throws IOException, InterruptedException {
       String line = (caseSensitive) ?
           value.toString() : value.toString().toLowerCase();

       for (String pattern : patternsToSkip) {
         line = line.replaceAll(pattern, "");
       }

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
     job.setInputFormatClass(TextInputFormat.class);
     job.setOutputFormatClass(TextOutputFormat.class);

     int ngram_num = Integer.parseInt(args[0]);
     
     DistributedCache.addCacheFile(new Path(args[1]).toUri(),
             job.getConfiguration());
     //job.getConfiguration().setBoolean("wordcount.skip.patterns", true);

     FileInputFormat.setInputPaths(job, new Path(args.get(2)));
     FileOutputFormat.setOutputPath(job, new Path(args.get(3)));

     boolean success = job.waitForCompletion(true);
     return success ? 0 : 1;
   }

   public static void main(String[] args) throws Exception {
     int res = ToolRunner.run(new Configuration(), new Ngram(), args);
     System.exit(res);
   }
}
