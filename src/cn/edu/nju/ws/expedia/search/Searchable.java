package cn.edu.nju.ws.expedia.search;

import cn.edu.nju.ws.expedia.model.rdf.Node;
import cn.edu.nju.ws.expedia.model.rdf.URIResource;
import cn.edu.nju.ws.expedia.search.filter.SemanticTagsFilter;
import cn.edu.nju.ws.expedia.util.TwoTuple;

import java.util.List;
import java.util.Set;

/**
 * Created by Xiangqian on 2015/4/9.
 */
public interface Searchable {
    public Set<SemTag> getTaggedSemTags(SemanticTagsFilter tagsFilter);
    public String getURI();
    public double getSortScore();
    public String getIdentifier();
    public String getLabel(boolean loadFromDB);
    public List<TwoTuple<URIResource, Node>> getDescriptions(boolean loadFromDB);
}
