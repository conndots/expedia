package cn.edu.nju.ws.expedia.model.wordnet.query;

import cn.edu.nju.ws.expedia.model.wordnet.Synset;
import cn.edu.nju.ws.expedia.util.FourTuple;
import cn.edu.nju.ws.expedia.util.TwoTuple;

import java.util.List;

/**
 * Created by Xiangqian on 2015/1/9.
 */
public interface WordnetQueryAgent {
    public List<FourTuple<Integer, String, Character, Integer>> getWordsOfSynset(String synsetID);
    public List<TwoTuple<String, String>> getPointersOfSynset(String synsetID, String ptrType);
    public Synset getSynset(String sid);
    public void loadSynset(Synset synset);
}
