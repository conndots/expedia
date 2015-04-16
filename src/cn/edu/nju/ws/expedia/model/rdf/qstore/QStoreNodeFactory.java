package cn.edu.nju.ws.expedia.model.rdf.qstore;

import cn.edu.nju.ws.expedia.model.rdf.Literal;
import cn.edu.nju.ws.expedia.model.rdf.Node;
import cn.edu.nju.ws.expedia.model.rdf.NodeFactory;
import cn.edu.nju.ws.expedia.model.rdf.URIResource;
import cn.edu.nju.ws.expedia.model.rdf.qstore.cache.UriHashCache;

/**
 * Created by Xiangqian on 2015/4/9.
 */
public class QStoreNodeFactory implements NodeFactory {
    public static QStoreNodeFactory getInstance() {
        return new QStoreNodeFactory();
    }
    public URIResource getEntity(String uriHash) {
        String uri = UriHashCache.getInstance().getUriFromHashID(uriHash);
        String nodeType = null;
        if(uri.contains("/Category:"))
            nodeType = Node.CATEGORY;
        else
            nodeType = Node.ENTITY;
        URIResource entity = new URIResource(uriHash, uri, nodeType, QStoreEntityQueyImpl.getInstance());
        return entity;
    }
    public URIResource getCategory(String uriHash) {
        String uri = UriHashCache.getInstance().getUriFromHashID(uriHash);
        String nodeType = Node.CATEGORY;
        if(! uri.contains("/Category:"))
            nodeType = Node.ENTITY;
        URIResource cat = new URIResource(uriHash, uri, nodeType, QStoreEntityQueyImpl.getInstance());
        return cat;
    }

    public URIResource getOntClass(String uriHash) {
        String uri = UriHashCache.getInstance().getUriFromHashID(uriHash);
        URIResource oclass = new URIResource(uriHash, uri, Node.ONT_CLASS, QStoreEntityQueyImpl.getInstance());

        return oclass;
    }

    public URIResource getProperty(String uriHash) {
        String uri = UriHashCache.getInstance().getUriFromHashID(uriHash);
        URIResource p = new URIResource(uriHash, uri, Node.PROPERTY, QStoreEntityQueyImpl.getInstance());
        return p;
    }

    public Literal getLiteral(String literal) {
        return new Literal(literal);
    }
}