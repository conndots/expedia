package cn.edu.nju.ws.expedia.offline.lucene;

import cn.edu.nju.ws.expedia.util.ConfReader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Created by Xiangqian on 2015/4/11.
 */
public class IndexWriterFactory {
    private static IndexWriter WRITER = null;
    private static final Object LOCK = new Object();
    public static IndexWriter getWriter() {
        if (WRITER == null) {
            synchronized (LOCK) {
                if (WRITER == null) {
                    String indexPath = ConfReader.getConfProperty("entity_index_path");
                    try {
                        Directory dir = FSDirectory.open(Paths.get(indexPath));
                        Analyzer analyzer = AnalyzerFactory.getDefaultAnalyzer();
                        IndexWriterConfig iwconfig = new IndexWriterConfig(analyzer);
                        iwconfig.setRAMBufferSizeMB(500);
                        WRITER = new IndexWriter(dir, iwconfig);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return WRITER;
    }
    public static void closeWriter() throws IOException {
        if (WRITER != null) {
            WRITER.close();
            synchronized (LOCK) {
                WRITER = null;
            }
        }
    }
}
