package cn.edu.nju.ws.expedia.database;

import cn.edu.nju.ws.expedia.util.ConfReader;
import virtuoso.jena.driver.VirtGraph;

/**
 * Created by Xiangqian on 2015/4/9.
 */
public class VirtuosoGraphFactory {
    public static final String DBPEDIA_2014 = "http://dbpedia2014.org/",
            DBPEDIA_2014_PAGERANK = "http://dbpedia2014_pg.org/",
            DBPEDIA_ONTOLOGY = "http://dbpedia_ont.org/";

    public static VirtGraph getVirtGraph() {
        String virtUrl = ConfReader.getConfProperty("virtuoso_url"),
                virtUser = ConfReader.getConfProperty("virtuoso_user"),
                virtPw = ConfReader.getConfProperty("virtuoso_pw");
        return new VirtGraph(virtUrl, virtUser, virtPw);
    }
}
