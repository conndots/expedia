package cn.edu.nju.ws.expedia.util.nlp;


import cn.edu.nju.ws.expedia.util.nlp.snowball.exp.englishStemmer;

/**
 * Created by Xiangqian on 2015/2/28.
 */
public class NlpUtil {
    public static String getStemmedTokens(String sentence, boolean removeStopWord) {
        StringBuilder stemmed = new StringBuilder();

        sentence = sentence.replaceAll("[^\\w']+", " ").toLowerCase();
        String[] splits = sentence.split(" ");
        englishStemmer stemmer = new englishStemmer();
        for(String split : splits){
            if(removeStopWord && StopWordsChecker.isStopWord(split))
                continue;
            stemmer.setCurrent(split);
            if(stemmer.stem())
                stemmed.append(stemmer.getCurrent())
                        .append(" ");
        }
        return stemmed.toString();
    }

    public static String getSentenceRemoveStopWords(String sentence){
        StringBuilder removed = new StringBuilder();

        String[] splits = sentence.toLowerCase().split(" ");
        for(String split : splits){
            String replaced = split.replaceAll("[^\\w']+", "");
            if(StopWordsChecker.isStopWord(replaced))
                continue;
            removed.append(split).append(" ");
        }
        return removed.toString();
    }
}
