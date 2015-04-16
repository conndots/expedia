package cn.edu.nju.ws.expedia.controller.data.search;

import cn.edu.nju.ws.expedia.model.rdf.URIResource;
import cn.edu.nju.ws.expedia.search.Searchable;

/**
 * Created by Xiangqian on 2015/4/13.
 */
public class ResourceBean {
    private String uri = null;
    private String label = null;

    public static ResourceBean getInstance(URIResource ures) {
        ResourceBean instance = new ResourceBean();
        instance.uri = ures.getURI();
        instance.label = ures.getLabel(false);
        return instance;
    }
    public static ResourceBean getInstance(Searchable entity) {
        ResourceBean instance = new ResourceBean();
        instance.uri = entity.getURI();
        instance.label = entity.getLabel(false);
        return instance;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }


    public String getUri() {

        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
