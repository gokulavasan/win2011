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
       extends Mapper<Text, Text, IntWritable, ScoreTitleRecord> {

     static enum Counters { INPUT_WORDS }

     private IntWritable one = new IntWritable(1);
     private Text word = new Text();
     private int searchnum_max=1;
     private int ngram_num = 0;
     private Set<String> queryNgram = new HashSet<String>();
     private HashMap<String, String> textDocs = new HashMap<String, String>();
     private long numRecords = 0;
     private String inputFile;
     public void setup(Context context) 
     {
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
       for (Path queryFile : queryFiles) 
       {
         parseQueryFile(queryFile);
       }
      } 

     private void parseQueryFile(Path queryFile) {
       try {
         BufferedReader fis = new BufferedReader(new FileReader(
             queryFile.toString()));
         String pattern = null;
	 String entiretext = "";
         while ((pattern = fis.readLine()) != null) {
           entiretext+=pattern+" ";
         }
         NgramTokenizer queryTokens = new NgramTokenizer(entiretext, ngram_num);
         System.out.println("Start Query TOKENS!!!\n");
	 while(queryTokens.hasNext())
	 {
                String temp = queryTokens.next();
                 System.out.println(temp);
		queryNgram.add(temp);	
	 }	
         System.out.println("END Query TOKENS!!!\n");
       } catch (IOException ioe) {
         System.err.println("Caught exception while parsing the cached file '"
             + queryFile + "' : " + StringUtils.stringifyException(ioe));
       }
     }

     public void map(Text key, Text value, Context context)
         throws IOException, InterruptedException {
       String line = value.toString();
       String temp = null;
       String doc = "";
       String currentKey = null; 
       ScoreTitleRecord tmp;
       one.set(0);
       String titlename = key.toString();
       String docval = value.toString();
       int ngramcount = 0;
       NgramTokenizer doctoken = new NgramTokenizer(docval, ngram_num);
       while(doctoken.hasNext())
       {
       	if(queryNgram.contains(doctoken.next()))
              {
              	ngramcount++;
              }   
       }
       word.set(titlename);
       
       tmp = new ScoreTitleRecord (new IntWritable(ngramcount), word);
       context.write(one,tmp); 
       tmp = null;
     }
   }

   public static void printComparison(ScoreTitleRecord a, ScoreTitleRecord b){
      System.out.println("Comparing "+a.title+"("+a.score+") to "+b.title+"("+b.score+")");
   }

   public static class Combine
       extends Reducer<IntWritable,ScoreTitleRecord, IntWritable, ScoreTitleRecord> {
     public void reduce(IntWritable key, Iterable<ScoreTitleRecord> values,
         Context context) throws IOException, InterruptedException {
       List<ScoreTitleRecord> max_records = new ArrayList<ScoreTitleRecord>(20);
       ScoreTitleRecord MinRecord;
       for (ScoreTitleRecord valtmp : values) {
          ScoreTitleRecord val = new ScoreTitleRecord( new IntWritable(valtmp.score.get()), new Text(valtmp.title));
          if (max_records.size() < 20){
             max_records.add(val);
          }else {
             MinRecord = max_records.get(0);
             Iterator<ScoreTitleRecord> it = max_records.iterator();
             while (it.hasNext()){
                ScoreTitleRecord rec = it.next();
                if (rec.compareTo(MinRecord) <0){
                  MinRecord=rec;
                }
             }
             if (MinRecord.compareTo(val) < 0){
                max_records.remove(MinRecord);
                max_records.add(val);
             }
          }
       }
       Iterator<ScoreTitleRecord> it = max_records.iterator();
       while (it.hasNext()){
         context.write(key,it.next());
       }
     }
   }


   public static class Reduce
       extends Reducer<IntWritable,ScoreTitleRecord, IntWritable, Text> {
     public void reduce(IntWritable key, Iterable<ScoreTitleRecord> values,
         Context context) throws IOException, InterruptedException {
       List<ScoreTitleRecord> max_records = new ArrayList<ScoreTitleRecord>(20);
       ScoreTitleRecord MinRecord;
       for (ScoreTitleRecord valtmp : values) {
          ScoreTitleRecord val = new ScoreTitleRecord( new IntWritable(valtmp.score.get()), new Text(valtmp.title));
          if (max_records.size() < 20){
             max_records.add(val);
          }else {
             MinRecord = max_records.get(0);
             Iterator<ScoreTitleRecord> it = max_records.iterator();
             while (it.hasNext()){
                ScoreTitleRecord rec = it.next();
                if (rec.compareTo(MinRecord) <0){
                  MinRecord=rec;
                }
             }
             if (MinRecord.compareTo(val) < 0){
                max_records.remove(MinRecord);
                max_records.add(val);
             }
          }
       }
       Collections.sort(max_records, Collections.reverseOrder());
       Iterator<ScoreTitleRecord> it = max_records.iterator();
       while (it.hasNext()){
         ScoreTitleRecord rec = it.next();
         context.write(rec.score,rec.title);
       }
     }
   }

   public int run(String[] args) throws Exception {
     //Args format: <#Ngram> <query_file> <input_dir> <output_dir>
     Job job = new Job(getConf());
     job.setJarByClass(Ngram.class);
     job.setJobName("Ngram");

     job.setOutputKeyClass(IntWritable.class);
     job.setOutputValueClass(ScoreTitleRecord.class);

     job.setMapperClass(Map.class);
     job.setCombinerClass(Combine.class);
     job.setReducerClass(Reduce.class);

     // Note that these are the default.
     job.setInputFormatClass(WholePageTextInputFormat.class);
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
