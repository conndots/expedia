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
public class HyponymFactory {
    private static HyponymFactory INSTANCE = null;
    private static final Object LOCK = new Object();

    public static HyponymFactory getInstance(){
        if(INSTANCE == null){
            synchronized(LOCK){
                if(INSTANCE == null){
                    INSTANCE = new HyponymFactory();
                }
            }
        }
        return INSTANCE;
    }

    private LoadingCache<String, Set<String>> cache = null;
    private HyponymFactory(){
        this.cache = CacheBuilder.newBuilder().maximumSize(10000)
//                .expireAfterAccess(15, TimeUnit.MINUTES)
                .build(new CacheLoader<String, Set<String>>(){

                    @Override
                    public Set<String> load(String sid) throws Exception {
                        List<TwoTuple<String, String>> hypos =
                                WordnetQueryAgentFactory.getDefaultAgentInstance().getPointersOfSynset(sid, NounSynsetPointer.HYPONYM);
                        Set<String> hypoIDs = new HashSet<String>(hypos.size());
                        for(TwoTuple<String, String> tuple : hypos){
                            hypoIDs.add(tuple.getSecond());
                        }
                        return hypoIDs;
                    }
                });
    }

    public boolean hasDirectHyponymRelation(String superSID, String subSID) throws ExecutionException {
        Set<String> subs = this.cache.get(superSID);
        return subs.contains(subSID);
    }

    public Set<String> getHyponymsOfSynset(String synsetID) throws ExecutionException {
        return this.cache.get(synsetID);
    }
}
