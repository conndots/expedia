package cn.edu.nju.ws.expedia.search;

import cn.edu.nju.ws.expedia.model.rdf.NodeFactory;
import cn.edu.nju.ws.expedia.model.rdf.URIResource;

import java.util.List;

/**
 * Created by Xiangqian on 2015/4/10.
 */
public class SearchableFactory {
    private NodeFactory nodeFactory = null;
    public static SearchableFactory getInstance(NodeFactory factory) {
        SearchableFactory instance = new SearchableFactory();
        instance.nodeFactory = factory;
        return instance;
    }

    public Searchable getSearchable(String id, String label, double queryRelativeScore, double pageRankScore) {
        URIResource res = this.nodeFactory.getEntity(id);
        SearchableEntity ret = new SearchableEntity(res);
        ret.sortScore = pageRankScore * queryRelativeScore;
        ret.resource.setLabel(label);
        return ret;
    }

    public Searchable getSearchable(String id, String label, String[] typeIDs, double queryRelativeScore, double pageRankScore) {
        URIResource res = this.nodeFactory.getEntity(id);
        SearchableEntity ret = new SearchableEntity(res, typeIDs);
        ret.resource.setLabel(label);
        ret.sortScore = pageRankScore * queryRelativeScore;
        return ret;
    }
}
