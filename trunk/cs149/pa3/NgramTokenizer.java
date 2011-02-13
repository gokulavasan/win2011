import java.util.*;

public class NgramTokenizer {
        Tokenizer token;
        int ngram_count;
        boolean found_flag;
        Vector<String> ngram;
        public NgramTokenizer (String text, int count) {
           ngram_count = count;
           token = new Tokenizer (text);
           found_flag=false;
           ngram = new Vector<String>();
        }
        public boolean hasNext() {
           return token.hasNext();
        }
        public String next(){
           String ngram_txt  = "";
           if (!found_flag){
              for (int i = 0; i < ngram_count; i++){
                 if (token.hasNext()){
                     ngram.addElement(token.next());
                 }
              }
              found_flag=true;
           } else {
              if (token.hasNext()){
                 ngram.removeElementAt(0);
                 ngram.addElement(token.next());
              }
           }

           for (int i = 0; i < ngram.size(); i++){
             ngram_txt+=ngram.get(i);
           }
           return ngram_txt;
        }
     }

