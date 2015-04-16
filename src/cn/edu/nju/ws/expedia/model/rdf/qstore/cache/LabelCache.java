package cn.edu.nju.ws.expedia.model.rdf.qstore.cache;

import cn.edu.nju.ws.expedia.database.DBConnectionFactory;
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
 * Created by Xiangqian on 2015/4/8.
 */
public class LabelCache {
    private static LabelCache INSTANCE = null;
    private static final Object LOCK = new Object();

    public static LabelCache getInstance() {
        if (INSTANCE == null) {
            synchronized (LOCK) {
                if (INSTANCE == null) {
                    INSTANCE = new LabelCache();
                }
            }
        }
        return INSTANCE;
    }

    private LoadingCache<String, String> cache = null;
    private LabelCache() {
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(5000)
                .expireAfterAccess(15, TimeUnit.MINUTES)
                .build(new CacheLoader<String, String>() {

                    @Override
                    public String load(String s) throws Exception {
                        return LabelCache.getLabelFromHash(s);
                    }
                });
    }
    private static String getLabelFromHash(String uriHash) throws NoValueException {
        String label = null;
        String sql = "SELECT label FROM label WHERE uri_hash=?;";
        Connection conn = DBConnectionFactory.getInstance().getDefaultDBConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, uriHash);
            rs = ps.executeQuery();

            if(rs.next()) {
                label = rs.getString(1);
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
        if (label == null)
            throw new NoValueException();
        return label;
    }

    public String getLabelFromUriHash(String uriHash, boolean loadFromDB) {
        String l = null;
        if (loadFromDB)
            try {
                this.cache.get(uriHash);
            } catch (ExecutionException e) {
                return null;
            }
        else
            this.cache.getIfPresent(uriHash);
        return l;
    }
}
