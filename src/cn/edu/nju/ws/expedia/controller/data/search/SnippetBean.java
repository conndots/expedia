package cn.edu.nju.ws.expedia.controller.data.search;

import cn.edu.nju.ws.expedia.model.rdf.Literal;
import cn.edu.nju.ws.expedia.model.rdf.Node;
import cn.edu.nju.ws.expedia.model.rdf.URIResource;

/**
 * Created by Xiangqian on 2015/4/10.
 */
public class SnippetBean {
    public ResourceBean getP() {
        return p;
    }

    public void setP(ResourceBean p) {
        this.p = p;
    }

    public String getOl() {
        return ol;
    }

    public void setOl(String ol) {
        this.ol = ol;
    }

    public ResourceBean getOr() {
        return or;
    }

    public void setOr(ResourceBean or) {
        this.or = or;
    }

    private ResourceBean p = null;
    private String ol = null;
    private ResourceBean or = null;
    public static SnippetBean getInstance(URIResource p, Node o) {
        SnippetBean instance = new SnippetBean();
        instance.p = ResourceBean.getInstance(p);
        if (o instanceof Literal) {
            instance.ol = ((Literal) o).getContent();
        }
        else if (o instanceof URIResource){
            instance.or = ResourceBean.getInstance((URIResource) o);
        }
        else
            return null;
        return instance;
    }

}
