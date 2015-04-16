package cn.edu.nju.ws.expedia.search.filter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import cn.edu.nju.ws.expedia.database.DBConnectionFactory;
import cn.edu.nju.ws.expedia.search.Searchable;
import cn.edu.nju.ws.expedia.search.SemTag;
import cn.edu.nju.ws.expedia.util.MD5;
import cn.edu.nju.ws.expedia.util.NoValueException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class EntityStagDistComputingCache {
	private static EntityStagDistComputingCache INSTANCE = null;
    private static Object LOCK = new Object();

    public static EntityStagDistComputingCache getInstance() {
        if (INSTANCE == null) {
            synchronized (LOCK) {
                INSTANCE = new EntityStagDistComputingCache();
            }
        }
        return INSTANCE;
    }

    private Cache<String, Map<String, Float>> cache = null;
    private EntityStagDistComputingCache() {
        this.cache = CacheBuilder.newBuilder().maximumSize(3000)
                .expireAfterAccess(20, TimeUnit.MINUTES)
                .build();
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
    
    public Map<String, Float> getDistancesOfEntityAndTags(final Searchable entity, final SemanticTagsFilter filter) {
    	try {
			Map<String, Float> map = this.cache.get(entity.getIdentifier(), new Callable<Map<String, Float>>() {

				@Override
				public Map<String, Float> call() throws Exception {
					Set<SemTag> tags = entity.getTaggedSemTags(filter);
					Map<String, Float> distances = EntityStagDistanceFunctionDefaultImpl.getDistance(tags);
					return distances;
				}
				
			});
			return map;
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
        return new HashMap<String, Float>();
    }

    public float getDistanceOfEntityAndTag(final Searchable entity, final String tagID, final SemanticTagsFilter filter) {
        try {
			Map<String, Float> map = this.cache.get(entity.getIdentifier(), new Callable<Map<String, Float>>() {

				@Override
				public Map<String, Float> call() throws Exception {
					Set<SemTag> tags = entity.getTaggedSemTags(filter);
					Map<String, Float> distances = EntityStagDistanceFunctionDefaultImpl.getDistance(tags);
					return distances;
				}
				
			});
			Float distance = map.get(tagID);
			if (distance == null)
				return 1f;
			return distance;
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
        return 1f;
    }

}
