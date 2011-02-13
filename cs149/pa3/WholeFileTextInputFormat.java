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

public class WholeFileTextInputFormat extends FileInputFormat<Text, Text> {

    @Override
    public RecordReader<Text, Text>  createRecordReader(InputSplit split,
                       TaskAttemptContext context) {
        return new WholeFileReader();
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

    public static class WholeFileReader extends RecordReader<Text, Text> {
        private CompressionCodecFactory compressionCodecs = null;
        private long start;
        private LineReader in;
        private Text key = null;
        private Text value = null;
        private Text buffer = new Text();

        public void initialize(InputSplit genericSplit,
                               TaskAttemptContext context) throws IOException {
            FileSplit split = (FileSplit) genericSplit;
            Configuration job = context.getConfiguration();
            start = split.getStart();
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

 

        public boolean nextKeyValue() throws IOException {
            int newSize = 0;
            StringBuilder sb = new StringBuilder();
            newSize = in.readLine(buffer);
            while (newSize > 0) {
                String str = buffer.toString();
                sb.append(str);
                sb.append("\n");
                newSize = in.readLine(buffer);
            }

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
            return 0.0f;
        }

        public synchronized void close() throws IOException {
            if (in != null) {
                in.close();
            }
        }
    }
}
