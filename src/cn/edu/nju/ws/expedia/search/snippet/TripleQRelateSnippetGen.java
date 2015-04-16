package cn.edu.nju.ws.expedia.search.snippet;

import cn.edu.nju.ws.expedia.controller.data.search.SnippetBean;
import cn.edu.nju.ws.expedia.controller.data.search.SnippetsBean;
import cn.edu.nju.ws.expedia.model.rdf.Node;
import cn.edu.nju.ws.expedia.model.rdf.URIResource;
import cn.edu.nju.ws.expedia.offline.index.EntityIndexer;
import cn.edu.nju.ws.expedia.search.QueryContext;
import cn.edu.nju.ws.expedia.search.Searchable;
import cn.edu.nju.ws.expedia.search.SemTag;
import cn.edu.nju.ws.expedia.util.TwoTuple;
import cn.edu.nju.ws.expedia.util.nlp.NlpUtil;

import java.util.*;

/**
 * Created by Xiangqian on 2015/4/12.
 */
public class TripleQRelateSnippetGen implements EntitySnippetGenerator {
    class TermVector{
        HashMap<String, Double> word2freq = null;
        Vocabulary vocab = null;
        TermVector(Vocabulary vocab){
            this.word2freq = new HashMap<String, Double>();
            this.vocab = vocab;
            this.vocab.addDoc();
        }
        void termDisplay(String term, double coef){
            Double freq = this.word2freq.get(term);
            if(freq == null){
                freq = 0d;
                this.vocab.addTermFromDocFirstTime(term);
            }
            freq += coef;
            this.word2freq.put(term, freq);
        }
        boolean normalized = false;
        void normalizeValues(){
            if(normalized)
                return;
            double maximum = 0d;
            for(String key : this.word2freq.keySet()){
                Double val = this.word2freq.get(key);
                if(val > maximum)
                    maximum = val;
            }
            HashMap<String, Double> temp = new HashMap<String, Double>();
            for(String key : this.word2freq.keySet()){
                Double val = this.word2freq.get(key);
                temp.put(key, val / maximum);
            }
            this.word2freq = temp;
            this.normalizeIDFValues();
            this.normalized = true;
        }
        void normalizeIDFValues(){
            HashMap<String, Double> temp = new HashMap<String, Double>();
            for(String key : this.word2freq.keySet()){
                Double val = this.word2freq.get(key);
                Integer tdocnum = this.vocab.term2doc.get(key);
                double finalVal = val * Math.log((0d + this.vocab.docnum) / (0d + tdocnum)) / Math.log(2d);
                temp.put(key, finalVal);
            }
            this.word2freq = temp;
        }
        double getNorm(){
            double ret = 0d;
            for(Double val : this.word2freq.values()){
                ret += val * val;
            }
            return Math.sqrt(ret);
        }
        public double getSimilarityWithQueryVector(TermVector queryVector){
            this.normalizeValues();
            queryVector.normalizeValues();
            double up = 0d;
            for(String key : this.word2freq.keySet()){
                if(! queryVector.word2freq.containsKey(key))
                    continue;
                Double val0 = this.word2freq.get(key),
                        val1 = queryVector.word2freq.get(key);
                up += val0 * val1;
            }
            return up / (this.getNorm() * queryVector.getNorm());
        }
        public void setSelection(TermVector qvec){
            for(String key : this.word2freq.keySet()){
                if(! qvec.word2freq.containsKey(key))
                    continue;
                Double val1 = qvec.word2freq.get(key);
                qvec.word2freq.put(key, val1 / 2);
            }
        }
    }
    class Vocabulary {
        HashMap<String, Integer> term2doc = null;
        int docnum = 0;
        Vocabulary(){
            this.term2doc = new HashMap<String, Integer>();
        }
        void addDoc(){
            this.docnum ++;
        }
        void addTermFromDocFirstTime(String term){
            Integer docnum = this.term2doc.get(term);
            if(docnum == null)
                docnum = 0;
            this.term2doc.put(term, docnum + 1);
        }
    }

    private String[] getTokensFromStr(String text) {
        String stemmed = NlpUtil.getStemmedTokens(text, true);
        return stemmed.split(" ");
    }

