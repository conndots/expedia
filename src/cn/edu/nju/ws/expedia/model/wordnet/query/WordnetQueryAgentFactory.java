package cn.edu.nju.ws.expedia.model.wordnet.query;

/**
 * Created by Xiangqian on 2015/1/10.
 */
public class WordnetQueryAgentFactory {
    public static WordnetQueryAgent getDefaultAgentInstance(){
        return new WordnetQueryAgentDBImpl();
    }
}
