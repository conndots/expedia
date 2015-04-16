package cn.edu.nju.ws.expedia.model.rdf.qstore.cache;

import cn.edu.nju.ws.expedia.database.DBConnectionFactory;
import cn.edu.nju.ws.expedia.model.rdf.Node;
import cn.edu.nju.ws.expedia.model.rdf.URIResource;
import cn.edu.nju.ws.expedia.model.rdf.Util;
import cn.edu.nju.ws.expedia.model.rdf.qstore.QStoreNodeFactory;
import cn.edu.nju.ws.expedia.util.NoValueException;
import cn.edu.nju.ws.expedia.util.TwoTuple;
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
 * Created by Xiangqian on 2015/4/8.
 */
public class TripleCache {
    private static TripleCache INSTANCE = null;
    private final static Object LOCK = new Object();
    public static TripleCache getInstance() {
        if (INSTANCE == null) {
            synchronized(LOCK) {
                if (INSTANCE == null) {
                    INSTANCE = new TripleCache();
                }
            }
        }
        return INSTANCE;
    }

    private LoadingCache<String, List<TwoTuple<URIResource, Node>>> cache = null;
    private TripleCache() {
        this.cache = CacheBuilder.newBuilder().maximumSize(2000)
                .expireAfterAccess(20, TimeUnit.MINUTES)
                        .build(new CacheLoader<String, List<TwoTuple<URIResource, Node>>>(

                        ) {
                            @Override
                            public List<TwoTuple<URIResource, Node>> load(String s) throws Exception {
                                return TripleCache.loadTriplesFromDB(s);
                            }
                        });
    }

    private static List<TwoTuple<URIResource, Node>> loadTriplesFromDB(String uriHash) throws NoValueException {
        String sql = "SELECT p, o, lang FROM quadruple WHERE s = ?;";
        Connection conn = DBConnectionFactory.getInstance().getDefaultDBConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<TwoTuple<URIResource, Node>> triples = new ArrayList<TwoTuple<URIResource, Node>>();

        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, "u" + uriHash);
            rs = ps.executeQuery();

            while(rs.next()) {
                String p = rs.getString(1);
                String o = rs.getString(2);
                String lang = rs.getString(3);

                URIResource prop = QStoreNodeFactory.getInstance().getProperty(p);
                Node obj = null;
                if(o.charAt(0) == 'u') {
                    if(p.equals(Util.PURL_SUBJECT_MD5))
                        obj = QStoreNodeFactory.getInstance().getCategory(o.substring(1));
                    else if(p.equals(Util.RDF_TYPE_MD5)) {
                        obj = QStoreNodeFactory.getInstance().getOntClass(o.substring(1));
                    }
                    obj = QStoreNodeFactory.getInstance().getEntity(o.substring(1));
                }
                else if(o.charAt(0) == 'l') {
                    if (lang != null && (! lang.equals("en")))
                        continue;
                    String content = o.substring(1);
                    obj = QStoreNodeFactory.getInstance().getLiteral(content);
                }
                else
                    continue;

                triples.add(new TwoTuple<URIResource, Node>(prop, obj));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (triples.size() == 0)
            throw new NoValueException();
        return triples;
    }

    public List<TwoTuple<URIResource, Node>> getTriplesOfSubject(final String uriHash, final boolean loadFromDB){
        List<TwoTuple<URIResource, Node>> triples = null;
        if (! loadFromDB) {
            triples = this.cache.getIfPresent(uriHash);
        }
        else {
            try {
                triples = this.cache.get(uriHash);
            } catch (ExecutionException e) {
                return null;
            }
        }
        return triples;
    }
}
