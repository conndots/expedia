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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Xiangqian on 2015/4/12.
 */
public class TypeID2SynsetIDCache {
    private static TypeID2SynsetIDCache INSTANCE = null;
    private final static Object LOCK = new Object();

    public static TypeID2SynsetIDCache getInstance() {
        if (INSTANCE == null) {
            synchronized (LOCK) {
                if (INSTANCE == null) {
                    INSTANCE = new TypeID2SynsetIDCache();
                }
            }
        }
        return INSTANCE;
    }

    private LoadingCache<String, String> cache = null;
    private TypeID2SynsetIDCache() {
        this.cache = CacheBuilder.newBuilder().maximumSize(5000)
                .expireAfterAccess(15, TimeUnit.MINUTES)
                .build(new CacheLoader<String, String>() {

                    @Override
                    public String load(String s) throws Exception {
                        return getMappedElements(s);
                    }
                });

    }
    private static String getMappedElements(String input) throws NoValueException {
        String sql = "SELECT synset_id FROM class_to_wordnet_mappings WHERE class_hash=?;";
        String mapped = null;
        Connection conn = DBConnectionFactory.getInstance().getDefaultDBConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, input);
            rs = ps.executeQuery();
            if (rs.next()) {
                mapped = rs.getString(1);
            }
        } catch(SQLException e) {
            e.printStackTrace();
            return "E";
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

        if (mapped == null)
            throw new NoValueException();

        return mapped;
    }

    public String getMappedWordnetIDsFromTypeID(String typeID) {
        String wnID = null;
        try {
            wnID = this.cache.get(typeID);
        } catch (ExecutionException e) {
            return null;
        }
        if (wnID.equals("E")) {
            this.cache.refresh(typeID);
            wnID = this.cache.getUnchecked(typeID);
            if (wnID.equals("E"))
                return null;
        }
        return wnID;
    }

    void addMappingToCache(String typeID, String sid) {
        this.cache.put(typeID, sid);
    }
}
