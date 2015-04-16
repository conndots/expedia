package cn.edu.nju.ws.expedia.model.rdf.estore;

import cn.edu.nju.ws.expedia.model.rdf.EntityQuery;
import cn.edu.nju.ws.expedia.model.rdf.Node;
import cn.edu.nju.ws.expedia.model.rdf.URIResource;
import cn.edu.nju.ws.expedia.model.rdf.estore.cache.LabelCache;
import cn.edu.nju.ws.expedia.model.rdf.estore.cache.TripleCache;
import cn.edu.nju.ws.expedia.util.TwoTuple;

import java.util.List;

/**
 * Created by Xiangqian on 2015/4/9.
 */
public class EStoreEntityQueryImpl implements EntityQuery {
    public List<TwoTuple<URIResource, Node>> getEntityDescriptions(String id, boolean loadFromDB) {
        return TripleCache.getInstance().getTriplesOfSubject(id, loadFromDB);
    }

    public String getLabel(String id, boolean loadFromDB) {
        return LabelCache.getInstance().getLabelFromUri(id, loadFromDB);
    }

    public static EStoreEntityQueryImpl getInstance() {
        return new EStoreEntityQueryImpl();
    }
}
