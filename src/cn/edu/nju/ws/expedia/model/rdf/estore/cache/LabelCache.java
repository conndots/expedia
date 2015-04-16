package cn.edu.nju.ws.expedia.model.rdf.estore.cache;

import cn.edu.nju.ws.expedia.database.VirtuosoGraphFactory;
import cn.edu.nju.ws.expedia.model.rdf.Util;
import cn.edu.nju.ws.expedia.util.NoValueException;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Literal;
import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Xiangqian on 2015/4/9.
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
    private static String getLabelFromHash(String uri) throws NoValueException {
        String l = null;
        VirtGraph vgraph = VirtuosoGraphFactory.getVirtGraph();
        Query query = null;
        VirtuosoQueryExecution vqe = null;

        String sparql = "SELECT ?label FROM <" + VirtuosoGraphFactory.DBPEDIA_2014 + "> WHERE {" +
                "<%s> <%s> ?label. }";
        sparql = String.format(sparql, uri, Util.RDFS_NS + "label");
        try {
            query = QueryFactory.create(sparql);
            vqe = VirtuosoQueryExecutionFactory.create(query, vgraph);
            com.hp.hpl.jena.query.ResultSet rs = vqe.execSelect();
            QuerySolution qsol = null;

            while (rs.hasNext()) {
                qsol = rs.nextSolution();
                Literal label = qsol.getLiteral("label");

                if (label.getLanguage() == null || label.getLanguage().equals("en")) {
                    l = label.getString();
                    break;
                }
            }
        } catch(RuntimeException e){
            e.printStackTrace();
        }finally {
            if (vqe != null)
                vqe.close();
            vgraph.close();
        }
        if (l == null)
            throw new NoValueException();
        return l;
    }

    public String getLabelFromUri(String uri, boolean loadFromDB) {
        String l = null;
        if(loadFromDB)
            try {
                l = this.cache.get(uri);
            } catch (ExecutionException e) {
                return null;
            }
        else
            l = this.cache.getIfPresent(uri);
        return l;
    }
}
