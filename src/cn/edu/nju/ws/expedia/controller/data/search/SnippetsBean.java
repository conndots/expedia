package cn.edu.nju.ws.expedia.controller.data.search;

import cn.edu.nju.ws.expedia.util.ConfReader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Xiangqian on 2015/4/13.
 */
public class SnippetsBean {
    private List<SnippetBean> snippets = new ArrayList<SnippetBean>(Integer.valueOf(ConfReader.getConfProperty("snippet_num")));

    public void addSnippetBean(SnippetBean snippet) {
        this.snippets.add(snippet);
    }

    public List<SnippetBean> getSnippets() {
        return snippets;
    }
}
