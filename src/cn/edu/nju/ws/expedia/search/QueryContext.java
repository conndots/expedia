package cn.edu.nju.ws.expedia.search;

import cn.edu.nju.ws.expedia.controller.data.search.SnippetBean;
import cn.edu.nju.ws.expedia.controller.data.search.SnippetsBean;
import cn.edu.nju.ws.expedia.model.rdf.NodeFactory;
import cn.edu.nju.ws.expedia.search.filter.SemanticTagsFilter;
import cn.edu.nju.ws.expedia.search.query.DefaultIndexQueryAgent;
import cn.edu.nju.ws.expedia.search.query.IndexQueryAgent;
import cn.edu.nju.ws.expedia.search.snippet.EntitySnippetGenerator;
import cn.edu.nju.ws.expedia.search.snippet.TripleQRelateSnippetGen;
import cn.edu.nju.ws.expedia.search.sort.RelavanceImportanceCombinedComparator;
import cn.edu.nju.ws.expedia.util.ConfReader;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Xiangqian on 2015/4/10.
 */
public class QueryContext {
    protected String identifier = null;
    protected SearchableFactory entityFactory = null;
    protected ArrayList<Searchable> searchResults = null;
    protected String query = null;
    protected List<SemTag> selectedTags = null;
    protected List<SemTag> excludedTags = null;
    protected volatile List<SemTag> candidateSemTags = null;
    protected volatile List<List<SnippetBean>> snippetsForCurrentPage = null;
    protected volatile int currentPage;
    protected volatile boolean tagsStarted = false, snippetsStarted = false;
    
    protected final Object SN_LOCK = new Object();

    protected IndexQueryAgent queryAgent = null; //synchronized
    protected Comparator<Searchable> comparator = null;
    protected SemanticTagsFilter tagsFilter = null; //asynchronously
    protected EntitySnippetGenerator snippetGen = null; //asynchronously

    protected QueryContext(NodeFactory nodeFactory, String query, List<SemTag> selectedTags, List<SemTag> excludedTags) {
        this.entityFactory = SearchableFactory.getInstance(nodeFactory);
        this.query = query;
        this.selectedTags = selectedTags;
        this.excludedTags = excludedTags;
    }

    public static QueryContext getInstance(String query, List<String> selectedTagIDs, List<String> excludedTagIDs,
                                           NodeFactory nodeFactory, SemanticTagsFilter filter) {
//    	System.out.println("Create new QueryContext instance." + query);
        List<SemTag> selectedTags = null,
                excludedTags = null;
        if (selectedTagIDs != null && filter != null) {
            selectedTags = new ArrayList<SemTag>(selectedTagIDs.size());
            for (String stid : selectedTagIDs) {
                SemTag tag = filter.getSemTagInstance(stid);
                if (tag != null)
                    selectedTags.add(tag);
            }
        }
        if (excludedTagIDs != null && filter != null) {
            excludedTags = new ArrayList<SemTag>(excludedTagIDs.size());
            for (String etid : excludedTagIDs) {
                SemTag tag = filter.getSemTagInstance(etid);
                if (tag != null)
                    excludedTags.add(tag);
            }
        }
        QueryContext instance = new QueryContext(nodeFactory, query, selectedTags, excludedTags);
        instance.queryAgent = new DefaultIndexQueryAgent();
        instance.comparator = new RelavanceImportanceCombinedComparator();
        instance.snippetGen = new TripleQRelateSnippetGen();
        instance.tagsFilter = filter;
        if (filter != null)
        	filter.setQueryContext(instance);

        return instance;
    }

    public List<SemTag> getCandidateSemTags() {
        return this.candidateSemTags;
    }

    public List<SemTag> getCandidateSemTags(long timeout, TimeUnit unit) throws IllegalAccessException {
        if (this.tagsFilter == null)
            throw new IllegalAccessException("No given filters here.");
        if (this.candidateSemTags != null)
            return this.candidateSemTags;
        if (! this.tagsStarted)
            this.startSetCandidateSemTags();

        long startNano = System.nanoTime();
        long timeoutNano = unit.toNanos(timeout);
        while(candidateSemTags == null) {
            if (System.nanoTime() - startNano > timeoutNano)
                break;
        }
        if (candidateSemTags != null)
            tagsStarted = false;
        return this.candidateSemTags;
    }

    /**
     * Supposed to start running this in a single thread asynchronously.
     */
    public void startSetCandidateSemTags() throws IllegalAccessException {
        if (this.tagsFilter == null)
            throw new IllegalAccessException("No given filters here.");
        if (this.candidateSemTags != null)
            return;
        this.tagsStarted = true;
        
        if (this.searchResults == null)
        	setQueryResultsFromAgent();

        this.tagsFilter.setEntities(this.searchResults);
        this.candidateSemTags = this.tagsFilter.getSelectedStags();
    }

