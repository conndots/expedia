package cn.edu.nju.ws.expedia.search.query;

import cn.edu.nju.ws.expedia.model.rdf.NodeFactory;
import cn.edu.nju.ws.expedia.search.Searchable;
import cn.edu.nju.ws.expedia.search.SearchableFactory;
import cn.edu.nju.ws.expedia.search.SemTag;
import cn.edu.nju.ws.expedia.search.filter.SemanticTagsFilter;
import cn.edu.nju.ws.expedia.util.nlp.NlpUtil;
import cn.edu.nju.ws.expedia.offline.index.EntityIndexer;
import cn.edu.nju.ws.expedia.offline.lucene.AnalyzerFactory;
import cn.edu.nju.ws.expedia.offline.lucene.IndexSearcherFactory;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.BooleanFilter;
import org.apache.lucene.queries.TermsFilter;
import org.apache.lucene.search.*;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.util.QueryBuilder;

import java.io.IOException;
import java.util.*;

/**
 * Created by Xiangqian on 2015/4/11.
 */
public class DefaultIndexQueryAgent implements IndexQueryAgent {
    protected Logger logger = Logger.getLogger(DefaultIndexQueryAgent.class);
    protected Set<String> loadFields = null;
    protected QueryBuilder queryBuilder = null;

    public DefaultIndexQueryAgent() {
        this.queryBuilder = new QueryBuilder(AnalyzerFactory.getDefaultAnalyzer());
        this.loadFields = new HashSet<String>();
        this.loadFields.add(EntityIndexer.ID_FIELD);
        this.loadFields.add(EntityIndexer.PAGERANK_SCORE_FIELD_LOGGED);
        this.loadFields.add(EntityIndexer.TYPEID_FIELD);
        this.loadFields.add(EntityIndexer.LABEL_FIELD);
    }

    private static final double LOG_7 = Math.log(7d);

