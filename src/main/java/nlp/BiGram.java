package nlp;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jaldeep on 08/10/15.
 */
public class BiGram {

    private String word1;
    private String word2;

    public BiGram()  {
        this.word1 = "";
        this.word2 = "";
    }

    public BiGram(String w1, String w2)  {
        this.word1 = w1;
        this.word2 = w2;
    }

    public void setWord1(String word)  {
        this.word1 = word;
    }

    public void setWord2(String word)  {
        this.word2 = word;
    }


    public String getWord1() {
        return word1;
    }

    public String getWord2() {
        return word2;
    }

}
