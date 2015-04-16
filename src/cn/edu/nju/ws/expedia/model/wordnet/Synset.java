package cn.edu.nju.ws.expedia.model.wordnet;

import cn.edu.nju.ws.expedia.model.wordnet.cache.HypernymFactory;
import cn.edu.nju.ws.expedia.model.wordnet.cache.HyponymFactory;
import cn.edu.nju.ws.expedia.model.wordnet.cache.SynsetFactory;
import cn.edu.nju.ws.expedia.model.wordnet.query.WordnetQueryAgent;
import cn.edu.nju.ws.expedia.model.wordnet.query.WordnetQueryAgentFactory;
import cn.edu.nju.ws.expedia.util.FourTuple;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Created by Xiangqian on 2015/4/7.
 */
public class Synset {
    protected String sid = null;
    protected char pos;
    protected int catID, numWords = -1, numPointers = -1;
    protected String gloss = null;
    protected boolean isInstance = false;
    protected List<String> words = null;

    /**
     * to test whether the toTest is a legal Synset id.
     * @param toTest
     * @return
     */
    public static boolean isSynsetID(String toTest){
        return toTest.matches("[nrav][0-9]+");
    }

    public Synset(String sid, char pos, int catID, int numWords, int numPointers, String gloss, boolean isInstance){
        this.sid = sid;
        this.pos = pos;
        this.catID = catID;
        this.numWords = numWords;
        this.numPointers = numPointers;
        this.gloss = gloss;
        this.isInstance = isInstance;
    }
    public Synset(String sid) {
        this.sid = sid;
        this.pos = sid.charAt(0);
    }

    public void setGloss(String g) {
        this.gloss = g;
    }
    public void setNumWords(int n) {
        this.numWords = n;
    }
    public int getNumWords() {
        if(this.numWords == -1)
            this.loadBasic();
        return this.numWords;
    }
    public int getNumPointers() {
        if(this.numPointers == -1)
            this.loadBasic();
        return this.numPointers;
    }
    public void setNumPointers(int n) {
        this.numPointers = n;
    }
    public void setIsInstance(boolean is) {
        this.isInstance = is;
    }
    public void setCatID(int catid) {
        this.catID = catid;
    }

    public Synset(String sid, char pos, int catID, int numWords, int numPointers, String gloss){
        this.sid = sid;
        this.pos = pos;
        this.catID = catID;
        this.numWords = numWords;
        this.numPointers = numPointers;
        this.gloss = gloss;
    }

    protected final Object bLock = new Object();
    protected void loadBasic() {
        if(this.gloss == null) {
            synchronized (bLock) {
                WordnetQueryAgentFactory.getDefaultAgentInstance().loadSynset(this);
            }
        }
    }

    protected final Object wLock = new Object();
    protected final void loadWords(){
        if(this.words == null) {
            synchronized(wLock) {
                if(this.words == null) {
                    this.words = new ArrayList<String>();
                    for(FourTuple<Integer, String, Character, Integer> tuple :
                            WordnetQueryAgentFactory.getDefaultAgentInstance().getWordsOfSynset(this.sid)) {
                        this.words.add(/**NlpUtil.getStemmedTokens(**/tuple.getSecond().replace("_", " ").toLowerCase()/**, false)**/);
                    }
                }
            }

        }

    }

    public String getIdentifier() {
        return this.sid;
    }

    public String getRepresentiveLabel() {
        this.loadWords();
        return this.words.size() == 0 ? "" : this.words.get(0);
    }

    public List<String> getLabels() {
        this.loadWords();
        return this.words;
    }


    public boolean isGrouped() {
        return false;
    }

    public List<Synset> getHyponyms(boolean isDirect) {
        List<Synset> nodes = new ArrayList<Synset>();
        if(isDirect){
            WordnetQueryAgent wnQuery = WordnetQueryAgentFactory.getDefaultAgentInstance();
            try {
                Set<String> hypos = HyponymFactory.getInstance().getHyponymsOfSynset(this.sid);
                for(String hypo : hypos) {
                    try {
                        nodes.add(SynsetFactory.getInstance().getSynset(hypo));
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        else {
            Synset current = this;
            LinkedList<Synset> stack = new LinkedList<Synset>();
            HashSet<String> visited = new HashSet<String>();
            boolean polled = false;
            while(current != null) {
                if(! polled) {
                    stack.add(current);
                    if(! visited.contains(current.getIdentifier())) {
                        if(current != this)
                            nodes.add(current);
                        visited.add(current.getIdentifier());
                    }
                }
                polled = false;

                Synset next = null;
                try {
                    Set<String> hypos = HyponymFactory.getInstance().getHyponymsOfSynset(current.getIdentifier());
                    for(String hypo : hypos) {
                        if(visited.contains(hypo))
                            continue;
                        try {
                            next = SynsetFactory.getInstance().getSynset(hypo);
                            break;
                        } catch (Exception e) {
                            e.printStackTrace();
                            continue;
                        }
                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                if(next == null) {
                    current = stack.pollLast();
                    polled = true;
                }
                else {
                    current = next;
                }
            }
        }
        return nodes;
    }

    public List<Synset> getHypernymSynsets(boolean isDirect) {
        List<Synset> nodes = new ArrayList<Synset>();
        if(isDirect){
            WordnetQueryAgent wnQuery = WordnetQueryAgentFactory.getDefaultAgentInstance();
            try {
                Set<String> hypers = HypernymFactory.getInstance().getHypernymsOfSynset(this.sid);
                for(String hyper : hypers) {
                    try {
                        nodes.add(SynsetFactory.getInstance().getSynset(hyper));
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        else {
            Synset current = this;
            LinkedList<Synset> stack = new LinkedList<Synset>();
            HashSet<String> visited = new HashSet<String>();
            boolean polled = false;
            while(current != null) {
                if(! polled) {
                    stack.add(current);
                    if(! visited.contains(current.getIdentifier())) {
                        if(current != this)
                            nodes.add(current);
                        visited.add(current.getIdentifier());
                    }
                }
                polled = false;

                Synset next = null;
                try {
                    Set<String> hypers = HypernymFactory.getInstance().getHypernymsOfSynset(current.getIdentifier());
                    for(String hyper : hypers) {
                        if(visited.contains(hyper))
                            continue;
                        try {
                            next = SynsetFactory.getInstance().getSynset(hyper);
                            break;
                        } catch (Exception e) {
                            e.printStackTrace();
                            continue;
                        }
                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                if(next == null) {
                    current = stack.pollLast();
                    polled = true;
                }
                else {
                    current = next;
                }
            }
        }
        return nodes;
    }

    public String getGloss(){
        if(this.gloss == null)
            this.loadBasic();
        return this.gloss;
    }

    public char getPos(){
        return this.pos;
    }

    public boolean equals(Object o){
        if(o instanceof Synset){
            Synset s = (Synset) o;
            return this.sid.equals(s.sid);
        }
        return false;
    }
    public int hashCode(){
        return this.sid.hashCode();
    }
}
