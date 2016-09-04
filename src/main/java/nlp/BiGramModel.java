package nlp;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.StringReader;


import java.util.*;

/**
 * A BiGramModel developed specifically for line generation in a poem.
 *
 */
public class BiGramModel  {


    private int numberOfWords;
    private static final int N = 2;
    private HashMap<String, HashMap<String, Integer>> wordSequenceDistribution;

    public BiGramModel(InputStream in) {
        BufferedReader inRead = new BufferedReader(
                new InputStreamReader(in));


        wordSequenceDistribution = new HashMap<>();
        this.buildModel(inRead);


    }

    public BiGramModel(String in) {
        BufferedReader inRead = new BufferedReader(
                new StringReader(in));



        wordSequenceDistribution = new HashMap<>();
        this.buildModel(inRead);

    }

    private List<String> createGrams(String line, char separator) {
        String[] split = line.toLowerCase().split(
                EnvironmentConstants.WORD_SEP_REGEX);
        List<String> nGrams = new LinkedList<>();


        for(int i = 0; i < ((split.length - N) + 1); i++) {
            StringBuilder sp = new StringBuilder();

            sp.append(split[i]);
            sp.append(separator);
            sp.append(split[i + 1]);

            nGrams.add(sp.toString());

        }

        return nGrams;
    }


    private HashMap<String, Integer> getDistributionEntries(String word) {
        word = word.toLowerCase().trim();
        if( ! wordSequenceDistribution.containsKey(word)) {
            wordSequenceDistribution.put(word,
                    new HashMap<>());
        }

        return wordSequenceDistribution.get(word);
    }

    private void buildModel(BufferedReader br) {
        final char separator = ' ';
        String readLine;

        try {
            while ((readLine = br.readLine()) != null) {
                if (readLine.startsWith("#")) continue;

                List<String> nGrams = createGrams(readLine, separator);
                for (int i = 0; i < nGrams.size(); i++) {
                    String[] tokens = nGrams.get(i).split("" + separator);
                    HashMap<String, Integer> distributionEntries =
                            getDistributionEntries(tokens[0]);


                    Integer prevEntryCount = distributionEntries.get(tokens[1]);
                    int entryCount = (prevEntryCount == null) ? 0 : prevEntryCount;
                    distributionEntries.put(tokens[1], (entryCount + 1));
                }
            }
        } catch(IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.exit( -1 );
        }
    }


    private int sumDistributionEntriesCount(String word) {
        HashMap<String, Integer> distEntries = getDistributionEntries(word);

        int sum = 0;
        for(String entryWord : distEntries.keySet()) {
            Integer termIntg = distEntries.get(entryWord);
            int term = (termIntg == null) ? 0 : termIntg;
            sum += term;
        }

        return sum;

    }


    /**
     * Returns a random Bi-gram (w1, w2) where w1 is random and w2 is the most probable
     * gram in the gram distribution entry for w1.
     *
     * An example could be: Random word w1 is "Hello", then word w2 would be the most probable word
     * following "Hello".
     *
     *
     * OBS! Issue! This method might randomly choose a rhyming word A2 in the rhyming scheme A1A2B1B2.
     * So the end result might not rhyme. This have to be fixed somehow!!!
     */
    public BiGram getRandomBiGram() {
        final Set<String> words = wordSequenceDistribution.keySet();
        int limit = wordSequenceDistribution.size();
        Random rand = new Random(); // Probably only used here

        BiGram bigramRes = null;
        int elemIdx = rand.nextInt(limit);

        int i = 0;
        for(String w1 : words) {

            if( i == elemIdx ) {
                PredictedWord pw = getMostPredictedWord(w1);
                bigramRes = new BiGram(w1, pw.getWord());
                break;
            }

            i++;
        }

        assert(bigramRes != null);

        return bigramRes;

    }

    public double getGramProbability(String w1, String w2) {
        int distSum = sumDistributionEntriesCount(w1);

        if( ! wordSequenceDistribution.get(w1).containsKey(w2)) {
            // If the word is not there, return probability 0
            return 0.0;
        }

        int w2Count = wordSequenceDistribution.get(w1).get(w2);
        return ((double) w2Count) / distSum;
    }


    public List<PredictedWord> getSortedPredictionDistribution(String word) {
        HashMap<String, Integer> distEntries = getDistributionEntries(word);
        ArrayList<PredictedWord> pwDistributions = new ArrayList<>(distEntries.size());

        double wordProbability;
        int distributionCountSum, entryCount;
        distributionCountSum = sumDistributionEntriesCount(word);

        for(String predictedWord : distEntries.keySet()) {
            entryCount = distEntries.get(predictedWord);
            wordProbability = ((double) entryCount) / distributionCountSum;
            pwDistributions.add(new PredictedWord(predictedWord, wordProbability));

        }

        Collections.sort(pwDistributions,
                (pw1, pw2) -> PredictedWord.compare(pw1, pw2));


        Collections.reverse(pwDistributions);
        return pwDistributions;
    }

    public PredictedWord getMostPredictedWord(String word) {
        HashMap<String, Integer> distEntries = getDistributionEntries(word);
        int distEntryCountSum = sumDistributionEntriesCount(word);

        int maxOccurances = Integer.MIN_VALUE;
        String mostOccuringWord = "";

        for(String predictedWord  : distEntries.keySet()) {

            if(distEntries.get(predictedWord) > maxOccurances &&
                    ( ! predictedWord.equalsIgnoreCase(EnvironmentConstants.SOL_TAG))) {

                maxOccurances = distEntries.get(predictedWord);
                mostOccuringWord = predictedWord;
            }
        }

        return new PredictedWord(mostOccuringWord,
                ((double)maxOccurances) / distEntryCountSum);
    }

    public static void main(String[] args) {
        String sentence = "Hello, this is a sentence, hello again, hello again, hello again, hello is!";
        BiGramModel bgm = new BiGramModel(sentence);

        System.out.println("Hello\t--->  ");
        for(PredictedWord pw : bgm.getSortedPredictionDistribution("hello")) {
            int percentProb = (int)(pw.getProbability() * 100);
            System.out.println("\t" + pw.getWord() + "\t(" + percentProb + "%)");
        }
    }
}
