package cn.edu.nju.ws.expedia.util.nlp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Xiangqian on 2015/3/4.
 */
public class StopWordsChecker {
    private static HashSet<String> STOP_WORDS = null;
    private static Object LOCK = new Object();
    public static boolean isStopWord(String word){
        if(STOP_WORDS == null){
            synchronized(LOCK){
                if(STOP_WORDS == null){
                    STOP_WORDS = new HashSet<String>();

                    BufferedReader br = new BufferedReader(new InputStreamReader(StopWordsChecker.class.getClassLoader().getResourceAsStream("stop-words-english.txt")));
                    try {
                        String line = br.readLine();
                        while(line != null) {
                            String sword = line.trim();
                            STOP_WORDS.add(sword);

                            line = br.readLine();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally{
                        try {
                            br.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        }
        return STOP_WORDS.contains(word);
    }
    public static Set<String> getStopwords() {
        if (STOP_WORDS == null) {
            synchronized(LOCK){
                if(STOP_WORDS == null){
                    STOP_WORDS = new HashSet<String>();

                    BufferedReader br = new BufferedReader(new InputStreamReader(StopWordsChecker.class.getClassLoader().getResourceAsStream("stop-words-english.txt")));
                    try {
                        String line = br.readLine();
                        while(line != null) {
                            String sword = line.trim();
                            STOP_WORDS.add(sword);

                            line = br.readLine();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally{
                        try {
                            br.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        }
        return STOP_WORDS;
    }
}
