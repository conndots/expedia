package cn.edu.nju.ws.expedia.offline.index;

import cn.edu.nju.ws.expedia.database.DBConnectionFactory;
import cn.edu.nju.ws.expedia.database.VirtuosoGraphFactory;
import cn.edu.nju.ws.expedia.model.rdf.Literal;
import cn.edu.nju.ws.expedia.model.rdf.Node;
import cn.edu.nju.ws.expedia.model.rdf.URIResource;
import cn.edu.nju.ws.expedia.model.rdf.estore.EStoreNodeFactory;
import cn.edu.nju.ws.expedia.model.rdf.ont.OntologyAgent;
import cn.edu.nju.ws.expedia.util.MD5;
import cn.edu.nju.ws.expedia.util.TwoTuple;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Resource;

import cn.edu.nju.ws.expedia.offline.lucene.IndexSearcherFactory;
import cn.edu.nju.ws.expedia.offline.lucene.IndexWriterFactory;

import org.apache.log4j.Logger;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;

import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Xiangqian on 2015/4/10.
 */
public class EntityIndexer {
    public static final String ID_FIELD = "id",
            LABEL_FIELD = "label",
            CONTENT_FIELD = "content",
            TYPEID_FIELD = "typeID",
            PAGERANK_SCORE_FIELD_LOGGED = "pgscore";

    public final static double PG_LOG = Math.log(10);

