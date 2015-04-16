package cn.edu.nju.ws.expedia.model.wordnet.cache;

import cn.edu.nju.ws.expedia.model.wordnet.Synset;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.concurrent.ExecutionException;

/**
 * Created by Xiangqian on 2015/1/9.
 */
public class SynsetFactory {
    private static SynsetFactory INSTANCE = null;
    private static final Object LOCK = new Object();

    public static SynsetFactory getInstance(){
        if(INSTANCE == null){
            synchronized(LOCK){
                if(INSTANCE == null){
                    INSTANCE = new SynsetFactory();
                }
            }
        }
        return INSTANCE;
    }

    private LoadingCache<String, Synset> cache = null;
    private SynsetFactory(){
        this.cache = CacheBuilder.newBuilder().maximumSize(6000)
//                .expireAfterAccess(15, TimeUnit.MINUTES)
                .build(new CacheLoader<String, Synset>(){

                    @Override
                    public Synset load(String sid) throws Exception {
                        return new Synset(sid);
                    }
                });
    }

    public Synset getSynset(String sid)  {
        if(! Synset.isSynsetID(sid))
            return null;
        Synset synset = null;
        try {
            synset = this.cache.get(sid);
        } catch (RuntimeException e) {
            e.printStackTrace();
            System.err.println("WRONG SID: " + sid);
            return null;
        } catch (ExecutionException e) {
            e.printStackTrace();
            System.err.println("WRONG SID: " + sid);
            return null;
        }
        return synset;
    }
}
