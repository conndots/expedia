package cn.edu.nju.ws.expedia.model.rdf.qstore;

import cn.edu.nju.ws.expedia.model.rdf.EntityQuery;
import cn.edu.nju.ws.expedia.model.rdf.Node;
import cn.edu.nju.ws.expedia.model.rdf.URIResource;
import cn.edu.nju.ws.expedia.model.rdf.qstore.cache.LabelCache;
import cn.edu.nju.ws.expedia.model.rdf.qstore.cache.TripleCache;
import cn.edu.nju.ws.expedia.util.TwoTuple;

import java.util.List;

/**
 * Created by Xiangqian on 2015/4/9.
 */
public class QStoreEntityQueyImpl implements EntityQuery {
    public List<TwoTuple<URIResource, Node>> getEntityDescriptions(String id, boolean loadFromDB) {
        return TripleCache.getInstance().getTriplesOfSubject(id, loadFromDB);
    }

    public String getLabel(String id, boolean loadFromDB) {
        return LabelCache.getInstance().getLabelFromUriHash(id, loadFromDB);
    }

    public static QStoreEntityQueyImpl getInstance() {
        return new QStoreEntityQueyImpl();
    }
}
