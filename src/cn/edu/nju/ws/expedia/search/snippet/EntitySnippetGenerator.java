package cn.edu.nju.ws.expedia.search.snippet;

import cn.edu.nju.ws.expedia.controller.data.search.SnippetBean;
import cn.edu.nju.ws.expedia.controller.data.search.SnippetsBean;
import cn.edu.nju.ws.expedia.search.QueryContext;
import cn.edu.nju.ws.expedia.search.Searchable;

import java.util.List;

/**
 * Created by Xiangqian on 2015/4/10.
 */
public interface EntitySnippetGenerator {
    public List<SnippetBean> getSnippetsForEntity(Searchable entity, QueryContext context, int num);
}