    public ArrayList<Searchable> getEntitiesForQuery(String query, List<SemTag> selectedTags, List<SemTag> excludedTags,
                                                SemanticTagsFilter filter, SearchableFactory factory, Comparator<Searchable> comp, int maximumNum) {
        ArrayList<Searchable> allResults = maximumNum > 0 ? new ArrayList<Searchable>(maximumNum) : new ArrayList<Searchable>(700);
        IndexSearcher isearcher = IndexSearcherFactory.getIndexSearcher();

        Query labelQuery = queryBuilder.createBooleanQuery(EntityIndexer.LABEL_FIELD, query, BooleanClause.Occur.SHOULD);
        if (labelQuery == null) {
        	query = NlpUtil.getStemmedTokens(query, false);
        	String[] splits = query.split(" ");
        	if (splits.length > 1) {
        		BooleanQuery bq = new BooleanQuery();
        		for (String split : splits) {
        			bq.add(new FuzzyQuery(new Term(EntityIndexer.LABEL_FIELD, split)), Occur.SHOULD);
        		}
        		labelQuery = bq;
        	}
        	else 
        		labelQuery = new FuzzyQuery(new Term(EntityIndexer.LABEL_FIELD, query));
        }
        if (labelQuery instanceof BooleanQuery) {
            ((BooleanQuery) labelQuery).setMinimumNumberShouldMatch(1);
        }
        labelQuery.setBoost(1.3f);

        Query contentQuery = queryBuilder.createBooleanQuery(EntityIndexer.CONTENT_FIELD, query, BooleanClause.Occur.SHOULD);
        if (contentQuery == null) {
        	query = NlpUtil.getStemmedTokens(query, false);
        	String[] splits = query.split(" ");
        	if (splits.length > 1) {
        		BooleanQuery bq = new BooleanQuery();
        		for (String split : splits) {
        			bq.add(new FuzzyQuery(new Term(EntityIndexer.LABEL_FIELD, split)), Occur.SHOULD);
        		}
        		contentQuery = bq;
        	}
        	else 
        		contentQuery = new FuzzyQuery(new Term(EntityIndexer.LABEL_FIELD, query));
        }
        if(contentQuery instanceof BooleanQuery) {
            ((BooleanQuery) contentQuery).setMinimumNumberShouldMatch(1);
        }
        contentQuery.setBoost(1f);

        BooleanQuery bquery = new BooleanQuery();
        bquery.add(labelQuery, BooleanClause.Occur.SHOULD);
        bquery.add(contentQuery, BooleanClause.Occur.SHOULD);
        bquery.setMinimumNumberShouldMatch(1);

        ArrayList<Filter> filters = null;
        if (selectedTags != null && selectedTags.size() > 0) {
            filters = new ArrayList<Filter>(selectedTags.size());
            for (SemTag tag : selectedTags) {
                List<String> mappedTypeIDs = filter.getTypeIDsOfSemTag(tag);
                if (mappedTypeIDs == null)
                	continue;
                List<Term> terms = new ArrayList<Term>(mappedTypeIDs.size());
                for (String typeID : mappedTypeIDs) {
                    terms.add(new Term(EntityIndexer.TYPEID_FIELD, typeID));
                }
                TermsFilter tfilter = new TermsFilter(terms);
                filters.add(tfilter);
            }
        }
        ArrayList<Filter> inverseFilters = null;
        if (excludedTags != null && excludedTags.size() > 0) {
            inverseFilters = new ArrayList<Filter>(excludedTags.size());
            for (SemTag tag : excludedTags) {
                List<String> mappedTypeIDs = filter.getTypeIDsOfSemTag(tag);
                List<Term> terms = new ArrayList<Term>(mappedTypeIDs.size());
                for (String typeID : mappedTypeIDs) {
                    terms.add(new Term(EntityIndexer.TYPEID_FIELD, typeID));
                }
                TermsFilter tfilter = new TermsFilter(terms);
                inverseFilters.add(tfilter);
            }
        }

        BooleanFilter typeFilter = null;
        if (filters != null && filters.size() > 0) {
            typeFilter = new BooleanFilter();

            for (Filter f : filters) {
                typeFilter.add(f, BooleanClause.Occur.MUST);
            }
            filters.clear();
            filters = null;
        }
        if (inverseFilters != null && inverseFilters.size() > 0) {
            if (typeFilter == null)
                typeFilter = new BooleanFilter();

            for (Filter f : inverseFilters) {
                typeFilter.add(f, BooleanClause.Occur.MUST_NOT);
            }
            inverseFilters.clear();
            inverseFilters = null;
        }

        try {
            TopDocs tdocs = isearcher.search(bquery, typeFilter, maximumNum > 0 ? maximumNum : 700);
            ScoreDoc[] sdocs = tdocs.scoreDocs;
            for (ScoreDoc sdoc : sdocs) {
                double score = (double) sdoc.score;
                Document doc = isearcher.doc(sdoc.doc, this.loadFields);
                String id = doc.get(EntityIndexer.ID_FIELD);
                String[] typeIDs = doc.getValues(EntityIndexer.TYPEID_FIELD);
                double pagerankScore = doc.getField(EntityIndexer.PAGERANK_SCORE_FIELD_LOGGED).numericValue().doubleValue();
                String label = doc.get(EntityIndexer.LABEL_FIELD);

                Searchable entity = factory.getSearchable(id, label, typeIDs, score, pagerankScore);
                allResults.add(entity);
            }
        } catch (IOException e) {
            e.printStackTrace();
            this.logger.error(e.getMessage());
            return allResults;
        }

        if (comp != null && allResults.size() > 0)
            Collections.sort(allResults, comp);

        return allResults;
    }

    public ArrayList<Searchable> getEntitiesForQuery(String query, SearchableFactory factory,  Comparator<Searchable> comp, int maximumNum) {
        return this.getEntitiesForQuery(query, null, null, null, factory, comp, maximumNum);
    }
}
