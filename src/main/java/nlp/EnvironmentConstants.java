package nlp;


import edu.stanford.nlp.tagger.maxent.MaxentTagger;
/**
 * Created by paulosk on 08/10/15.
 */
public class EnvironmentConstants {

    public static final String WORD_SEP_REGEX = "[,.?!\" ]+";

    //Paulos
    //public static final String PROJECT_ROOT = "/Users/paulosk/School/ai/proj/";

    //Jaldeep
    public static final String PROJECT_ROOT = "/Users/Jaldeep/Documents/Artificiell Intelligens/ai_course_project/";


    public static final String CORPUS_FILES_PATH = PROJECT_ROOT + "corpus_files/";


    public static final String SOL_TAG = "<s>"; // Start of line symbol



    public static final MaxentTagger PART_OF_SPEECH_TAGGER;
    private static final String POS_TAGGER_FILE_PATH = PROJECT_ROOT +
            "pos_models/english-left3words-distsim.tagger";

    static {
        PART_OF_SPEECH_TAGGER = new MaxentTagger(POS_TAGGER_FILE_PATH);
    }
}
