package reconstruct_corpus;

import edu.stanford.nlp.ling.tokensregex.Env;
import nlp.EnvironmentConstants;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import nlp.Grammar;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Jaldeep on 07/10/15.
 */
public class ReconstructCorpus {
    private HashSet<String> firstWord;

    public static void main(String [] args) throws IOException
    {
        new ReconstructCorpus();
    }

    public ReconstructCorpus()  throws IOException{
        File readFile = new File(EnvironmentConstants.CORPUS_FILES_PATH + "corpus_main");
        File writeFile = new File(EnvironmentConstants.CORPUS_FILES_PATH + "reversed_corpus_main");
        reverseCorpus(readFile, writeFile);

        readFile = new File(EnvironmentConstants.CORPUS_FILES_PATH + "corpus_main");
        writeFile = new File(EnvironmentConstants.CORPUS_FILES_PATH + "rhyming_words_corpus");
        rhymingWords(readFile, writeFile);

        readFile = new File(EnvironmentConstants.CORPUS_FILES_PATH + "corpus_main");
        writeFile = new File(EnvironmentConstants.CORPUS_FILES_PATH + "word_grammar_tokens");
        partOfSpeechCorpus(readFile, writeFile);

        tokensReversed();

        firstWord = new HashSet<>();
        createSentenceStarterList();
    }

    private void reverseCorpus(File rFile, File wFile)    {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(rFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(wFile));

            StringBuffer sBuffer;
            String currentLine = reader.readLine();
            while(currentLine != null)  {
                if(!currentLine.startsWith("#")) {
                    String line[] = currentLine.split(" ");
                    sBuffer = new StringBuffer();
                    for (int i = (line.length - 1); i >= 0; i--) {
                        sBuffer.append(line[i]);
                        sBuffer.append(" ");
                    }
                    sBuffer.append(EnvironmentConstants.SOL_TAG + "\n");
                } else  {
                    sBuffer = new StringBuffer("###\n");
                }
                writer.write(sBuffer.toString());
                currentLine = reader.readLine();
            }
            reader.close();
            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.exit( -1 );
        }
    }

    private void rhymingWords(File rFile, File wFile)    {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(rFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(wFile));

            StringBuffer sBuffer = new StringBuffer();
            String currentLine = reader.readLine();
            while(currentLine != null)  {
                if(!currentLine.startsWith("#")) {
                        String line[] = currentLine.split(" ");
                        sBuffer.append(line[line.length - 1] + " ");
                } else {
                    sBuffer.append("\n###\n");
                    writer.write(sBuffer.toString());
                    sBuffer = new StringBuffer();
                }
                currentLine = reader.readLine();
            }
            reader.close();
            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private void partOfSpeechCorpus(File rFile, File wFile) {
        MaxentTagger posTagger = EnvironmentConstants.PART_OF_SPEECH_TAGGER;
        try {

            HashMap<String, PrintWriter> tagTmpFiles = new HashMap<>();

            BufferedReader inRead = new BufferedReader(new InputStreamReader(
                    new FileInputStream(rFile)));

            String readLine = "";
            while((readLine = inRead.readLine()) != null ) {
                if(readLine.startsWith("#")) continue;

                /* Split line into its word and re-join with space separation.
                 * Have to investigate wether this is necessary for the tagger tool,
                 * or whether we introduce any errors by doing this. */
                StringBuilder sb = new StringBuilder();
                String[] lineWords = readLine.split(EnvironmentConstants.WORD_SEP_REGEX);
                for(String w : lineWords) { sb.append(w); sb.append(" "); }

                String taggedSentence = posTagger.tagString(sb.toString().trim());
                lineWords = taggedSentence.split(" "); // Split by space again to get words with tags

                for(String taggedWord : lineWords) {
                    String[] split = taggedWord.split("_");
                    String word = split[0];
                    String tag = "_" + split[1].trim().toUpperCase();

                    if( Grammar.isPunctuationSymbol(tag) ) continue;

                    if( ! tagTmpFiles.keySet().contains(tag)) {
                        tagTmpFiles.put(tag, new PrintWriter(
                                EnvironmentConstants.CORPUS_FILES_PATH + tag));
                    }

                    PrintWriter tagOut = tagTmpFiles.get(tag);
                    tagOut.println(word);
                    tagOut.flush();
                }
            }

            // Close all output streams before starting to read from them
            for(String tag : tagTmpFiles.keySet()) tagTmpFiles.get(tag).close();

        } catch(FileNotFoundException e1) {
            System.err.println(e1.getMessage());
            e1.printStackTrace();
            System.exit(-1);

        } catch(IOException e2) {
            System.err.println(e2.getMessage());
            e2.printStackTrace();
            System.exit(-1);
        }
    }

    private void tokensReversed()   {
        MaxentTagger m = EnvironmentConstants.PART_OF_SPEECH_TAGGER;
        Grammar grammar = new Grammar();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(EnvironmentConstants.CORPUS_FILES_PATH + "corpus_main")));
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(EnvironmentConstants.CORPUS_FILES_PATH + "corpus_tokenized_reverse")));
            String currentLine = reader.readLine();
            while(currentLine != null)  {
                if(currentLine.startsWith("#")) {
                    writer.write("###" + "\n");
                } else {
                    String lineWithTags = m.tagString(currentLine);
                    String[] words = lineWithTags.split(" ");
                    StringBuilder strB = new StringBuilder();
                    for(int i = words.length-1; i >= 0; i--)   {
                        String tag = words[i].split("_")[1];
                        String token = "_" + tag.toUpperCase();
                        if( ! Grammar.isPunctuationSymbol(token))  {
                            strB.append( token + " ");
                        }
                    }
                    strB.append("\n");
                    writer.write(strB.toString());
                }
                currentLine = reader.readLine();
            }
            reader.close();
            writer.close();
        } catch (IOException e) {}
    }

    public void createSentenceStarterList() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(
                new File(EnvironmentConstants.CORPUS_FILES_PATH + "corpus_main")));
        BufferedWriter writer = new BufferedWriter(
                new FileWriter(new File(EnvironmentConstants.CORPUS_FILES_PATH + "first_words_corpus")));
        String currentLine = reader.readLine();
        while(currentLine!= null)   {
            if(!currentLine.startsWith("#"))    {
                String line[] = currentLine.split(" ");
                String word = line[0].toLowerCase().replaceAll(EnvironmentConstants.WORD_SEP_REGEX, "");
                firstWord.add(word);
                writer.write(word + "\n");
            }
            currentLine = reader.readLine();
        }

        reader.close();
    }

    public HashSet<String> getFirstWords() {
        return firstWord;
    }


}
