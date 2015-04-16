package cn.edu.nju.ws.expedia.model.rdf.estore.cache;

import cn.edu.nju.ws.expedia.database.VirtuosoGraphFactory;
import cn.edu.nju.ws.expedia.model.rdf.Node;
import cn.edu.nju.ws.expedia.model.rdf.URIResource;
import cn.edu.nju.ws.expedia.model.rdf.Util;
import cn.edu.nju.ws.expedia.model.rdf.estore.EStoreNodeFactory;
import cn.edu.nju.ws.expedia.util.NoValueException;
import cn.edu.nju.ws.expedia.util.TwoTuple;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Xiangqian on 2015/4/9.
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
            this.cache = CacheBuilder.newBuilder().maximumSize(1000)
                    .expireAfterAccess(10, TimeUnit.MINUTES)
                    .build(new CacheLoader<String, List<TwoTuple<URIResource, Node>>>(

                    ) {
                        @Override
                        public List<TwoTuple<URIResource, Node>> load(String s) throws Exception {
                            return TripleCache.loadTriplesFromDB(s);
                        }
                    });
        }

        private static List<TwoTuple<URIResource, Node>> loadTriplesFromDB(String uri) throws NoValueException {
            List<TwoTuple<URIResource, Node>> triples = new ArrayList<TwoTuple<URIResource, Node>>();

            VirtGraph vgraph = VirtuosoGraphFactory.getVirtGraph();
            Query query = null;
            VirtuosoQueryExecution vqe = null;

            String sparql = "SELECT ?p ?o FROM <" + VirtuosoGraphFactory.DBPEDIA_2014 + "> WHERE {" +
                    "<%s> ?p ?o. }";
            sparql = String.format(sparql, uri);
            try {
                query = QueryFactory.create(sparql);
                vqe = VirtuosoQueryExecutionFactory.create(query, vgraph);
                com.hp.hpl.jena.query.ResultSet rs = vqe.execSelect();
                QuerySolution qsol = null;

                while (rs.hasNext()) {
                    qsol = rs.nextSolution();
                    Resource p = qsol.getResource("p");
                    RDFNode o = qsol.get("o");

                    URIResource prop = EStoreNodeFactory.getInstance().getProperty(p.getURI());
                    Node obj = null;
                    if (o.isLiteral()) {
                        if (o.asLiteral().getLanguage() != null && (! o.asLiteral().getLanguage().equals("en")))
                            continue;
                        obj = EStoreNodeFactory.getInstance().getLiteral(o.asLiteral().getString());
                    }
                    else if(o.isURIResource()) {
                        if (p.getURI().equals("http://purl.org/dc/terms/subject")) {
                            obj = EStoreNodeFactory.getInstance().getCategory(o.asResource().getURI());
                        }
                        else if (p.getURI().equals(Util.RDF_NS + "type")) {
                            obj = EStoreNodeFactory.getInstance().getOntClass(o.asResource().getURI());
                        }
                        else {
                            obj = EStoreNodeFactory.getInstance().getEntity(o.asResource().getURI());
                        }
                    }
                    else {
                        continue;
                    }
                    triples.add(new TwoTuple<URIResource, Node>(prop, obj));
                }
            } catch(RuntimeException e){
                e.printStackTrace();
            }finally {
                if (vqe != null)
                    vqe.close();
                vgraph.close();
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
