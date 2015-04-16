/**
 * Created by Xiangqian on 2015/4/14.
 */
SYNSET_TAGS_SEARCH = 2
TYPE_TAGS_SEARCH = 1
NO_TAGS_SEARCH = 0

DEFAULT_DES_KEY = CryptoJS.enc.Utf8.parse("this is a des key for sview system.");
function getEncrypedMessage(message) {
    var encrypted = CryptoJS.DES.encrypt(message, DEFAULT_DES_KEY, {
        mode : CryptoJS.mode.ECB,
        padding : CryptoJS.pad.Pkcs7
    });
    return encrypted;
}
function getHrefForBrowsingSingleEntityViaSView(eid, etype, lang) {
    var entype = etype || 'o';
    var dlang = lang || 'en';
    var ret = 'http://ws.nju.edu.cn/sview/views/eview.jsp?lang=' + dlang;
    var encrypted = getEncrypedMessage(entype + eid);
    ret += '&id=' + encodeURIComponent(encodeURIComponent(encrypted));
    return ret;
}

$(document).ready(function() {
    CURR_STYPE = getCurrentSearchType();
    $('#qform').submit(function(event) {
        event.preventDefault();
        var query = $('#query').val();
        if (query && query.length > 0) {
            var redirect = getQueryUrl(query, 0, CURR_STYPE);
            window.location.href = redirect;
        }
    });
    
    var $restrictsPanel = $('#tag-restricts');
    if ($restrictsPanel.length > 0) {
    	$restrictsPanel.on('click', '.stag', function(event) {
    		$(this).remove();
    		var queryUrl = getQueryUrl(getQueryText(), 0, CURR_STYPE, getSelectedTags(), getExcludedTags());
    		window.location.href = queryUrl;
    	});
    	$restrictsPanel.on('click', '.removeall', function(event) {
    		event.preventDefault();
    		$restrictsPanel.children('.stag').each(function(i) {
    			$(this).remove();
    		});
    		var queryUrl = getQueryUrl(getQueryText(), 0, CURR_STYPE, getSelectedTags(), getExcludedTags());
    		window.location.href = queryUrl;
    	});
    }

    var $tagsFilterPanel = $('div.semfilter-panel');
    if ($tagsFilterPanel.length > 0) {
        //new Spinner({
        //    background: "transparent",
        //    lines: 10,
        //    length: 10,
        //    shadow: false,
        //    hwaccel: true,
        //    top: 'auto',
        //    left: 'auto',
        //    radius: 10
        //}).spin($tagsFilterPanel);

        var $candTags = $tagsFilterPanel.children('.cand-tag');
        if ($candTags.length == 0) {
            loadCandidateTags($tagsFilterPanel);
        }
    }

    $('body').on('click', '.query-link', function(event) {
        var page = $(this).attr('page');
        if (page) {
            var qurl = getQueryUrl(getQueryText(), page, CURR_STYPE, getSelectedTags(), getExcludedTags());
            window.location.href = qurl;
            return;
        }
        page = 0;
        var isOther = $(this).hasClass("others-tag");
        if (isOther) {
            var excluded = getExcludedTags();
            $tagsFilterPanel.children('.cand-tag').each(function(i) {
                excluded.push($(this).attr('tagID'));
            });
            var qurl = getQueryUrl(getQueryText(), 0, CURR_STYPE, getSelectedTags(), excluded);
            window.location.href = qurl;
        }
        else {
            var selected = getSelectedTags();
            var tagID = $(this).attr('tagID');
            selected.push(tagID);
            var qurl = getQueryUrl(getQueryText(), 0, CURR_STYPE, selected, getExcludedTags());
            window.location.href = qurl;
        }
    });

    var $resultsPanel = $('.results-panel');
    if ($resultsPanel.length > 0) {
        $resultsPanel.on('click', '.entity-link', function (event) {
            var uri = $(this).attr('uri');
            var burl = getHrefForBrowsingSingleEntityViaSView(uri);
            window.open(burl, "_blank");
        });
        loadEntitySnippets($resultsPanel);
    }
});

function getCurrentPage() {
    return $('#curr-page').text();
}

function loadEntitySnippets($resultsPanel) {
    var qcontextObj = packCurrentQueryContextToJSONObject();
    var qcontextStr = encodeURIComponent(JSON.stringify(qcontextObj));
    var dataStr = getCurrentPage();
//    console.log("load snippets for " +  qcontextStr);
    $.ajax({
        url: 'search',
        data: {action: 'getSnippetsForPage',
                qcontext: qcontextStr,
                data: dataStr},
        dataType: 'json',
        type: 'post',
        success: function(resp) {
//        	console.log("tags response: " + JSON.stringify(resp));
        	if (resp.length == 0) {
        		loadEntitySnippets($resultsPanel);
        		return;
        	}
            var $entities = $resultsPanel.children('.entity-block');
            $entities.each(function(i) {
            	$(this).children('.entity-snippets').removeClass('loading-data');
                setSnippetsForEntity($(this).children('.entity-snippets').children('ul'), resp[i]);
            });
        }

    });
}

