package cn.edu.nju.ws.expedia.model.rdf;

/**
 * Created by Xiangqian on 2015/4/8.
 */
public class Literal implements Node {
    protected String content = null;

    public Literal(String content) {
        this.content = content;
    }

    public String getNodeType() {
        return Node.LITERAL;
    }

    public String getContent() {
        return content;
    }
    public String toString() {
        return this.content;
    }
}