    public void loadEntitySnippetsForPage(int page) {
        if (this.currentPage == page && this.snippetsForCurrentPage != null)
            return;
        this.currentPage = page;
        this.snippetsStarted = true;
        
        if (this.searchResults == null) {
        	setQueryResultsFromAgent();
        }

        int pageEntityNum = Integer.valueOf(ConfReader.getConfProperty("page_entity_num"));
        int start = pageEntityNum * page;
        if (start >= this.searchResults.size())
            return;
        int end = start + pageEntityNum > this.searchResults.size() ? this.searchResults.size() : start + pageEntityNum;

        int snippetNum = Integer.valueOf(ConfReader.getConfProperty("snippet_num"));
        ArrayList<List<SnippetBean>> snippets = new ArrayList<List<SnippetBean>>(pageEntityNum);
        for (int i = start; i < end; i ++) {
            Searchable entity = this.searchResults.get(i);
            List<SnippetBean> sbean = this.snippetGen.getSnippetsForEntity(entity, this, snippetNum);
            snippets.add(sbean);
        }
        this.snippetsForCurrentPage = snippets;
    }
    protected void setQueryResultsFromAgent() {
    	if (this.searchResults != null)
    		return;
    	int maximumEnityNum = Integer.valueOf(ConfReader.getConfProperty("max_entity_num"));
        this.searchResults = this.queryAgent.getEntitiesForQuery(this.query, this.selectedTags, this.excludedTags,
                this.tagsFilter, this.entityFactory, this.comparator, maximumEnityNum);
    }
    public List<List<SnippetBean>> getEntitySnippetsForPage(int page, long timeout, TimeUnit unit) {
        if (this.currentPage == page && this.snippetsForCurrentPage != null)
            return this.snippetsForCurrentPage;
        if (this.currentPage != page || (! this.snippetsStarted)) {
            this.currentPage = page;
            this.loadEntitySnippetsForPage(page);
        }
        long startNano = System.nanoTime();
        long timeoutNano = unit.toNanos(timeout);
        while (this.snippetsForCurrentPage == null) {
            if (System.nanoTime() - startNano > timeoutNano)
                break;
        }
        if (this.snippetsForCurrentPage != null)
            this.snippetsStarted = false;
        return this.snippetsForCurrentPage;
    }

    public String getQuery() {
        return this.query;
    }
    public List<SemTag> getSelectedTags() {
        return this.selectedTags;
    }
    public List<SemTag> getExcludedTags() {
        return this.excludedTags;
    }

    /**
     *
     * @param page current page num,starts from 0
     * @return the total num of entities in result.
     */
    public int getQueryEntitiesInPage(int page, List<Searchable> result) {
        if (result == null)
            throw new IllegalArgumentException("The result list should be initialized.");
        if (this.searchResults == null) {
        	setQueryResultsFromAgent();
        }
        int pageEntityNum = Integer.valueOf(ConfReader.getConfProperty("page_entity_num"));
        int start = pageEntityNum * page;
        if (start >= this.searchResults.size())
            return this.searchResults.size();

        int end = start + pageEntityNum > this.searchResults.size() ? this.searchResults.size() : start + pageEntityNum;

        result.addAll(this.searchResults.subList(start, end));
        return this.searchResults.size();
    }

    private String getQueryContextID() {
        List<String> seleTagIDs = null, exclTagIDs = null;
        if (this.selectedTags != null) {
            seleTagIDs = new ArrayList<String>(this.selectedTags.size());
            for (SemTag tag : this.selectedTags) {
                seleTagIDs.add(tag.getIdentifier());
            }
        }
        if (this.excludedTags != null) {
            exclTagIDs = new ArrayList<String>(this.excludedTags.size());
            for (SemTag tag : this.excludedTags) {
                exclTagIDs.add(tag.getIdentifier());
            }
        }
        return Util.getQueryContextIDFrom(this.query, seleTagIDs, exclTagIDs);
    }
    public String getIdentifier() {
        if (this.identifier == null)
            this.identifier = this.getQueryContextID();
        return this.identifier;
    }

    public boolean equals(Object o) {
        if (o instanceof QueryContext) {
            QueryContext qc = (QueryContext) o;
            return this.getIdentifier().equals(qc.getIdentifier());
        }
        return false;
    }
    public int hashCode() {
        return this.getIdentifier().hashCode();
    }
}
