package nlp;

import java.io.*;
import java.util.*;

/**
 * Created by paulosk on 13/10/15.
 */
public class Grammar {


    public static final List<String> GRAMMAR_TOKENS = Arrays.asList(
                "_CC", "_CD", "_DT", "_EX", "_FW", "_IN", "_JJ", "_JJR", "_JJS",
                "_MD", "_NN", "_NNP", "_NNPS", "_NNS", "_PDT", "_POS", "_PRP", "_PRP$",
                "_RB", "_RBR", "_RP", "_TO", "_UH", "_VB", "_VBD", "_VBG", "_VBN", "_VBP",
                "_VBZ", "_WDT", "_WP", "_WRB"
    );


    public final static HashMap<String, List<String>> grammarRules;

    static {
        grammarRules = new HashMap<>();

        grammarRules.put("_PRP", Arrays.asList(
                "_PRP","_JJ","_DT","_VB","_TO","_RB"
        ));


        grammarRules.put("_NN", Arrays.asList(
                "_NN",  "_PRP$",  "_IN",  "_RB",  "_VBG"
        ));


        grammarRules.put("_VB", Arrays.asList(
                "_VB",  "_TO",  "_NN", "_JJ", "_DT"
        ));

        grammarRules.put("_IN", Arrays.asList(
                "_IN", "_NN", "_PRP$", "_VBP", "_PRP"
        ));

        grammarRules.put("_JJ", Arrays.asList(
                "_JJ","_RB","_RB","_VBZ","_NN","_DT"
        ));

        grammarRules.put("_VBZ", Arrays.asList(
                "_VBZ","_NN","_DT","_WP","_IN","_VB"
        ));

        grammarRules.put("_DT", Arrays.asList(
                "_DT","_IN","_JJ","_VBD","_WP"
        ));
    }


    private static Iterable<String> getGrammarTagIterator() {
        return GRAMMAR_TOKENS;
    }

    /**
     * This method returns the tags in sorted order with highest occurances first.
     * This means that the higher the element is in the list, the more often was the
     * word found to have that grammar tag.
     * @param word
     * @return
     */
    public static  List<String> getGrammarTagsForWord(String word) {
        word = word.trim();

        int fileOccurances;
        List<PredictedGrammarTag> tagProbabilities = new ArrayList<>();

        try {

            BufferedReader br;
            String readLine, readClean;

            for (String tag : getGrammarTagIterator()) {
                fileOccurances = 0;

                br = new BufferedReader(new InputStreamReader(
                        new FileInputStream(EnvironmentConstants.CORPUS_FILES_PATH + tag)
                ));

                while((readLine = br.readLine()) != null) {
                    readClean = readLine.replaceAll(EnvironmentConstants.WORD_SEP_REGEX, "");
                    readClean = readClean.toLowerCase().trim();
                    if(readClean.equals(word)) fileOccurances++;
                }

                br.close();

                PredictedGrammarTag pgt = new PredictedGrammarTag(tag, fileOccurances);
                if(fileOccurances > 0) tagProbabilities.add(pgt);
            }

        } catch(FileNotFoundException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.exit( -1 );

        } catch(IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.exit( -1 );

        }


        Collections.sort(tagProbabilities, (pgt1, pgt2) -> {
            double p1 = pgt1.getOccurances();
            double p2 = pgt2.getOccurances();

            if (p1 > p2) return 1;
            else if (p1 < p2) return -1;
            else return 0;

        });

        Collections.reverse(tagProbabilities);

        List<String> res = new ArrayList<>(tagProbabilities.size());
        for(int i = 0; i < tagProbabilities.size(); i++)
            res.add(i, tagProbabilities.get(i).getTag());
        return res;
    }

    public static boolean wordHasTag(String word, String tag) {
        boolean hasTag = false;
        for(String t: getGrammarTagsForWord(word)) {
            if(t.equalsIgnoreCase(tag)) return true;
        }
        return true;
    }


    private static final String punctuationRegex = "[\\.,!?`'´\"_:;]+";


    public static boolean isPunctuationSymbol(String sym) {
        return sym.matches(punctuationRegex);
    }

    public static boolean isPunctuationSymbol(char sym) {
        return (sym + "").matches(punctuationRegex);
    }
}
