package cn.edu.nju.ws.expedia.model.rdf;

import cn.edu.nju.ws.expedia.util.TwoTuple;

import java.util.List;

/**
 * Created by Xiangqian on 2015/4/9.
 */
public interface EntityQuery {
    public List<TwoTuple<URIResource, Node>> getEntityDescriptions(String id, boolean loadFromDB);
    public String getLabel(String id, boolean loadFromDB);
}
