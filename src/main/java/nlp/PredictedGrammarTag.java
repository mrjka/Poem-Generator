package nlp;

/**
 * Created by paulosk on 14/10/15.
 */
public class PredictedGrammarTag {

    private final String tag;
    private final int occurances;

    public PredictedGrammarTag(String tag, int occurances) {
        this.tag = tag;
        this.occurances = occurances;
    }

    public String getTag() {
        return tag;
    }

    public int getOccurances() {
        return occurances;
    }
}
