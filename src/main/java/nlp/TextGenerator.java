package nlp;

import edu.stanford.nlp.ling.tokensregex.Env;
import reconstruct_corpus.ReconstructCorpus;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Jaldeep on 08/10/15.
 */
public class TextGenerator {
    private int currentRhymingPairIndex;
    private List<BiGram> pairs;
    private BiGram currentPair;

    private static BiGramModel mainCorpusBiGram;

    private static BiGramModel aaBiGram;
    private static BiGramModel abBiGram;

    private static BiGramModel corpusBiGram;
    private static BiGramModel grammarTokenBiGram;

    public static void main(String args[]) throws Exception  {
        TextGenerator txtGen = new TextGenerator();
    }


    public static void init() {
        try {

            mainCorpusBiGram = new BiGramModel(new FileInputStream(
                    new File(EnvironmentConstants.CORPUS_FILES_PATH + "reversed_corpus_main")
            ));

            grammarTokenBiGram = new BiGramModel(new FileInputStream(
                    new File(EnvironmentConstants.CORPUS_FILES_PATH + "corpus_tokenized_reverse")
            ));

            corpusBiGram = new BiGramModel(new FileInputStream(new File(EnvironmentConstants.CORPUS_FILES_PATH + "corpus_main")));



            createBiGramAABB(new File(EnvironmentConstants.CORPUS_FILES_PATH + "rhyming_words_corpus"));


        } catch(IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.exit( -1 );
        }

    }

