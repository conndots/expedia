package cn.edu.nju.ws.expedia.model.rdf;

/**
 * Created by Xiangqian on 2015/4/8.
 */
public interface Node {
    public static final String URI_RESOURCE = "ures",
            ENTITY = "ent",
            CATEGORY = "cat",
            ONT_CLASS = "class",
            PROPERTY = "prop",
            LITERAL = "lit";
    public String getNodeType();
}
