package cn.edu.nju.ws.expedia.model.rdf;

/**
 * Created by Xiangqian on 2015/4/9.
 */
public interface NodeFactory {
    public URIResource getEntity(String id);
    public URIResource getCategory(String identifier);
    public URIResource getOntClass(String id);
    public URIResource getProperty(String id);
    public Literal getLiteral(String literal);
}