    private static void createBiGramAABB(File inputFile) throws IOException {

        StringBuilder aaBuf = new StringBuilder();
        StringBuilder abBuf = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(inputFile)
        ));


        String readLine;
        while((readLine = br.readLine()) != null) {
            if(readLine.startsWith("#")) continue;

            String[] words = readLine.split(
                    EnvironmentConstants.WORD_SEP_REGEX);

            StringBuilder tmpBuf;

            for(int i = 1; i < words.length; i++) {
                tmpBuf = (i % 2 == 1) ? aaBuf : abBuf;
                tmpBuf.append(words[i - 1] + " " + words[i]);
                tmpBuf.append("\n###\n");
            }
        }

        PrintWriter pw = new PrintWriter(System.getenv("TMPDIR") + "aaBiGrams");
        pw.println(aaBuf.toString());
        pw.flush();
        pw.close();

        pw = new PrintWriter(System.getenv("TMPDIR") + "abBiGrams");
        pw.println(abBuf.toString());
        pw.flush();;
        pw.close();

        aaBiGram = new BiGramModel(aaBuf.toString());
        abBiGram = new BiGramModel(abBuf.toString());
    }


    public TextGenerator() throws Exception  {
        init();

        currentRhymingPairIndex = -1;

        String a1, a2;

        a1 = a2 = "";
        //a1 = "free";
        //a2 = "be";

        //List<String> grammar1 = Grammar.grammarRules.get("_JJ");
        //List<String> grammar2 = Grammar.grammarRules.get("_VB");
        List<String> grammar1 = null;
        List<String> grammar2 = null;
        BiGram wordPair;



        while( grammar1 == null || grammar2 == null) {

            wordPair = aaBiGram.getRandomBiGram();
            a1 = wordPair.getWord1();
            a2 = wordPair.getWord2();

            for(String tag : Grammar.getGrammarTagsForWord(a1)) {

                if(Grammar.grammarRules.keySet().contains(tag)) {
                    grammar1 = Grammar.grammarRules.get(tag);
                    break;
                }
            }

            for(String tag : Grammar.getGrammarTagsForWord(a2)) {

                if(Grammar.grammarRules.keySet().contains(tag)) {
                    grammar2 = Grammar.grammarRules.get(tag);
                    break;
                }
            }
        }


        List<String> poems1 = generatePoemLinesForGrammars(a1, grammar1);
        System.out.println(getMostProbableSentence(poems1) + ",");
        List<String> poems2 = generatePoemLinesForGrammars(a2, grammar2);
        System.out.println(getMostProbableSentence(poems2));
    }

    public String getMostProbableSentence(List<String> sentences)   {
        double totalProb;
        Double[] sentenceProbs = new Double[sentences.size()];
        double nextWordProb,prevWordProb;
        for(int i = 0; i < sentences.size(); i++) {
            String sentence = sentences.get(i);
            totalProb = 0;
            String[] sentenceSplitted = sentence.split(" ");
            for(int j = 0; j < sentenceSplitted.length - 2; j++)    {
                List<PredictedWord> predcitedWords = corpusBiGram.getSortedPredictionDistribution(sentenceSplitted[j]);
                nextWordProb = 0;
                for(PredictedWord word : predcitedWords)    {
                    if(word.getWord().equals(sentenceSplitted[j+1]))  {
                        nextWordProb = word.getProbability();
                        break;
                    }
                }
                predcitedWords = mainCorpusBiGram.getSortedPredictionDistribution(sentenceSplitted[j+2]);
                prevWordProb = 0;
                for(PredictedWord word : predcitedWords)    {
                    if(word.getWord().equals(sentenceSplitted[j+1]))  {
                        prevWordProb = word.getProbability();
                        break;
                    }
                }

                totalProb = totalProb + (nextWordProb + prevWordProb);
            }
            sentenceProbs[i] = totalProb;
        }

        double maxProb = Double.MIN_VALUE;
        int maxIndex = -1;
        for(int i = 0; i < sentenceProbs.length; i++)   {
            if(sentenceProbs[i] > maxProb)  {
                maxProb = sentenceProbs[i];
                maxIndex = i;
            }
        }
        String sentence = sentences.get(maxIndex);
        return (Character.toUpperCase(sentence.charAt(0)) + sentence.substring(1));
    }

    private String stringBuilderToLineString(StringBuilder sb) {
        StringBuilder lineBuilder;

        List<String> split = Arrays.asList(
                sb.toString().split(" "));
        Collections.reverse(split);

        lineBuilder = new StringBuilder();
        for(int i = 0; i < split.size(); i++) {
            lineBuilder.append(split.get(i));
            lineBuilder.append(" ");
        }
        return lineBuilder.toString().trim();
    }

    private void poemLinesForGrammar(String lastWord, final List<String> grammar,
                    int iTag, final StringBuilder line, List<String> completeLines) {


        // Number probable words to traverse by, i.e. the branching factor
        final int TRY_WORDS = 10;

        // Base case. Have filled all grammars
        if(iTag >= (grammar.size()-1)) {
            String res = stringBuilderToLineString(line);
            //System.out.println(res);
            completeLines.add(res);
            return;
        }


        // Get all the possible words coming from the lastWord, and
        // keep the ones that fulfil the current tag in the grammar.
        List<String> nextWords = mainCorpusBiGram
                .getSortedPredictionDistribution(lastWord)
                .stream()
                .map(pw -> pw.getWord())
                .filter(w -> Grammar.wordHasTag(w, grammar.get(iTag)))
                .collect(Collectors.toList());

        if(nextWords.size() >= TRY_WORDS) {
            nextWords.subList(0, TRY_WORDS);
        }

        StringBuilder pathLine;

        for(String nextWord : nextWords) {
            // Construct a "so-far-copy" of the string path taken ...
            pathLine = new StringBuilder(line);
            pathLine.append(nextWord + " ");

            // ... use it to add the next word and recurse over its path.
            poemLinesForGrammar(nextWord, grammar,
                    (iTag + 1), pathLine, completeLines);

        }
    }


    private List<String> generatePoemLinesForGrammars(String rhyme, List<String> grammar)  {
        List<String> lineResults = new ArrayList<>(grammar.size());

        poemLinesForGrammar(rhyme, grammar, 0,
                new StringBuilder(rhyme + " "), lineResults);

        return lineResults;

    }

    private String getMostFittingWord(String grammarToken, List<PredictedWord> predictWord) {
        for(int i = 0; i < predictWord.size(); i++) {
            if(Grammar.getGrammarTagsForWord(predictWord.get(i).getWord()).get(0).
                    equalsIgnoreCase(grammarToken))  {
                return predictWord.get(i).getWord();
            }
        }
        return "";
    }


    private List<BiGram> getRhymingWords()  {
        List<BiGram> pairs = new LinkedList<>();
        try{
            BufferedReader reader = new BufferedReader(
                    new FileReader(new File(EnvironmentConstants.CORPUS_FILES_PATH + "rhyming_words_corpus")));

            String currentLine = "";
            while((currentLine = reader.readLine()) != null) {
                String[] words = currentLine.split(EnvironmentConstants.WORD_SEP_REGEX);


                for(int i = 0; i < words.length; i++) {
                    if(i % 2 == 1) {
                        pairs.add(new BiGram(words[i - 1], words[i]));
                    }
                }
            }

            reader.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.exit( -1 );
        }

        return pairs;
    }

    private BufferedReader createBufferedReader(String fileName) {
        try {
            return new BufferedReader(new InputStreamReader(
                    new FileInputStream(fileName)));

        } catch(FileNotFoundException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.exit( -1 );
        }

        return null;
    }

    private List<String> findGrammarLineStartingWith(List<String> grammarTag) {
        final String grammarReverseFile =
                EnvironmentConstants.CORPUS_FILES_PATH + "corpus_tokenized_reverse";

        BufferedReader br = createBufferedReader(grammarReverseFile);

        List<String> grammars = new LinkedList<>();
        String readLine;

        try {
            while ((readLine = br.readLine()) != null) {
                String[] grammar = readLine.split(" ");

                for(String tag : grammarTag) {
                    if(grammar[0].trim().equalsIgnoreCase(tag)) {
                        grammars.add(readLine);
                    }
                }

            }
        } catch(IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.exit( -1 );
        }

        return grammars;
    }

}