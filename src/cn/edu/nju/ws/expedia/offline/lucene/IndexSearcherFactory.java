package cn.edu.nju.ws.expedia.offline.lucene;

import cn.edu.nju.ws.expedia.util.ConfReader;
import org.apache.log4j.Logger;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Created by Xiangqian on 2015/4/11.
 */
public class IndexSearcherFactory {
    private static IndexSearcher SEARCHER = null;
    private static final Object LOCK = new Object();

    private static void setSearcher() throws IOException {
        String indexPath = ConfReader.getConfProperty("entity_index_path");
        Directory dir = FSDirectory.open(Paths.get(indexPath));
        IndexReader reader = DirectoryReader.open(dir);

        SEARCHER = new IndexSearcher(reader);
    }

    private static Logger LOGGER = Logger.getLogger(IndexSearcherFactory.class);

    public static IndexSearcher getIndexSearcher() {
        if (SEARCHER == null) {
            synchronized (LOCK) {
                if (SEARCHER == null) {
                    try {
                        setSearcher();
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage());
                    }
                }
            }
        }
        return SEARCHER;
    }
}