function setSnippetsForEntity($target, snippetsList) {
    var height = 0;
    $.each(snippetsList, function(i, snippet) {
        var $compo = getSnippetComponent(snippet);
        $target.append($compo);
        height += $compo.outerHeight(true);
    });
    $target.height(height);
}
function getSnippetComponent(snippet) {
    var $li = $('<li>');
    var $rel = $('<span>').addClass('snippet-relation').text(snippet.p.label + " : ").appendTo($li);
    var $content = $('<span>').addClass('snippet-content').appendTo($li);
    if (snippet['or']) {
        var o = snippet['or'];
        $content.append($('<a>').addClass('entity-link link').attr('uri', o.uri).text(o.label));
    }
    else if (snippet['ol']) {
        var o = snippet['ol'];
        $content.append(summaryLiteral(o));
    }
    return $li;
}
function summaryLiteral(str) {
    if (str.length > 200)
        return str.substr(0, 200) + '...';
    return str;
}

function packCurrentQueryContextToJSONObject() {
    return {
        query: getQueryText(),
        searchType: getCurrentSearchType(),
        seleTagIDs: getSelectedTags(),
        exclTagIDs: getExcludedTags()
    };
}

function loadCandidateTags($target) {
    var qcontextObj = packCurrentQueryContextToJSONObject();
    var qcontextStr = encodeURIComponent(JSON.stringify(qcontextObj));
//    console.log("load snippets for " +  qcontextStr);

    $.ajax({
        url: 'search',
        data: {action: 'getCandidateTags',
                qcontext: qcontextStr},
        dataType: 'json',
        type: 'post',
        success: function(resp) {
        	console.log("tags response:" + JSON.stringify(resp));
        	
            if (resp['status'] == 'not ready') {
                loadCandidateTags($target);
                return;
            }
            $target.removeClass('loading-data');
            $.each(resp, function(i, stag) {
                var $span = getTagComponent(stag);
                $span.addClass('cand-tag');
                $target.append($span);
            });
            var $otherTag = getTagComponent({tagID: '', label: 'Others'});
            $otherTag.addClass('others-tag');
            $target.append($otherTag);
        }
    });
}

function getTagComponent(stag) {
    return $('<span>').addClass('query-link list-group-item ctag').attr('tagID', stag.tagID).append($('<span>').attr('aria-hidden', 'true').addClass('glyphicon glyphicon-tag')).append('&nbsp;&nbsp;').append(stag.label);
}

function getCurrentSearchType() {
    return $('#search-type').text();
}

function getQueryText() {
    var $qp = $('#curr-query');
    if ($qp.length > 0) {
        var query = $qp.text();
        return query;
    }
    var query = $('#query').val();
    query = query.trim();
    return query;
}

function getSelectedTags() {
    var $restricts = $('#tag-restricts');
    if ($restricts.length == 0)
        return [];
    var tagIDs = [];
    $restricts.children('.stag.selected').each(function(i) {
        tagIDs.push($(this).attr('tagID'));
    });
    return tagIDs;
}

function getExcludedTags() {
    var $restricts = $('#tag-restricts');
    if ($restricts.length == 0)
        return [];
    var excluded = [];
    $restricts.children('.stag.excluded').each(function(i) {
        var tagID = $(this).attr('tagID');
        excluded.push(tagID);
    });
    return excluded;
}

function getQueryUrl(query, page, searchType, selectedTagIDs, excludedTagIDs) {
    if (searchType == undefined) {
        searchType = SYNSET_TAGS_SEARCH;
    }
    if (page == undefined) {
        page = 0;
    }
    var queryUrl = "/expedia/search.jsp?";
    queryUrl += "t=" + searchType;
    var queryEncoded = encodeURIComponent(query);
    queryUrl += "&q=" + queryEncoded;
    queryUrl += "&p=" + page;

    if (selectedTagIDs && selectedTagIDs.length > 0) {
        var ststr = "&st=";
        $.each(selectedTagIDs, function(i, tid) {
            ststr += tid;
            if (i < selectedTagIDs.length - 1)
                ststr += ";";
        });
        queryUrl += ststr;
    }
    if (excludedTagIDs && excludedTagIDs.length > 0) {
        var etstr = "&et=";
        $.each(excludedTagIDs, function(i, tid) {
            etstr += tid;
            if (i < excludedTagIDs.length - 1)
                etstr += ";";
        });
        queryUrl += etstr;
    }
    return queryUrl;
}
