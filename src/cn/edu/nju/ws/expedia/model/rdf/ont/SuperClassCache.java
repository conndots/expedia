package cn.edu.nju.ws.expedia.model.rdf.ont;

import cn.edu.nju.ws.expedia.database.VirtuosoGraphFactory;
import cn.edu.nju.ws.expedia.model.rdf.Util;
import cn.edu.nju.ws.expedia.util.NoValueException;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Resource;
import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Xiangqian on 2015/4/13.
 */
public class SuperClassCache {
    private static SuperClassCache INSTANCE = null;
    private static final Object LOCK = new Object();

    public static SuperClassCache getInstance() {
        if (INSTANCE == null) {
            synchronized (LOCK) {
                if (INSTANCE == null) {
                    INSTANCE = new SuperClassCache();
                }
            }
        }
        return INSTANCE;
    }

    private LoadingCache<String, List<String>> cache = null;
    private SuperClassCache() {
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(3000).expireAfterAccess(15, TimeUnit.MINUTES)
                .build(new CacheLoader<String, List<String>>() {
                    @Override
                    public List<String> load(String s) throws Exception {
                        return SuperClassCache.getSuperClassOf(s);
                    }
                });
    }
    private static List<String> getSuperClassOf(String uri) throws NoValueException {
        List<String> supers = new ArrayList<String>(3);

        String sparql = "SELECT ?super FROM <%s> WHERE {" +
                "<%s> <" + Util.RDFS_NS + "subClassOf> ?super.}";
        sparql = String.format(sparql, VirtuosoGraphFactory.DBPEDIA_ONTOLOGY, uri);

        VirtGraph vgraph = VirtuosoGraphFactory.getVirtGraph();
        VirtuosoQueryExecution vqe = null;
        Query q = null;
        ResultSet rs = null;

        try {
            q = QueryFactory.create(sparql);
            vqe = VirtuosoQueryExecutionFactory.create(q, vgraph);
            rs = vqe.execSelect();
            QuerySolution qsol = null;
            while (rs.hasNext()) {
                qsol = rs.nextSolution();
                Resource sup = qsol.getResource("super");
                if (! sup.isAnon())
                    supers.add(sup.getURI());
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            if (vqe != null)
                vqe.close();
            vgraph.close();
        }

        if (supers.size() == 0)
            throw new NoValueException();
        return supers;
    }

    public List<String> getSuperClassesOf(String uri) {
        try {
            List<String> supers = this.cache.get(uri);
            return supers;
        } catch (ExecutionException e) {
            return new ArrayList<String>(0);
        }
    }
}
