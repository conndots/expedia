package cn.edu.nju.ws.expedia.model.rdf.qstore.cache;

import cn.edu.nju.ws.expedia.database.DBConnectionFactory;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Xiangqian on 2015/4/8.
 */
public class UriHashCache {
    private static UriHashCache INSTANCE = null;
    private static final Object LOCK = new Object();

    public static UriHashCache getInstance() {
        if (INSTANCE == null) {
            synchronized (LOCK) {
                if (INSTANCE == null) {
                    INSTANCE = new UriHashCache();
                }
            }
        }
        return INSTANCE;
    }

    private LoadingCache<String, String> cache = null;
    private UriHashCache() {
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(20000)
                .expireAfterAccess(20, TimeUnit.MINUTES)
                .build(new CacheLoader<String, String>() {

                    @Override
                    public String load(String s) throws Exception {
                        return UriHashCache.getUriFromHash(s);
                    }
                });
    }
    private static String getUriFromHash(String uriHash) {
        String uri = "";
        String sql = "SELECT uri FROM uri WHERE uri_hash=?;";
        Connection conn = DBConnectionFactory.getInstance().getDefaultDBConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, uriHash);
            rs = ps.executeQuery();

            if(rs.next()) {
                uri = rs.getString(1);
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
        return uri;
    }

    public String getUriFromHashID(String uriHash) {
        String uri = this.cache.getUnchecked(uriHash);
        if(uri.length() == 0)
            return null;
        return uri;
    }
}
