package cn.edu.nju.ws.expedia.model.rdf.estore;

import cn.edu.nju.ws.expedia.model.rdf.Literal;
import cn.edu.nju.ws.expedia.model.rdf.Node;
import cn.edu.nju.ws.expedia.model.rdf.NodeFactory;
import cn.edu.nju.ws.expedia.model.rdf.URIResource;

/**
 * Created by Xiangqian on 2015/4/9.
 */
public class EStoreNodeFactory implements NodeFactory {
    public static EStoreNodeFactory getInstance() {
        return new EStoreNodeFactory();
    }

    public URIResource getEntity(String uri) {
        String nodeType = null;
        if(uri.contains("/Category:"))
            nodeType = Node.CATEGORY;
        else
            nodeType = Node.ENTITY;
        URIResource ures = new URIResource(uri, uri, nodeType, EStoreEntityQueryImpl.getInstance());
        return ures;
    }

    public URIResource getCategory(String uri) {
        String nodeType = Node.CATEGORY;
        if(! uri.contains("/Category:"))
            nodeType = Node.ENTITY;
        URIResource cat = new URIResource(uri, uri, nodeType, EStoreEntityQueryImpl.getInstance());
        return cat;
    }

    public URIResource getOntClass(String uri) {
        URIResource oclass = new URIResource(uri, uri, Node.ONT_CLASS, EStoreEntityQueryImpl.getInstance());
        return oclass;
    }

    public URIResource getProperty(String uri) {
        URIResource oclass = new URIResource(uri, uri, Node.PROPERTY, EStoreEntityQueryImpl.getInstance());
        return oclass;
    }

    public Literal getLiteral(String literal) {
        return new Literal(literal);
    }
}
