package cn.edu.nju.ws.expedia.offline.lucene;

import cn.edu.nju.ws.expedia.util.nlp.StopWordsChecker;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;

import java.util.Set;

/**
 * Created by Xiangqian on 2015/4/11.
 */
public class AnalyzerFactory {
    private static CharArraySet getStopwords() {
        CharArraySet set = new CharArraySet(635, true);

        Set<String> stopWords = StopWordsChecker.getStopwords();

        for (String stopword : stopWords) {
            set.add(stopword);
        }
        return set;
    }

    public static Analyzer getDefaultAnalyzer() {
        CharArraySet stopwords = getStopwords();
        return new EnglishAnalyzer(stopwords);
    }
}
