package reconstruct_corpus;


import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;



import nlp.EnvironmentConstants;
import nlp.Grammar;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by paulosk on 11/10/15.
 */
public class Test {

    public static void main(String[] args) {
        LexicalizedParser.main(new String[] {
                "-outputFormat", "wordsAndTags", "-printPCFGkBest", "1", "englishPCFG.ser.gz",
                EnvironmentConstants.PROJECT_ROOT + "sentence.txt"
        });

    }
}