    public static List<TwoTuple<String, Double>> getEntitiesWithPGScoreToIndex() {
        List<TwoTuple<String, Double>> toIndex = new ArrayList<TwoTuple<String, Double>>();

        String sparql = "SELECT ?e ?pg FROM <%s> WHERE {" +
                "?e <http://purl.org/voc/vrank#hasRank> ?bn. " +
                "?bn <http://purl.org/voc/vrank#rankValue> ?pg. " +
                "FILTER (?pg > 0) " +
                "}";

        sparql = String.format(sparql, VirtuosoGraphFactory.DBPEDIA_2014_PAGERANK);

        VirtGraph vgraph = VirtuosoGraphFactory.getVirtGraph();
        Query query = null;
        VirtuosoQueryExecution vqe = null;
        com.hp.hpl.jena.query.ResultSet rs = null;

        try {
            query = QueryFactory.create(sparql);
            vqe = VirtuosoQueryExecutionFactory.create(query, vgraph);
            rs = vqe.execSelect();

            QuerySolution sol = null;
            while (rs.hasNext()) {
                sol = rs.nextSolution();
                Resource e = sol.getResource("e");
                com.hp.hpl.jena.rdf.model.Literal pg = sol.getLiteral("pg");
                if (e.isAnon())
                    continue;
                String euri = e.getURI();
                if (euri.contains("/Category:"))
                    continue;
                double pgScore = pg.getDouble();
                pgScore = 0.01 + (Math.log(1d + pgScore) / PG_LOG);
                toIndex.add(new TwoTuple<String, Double>(euri, pgScore));
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            if (vqe != null)
                vqe.close();
            vgraph.close();
        }

        return toIndex;
    }

    public static List<String> getEntitiesToIndex1() {
        List<String> toIndex = new ArrayList<String>();

        String sql = "SELECT uri FROM entity WHERE refdoc_hash='0146bae801425f30e965f706c4312e25';";
        Connection conn = DBConnectionFactory.getInstance().getDefaultDBConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while(rs.next()) {
                String uri = rs.getString(1);
                if (uri.contains("/Category:"))
                    continue;
                toIndex.add(uri);
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
        return toIndex;
    }

    private static String getRepeatedString(String toRepeat, int time, String separator) {
        if (time == 1) {
            return toRepeat;
        }
        StringBuilder repeated = new StringBuilder();
        for(int i = 0; i < time; i ++) {
            repeated.append(toRepeat).append(" ");
        }
        return repeated.toString();
    }

    private static double getPagerankScoreForEntity1(String uri) {
        String sql = "SELECT pr_val FROM dbpedia_2014_pagerank_rankval WHERE uri_hash=?;";
        Connection conn = DBConnectionFactory.getInstance().getDefaultDBConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, MD5.makeMD5Str(uri));
            rs = ps.executeQuery();
            if (rs.next()) {
                return Math.log(1d + rs.getDouble(1));
            }
            return 0d;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0d;
    }

    private static double getPagerankScoreForEntity(String uri) {
        String sparql = "SELECT ?o FROM <%s> WHERE {\n" +
                "<%s> <http://purl.org/voc/vrank#hasRank> ?bn.\n" +
                "?bn <http://purl.org/voc/vrank#rankValue> ?o.\n" +
                "}";
        sparql = String.format(sparql, VirtuosoGraphFactory.DBPEDIA_2014_PAGERANK, uri);
        VirtGraph vgraph = VirtuosoGraphFactory.getVirtGraph();
        Query query = null;
        VirtuosoQueryExecution vqe = null;
        com.hp.hpl.jena.query.ResultSet rs = null;
        double ret = 0d;
        try {
            query = QueryFactory.create(sparql);
            vqe = VirtuosoQueryExecutionFactory.create(query, vgraph);
            rs = vqe.execSelect();
            if (rs.hasNext()) {
                QuerySolution qsol = rs.nextSolution();
                com.hp.hpl.jena.rdf.model.Literal score = qsol.getLiteral("o");
                double scored = score.getDouble();
                ret = Math.log(1d + scored);
            }
        } catch (RuntimeException e) {
            LOGGER.error(e.getMessage());
        } finally {
            if (vqe != null)
                vqe.close();
            vgraph.close();
        }
        return ret;
    }

    private static final Logger LOGGER = Logger.getLogger(EntityIndexer.class);

    private static void indexURIResource(URIResource ures, double pg) throws IOException {
        IndexWriter iwriter = IndexWriterFactory.getWriter();

        String uri = ures.getURI();
        List<TwoTuple<URIResource, Node>> descs = ures.getDescriptions(true);
        if (descs == null) {
            System.err.println("No descriptions for:" + uri);
            return;
        }

        Document entityDoc = new Document();
        String label = ures.getLabel(false);
        List<URIResource> types = ures.getTypes(true);


        Field idField = new StringField(ID_FIELD, uri, Field.Store.YES);
        entityDoc.add(idField);

        HashSet<String> typeUris = new HashSet<String>();
        for (URIResource type : types) {
            typeUris.add(type.getURI());
            typeUris.addAll(OntologyAgent.getSuperClasses(type.getURI(), false));
        }
        for (String turi : typeUris) {
            String thash = MD5.makeMD5Str(turi);
            Field typeField = new StringField(TYPEID_FIELD, thash, Field.Store.YES);
            entityDoc.add(typeField);
        }

        Field labelField = new TextField(LABEL_FIELD, label, Field.Store.YES);
        labelField.setBoost(2f);
        entityDoc.add(labelField);

        if (descs == null) {
            System.err.println("null");
            return;
        }
        for (TwoTuple<URIResource, Node> triple : descs) {
            StringBuilder contentStr = new StringBuilder();
            contentStr.append(label).append(" ").append(triple.getFirst().getLabel(false)).append(" ");
            Node o = triple.getSecond();
            String ocontent = null;
            float boost = 0.5f;
            if(o.getNodeType().equals(Node.CATEGORY)) {
                ocontent = ((URIResource) o).getLabel(false);
                boost = 1f;
            }
            else if (o.getNodeType().equals(Node.ENTITY)) {
                ocontent = ((URIResource) o).getLabel(false);
                boost = 0.9f;
            }
            else if (o.getNodeType().equals(Node.LITERAL)) {
                ocontent = ((Literal) o).getContent();
                boost = 0.8f;
            }
            else if (o.getNodeType().equals(Node.ONT_CLASS)) {
                ocontent = ((URIResource) o).getLabel(false);
                boost = 1.1f;
            }
            if (ocontent != null)
                contentStr.append(ocontent);
            contentStr.append(". ");
            Field contentField = new TextField(CONTENT_FIELD, contentStr.toString(), Field.Store.NO);
            contentField.setBoost(boost);
            entityDoc.add(contentField);
        }

//        double pagerankScore = EntityIndexer.getPagerankScoreForEntity(uri);
        Field prScoreField=  new DoubleField(PAGERANK_SCORE_FIELD_LOGGED, pg, Field.Store.YES);
        entityDoc.add(prScoreField);

        iwriter.addDocument(entityDoc);
        iwriter.commit();
    }

    public static void indexProcess(int threadNum, int start) {
        List<TwoTuple<String, Double>> entityIDs = EntityIndexer.getEntitiesWithPGScoreToIndex();
        LOGGER.debug("to index " + entityIDs.size() + " entities.");
        ExecutorService exeServ = Executors.newFixedThreadPool(threadNum);

        for (int i = start; i < entityIDs.size(); i ++) {
            final TwoTuple<String, Double> tuple = entityIDs.get(i);
            final int count = i;
            exeServ.execute(new Runnable() {

                /**
                 * When an object implementing interface <code>Runnable</code> is used
                 * to create a thread, starting the thread causes the object's
                 * <code>run</code> method to be called in that separately executing
                 * thread.
                 * <p/>
                 * The general contract of the method <code>run</code> is that it may
                 * take any action whatsoever.
                 *
                 * @see Thread#run()
                 */
                public void run() {
                    URIResource res = EStoreNodeFactory.getInstance().getEntity(tuple.getFirst());

                    try {
                        EntityIndexer.indexURIResource(res, tuple.getSecond());
                        LOGGER.debug("" + count + " entity " + res.getURI() + " finish.");
                    } catch (Exception e) {
                        e.printStackTrace();
                        LOGGER.error(String.format("Error %s in indexing entity <%s>.", e.getMessage(), res.getURI()));
                    }
                }
            });
        }
        exeServ.shutdown();
        while(! exeServ.isTerminated());

        try {
            IndexWriterFactory.closeWriter();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int tnum = 1, start = 0;
        if (args.length == 0) {
            tnum = Runtime.getRuntime().availableProcessors();
        }
        else {
            tnum = Integer.valueOf(args[0]);
            start = Integer.valueOf(args[1]);
        }
        EntityIndexer.indexProcess(tnum, start);
//        URIResource res = EStoreNodeFactory.getInstance().getEntity("http://dbpedia.org/resource/Beijing");
//        try {
//			EntityIndexer.indexURIResource(res, 500);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//        System.out.println(EntityIndexer.getEntitiesWithPGScoreToIndex().size());
    }
}
