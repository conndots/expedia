
package cn.edu.nju.ws.expedia.util.nlp.snowball;


import cn.edu.nju.ws.expedia.util.nlp.snowball.exp.englishStemmer;

public abstract class SnowballStemmer extends SnowballProgram {
    public abstract boolean stem();
    public static void main(String[] args){
    	englishStemmer stemmer = new englishStemmer();
    	stemmer.setCurrent("emulation");
    	if(stemmer.stem())
    		System.out.println(stemmer.getCurrent());
    }
};
