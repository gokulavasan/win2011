/**
* Originally written by Steve Lewis
* lordjoe2000@gmail.com
* See http://lordjoesoftware.blogspot.com/
*/

import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.*;
import org.apache.hadoop.io.compress.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.util.*;

import java.io.*;

 
/**
* Splitter that reads a whole file as a single record
* This is useful when you have a large number of files
* each of which is a complete unit - for example XML Documents
*/

public class WholePageTextInputFormat extends FileInputFormat<Text, Text> {

    @Override
    public RecordReader<Text, Text>  createRecordReader(InputSplit split,
                       TaskAttemptContext context) {
        return new WholePageReader();
    }

    @Override
    protected boolean isSplitable(JobContext context, Path file) {
        return false;
    }

    /**
     * Custom RecordReader which returns the entire file as a
     * single value with the name as a key
     * Value is the entire file
     * Key is the file name
     */

    public static class WholePageReader extends RecordReader<Text, Text> {
        private CompressionCodecFactory compressionCodecs = null;
        private long start;
        private long end;
        private long pos;
        private LineReader in;
        private Text key = null;
        private Text value = null;
        private Text buffer = new Text();

        public void initialize(InputSplit genericSplit,
                               TaskAttemptContext context) throws IOException {
            FileSplit split = (FileSplit) genericSplit;
            Configuration job = context.getConfiguration();
            start = split.getStart();
            pos = start;
            end = split.getStart() + split.getLength();
            final Path file = split.getPath();
            compressionCodecs = new CompressionCodecFactory(job);
            final CompressionCodec codec = compressionCodecs.getCodec(file);

            // open the file and seek to the start of the split
            FileSystem fs = file.getFileSystem(job);
            FSDataInputStream fileIn = fs.open(split.getPath());
            if (codec != null) {
                in = new LineReader(codec.createInputStream(fileIn), job);
              }
            else {
                in = new LineReader(fileIn, job);
            }
            if (key == null) {
                key = new Text();
            }

            key.set(split.getPath().getName());

            if (value == null) {
                value = new Text();
            }
        }

        public boolean isTitleStr(String str) {
           int start = str.indexOf("<title>");
           int end = str.indexOf("</title>");
           if (start !=-1 && end !=-1) {
               return true;
           }
           return false;

         }
        public String getTitle (String str){
           int start = str.indexOf("<title>");
           int end = str.indexOf("</title>");
           return str.substring(start+7,end);
        }

        public boolean nextKeyValue() throws IOException {
            int newSize = 0;
            StringBuilder sb = new StringBuilder();
   
            //Loop through chunk until we hit a title string
            while (!isTitleStr(buffer.toString())){
              newSize = in.readLine(buffer);
              if (newSize == 0){
                break;
              }
            }
            //Now that we are at the title, save it as the key
            if (newSize >0){
              key.set(getTitle(buffer.toString()));
            }
            //Loop through the rest getting the page contents
            while (newSize > 0) {
                newSize = in.readLine(buffer);
                if (isTitleStr(buffer.toString())){
                  break;
                }
                String str = buffer.toString();
                pos +=str.length();
                sb.append(str);
                sb.append("\n");
            }
            buffer.clear();
            String s = sb.toString();
            value.set(s);
            if (sb.length() == 0) {
                key = null;
                value = null;
                return false;
            }
            else {
                return true;
            }
        }

 

        @Override
        public Text getCurrentKey() {
            return key;
        }

        @Override
        public Text getCurrentValue() {
            return value;
        }

        /**
         * Get the progress within the split
         */
        public float getProgress() {
            if (start == end) {
              return 0.0f;
            } else {
               return 1.0f;
            }
        }

        public synchronized void close() throws IOException {
            if (in != null) {
                in.close();
            }
        }
    }
}