    public List<SnippetBean> getSnippetsForEntity(Searchable entity, QueryContext context, int num) {
        String query = context.getQuery();
        List<SemTag> selectedTags = context.getSelectedTags();

        Vocabulary vocab = new Vocabulary();
        final TermVector queryVec = new TermVector(vocab);
        String[] queryTokens = getTokensFromStr(query);
        for (String qtoken : queryTokens) {
            queryVec.termDisplay(qtoken, 1d);
        }
        if (selectedTags != null) {
            for (SemTag tag : selectedTags) {
                List<String> labels = tag.getLabels();
                for (String l : labels) {
                    String[] sl = getTokensFromStr(l.replace("_", " "));
                    for (String t : sl) {
                        queryVec.termDisplay(t, 0.7d);
                    }
                }
            }
        }

        List<TwoTuple<URIResource, Node>> descs = entity.getDescriptions(true);
        List<TwoTuple<TwoTuple<URIResource, Node>, TermVector>> vectors =
                new ArrayList<TwoTuple<TwoTuple<URIResource, Node>, TermVector>>(descs.size());
        for (TwoTuple<URIResource, Node> desc : descs) {
            TermVector vec = new TermVector(vocab);
            String p = desc.getFirst().getLabel(false);
            for (String ptoken : getTokensFromStr(p)) {
                vec.termDisplay(ptoken, 1d);
            }
            String o = desc.getSecond().toString();
            for (String otoken : getTokensFromStr(o)) {
                vec.termDisplay(otoken, 1d);
            }
            vectors.add(new TwoTuple<TwoTuple<URIResource, Node>, TermVector>(desc, vec));
        }

        PriorityQueue<TwoTuple<TwoTuple<URIResource, Node>, TermVector>> pqueue = new
                PriorityQueue<TwoTuple<TwoTuple<URIResource, Node>, TermVector>>(vectors.size(), new Comparator<TwoTuple<TwoTuple<URIResource, Node>, TermVector>>() {

            /**
             * Compares its two arguments for order.  Returns a negative integer,
             * zero, or a positive integer as the first argument is less than, equal
             * to, or greater than the second.<p>
             * <p/>
             * In the foregoing description, the notation
             * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
             * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
             * <tt>0</tt>, or <tt>1</tt> according to whether the value of
             * <i>expression</i> is negative, zero or positive.<p>
             * <p/>
             * The implementor must ensure that <tt>sgn(compare(x, y)) ==
             * -sgn(compare(y, x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
             * implies that <tt>compare(x, y)</tt> must throw an exception if and only
             * if <tt>compare(y, x)</tt> throws an exception.)<p>
             * <p/>
             * The implementor must also ensure that the relation is transitive:
             * <tt>((compare(x, y)&gt;0) &amp;&amp; (compare(y, z)&gt;0))</tt> implies
             * <tt>compare(x, z)&gt;0</tt>.<p>
             * <p/>
             * Finally, the implementor must ensure that <tt>compare(x, y)==0</tt>
             * implies that <tt>sgn(compare(x, z))==sgn(compare(y, z))</tt> for all
             * <tt>z</tt>.<p>
             * <p/>
             * It is generally the case, but <i>not</i> strictly required that
             * <tt>(compare(x, y)==0) == (x.equals(y))</tt>.  Generally speaking,
             * any comparator that violates this condition should clearly indicate
             * this fact.  The recommended language is "Note: this comparator
             * imposes orderings that are inconsistent with equals."
             *
             * @param o1 the first object to be compared.
             * @param o2 the second object to be compared.
             * @return a negative integer, zero, or a positive integer as the
             * first argument is less than, equal to, or greater than the
             * second.
             * @throws NullPointerException if an argument is null and this
             *                              comparator does not permit null arguments
             * @throws ClassCastException   if the arguments' types prevent them from
             *                              being compared by this comparator.
             */
            public int compare(TwoTuple<TwoTuple<URIResource, Node>, TermVector> o1, TwoTuple<TwoTuple<URIResource, Node>, TermVector> o2) {
                TermVector v1 = o1.getSecond(), v2 = o2.getSecond();
                double simi1 = queryVec.getSimilarityWithQueryVector(v1),
                        simi2 = queryVec.getSimilarityWithQueryVector(v2);
                if (simi1 > simi2)
                    return -1;
                else if (simi1 < simi2)
                    return 1;
                return 0;
            }
        });
        for (TwoTuple<TwoTuple<URIResource, Node>, TermVector> vec : vectors) {
            pqueue.add(vec);
        }

        List<SnippetBean> snippetsList = new ArrayList<SnippetBean>(num);
        for (int i = 0; i < num && (! pqueue.isEmpty()); i ++) {
            TwoTuple<TwoTuple<URIResource, Node>, TermVector> polled = pqueue.poll();
            SnippetBean bean = SnippetBean.getInstance(polled.getFirst().getFirst(), polled.getFirst().getSecond());
            snippetsList.add(bean);
        }
        pqueue.clear();
        vectors.clear();
        pqueue = null; vectors = null;

        return snippetsList;
    }
}
