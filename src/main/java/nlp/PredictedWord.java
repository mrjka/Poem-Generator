package nlp;

import java.util.Comparator;

/**
 * Created by paulosk on 08/10/15.
 */
public class PredictedWord  {

    private final double probability;
    private final String word;

    public PredictedWord(String word, double probability) {
        this.probability = probability;
        this.word = word;
    }

    public double getProbability() {
        return probability;
    }

    public String getWord() {
        return word;
    }


    public static  int compare(PredictedWord pw1, PredictedWord pw2) {
        double p1 = pw1.getProbability();
        double p2 = pw2.getProbability();

        if(p1 > p2) return 1;
        else if(p1 < p2) return -1;
        else return 0;
    }
}
