package cn.edu.nju.ws.expedia.search.filter;

import cn.edu.nju.ws.expedia.database.DBConnectionFactory;
import cn.edu.nju.ws.expedia.util.MD5;
import cn.edu.nju.ws.expedia.util.NoValueException;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Xiangqian on 2015/4/13.
 */
public class EntityStagDistCache {
    private static EntityStagDistCache INSTANCE = null;
    private static Object LOCK = new Object();

    public static EntityStagDistCache getInstance() {
        if (INSTANCE == null) {
            synchronized (LOCK) {
                INSTANCE = new EntityStagDistCache();
            }
        }
        return INSTANCE;
    }

    private LoadingCache<String, Integer> cache = null;
    private EntityStagDistCache() {
        this.cache = CacheBuilder.newBuilder().maximumSize(6000)
                .expireAfterAccess(20, TimeUnit.MINUTES)
                .build(new CacheLoader<String, Integer>() {
                    @Override
                    public Integer load(String s) throws Exception {
                        String[] splits = s.split(";");
                        return EntityStagDistCache.getDistanceOf(splits[0], splits[1]);
                    }
                });
    }
    private static int getDistanceOf(String uriHash, String sid) throws NoValueException {
        Connection conn = DBConnectionFactory.getInstance().getDefaultDBConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        Integer dist = null;

        String sql = "SELECT distance FROM entity_tag_distance WHERE uri_hash=? AND tag_id=?;";
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, uriHash);
            ps.setString(2, sid);
            rs = ps.executeQuery();
            if (rs.next()) {
                dist = rs.getInt(1);
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

        if (dist == null)
            throw new NoValueException();
        return dist;
    }

    public int getDistanceOfEntityAndTag(String uri, String tagID) {
        String uriHash = MD5.makeMD5Str(uri);
        String key = uriHash + ";" + tagID;
        try {
            int dist = this.cache.get(key);
            return dist;
        } catch (ExecutionException e) {
            return 1;
        }
    }
}
