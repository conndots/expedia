package cn.edu.nju.ws.expedia.search;

import cn.edu.nju.ws.expedia.search.filter.SemanticTagsFilter;

import java.util.List;

/**
 * Created by Xiangqian on 2015/4/9.
 */
public interface SemTag {
    public double getCost(SemanticTagsFilter filter);
    public String getRepresentiveLabel();
    public List<String> getLabels();
    public String getIdentifier();
}
