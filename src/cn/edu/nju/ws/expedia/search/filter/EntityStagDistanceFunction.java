package cn.edu.nju.ws.expedia.search.filter;

import cn.edu.nju.ws.expedia.search.Searchable;
import cn.edu.nju.ws.expedia.search.SemTag;

import java.util.List;

/**
 * Created by Xiangqian on 2015/4/8.
 */
public interface EntityStagDistanceFunction  {
    public void initiation(List<Searchable> entities);
    public float getDistanceOfEntityAndStag(Searchable entity, SemTag stag);
    public int getSemTagTaggingCount(SemTag stag);
    public int getMaximumSemTagTaggingCount();
}
