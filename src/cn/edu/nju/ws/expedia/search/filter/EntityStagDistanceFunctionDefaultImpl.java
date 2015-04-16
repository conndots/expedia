package cn.edu.nju.ws.expedia.search.filter;

import cn.edu.nju.ws.expedia.database.DBConnectionFactory;
import cn.edu.nju.ws.expedia.model.wordnet.cache.HypernymFactory;
import cn.edu.nju.ws.expedia.search.Searchable;
import cn.edu.nju.ws.expedia.search.SemTag;
import cn.edu.nju.ws.expedia.util.TwoTuple;
import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.HashMultimap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by Xiangqian on 2015/4/13.
 */
public class EntityStagDistanceFunctionDefaultImpl implements EntityStagDistanceFunction {
    protected SemanticTagsFilter filter = null;
    protected ConcurrentHashMap<String, Map<String, Float>> entityTagDistances = null;
    protected ConcurrentHashMultiset<String> tagTaggingStats = null;
    protected int maximumTagging = 0;

    static EntityStagDistanceFunction getInstance(SemanticTagsFilter filter) {
        EntityStagDistanceFunctionDefaultImpl ins = new EntityStagDistanceFunctionDefaultImpl();
        ins.filter = filter;
        return ins;
    }

    public void initiation(List<Searchable> entities) {
        this.entityTagDistances = new ConcurrentHashMap<String, Map<String, Float>>(entities.size());
        final ConcurrentHashMap<String, Map<String, Float>> map = this.entityTagDistances;
        this.tagTaggingStats = ConcurrentHashMultiset.create();
        final ConcurrentHashMultiset<String> counts = this.tagTaggingStats;
        final SemanticTagsFilter f = this.filter;

        ExecutorService exeServ = Executors.newFixedThreadPool(5);

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
                    Set<SemTag> tagged = entity.getTaggedSemTags(f);
                    for (SemTag tag : tagged) {
                    	counts.add(tag.getIdentifier());
                    }
                    Map<String, Float> tag2Dist = EntityStagDistComputingCache.getInstance().getDistancesOfEntityAndTags(entity, filter);
                    map.put(entity.getURI(), tag2Dist);
                }
            });
        }
        exeServ.shutdown();
        while(! exeServ.isTerminated());

        for (String tagID : this.tagTaggingStats) {
            int count = this.tagTaggingStats.count(tagID);
            if (count > this.maximumTagging) {
                this.maximumTagging = count;
            }
        }
        if (this.maximumTagging == 0)
        	this.maximumTagging = 1;
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
        int count = this.tagTaggingStats.count(stag);
        return count;
    }

    public int getMaximumSemTagTaggingCount() {
        return this.maximumTagging;
    }

    public static Map<String, Float> getDistance(Set<SemTag> taggedTags) {
        HashMap<String, Float> tag2Distance = new HashMap<String, Float>(taggedTags.size());
        
        HashSet<String> taggedTagIDs = new HashSet<String>();
        for (SemTag tag : taggedTags) {
        	taggedTagIDs.add(tag.getIdentifier());
        }

        HashSet<String> doned = new HashSet<String>();
        HashMultimap<String, String> tempGraph = HashMultimap.create();
        HashSet<String> minimals = new HashSet<String>(taggedTagIDs);

        for (String tagID : taggedTagIDs) {
            if (doned.contains(tagID))
                continue;
            doned.add(tagID);

            LinkedList<TwoTuple<String, String>> queue = new LinkedList<TwoTuple<String, String>>();
            queue.add(new TwoTuple<String, String>(tagID, tagID));
            while (! queue.isEmpty()) {
                TwoTuple<String, String> tuple = queue.pollFirst();
                String currSID = tuple.getFirst(),
                        lastSub = tuple.getSecond();

                if (! currSID.equals(tagID) && taggedTagIDs.contains(currSID)) {
                    tempGraph.put(lastSub, currSID);
                    lastSub = currSID;
                    minimals.remove(currSID);
                    doned.add(currSID);
                }

                Set<String> hypernyms = null;
                try {
                    hypernyms = HypernymFactory.getInstance().getHypernymsOfSynset(currSID);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                if (hypernyms != null) {
                    for (String hyper : hypernyms) {
                        queue.add(new TwoTuple<String, String>(hyper, lastSub));
                    }
                    hypernyms = null;
                }
            }
        }

        LinkedList<TwoTuple<String, Integer>> queue = new LinkedList<TwoTuple<String, Integer>>();
        for (String min : minimals) {
            queue.add(new TwoTuple<String, Integer>(min, 1));
        }
        while (! queue.isEmpty()) {
            TwoTuple<String, Integer> tuple = queue.pollFirst();
            String currSID = tuple.getFirst();
            int distance = tuple.getSecond();
            tag2Distance.put(currSID, 0f + distance);

            Set<String> nexts = tempGraph.get(currSID);
            for (String next : nexts) {
                queue.add(new TwoTuple<String, Integer>(next, distance + 1));
            }
        }

        doned.clear();
        doned = null;
        minimals.clear();
        minimals = null;
        tempGraph.clear();
        tempGraph = null;

        return tag2Distance;
    }

    public static Set<String> selectRandomSynsets(int num) {
        Set<String> selected = new HashSet<String>();

        Connection conn = DBConnectionFactory.getInstance().getDefaultDBConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement("SELECT id FROM synsets WHERE pos='n' ORDER BY RAND() LIMIT " + num);
            rs = ps.executeQuery();
            while (rs.next()) {
                String sid = rs.getString(1);
                selected.add(sid);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            if (ps != null)
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return selected;
    }

    public static void main(String[] args) {
//        Set<String> tags = selectRandomSynsets(30);
//        long start = System.nanoTime();
//        Map<String, Float> dist = EntityStagDistanceFunctionDefaultImpl.getDistance(tags);
//        long end = System.nanoTime();
//
//        System.out.println(dist.toString());
//        System.out.println(TimeUnit.NANOSECONDS.toSeconds(end - start));
    }
}
