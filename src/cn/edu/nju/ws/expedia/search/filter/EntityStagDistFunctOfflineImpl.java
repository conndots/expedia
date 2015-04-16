package cn.edu.nju.ws.expedia.search.filter;

import cn.edu.nju.ws.expedia.search.Searchable;
import cn.edu.nju.ws.expedia.search.SemTag;
import com.google.common.collect.ConcurrentHashMultiset;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Xiangqian on 2015/4/13.
 */
public class EntityStagDistFunctOfflineImpl implements EntityStagDistanceFunction {
    protected SemanticTagsFilter filter = null;
    protected ConcurrentHashMap<String, Map<String, Float>> entityTagDistances = null;
    protected ConcurrentHashMultiset<String> tagTaggingStats = null;
    protected int maximumTagging = 0;

    static EntityStagDistanceFunction getInstance(SemanticTagsFilter filter) {
        EntityStagDistFunctOfflineImpl ins = new EntityStagDistFunctOfflineImpl();
        ins.filter = filter;
        return ins;
    }
    public void initiation(List<Searchable> entities) {
        this.entityTagDistances = new ConcurrentHashMap<String, Map<String, Float>>(entities.size());
        final ConcurrentHashMap<String, Map<String, Float>> map = this.entityTagDistances;

        this.tagTaggingStats = ConcurrentHashMultiset.create();
        final ConcurrentHashMultiset<String> counts = this.tagTaggingStats;

        ExecutorService exeServ = Executors.newFixedThreadPool(2);
        final SemanticTagsFilter f = this.filter;
        for (final Searchable entity : entities) {
            exeServ.execute(new Runnable() {

                /**
                 * When an object implementing interface <code>Runnable</code> is used
                 * to create a thread, starting the thread causes the object's
                 * <code>run</code> method to be called in that separately executing
                 * thread.
                 * <p/>
                 * The general contract of the method <code>run</code> is that it may
                 * take any action whatsoever.
                 *
                 * @see Thread#run()
                 */
                public void run() {
                    Set<SemTag> tags = entity.getTaggedSemTags(f);
                    String uri = entity.getURI();
                    for (SemTag tag : tags) {
                        String tid = tag.getIdentifier();
                        counts.add(tid, 1);
                        int dist = EntityStagDistCache.getInstance().getDistanceOfEntityAndTag(uri, tid);
                        Map<String, Float> inner = map.get(uri);
                        if (inner == null) {
                            inner = new HashMap<String, Float>();
                            map.put(uri, inner);
                        }
                        inner.put(tag.getIdentifier(), 0f + dist);
                    }
                }
            });
        }

        exeServ.shutdown();
        while (! exeServ.isTerminated());

        for (String tagID : this.tagTaggingStats) {
            int count = this.tagTaggingStats.count(tagID);
            if (count > this.maximumTagging) {
                this.maximumTagging = count;
            }
        }
    }

    public float getDistanceOfEntityAndStag(Searchable entity, SemTag stag) {
        String uri = entity.getURI();
        String tagID = stag.getIdentifier();
        Map<String, Float> inner = this.entityTagDistances.get(uri);
        if (inner == null)
            return 1;
        Float dist = inner.get(tagID);
        if (dist != null)
            return dist;
        return 1;
    }

    public int getSemTagTaggingCount(SemTag stag) {
        return 0;
    }

    public int getMaximumSemTagTaggingCount() {
        return 0;
    }
}
