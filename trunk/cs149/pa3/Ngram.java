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
	 while(queryTokens.hasNext())
	 {
                String temp = queryTokens.next();
		queryNgram.add(temp);	
	 }	
       } catch (IOException ioe) {
         System.err.println("Caught exception while parsing the cached file '"
             + queryFile + "' : " + StringUtils.stringifyException(ioe));
       }
     }

     public void map(Text key, Text value, Context context)
         throws IOException, InterruptedException {
       String line = value.toString();
       StringReader stringReader = new StringReader(line);
       BufferedReader bufReader = new BufferedReader(stringReader);
       String temp = null;
       String doc = "";
       String currentKey = null; 
       
       while((temp = bufReader.readLine())!=null)
       {
	  int start = temp.indexOf("<title>");
	  int end = temp.indexOf("</title>");
          if(start== -1 && end == -1)
	  {
		continue;
          }
	  else
	  {
		break;
          }
       }

       do
       { 
         int start = temp.indexOf("<title>");
	 int end = temp.indexOf("</title>");
         if (start == -1 && end == -1)
	 {	
		doc += temp;
	 }
         else
         {
	   if(currentKey != null)
	   {
		textDocs.put(currentKey, doc);
	   } 
           currentKey = temp.substring(start+7,end); 	
	   doc = "";
         }
        } while((temp = bufReader.readLine())!=null); 

	Set keys = textDocs.keySet();
        Iterator keysIter = keys.iterator();
        while(keysIter.hasNext())
        {
	 String titlename = (String)keysIter.next();
         String docval = (String)textDocs.get(titlename);
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
         one.set(0);
         
         ScoreTitleRecord tmp = new ScoreTitleRecord (new IntWritable(ngramcount), word);
	 context.write(one,tmp); 
        } 	
	//Tokenizer chunkTokens = new Tokenizer(line);
       /*StringTokenizer tokenizer = new StringTokenizer(line);
       while (tokenizer.hasMoreTokens()) {
         word.set(tokenizer.nextToken());
         context.write(word, one);
         context.getCounter(Counters.INPUT_WORDS).increment(1);
       }

       if ((++numRecords % 100) == 0) {
         context.setStatus("Finished processing " + numRecords
             + " records " + "from the input file: " + inputFile);
       }*/
     }
   }

   public static class Combine
       extends Reducer<IntWritable,ScoreTitleRecord, IntWritable, ScoreTitleRecord> {
     public void reduce(IntWritable key, Iterable<ScoreTitleRecord> values,
         Context context) throws IOException, InterruptedException {
       Vector<ScoreTitleRecord> max_records = new Vector<ScoreTitleRecord>();
       for (ScoreTitleRecord valtmp : values) {
          ScoreTitleRecord val = new ScoreTitleRecord( new IntWritable(valtmp.score.get()), new Text(valtmp.title));
          if (max_records.size() < 20){
             max_records.add(val);
          }else {
             for (int i = 0; i < max_records.size(); i++){
               int newscore = val.score.get();
               int oldscore = max_records.elementAt(i).score.get();
               if (newscore > oldscore ||
                    newscore == oldscore && val.title.toString().compareTo(max_records.elementAt(i).title.toString())>0){
                 max_records.elementAt(i).score = val.score;
                 max_records.elementAt(i).title = val.title;
                 break;
               }
             }
          }
       }
       for (int i = 0; i < max_records.size(); i++){
         context.write(key,max_records.elementAt(i));
       }
     }
   }


   public static class Reduce
       extends Reducer<IntWritable,ScoreTitleRecord, IntWritable, Text> {
     public void reduce(IntWritable key, Iterable<ScoreTitleRecord> values,
         Context context) throws IOException, InterruptedException {
       Vector<ScoreTitleRecord> max_records = new Vector<ScoreTitleRecord>();
       for (ScoreTitleRecord valtmp : values) {
          ScoreTitleRecord val = new ScoreTitleRecord( new IntWritable(valtmp.score.get()), new Text(valtmp.title));
          if (max_records.size() < 20){
             max_records.add(val);
          }else {
             for (int i = 0; i < max_records.size(); i++){
               int newscore = val.score.get();
               int oldscore = max_records.elementAt(i).score.get();
               if (newscore > oldscore ||
                    newscore == oldscore && val.title.toString().compareTo(max_records.elementAt(i).title.toString())>0){
                 max_records.elementAt(i).score = val.score;
                 max_records.elementAt(i).title = val.title;
                 break;
               }
             }
          }
       }

       for (int i = 0; i < max_records.size(); i++){
         context.write(max_records.elementAt(i).score,max_records.elementAt(i).title);
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
     job.setInputFormatClass(WholeFileTextInputFormat.class);
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
