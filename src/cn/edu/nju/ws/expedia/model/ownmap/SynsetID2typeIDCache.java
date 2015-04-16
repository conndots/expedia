package cn.edu.nju.ws.expedia.model.ownmap;

import cn.edu.nju.ws.expedia.database.DBConnectionFactory;
import cn.edu.nju.ws.expedia.util.NoValueException;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Xiangqian on 2015/4/12.
 */
public class SynsetID2typeIDCache {
    private static SynsetID2typeIDCache INSTANCE = null;
    private final static Object LOCK = new Object();

    public static SynsetID2typeIDCache getInstance() {
        if (INSTANCE == null) {
            synchronized (LOCK) {
                if (INSTANCE == null) {
                    INSTANCE = new SynsetID2typeIDCache();
                }
            }
        }
        return INSTANCE;
    }

    private LoadingCache<String, List<String>> cache = null;
    private SynsetID2typeIDCache() {
        this.cache = CacheBuilder.newBuilder().maximumSize(4000)
                .expireAfterAccess(20, TimeUnit.MINUTES)
                .build(new CacheLoader<String, List<String>>() {
                    @Override
                    public List<String> load(String s) throws Exception {
                        return SynsetID2typeIDCache.getMappedElements(s);
                    }
                });
    }

    private static List<String> getMappedElements(String sid) throws NoValueException {
            String sql = "SELECT class_hash FROM class_to_wordnet_mappings WHERE synset_id=?;";
            List<String> mapped = new ArrayList<String>(5);
            Connection conn = DBConnectionFactory.getInstance().getDefaultDBConnection();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = conn.prepareStatement(sql);
                ps.setString(1, sid);
                rs = ps.executeQuery();
                while (rs.next()) {
                    mapped.add(rs.getString(1));
                }
            } catch(SQLException e) {
                e.printStackTrace();
            } finally {
                if (rs != null)
                    try {
                        rs.close();
                    }catch(SQLException e) {
                        e.printStackTrace();
                    }
                if (ps != null)
                    try {
                        ps.close();
                    }catch(SQLException e) {
                        e.printStackTrace();
                    }
                try {
                    conn.close();
                } catch(SQLException e) {
                    e.printStackTrace();
                }
            }

            if (mapped.size() == 0)
                throw new NoValueException();

            return mapped;
    }

    public List<String> getMappedTypeIDsFromSynsetID(String sid) {
        try {
            List<String> mapped = this.cache.get(sid);
            for (String tid : mapped) {
                TypeID2SynsetIDCache.getInstance().addMappingToCache(tid,  sid);
            }
            return mapped;
        } catch (ExecutionException e) {
            return null;
        }
    }
}
