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

public class ScoreTitleRecord implements java.io.Serializable, org.apache.hadoop.io.Writable {
  IntWritable score;
  Text title;

  public ScoreTitleRecord(){
    score = new IntWritable();
    title = new Text();
  }
  public ScoreTitleRecord (IntWritable score, Text title){
     this.score = score;
     this.title = title;
  } 

   public void write(DataOutput out) throws IOException {
         score.write(out);
         title.write(out);
       }
       
       public void readFields(DataInput in) throws IOException {
         score.readFields(in);
         title.readFields(in);
       }
       
       public static ScoreTitleRecord read(DataInput in) throws IOException {
         ScoreTitleRecord w = new ScoreTitleRecord();
         w.readFields(in);
         return w;
       } 

    public String toString() {
       return title.toString() + " " + score.get();
    }
}
