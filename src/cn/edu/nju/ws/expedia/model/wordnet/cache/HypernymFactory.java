package cn.edu.nju.ws.expedia.model.wordnet.cache;

import cn.edu.nju.ws.expedia.model.wordnet.NounSynsetPointer;
import cn.edu.nju.ws.expedia.model.wordnet.query.WordnetQueryAgentFactory;
import cn.edu.nju.ws.expedia.util.TwoTuple;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Created by Xiangqian on 2015/1/10.
 */
public class HypernymFactory {
    private static HypernymFactory INSTANCE = null;
    private static final Object LOCK = new Object();
    public static HypernymFactory getInstance(){
        if(INSTANCE == null){
            synchronized(LOCK){
                if(INSTANCE == null){
                    INSTANCE = new HypernymFactory();
                }
            }
        }
        return INSTANCE;
    }

    private LoadingCache<String, Set<String>> cache = null;
    private HypernymFactory(){
        this.cache = CacheBuilder.newBuilder().maximumSize(10000)
//                .expireAfterAccess(15, TimeUnit.MINUTES)
                    .build(new CacheLoader<String, Set<String>>(){

                    @Override
                    public Set<String> load(String sid) throws Exception {
                        List<TwoTuple<String, String>> hypers =
                                WordnetQueryAgentFactory.getDefaultAgentInstance().getPointersOfSynset(sid, NounSynsetPointer.HYPERNYM);
                        Set<String> hyperIDs = new HashSet<String>(hypers.size());
                        for(TwoTuple<String, String> tuple : hypers){
                            hyperIDs.add(tuple.getSecond());
                        }
                        return hyperIDs;
                    }
                });
    }

    public boolean hasDirectHypernymRelation(String subSID, String superSID) throws ExecutionException {
        Set<String> supers = this.cache.get(subSID);
        return supers.contains(superSID);
    }

    public Set<String> getHypernymsOfSynset(String synsetID) throws ExecutionException {
        return this.cache.get(synsetID);
    }
}
