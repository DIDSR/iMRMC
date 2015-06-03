/*js/drexplain/drexplain.data-manager.js*/
DR_EXPLAIN.namespace( 'DR_EXPLAIN.dataManager' );

DR_EXPLAIN.dataManager = (function() {
	var data_resize = DR_EXPLAIN.data_resize;
	var data_search = DR_EXPLAIN.data_search;
	var data_menu = DR_EXPLAIN.data_menu;

	var drex_node_names = data_menu.DREX_NODE_NAMES;
	var drex_node_links = data_menu.DREX_NODE_LINKS;
	var drex_node_child_start = data_menu.DREX_NODE_CHILD_START;
	var drex_node_child_end = data_menu.DREX_NODE_CHILD_END;

	var drex_node_parent = new Array();
	var drex_node_deep = new Array();

	var drex = {};


	function drex_node(ndx)
	{
	  this.node_index = ndx;
	  this.title = drex_node_names[this.node_index];
	  this.link = drex_node_links[this.node_index];
	  this.deep = drex_node_deep[this.node_index];

	  this.children = function(){
		var result = new Array();
		for (var i = drex_node_child_start[this.node_index]; i < drex_node_child_end[this.node_index]; i++)
		  result.push(new drex_node(i));
		return result;
	  };
	  this.childrenCount = function(){
		if (drex_node_child_start[this.node_index] >= drex_node_child_end[this.node_index])
		  return 0;
		return drex_node_child_end[this.node_index] - drex_node_child_start[this.node_index];
	  };
	  this.parent = function(){
		if (drex_node_parent[this.node_index] == -1)
			return null;
		return new drex_node(drex_node_parent[this.node_index]);
	  };
	  this.isActive = function(){
		if (!drex.active_node)
			return false;
	    return (this.node_index == drex.active_node.node_index);
	  };
	};

	var initMenu = function() {

		drex_node_parent[0] = -1;
		drex_node_deep[0] = 0;
		for (var i in drex_node_names)
		{
		  for (var j = drex_node_child_start[i]; j < drex_node_child_end[i]; j++)
		  {
		    drex_node_parent[j] = i;
		    drex_node_deep[j] = drex_node_deep[i] + 1;
		  }
		}

		drex = new Object();
		drex.nodes_count = drex_node_names.length;

		drex.root_node = function()
		{
		  return new drex_node(0);
		};

		drex.active_node = null;
		for (var i in drex_node_links)
		{
		  if (drex_node_links[i] == getPageFilename())
		    drex.active_node = new drex_node(i);
		}


		function drex_resultContainer(){
		   this.result = "";
		}
	};


	function drex_keyword(ndx)
	{
	  this.keyword_index = ndx;
	  this.title = drex_keyword_names[this.keyword_index];
	  this.deep = drex_keyword_deep[this.keyword_index];

	  this.children = function(){
		var result = new Array();
		for (var i = drex_keyword_child_start[this.keyword_index]; i < drex_keyword_child_end[this.keyword_index]; i++)
		  result.push(new drex_keyword(i));
		return result;
	  };
	  this.childrenSorted = function(){
		var result = this.children();
		result.sort(function(a,b){
		  if (a.title.toLowerCase() < b.title.toLowerCase())
			return -1;
		  if (a.title.toLowerCase() > b.title.toLowerCase())
		    return 1;
		  return 0;
		});
		return result;
	  };
	  this.childrenCount = function(){
		if (drex_keyword_child_start[this.keyword_index] >= drex_keyword_child_end[this.keyword_index])
		  return 0;
		return drex_keyword_child_start[this.keyword_index] - drex_keyword_child_end[this.keyword_index];
	  }
	  this.parent = function(){
		if (drex_keyword_parent[this.keyword_index] == -1)
			return null;
		return new drex_keyword(drex_keyword_parent[this.keyword_index]);
	  };
	  this.nodes = function(){
		var result = new Array();
	 	for (var i = drex_keyword_nodes_start[this.keyword_index]; i < drex_keyword_nodes_end[this.keyword_index]; i++)
		  result.push(new drex_node(drex_keyword_nodes[i]));
		return result;
	  };
	  this.isActive = function(){
	    return this.keyword_index != 0;
	  };
	};




	var data_index = DR_EXPLAIN.data_index;

	var drex_node_keywords = data_index.DREX_NODE_KEYWORDS;
	var drex_node_keywords_start = data_index.DREX_NODE_KEYWORDS_START;
	var drex_node_keywords_end = data_index.DREX_NODE_KEYWORDS_END;

	var drex_keyword_names = data_index.DREX_KEYWORD_NAMES;
	var drex_keyword_child_start = data_index.DREX_KEYWORD_CHILD_START;
	var drex_keyword_child_end = data_index.DREX_KEYWORD_CHILD_END;

	var drex_keyword_parent = new Array();
	var drex_keyword_deep = new Array();

	var drex_keyword_nodes = new Array(drex_node_keywords.length);
	var drex_keyword_nodes_start = new Array(drex_keyword_names.length);
	var drex_keyword_nodes_end = new Array(drex_keyword_names.length);



	var initIndex = function() {

		//keyword tree structure
		drex_keyword_parent[0] = -1;
		drex_keyword_deep[0] = 0;
		for (var i in drex_keyword_names)
		{
		  for (var j = drex_keyword_child_start[i]; j < drex_keyword_child_end[i]; j++)
		  {
		    drex_keyword_parent[j] = i;
		    drex_keyword_deep[j] = drex_keyword_deep[i] + 1;
		  }
		}

		drex.keywords_count = drex_keyword_names.length;


		//making drex_keyword_nodes from drex_node_keywords
		var temp_drex_keyword_write_pos = new Array(drex.keywords_count + 1);

		for (var i = 0; i < temp_drex_keyword_write_pos.length; i++)
		  temp_drex_keyword_write_pos[i] = 0;
		for (var i in drex_node_keywords)
		  ++temp_drex_keyword_write_pos[drex_node_keywords[i] + 1];
		for (var i = 1; i < temp_drex_keyword_write_pos.length; i++)
		  temp_drex_keyword_write_pos[i] += temp_drex_keyword_write_pos[i-1];

		for (var i = 0; i < drex.keywords_count; i++)
		  drex_keyword_nodes_start[i] = temp_drex_keyword_write_pos[i];

		for (var i = 0; i < drex.nodes_count; i++)
		  for (var j = drex_node_keywords_start[i]; j < drex_node_keywords_end[i]; j++)
		  {
			var kw = drex_node_keywords[j];
			drex_keyword_nodes[temp_drex_keyword_write_pos[kw]] = i;
			++temp_drex_keyword_write_pos[kw];
		  }
		for (var i = 0; i < drex.keywords_count; i++)
			drex_keyword_nodes_end[i] = temp_drex_keyword_write_pos[i];


		drex_node.prototype.keywords = function(){
			var result = new Array();
		 	for (var i = drex_node_keywords_start[this.node_index]; i < drex_node_keywords_end[this.node_index]; i++)
			  result.push(new drex_keyword(drex_node_keywords[i]));
			return result;
		};

		//interface
		drex.root_keyword = function()
		{
		  return new drex_keyword(0);
		};
	};


	var getPageFilename = function() {
		if ( window[ 'drex_file_name' ] !== undefined ) {
			return window.drex_file_name;
		}
		else {
			return null;
		}
	};


	var API = {
		init: function() {
			initMenu();
			initIndex();
		},

		isFrameModeEnabled: function() {
			if ( data_resize.DREXPLAIN_FIT_HEIGHT_TO_WINDOW === 1 ) {
				return true;
			}
			else {
				return false;
			}
		},

		getStartingMenuWidth: function() {
			return parseInt(data_resize.DREX_INITIAL_MENU_WIDTH);
		},

		getSearchTextNoResults: function() {
			return data_search.DREXPLAIN_NOT_FOUND;
		},
		
		getSearchTextInPreviewMode: function() {
			return data_search.DREXPLAIN_PREVIEW_MODE_SEARCH_IS_DISABLED_NOTICE;
		},
		
		getErrorInLocalSearch: function() {
			return data_search.DREXPLAIN_ERROR_LOCAL_SEARCH;
		},

		getSearchTextEmptyString: function() {
			return data_search.DREXPLAIN_EMPTY_STRING;
		},

		getSearchTextInProgress: function() {
			return data_search.DREXPLAIN_IN_PROGRESS;
		},


		getPageFilename: function() {
			return getPageFilename();
		},


		//drex.nodes_count
		getNodesCount: function() {
			return drex.nodes_count;
		},

		//drex.root_node()
		getRootNode: function() {
			return drex.root_node();
		},

		//drex.root_node()
		getRootNodesArray: function() {
			if (!data_menu.DREX_HAS_ROOT_NODE)
				return drex.root_node().children();
			return [drex.root_node()];
		},

		//drex.active_node
		getSelectedNode: function() {
			return drex.active_node;
		},

		//!drex.active_node
		isSelectedNodeExists: function() {
			if ( !drex.active_node ) {
				return false;
			}
			else {
				return true;
			}
		},

		//function drex_node(ndx)
		createNodeClassByIndex: function( nodeIndex ) {
			return new drex_node( nodeIndex );
		},

		// DrexObjectsManager.menu.createNodeClassByIndex( i ) ).deep <= 0 ? 1 : 0;
		getNodeDeepByIndex: function( nodeIndex ) {
			return drex_node_deep[ nodeIndex ];
		},

		getIndexByNode: function( node ) {
			return node.node_index;
		},

		getDrex: function() {
			return drex;
		},

		getDrexNode: function( ndx ) {
			return drex_node( ndx );
		},
		
		getDrexMenuType: function() {
			return data_menu.DREX_MENU_TYPE;
		}
	};

	return API;

})();
/*js/drexplain/drexplain.dom.js*/
DR_EXPLAIN.namespace( 'DR_EXPLAIN.dom' );

/**
 * @returns {DomCached}
 */
DR_EXPLAIN.dom = {
	PAGE_CONTENT_HEADER__HIDDEN_CLASS: "m-pageContent__withoutHeader",
	PAGE_CONTENT_FOOTER__HIDDEN_CLASS: "m-pageContent__withoutFooter",
	PAGE_CONTENT_LEFT__HIDDEN_CLASS: "m-pageContent__withoutRight",
	PAGE_CONTENT_RIGHT__HIDDEN_CLASS: "m-pageContent__withoutLeft",

	TAB_WRAPPER_ITEM__SELECTED_CLASS: "m-tabs__wrapperItem__selected",
	TAB_SELECTOR_ITEM__SELECTED_CLASS: "m-tabs__selectorItem__selected",

	FRAME_ENABLED_CLASS: "m-pageView__state__frame",

	_isIeLessThan9: null,
	_isIe: null,

	init: function() {
		this.$html = $( "html" );
		this.$body = $( "body" );

		this.$internal_wrapper = $( "#internal_wrapper" );
		this.$pageContent = $( "#pageContent" );
		this.$pageContentHeader = $( "#pageContentHeader" );
		this.$pageContentFooter = $( "#pageContentFooter" );
		this.$pageContentArticleSide = $( "#pageContentArticleSide" );
		this.$pageContentLeft = $( "#pageContentLeft" );
		this.$pageContentRight = $( "#pageContentRight" );

		this.$tabWrapperItems = $( "#tabWrapperItems" );

		this.$article = $( "#article" );
		this.$articleHeader = $( "#article__header" );
		this.$articlePreWrapper = this.$article.children( ".b-article__preWrapper" );
		this.$articleWrapper = this.$article.find( ".b-article__wrapper" );
		this.$articleInnerWrapper = this.$article.find( ".b-article__innerWrapper" );
		this.$articleGeneratorCopyright = this.$article.find( ".b-article__generatorCopyright" );

		this.$headerSide__nav = $("#headerSide__nav");
		this.$headerSide__nav__breadCrumbs = $("#headerSide__nav__breadCrumbs");
		this.$headerSide__buttons = $("#headerSide__buttons");
		
		this.$splitter = $( "#splitter" );
		this.$workZone = $( "#workZone" );
		this.$workZoneSideNav = $( "#workZone_nav" );
		this.$workZoneSideNavContent = $( "#workZone_nav_content" );
		this.$workZoneSideArticle = $( "#workZone_article" );
		this.$workZoneSideArticleContent = $( "#workZone_article__content" );

		this.$tabsWrapperItems = $( "#tabsWrapperItems" );


		this.tabs = {};

		this.tabs.menu = {};
		this.tabs.menu.$selectorItem = $( "#tabSelector_menu" );
		this.tabs.menu.$wrapperItem = $( "#tabWrapper_menu" );
		this.tabs.menu.$wrapperItemInner = this.tabs.menu.$wrapperItem.children( ".b-tabs__wrapperItemInner" );
		this.tabs.menu.$tree = this.tabs.menu.$wrapperItemInner.children( ".b-tree" );

		this.tabs.index = {};
		this.tabs.index.$selectorItem = $( "#tabSelector_index" );
		this.tabs.index.$wrapperItem = $( "#tabWrapper_index" );
		this.tabs.index.$wrapperItemInner = this.tabs.index.$wrapperItem.children( ".b-tabs__wrapperItemInner" );
		this.tabs.index.$tree = this.tabs.index.$wrapperItemInner.children( ".b-tree" );


		this.tabs.search = {};
		this.tabs.search.$selectorItem = $( "#tabSelector_search" );
		this.tabs.search.$wrapperItem = $( "#tabWrapper_search" );
		this.tabs.search.$wrapperItemInner = this.tabs.search.$wrapperItem.children( ".b-tabs__wrapperItemInner" );
		this.tabs.search.$tree = this.tabs.search.$wrapperItemInner.children( ".b-tree" );



			// todo: remove or refactor

				this.$tabWrapperItemArr = this.tabs.menu.$wrapperItem.add( this.tabs.index.$wrapperItem ).add( this.tabs.search.$wrapperItem );
				this.$tabWrapperItemInnerArr = this.tabs.menu.$wrapperItemInner.add( this.tabs.index.$wrapperItemInner ).add( this.tabs.search.$wrapperItemInner );
				this.$treeArr = this.$tabWrapperItemInnerArr.find( ".b-tree" );
				this.$tabWrapperMenu = this.tabs.menu.$wrapperItem;

				// remove:
				this.$tabWrapperIndex = this.tabs.index.$wrapperItem;
				this.$tabWrapperSearch = this.tabs.search.$wrapperItem;

				this.$tabSelectorMenu = this.tabs.menu.$selectorItem;
				this.$tabSelectorIndex = this.tabs.index.$selectorItem;
				this.$tabSelectorSearch = this.tabs.search.$selectorItem;
			//


		this.$tabSearchFormWrapper = $( "#tabs_searchFormWrapper" );

		this.$tabSearchSubmit = $( "#tabs_searchSubmit" );
		this.$tabSearchInput = $( "#tabs_searchInput" );
		this.$tabSearchInputLabel = $( "#tabs_searchInput_label" );

		this.$workZoneSearchSubmit = $( "#workZone_searchSubmit" );
		this.$workZoneSearchInput = $( "#workZone_searchInput" );
		this.$workZoneSearchInputLabel = $( "#workZone_searchInput_label" );


		this.$keywordContextMenu = $( "#keywordContextMenu" );

		this.$searchProgress = $( "#searchProgress" );
		this.$hasVerticalScrollbar = {};
		this.$hasHorizontalScrollbar = {};
		

/*		this.$tabItemWrapperInnerArr = $( ".b-tabs__wrapperItemInner" );
		this.$treeArr = $( ".b-tabs__wrapperItemInner .b-tree" );*/


	},

	isIe: function() {
		if ( this._isIe === null ) {
			if ( this.$html.hasClass( "ie" ) ) {
				this._isIe = true;
			}
			else {
				this._isIe = false;
			}
		}

		return this._isIeLessThan9;
	},

	isIeLessThan9: function() {
		if ( this._isIeLessThan9 === null ) {
			if ( this.$html.hasClass( "ie6" ) || this.$html.hasClass( "ie7" ) || this.$html.hasClass( "ie8" )) {
				this._isIeLessThan9 = true;
			}
			else {
				this._isIeLessThan9 = false;
			}
		}

		return this._isIeLessThan9;
	},

	getVisibleTab: function() {
		if ( this.isTabVisible( this.tabs.menu ) ) {
			return this.tabs.menu;
		}
		else if ( this.isTabVisible( this.tabs.index ) ) {
			return this.tabs.index;
		}
		else if ( this.isTabVisible( this.tabs.search ) ) {
			return this.tabs.search;
		}
		else {
			return null;
		}
	},

	getVisibleItemWrapperInner: function() {
		var visibleTab = this.getVisibleTab();
		if ( visibleTab !== null ) {
			return visibleTab.$wrapperItemInner;
		}
		else {
			return null;
		}
	},

	isTabVisible: function( tab ) {
		if ( tab.$wrapperItem.hasClass( this.TAB_WRAPPER_ITEM__SELECTED_CLASS ) ) {
			return true;
		}
		else {
			return false;
		}
	},

	isKeywordContextMenuVisible: function() {
		if ( this.$keywordContextMenu.hasClass( "m-tree__contextMenu__visible" ) ) {
			return true;
		}
		else {
			return false;
		}
	},

	getCssNumericValue: function( $elem, cssProp ) {
		return parseInt( $elem.css( cssProp ), 10 );
	},

	isPageHeaderVisible: function() {
		if ( this.$pageContent.hasClass( this.PAGE_CONTENT_HEADER__HIDDEN_CLASS ) ) {
			return false;
		}
		else {
			return true;
		}
	},

	isPageFooterVisible: function() {
		if ( this.$pageContent.hasClass( this.PAGE_CONTENT_FOOTER__HIDDEN_CLASS ) ) {
			return false;
		}
		else {
			return true;
		}
	},

	isPageLeftVisible: function() {
		if ( this.$pageContent.hasClass( this.PAGE_CONTENT_LEFT__HIDDEN_CLASS ) ) {
			return false;
		}
		else {
			return true;
		}
	},

	isPageRightVisible: function() {
		if ( this.$pageContent.hasClass( this.PAGE_CONTENT_RIGHT__HIDDEN_CLASS ) ) {
			return false;
		}
		else {
			return true;
		}
	}
};
/*js/drexplain/drexplain.search-engine.js*/
DR_EXPLAIN.namespace( 'DR_EXPLAIN.searchEngine' );
DR_EXPLAIN.searchEngine = (function() {

		function isLocalSearch()
		{
			var local = /file:/i;
			return local.test(dirname());
		}
/**
         * Append a tag to the properties of an object for each item in an array
         *
         * @param {Object} tags
         *         The object whose property values will be modified
         * @param {Array} items
         *         The list of property names to set on the object
         * @param {String} tag
         *         The value to append to each property value in the object
         */
        function setTag(tags, items, tag) {
            for (var i = 0; i < items.length; i++) {
                var item = items[i];

                if (tags.hasOwnProperty(item)) {
                    tags[item] += tag;
                } else {
                    tags[item] = tag;
                }
            }
        }

 /**
         * Return a filtered list of property names for the specified object.
         * Each property value that causes the specified match function to
         * return true will be returned in the resulting array
         *
         * @param {Object} tags
         *         The object whose property names will be filtered
         * @param {Function} matchFunction
         *         A function that takes a single parameter, returning true or
         *         false based on the value of that parameter. "filter" will pass
         *         in each property name of the "tags" object to determine if it
         *         should be included in the resulting array
         * @return {Array} Returns an array of property names whose values
         *         were accepted by the matchFunction
         */
        function filter(tags, matchFunction) {
            var result = [];

            for (var p in tags) {
                if (matchFunction(tags[p])) {
                    result.push(p);
                }
            }

            return result;
        }

        /**
         * Find the intersection of two sets
         *
         * @param {Array} setA
         * @param {Array} setB
         * @return {Array} Returns the result of this set operation
         */
        function intersect(setA, setB) {
            var tags = {};

            setTag(tags, setA, "A");
            setTag(tags, setB, "B");

            return filter(tags, function(value) { return value == "AB" });
        }

        /**
         * Find the difference of two sets
         *
         * @param {Array} setA
         * @param {Array} setB
         * @return {Array} Returns the result of this set operation
         */
        function difference(setA, setB) {
            var tags = {};

            setTag(tags, setA, "A");
            setTag(tags, setB, "B");

            return filter(tags, function(value) { return value != "AB" });
        }

        /**
         * Remove all members of setA from setB
         *
         * @param {Array} setA
         * @param {Array} setB
         * @return {Array} Returns the result of this operation
         */
        function remove(setA, setB) {
            var tags = {};

            setTag(tags, setA, "A");
            setTag(tags, setB, "B");

            return filter(tags, function(value) { return value == "B" });
        }
		function unite(setA, setB)
		{
            var tags = {};

            setTag(tags, setA, "A");
            setTag(tags, setB, "B");
			return filter(tags, function(value) { return true;});
		}
/**
 * @author 1
 */
/*	var strFoundNothing = "Nothing was found";
	var strSearchStringIsEmpty = "Please, enter a string for search!";
	var strSearchInProgress = "Searching...";*/

	var IndexOfFiles = new Array();
	var StringsForSearch = new Array();
	var StringPairArray = new Array();
	var SearchResults=new Array();
	var iStringToSearch=0;
	var HTTP = {};

	HTTP.newRequest = function()
		{
			var xmlhttp=false;
			   /* running locally on IE5.5, IE6, IE7 */                                              
			     if(location.protocol=="file:"){
			      if(!xmlhttp)try{ xmlhttp=new ActiveXObject("MSXML2.XMLHTTP"); }catch(e){xmlhttp=false;}
			      if(!xmlhttp)try{ xmlhttp=new ActiveXObject("Microsoft.XMLHTTP"); }catch(e){xmlhttp=false;}
			     }                                                                                
			   /* IE7, Firefox, Safari, Opera...  */
			     if(!xmlhttp)try{ xmlhttp=new XMLHttpRequest(); }catch(e){xmlhttp=false;}
			   /* IE6 */
			     if(typeof ActiveXObject != "undefined"){
			      if(!xmlhttp)try{ xmlhttp=new ActiveXObject("MSXML2.XMLHTTP"); }catch(e){xmlhttp=false;}
			      if(!xmlhttp)try{ xmlhttp=new ActiveXObject("Microsoft.XMLHTTP"); }catch(e){xmlhttp=false;}
			     }
			   /* IceBrowser */
			     if(!xmlhttp)try{ xmlhttp=createRequest(); }catch(e){xmlhttp=false;}

			if (!xmlhttp)
			{
				throw new Error("Failed to initialize XMLHttpRequest");
			}
			return xmlhttp;
		};


	var request = HTTP.newRequest();

	function ID()
	{
		if (!SearchResults.length) getSearchResultOutput();
		var sID = dirname() + "/de_search/ids.txt";
		request.open("GET", sID, true);
		request.onreadystatechange = function()
		{
			if (request.readyState == 4)
			if (request.status == 200 || request.status == 0)
			{
				var arrFileId = (request.responseText).split(/\s*\n\s*/);
				var h;
				for (var i = 0; i < SearchResults.length; i++)
				{
					h = (SearchResults[i] - 1) * 3;
					SearchResults[i] = new Array();

					if (!arrFileId[h + 1] || !arrFileId[h + 2])
					{
						if (!!arrFileId[h + 2])
						{
							SearchResults[i][0] = arrFileId[h + 2];
							SearchResults[i][1] = arrFileId[h + 2];
						}
						else
						{
							//Something is wrong, abort search
							SearchResults = new Array();
							SearchResults[0] = new Array();
							SearchResults[0][0] = "Error!";
							SearchResults[0][1] = "mailto:support@drexplain.com";
							getSearchResultOutput();
							return;
						}
					}
					else
					{
						SearchResults[i][0] = arrFileId[h + 1];
						SearchResults[i][1] = arrFileId[h + 2];
					}
				}
				getSearchResultOutput();
			}
		}
		request.send(null);

	}

	function getSearchResultOutput()
	{
		$( document ).trigger( "searchComplete" );
	}

	function SearchInFile()
	{
		if (request.readyState != 4) return;
		if (request.status != 200 && request.status != 0)  return;

		var arrFileStrings	= (request.responseText).split(/\s*;\s*/);
		var stToSearch 		= StringPairArray[iStringToSearch][0];

		var isFirstIteration = true;
		var wasFound		= false;
		var curResults = new Array();
		for (var i = 0; i < arrFileStrings.length; i += 2)
		{

			if (arrFileStrings[i].indexOf(stToSearch) == 0)
			{
				var pages = arrFileStrings[i + 1];
				pages = pages.split(/\s*,\s*/);
				curResults = unite(curResults, pages);
				wasFound = true;
			}
		}
		if (iStringToSearch == 0) //this is first result - adding all curResults to SearchResults
			SearchResults = SearchResults.concat(curResults);
		else
			SearchResults = intersect(SearchResults, curResults);
		// If there are no results after a certain iteration then there's no sense to AND-search anymore
		if (!SearchResults.length) return getSearchResultOutput();
		iStringToSearch++;
		SearchForNextString();
	}



		function SearchForNextString()
		{
			if (iStringToSearch >= StringsForSearch.length) return ID();
			var sURL = dirname() + "/de_search/"+StringPairArray[iStringToSearch][1];
			request.open("GET", sURL, true);
			request.onreadystatechange = SearchInFile;
			request.send(null);
		}
	function strcmp ( str1, str2 ) {
	    // Binary safe string comparison
	    //
	    // version: 909.322
	    // discuss at: http://phpjs.org/functions/strcmp
	    // +   original by: Waldo Malqui Silva
	    // +      input by: Steve Hilder
	    // +   improved by: Kevin van Zonneveld (http://kevin.vanzonneveld.net)
	    // +    revised by: gorthaur
	    // *     example 1: strcmp( 'waldo', 'owald' );
	    // *     returns 1: 1
	    // *     example 2: strcmp( 'owald', 'waldo' );
	    // *     returns 2: -1
	    return ( ( str1 == str2 ) ? 0 : ( ( str1 > str2 ) ? 1 : -1 ) );
	}
		function AttachFilesToStrings()
		{
			for (var i = 0; i < StringsForSearch.length; i++)
			{
				var st = StringsForSearch[i].toUpperCase();
				var st1 = st.substr(0,1);
				var j = 0;
				var bFound = -1;
				while(j < IndexOfFiles.length && bFound == -1)
				{
					switch (strcmp(st1, IndexOfFiles[j].first.substr(0,1)))
					{
						case 0: bFound = true; break;
						case -1: bFound = false; break;
						case 1: ++j;
					}
				}
				if (bFound == -1 || bFound == false)
				{
					getSearchResultOutput();
					return;
				}
				//We have found words beginning with first letter of st
				bFound = false;
				switch (strcmp(st, IndexOfFiles[j].first))
				{
					case -1:
					{
						if (IndexOfFiles[j].first.indexOf(st) == 0) //st = 'skin', IndexOfFiles[j].first = 'skinner'
							bFound = true;
						break;
					}
					case 0:
					{
						bFound = true;
						break;
					}
					case 1: //st = 'skinner', IndexOfFiles[j].first = 'skin'
					{
						switch (strcmp(st, IndexOfFiles[j].last))
						{
							case -1:
							case 0:
							{
								bFound = true;
								break;
							}
							case 1:
							{
								if (st.indexOf(IndexOfFiles[j].last) == 0) //st = 'skinner', IndexOfFiles[j].last = 'skin'
									bFound = true;
								break;
							}
						}
					}
				}
				if (!bFound)
				{
					getSearchResultOutput();
					return;
				}

				//Replace strings for search with pairs (string,index file)
				StringPairArray[i] = new Array();
				StringPairArray[i][0]=st;
				StringPairArray[i][1]=IndexOfFiles[j].fileName;
			}
			SearchResults=new Array();
			iStringToSearch=0;
			SearchForNextString();
		}

		//Downloads prefixes.txt
		//Fills IndexOfFiles array
		function GetIndex()
		{
			SearchResults=new Array();
			NextStringToSearch=0; //?
			var sURL = dirname() + "/de_search/prefixes.txt";
			request.open("GET", sURL);
			request.onreadystatechange = function() {
			  if (request.readyState == 4)
				 if(request.status == 200 || request.status == 0)
				 {
					var arPrefixes = (request.responseText).split(/\s*;\s*/);
					var j = 0;
					for (var i = 0; i + 2 < arPrefixes.length; i+=3)
					{
						IndexOfFiles[j] = new Object();
						IndexOfFiles[j].fileName = arPrefixes[i] + ".txt";
						IndexOfFiles[j].first = arPrefixes[i+1];
						IndexOfFiles[j].last = arPrefixes[i+2];
						++j;
					}
					AttachFilesToStrings();
				 }
			};
			request.send(null);
		}

		function dirname()
		{
			var retValue = window.location.href;
			retValue = retValue.substring(0, retValue.lastIndexOf('/'));
			return retValue;
		}

function reverse(str) {
if(!str) return str;
return str.charAt(str.length-1) + reverse(str.substring(0,str.length-1));
}

function trimLeft(str) {
   for (var i=0; str.charAt(i) == ' '; i++);
   return str.substring(i, str.length);
}

function trimRight(str) {
   return reverse(trimLeft(reverse(str)));
}

function trim(str) {
   return trimRight(trimLeft(str));
}

function isEmpty(sToCheck) {
   var sTest;
   sTest = trim(sToCheck)
   if (sTest == null || sTest == "") {
      return true;
   }
   return false;
}

		function  DoesNotOperaSupportLocalSearch()
		{
			var version = jQuery.browser.version || "0";
            var splitVersion = version.split('.');
			return $.browser.opera && ((parseInt(splitVersion[0]) > 12) || ((parseInt(splitVersion[0]) == 12) && (parseInt(splitVersion[1]) >= 2)))
		}

		function searchmain(str)
		{
			
			if (isLocalSearch() && ($.browser.chrome || DoesNotOperaSupportLocalSearch()  ) )
				throw Error("LocalSearchNotSupportedInCurrentBrowser");
			$( document ).trigger( "searchBegin" );
			SearchResults=new Array();
			iStringToSearch=0;

			//Split the string into words
			var strs = str.split(/\s/g);
			StringsForSearch = new Array();
			for (var i = 0; i < strs.length; ++i)
				if (!isEmpty(strs[i]))
					StringsForSearch.push(strs[i]);
			//Download index.txt asyncronously and fill array of indexes
			GetIndex();

			return 1;
		}

	var querySplitter = /\s+/;


	function max(a,b){
	return a>b?a:b;
}
function min(a,b){
	return a<b?a:b;
}



	var API = {
		trim: function( str ) {
			return trim( str );
		},

		doSearch: function( str ) {
			return searchmain( str );
		},

		querySplitter: function() {
			return querySplitter;
		},

		getSearchResults: function() {
			return SearchResults;
		}
	};

	return API;


})();
/*js/drexplain/drexplain.search-manager.js*/
DR_EXPLAIN.namespace( 'DR_EXPLAIN.searchManager' );
DR_EXPLAIN.searchManager = (function(){
	var _class = {
		elementsArr: null,
		isFirstSearch: true,
		dom: null,
		highlightManager: null,
		dataManager: null,
		urlEncoder: null,
		searchEngine: null,


		init: function() {
			this.doSetDom();
			this.doSetHighlightManager();
			this.doSetDataManager();
			this.doSetNavTreeSearch();
			this.doSetUrlEncoder();
			this.doSetSearchEngine();
			this.doSetSearchInProgressText();
		},

		doSetSearchInProgressText: function() {
			this.dom.$searchProgress.prop( "alt", this.dataManager.getSearchTextInProgress() );
		},

		doSetHighlightManager: function() {
			this.highlightManager = DR_EXPLAIN.highlightManager;
		},

		doSetDataManager: function() {
			this.dataManager = DR_EXPLAIN.dataManager;
		},

		doSearchIfQueryStringNotEmpty: function() {
			this.doSetQueryStringByUrlEncoder();
			if ( this.elementsArr[ 0 ].$input.prop( "value" ) !== '' ) {
				this.elementsArr[ 0 ].$submit.trigger( "click" );
			}
		},

		runCustomButtons: function() {
			var searchInWorkZone = new CustomButton( this.dom.$workZoneSearchSubmit );
			searchInWorkZone.run();

			var searchInTab = new CustomButton( this.dom.$tabSearchSubmit );
			searchInTab.run();
		},

		runInputPlaceholders: function() {
			var searchInTabPlaceholder = new InputPlaceholder( this.dom.$tabSearchInput, this.dom.$tabSearchInputLabel, true );
			searchInTabPlaceholder.run();

			var searchInWorkZonePlaceholder = new InputPlaceholder( this.dom.$workZoneSearchInput, this.dom.$workZoneSearchInputLabel, true );
			searchInWorkZonePlaceholder.run();
		},

		runInputSync: function() {
			var that = this;

			var $inputArr = this.dom.$tabSearchInput.add( this.dom.$workZoneSearchInput );
			var inputSync = new InputSync( $inputArr );
			inputSync.run();

			this.dom.$workZoneSearchSubmit.on( "click",function() {
				that.dom.$tabSelectorSearch.click();
			});
		},

		doSetNavTreeSearch: function() {
			this.navTreeSearch = DR_EXPLAIN.navTree_Search;
		},

		doSetDom: function() {
			this.dom = DR_EXPLAIN.dom;
			this.elementsArr = [
			         			{ $input: this.dom.$tabSearchInput, $submit: this.dom.$tabSearchSubmit },
			         			{ $input: this.dom.$workZoneSearchInput, $submit: this.dom.$workZoneSearchSubmit }
			         		];
		},

		doSetUrlEncoder: function() {
			var that = this;

			this.urlEncoder = DR_EXPLAIN.urlEncoder;
			this.urlEncoder.addSaveFunc(
					function() {
						if ( that.isSearchTabSelected() ) {
							return that.getSearchQuery();
						}
						else {
							return "";
						}

					},
					this.urlEncoder.KEY_NAME__SEARCH_QUERY
				);
		},

		doSetSearchEngine: function() {
			this.searchEngine = DR_EXPLAIN.searchEngine;
		},

		setUrlEncoder: function( urlEncoder, urlEncoderKeyName ) {
			this.urlEncoder = urlEncoder;
			this.urlEncoderKey = urlEncoderKeyName;
		},

		doSetQueryStringByUrlEncoder: function() {
			if( this.urlEncoder !== null ) {
				var queryString = this.urlEncoder.getValueByKey( this.urlEncoder.KEY_NAME__SEARCH_QUERY );
				if ( queryString !== null ) {
					for ( var index = 0; index < this.elementsArr.length; index += 1 ) {
						this.elementsArr[ index ].$input.prop( "value", queryString );
						this.elementsArr[ index ].$input.trigger( "keypressSync" );
					}
				}
			}
		},

		doBindEvents: function() {
			for ( var index = 0; index < this.elementsArr.length; index += 1 ) {
				this.bindEventToElement( this.elementsArr[ index ] );
			}

			var that = this;

			$( document ).on( "searchComplete.search", function() {
				that.searchComplete();
				that.hideSearchProgress();
			});

			$( document ).on( "searchBegin.search", function() {
				that.showSearchProgress();
			});

			$( document ).on( "searchError.search", function() {
				that.hideSearchProgress();
			});
		},

		hideSearchProgress: function() {
			if ( this.dom.$searchProgress.is( ":visible" ) ) {
				this.dom.$searchProgress.fadeOut( 333 );
			}
			else {
				this.dom.$searchProgress.css( "display", "none" );
			}

		},

		showSearchProgress: function() {
			$( "#searchProgress" ).fadeIn( 333 );
		},

		bindEventToElement: function( elem ) {
			var that = this;
			elem.$submit.closest( "form" ).on( "submit", function( e ){
				elem.$input.blur();
				that.dom.$tabSelectorSearch.click();
				that.onClick( elem );
				return false;
			});
		},

		onClick: function( elem ) {
			var s = this.searchEngine.trim(document.getElementById( elem.$input.prop( "id" ) ).value).replace(/\r/g,'').replace(/\n/g,'').replace(/\t/g,' ').replace(/\u00A0/g,'');
			s = s.replace(/[,;]/g, ' ');
			this.performSearch(s);
		},

		performSearch: function( s ) {
			var queryArray = this.searchEngine.trim(s).split(this.searchEngine.querySplitter());
			var output = '';
			if (queryArray.length == 0 || queryArray[0] == '')
				this.showSearchTextEmptyString();
			else
			{
				try	{
					this.searchEngine.doSearch(s);
				}
				catch(e){
					if (e.message == "LocalSearchNotSupportedInCurrentBrowser")
					{
						var $msg = $( '<div class="b-tree__searchResultText">' + this.dataManager.getErrorInLocalSearch() + '</div>' );
						this.dom.tabs.search.$tree.html( $msg );
						$( document ).trigger( "searchCompleteBuildTree" );
					}
				}
			}
		},

		searchComplete: function() {
			this.highlightManager.hide();
			var searchResultsArr = this.searchEngine.getSearchResults();
			if ( searchResultsArr.length > 0 ) {
				this.navTreeSearch.setNewContentBySearchResults( searchResultsArr );
				this.navTreeSearch.show();
				$( document ).trigger( "searchCompleteBuildTree" );


				var searchQuery = this.getSearchQuery();
				var searchQueryArr = this.searchEngine.trim( searchQuery ).split( this.searchEngine.querySplitter() );
				this.highlightManager.show( searchQueryArr );

				if ( this.isFirstSearch && this.isSearchTabSelectedOnStart() ) {
					if ( this.isFirstSearch ) {
						$( document ).trigger( "firstSearchCompleteWithSelectedSearchTab" );
						this.isFirstSearch = false;
					}

				}
			}
			else {
				var $msg = $( '<div class="b-tree__searchResultText">' + this.dataManager.getSearchTextNoResults() + '</div>' );
				this.dom.tabs.search.$tree.html( $msg );
				$( document ).trigger( "searchCompleteBuildTree" );
			}
		},

		showSearchTextEmptyString: function() {
			this.highlightManager.hide();
			var $msg = $( '<div class="b-tree__searchResultText">' + this.dataManager.getSearchTextEmptyString() + '</div>' );
			this.dom.tabs.search.$tree.html( $msg );
			$( document ).trigger( "searchCompleteBuildTree" );
		},

		isSearchTabSelected: function() {
			var $navTree_search = this.dom.tabs.search.$wrapperItem;
			if ( $navTree_search.hasClass( "m-tabs__wrapperItem__selected" ) ) {
				return true;
			}
			else {
				return false;
			}
		},

		isSearchTabSelectedOnStart: function() {
			var $navTree_search = this.dom.tabs.search.$wrapperItem;
			if ( $navTree_search.index() === parseInt( this.urlEncoder.getValueByKey( this.urlEncoder.KEY_NAME__TAB_INDEX ) ) ) {
				return true;
			}
			else {
				return false;
			}
		},

		getSearchQuery: function() {
			if ( this.elementsArr[ 0 ] !== undefined ) {
				return this.elementsArr[ 0 ].$input.prop( "value" );
			}
			else {
				return null;
			}
		},

		isSearchQueryNotEmpty: function() {
			var query = this.getSearchQuery();
			if ( query !== null ) {
				if ( query !== '' ) {
					return true;
				}
			}
			return false;
		}
	};

	var API = {
		init: function() {
			_class.init();
		},

		runCustomButtons: function() {
			_class.runCustomButtons();
		},

		runInputSync: function() {
			_class.runInputSync();
		},

		runInputPlaceholders: function() {
			_class.runInputPlaceholders();
		},

		doBindEvents: function() {
			_class.doBindEvents();
		},

		doSearchIfQueryStringNotEmpty: function() {
			_class.doSearchIfQueryStringNotEmpty();
		}
	};

	return API;
})();
/*js/drexplain/drexplain.highlight-manager.js*/
DR_EXPLAIN.namespace( 'DR_EXPLAIN.highlightManager' );
DR_EXPLAIN.highlightManager = (function(){

	var _class = {
		dom: null,
		$content: null,

		init: function() {
			this.doSetDom();
		},

		doSetDom: function() {
			this.dom = DR_EXPLAIN.dom;
			this.$content = this.dom.$articleInnerWrapper;
		},

		show: function( wordsArr ) {
			this.hide();
			for ( var index = 0; index < wordsArr.length; index += 1 ) {
				this.$content.highlight( wordsArr[ index ] );
			}
			this.hideFromCopyright();
		},

		hide: function() {
			this.$content.removeHighlight();
		},

		hideFromCopyright: function() {
			this.dom.$articleGeneratorCopyright.removeHighlight();
		}
	};

	var API = {
		init: function() {
			_class.init();
		},
		show: function( wordsArr ) {
			_class.show( wordsArr );
		},
		hide: function() {
			_class.hide();
		}
	};

	return API;
})();
/*js/drexplain/drexplain.tab-controller.js*/
DR_EXPLAIN.namespace( 'DR_EXPLAIN.tabController' );

DR_EXPLAIN.tabController = (function(){
	var _class = {
		DREX_SHOW_MENU: 1,
		DREX_SHOW_SEARCH: 1,
		DREX_SHOW_INDEX: 1,
		
		tabArr: [],
		urlEncoder: null,
		dom: null,

		init: function() {

			this.doSetDom();
			this.doSetUrlEncoder();
			this.doAddTabs();
		},

		addTab: function( $tabSelector, $tabWrapper, $tabWrapperInner ) {
			this.tabArr.push({
				$selector: $tabSelector,
				$wrapper: $tabWrapper,
				$wrapperInner: $tabWrapperInner,
				scrollTop: 0
			});
		},

		doSetDom: function() {
			this.dom = DR_EXPLAIN.dom;
		},

		doAddTabs: function() {
			if (this.DREX_SHOW_MENU)
				this.addTab( this.dom.tabs.menu.$selectorItem , this.dom.tabs.menu.$wrapperItem, this.dom.tabs.menu.$wrapperItemInner );
			if (this.DREX_SHOW_INDEX)
				this.addTab( this.dom.tabs.index.$selectorItem , this.dom.tabs.index.$wrapperItem, this.dom.tabs.index.$wrapperItemInner );
			if (this.DREX_SHOW_SEARCH)
				this.addTab( this.dom.tabs.search.$selectorItem , this.dom.tabs.search.$wrapperItem, this.dom.tabs.search.$wrapperItemInner );
		},

		doSetUrlEncoder: function() {
			var that  = this;
			this.urlEncoder = DR_EXPLAIN.urlEncoder;


			this.urlEncoder.addSaveFunc(
					function() {
						return that.getSelectedTabIndex();
					},
					that.urlEncoder.KEY_NAME__TAB_INDEX
				);

			this.urlEncoder.addSaveFunc(
					function() {
						return that.getVisibleTabScrollTop();
					},
					that.urlEncoder.KEY_NAME__MENU_SCROLL_TOP
			);
		},

		getVisibleTabScrollTop: function() {
			var selectedTab = this.getSelectedTab();
			if (!selectedTab)
				return 0;
			var $wrapperItemInner = selectedTab.$wrapperInner;
			var scrollTop = 0;
			if ( $wrapperItemInner !== null ) {
				scrollTop = $wrapperItemInner.scrollTop();
			}

			return scrollTop;
		},

		doSetScrollTopByUrlEncoder: function() {
			if( this.urlEncoder !== null ) {
				var scrollTop = this.urlEncoder.getValueByKey( this.urlEncoder.KEY_NAME__MENU_SCROLL_TOP );
				if ( scrollTop !== null ) {
					var selectedTab = this.getSelectedTab();
					if (!selectedTab)
						return;
					var $visibleWrapperItemInner = selectedTab.$wrapperInner;
					$visibleWrapperItemInner.scrollTop( scrollTop ).trigger( "scroll" );
				}
				else {
					var $selectedTreeElem = this.dom.tabs.menu.$tree.find( ".m-tree__itemContent__selected" );
					if ( $selectedTreeElem.length > 0 ) {
						var selectedTreeElemTop = ( $selectedTreeElem.offset() ).top - ( this.dom.$tabWrapperItems.offset() ).top - $selectedTreeElem.height() / 2;
						this.dom.tabs.menu.$wrapperItemInner.scrollTop( selectedTreeElemTop  ).trigger( "scroll" );
					}
				}
			}
		},

		doSetTabIndexByUrlEncoder: function() {
			if( this.urlEncoder !== null ) {
				var tabIndex = this.urlEncoder.getValueByKey( this.urlEncoder.KEY_NAME__TAB_INDEX );
				if ( tabIndex !== null ) {
					if ( this.tabArr[ tabIndex ] !== undefined ) {
						this.toggleTabState( this.tabArr[ tabIndex ].$selector );
					}
				}
			}
		},

		doBindEvents: function() {
			for ( var index = 0; index < this.tabArr.length; index += 1 ) {
				this.doBindEventToggleState( index );
				this.doBindScrollTopSaver( index );
			}

			this.doBindEventHover();
			var that = this;
			$( document ).on( "navResize",function() {
				that.doSetScrollPositionToSelectedTab();
			});
			$( document ).on( "firstSearchCompleteWithSelectedSearchTab",function() {
				that.doSetScrollTopByUrlEncoder();
			});
		},

		doBindEventToggleState: function( index ) {
			var that = this;

			this.tabArr[ index ].$selector.click(function(){
				if ( !$( this ).hasClass( "m-tabs__selectorItem__selected" ) ) {
					that.toggleTabState( $( this ) );
					$( window ).resize();
				}
			});
		},

		doBindScrollTopSaver: function( index ) {
			var that = this;

			this.tabArr[ index ].$wrapperInner.on( "scroll", function() {
				that.tabArr[ index ].scrollTop = $( this ).scrollTop();
			});
		},

		doBindEventHover: function() {
			if (this.tabArr.length == 0)
				return;
				
			var $selectorItems = this.tabArr[ 0 ].$selector.parent();

			$selectorItems.on( "mouseenter", ".b-tabs__selectorItem", function(){
				if ( $( this ).hasClass( "m-tabs__selectorItem__selected") )
					return false;
				$( this ).addClass( "m-tabs__selectorItem__hovered" );
			});

			$selectorItems.on( "mouseleave", ".b-tabs__selectorItem", function(){
				if ( $( this ).hasClass( "m-tabs__selectorItem__selected") )
					return false;
				$( this ).removeClass( "m-tabs__selectorItem__hovered" );
			});
		},

		toggleTabState: function( $selectedTabSelector ) {
			for ( var index = 0; index < this.tabArr.length; index += 1 ) {
				var $currTabSelector = this.tabArr[ index ].$selector;
				if ( $currTabSelector.prop( "id" ) === $selectedTabSelector.prop( "id" ) ) {
					this.showTabByIndex( index );
				}
				else {
					this.hideTabByIndex( index );
				}
			}

			$( document ).trigger( "newTabSelected.tabs" );
		},

		showTabByIndex: function( index ) {
			if (index == -1)
				return;
			var $tabSelector = this.tabArr[ index ].$selector;
			var $tabWrapper = this.tabArr[ index ].$wrapper;
			var $tabWrapperInner = this.tabArr[ index ].$wrapperInner;
			var scrollTop = this.tabArr[ index ].scrollTop;

			//console.log( 'show tab: %s %s', $tabSelector.prop( "id" ), $tabWrapper.prop( "id" ) );

			$tabSelector.
				addClass( "m-tabs__selectorItem__selected" ).
				removeClass( "m-tabs__selectorItem__hovered" ).
				removeClass( "m-tabs__selectorItem__unselected" );

			$tabWrapper.addClass( "m-tabs__wrapperItem__selected" );
			$tabWrapperInner.scrollTop( scrollTop );
		},

		hideTabByIndex: function( index ) {
			var $tabSelector = this.tabArr[ index ].$selector;
			var $tabWrapper = this.tabArr[ index ].$wrapper;

			//console.log( 'hide tab: %s %s', $tabSelector.prop( "id" ), $tabWrapper.prop( "id" ) );


			$tabSelector.
				removeClass( "m-tabs__selectorItem__selected" ).
				addClass( "m-tabs__selectorItem__unselected" );

			$tabWrapper.removeClass( "m-tabs__wrapperItem__selected" );

		},

		getSelectedTabIndex: function() {
			for ( var index = 0; index < this.tabArr.length; index += 1 ) {
				var $currTabSelector = this.tabArr[ index ].$selector;
				if ( $currTabSelector.hasClass( "m-tabs__selectorItem__selected" ) ) {
					return index;
				}
			}
			if (this.tabArr.length == 0)
				return -1;
			return 0;
		},

		getSelectedTab: function() {
			var selectedTabIndex = this.getSelectedTabIndex();
			if (selectedTabIndex == -1)
				return null;
			return this.tabArr[ selectedTabIndex ];
		},

		doSetScrollPositionToSelectedTab: function() {
			var selectedTabIndex = this.getSelectedTabIndex();
			this.showTabByIndex( selectedTabIndex );
		}
	};

	var API = {
		init: function() {
			_class.init();
		},

		doBindEvents: function() {
			_class.doBindEvents();
		},

		doSetTabIndexByUrlEncoder: function() {
			_class.doSetTabIndexByUrlEncoder();
		},

		doSetScrollTopByUrlEncoder: function() {
			_class.doSetScrollTopByUrlEncoder();
		},
		
		
		isMenuTabShown: function() {
			return _class.DREX_SHOW_MENU != 0;
		},

		isSearchTabShown: function() {
			return _class.DREX_SHOW_SEARCH != 0;
		},

		isIndexTabShown: function() {
			return _class.DREX_SHOW_INDEX != 0;
		}
	};

	return API;

})();
/*js/drexplain/drexplain.url-encoder.js*/
DR_EXPLAIN.namespace( 'DR_EXPLAIN.urlEncoder' );
DR_EXPLAIN.urlEncoder = (function(){
	var _class = {

		urlDecoded: null,
		funcArr: [],
		utils: null,
		dom: null,

		init: function() {
			this.utils = DR_EXPLAIN.utils;
			this.dom = DR_EXPLAIN.dom;
		},

		doBindOpenNextPageWithEncodedStringToLinks: function() {
			var that = this;
			this.dom.$internal_wrapper.on( "click", "a.local_link, a.b-breadCrumbs__link, a.b-controlButtons__link, a.b-tree__itemLink", function(e) {
				that.openNextPage( $( this ).prop( "href" ), $( this ).attr("href"), $( this ).prop( "target" ) == "_new" );
				e.preventDefault();
				return false;
			});
		},
		doBindOpenNextPageWithEncodedStringToLinksInClonedNode: function() {
			var that = this;
			$(".cloned_node").on("click", "a", function(e) {
				that.openNextPage( $( this ).prop( "href" ), $( this ).attr("href"), $( this ).prop( "target" ) == "_new" );
				e.preventDefault();
				return false;
			});
		},
		
		doBindOpenNextPageWithEncodedStringToLinksInKeywordContextMenu: function() {
			var that = this;
			this.dom.$keywordContextMenu.on( "click", "a.b-tree__itemLink", function(e) {
				that.openNextPage( $( this ).prop( "href" ), $( this ).attr("href"), $( this ).prop( "target" ) == "_new" );
				e.preventDefault();
				return false;
			});
		},
		

		openNextPage: function( nextpage, nextPageHref, atNewPage ) {

			atNewPage = typeof(atNewPage) != 'undefined' ? atNewPage : false;

			var stateParams = this.getEncodedUrl();

			var targetUrl = nextpage;
			if (nextPageHref && nextPageHref.length > 0 && nextPageHref.charAt(0) == '#')
			{
				targetUrl = nextPageHref;
				nextpage = nextPageHref;
			}
			if ( $( "html" ).hasClass( "ie" ) )
				targetUrl = this.utils.escapeXml(nextpage);
			var anch = "";
			var anchPos = -1;
			if ((anchPos = nextpage.indexOf("#")) != -1) {
				targetUrl = nextpage.substr(0, anchPos);
				anch = nextpage.substr(anchPos);
			}
			targetUrl += "?" + stateParams + anch;
			window.open(targetUrl, atNewPage ? "_new" : "_self");
		},


		doDecodeUrl: function() {
			var map = [];
			var urlParams = {};
			    var e,
			        a = /\+/g,  // Regex for replacing addition symbol with a space
			        r = /([^&=]+)=?([^&]*)/g,
			        d = function (s) { return decodeURIComponent(s.replace(a, " ")); },
			        q = window.location.search.substring(1);

			    while (e = r.exec(q))
			       urlParams[d(e[1])] = d(e[2]);

			for ( var key in urlParams ) {
				var value = this.decodeString( urlParams[ key ] );
				map[key] = value;
			}

			this.urlDecoded = map;
		},


		getValueByKey: function( key ) {
			if( this.urlDecoded[ key ] !== undefined ) {
				return this.urlDecoded[ key ];
			}
			else {
				return null;
			}
		},


		addSaveFunc: function( func, key ) {
			this.funcArr[ key ] = func;
		},

		getEncodedUrl: function() {
			var result = "";
			for ( var key in this.funcArr ) {
				var currFunc = this.funcArr[ key ];
				var value = currFunc();

				value = this.encodeString( value.toString() );

				if ( value.toString().length > 0 ) {
					if (result != "") {
						result += "&";
					}
					result += key + "=" + value;
				}
			}
			return result;
		},


		decodeString: function( str ) {
			var bits = this.utils.base64.decode(str, 0, true, true);
			if ( bits === null ) {
				return null;
			}
			var result = this.utils.ar2str(bits);
			result = this.utils.utf8.decode(result);
			return result;
		},

		encodeString: function( str )
		{
			var compressedStr = str;
			var utfEncodedStr = this.utils.utf8.encode(compressedStr);
			var bitArray = this.utils.str2ar(utfEncodedStr);
			return this.utils.base64.encode(bitArray, false, true);
		}
	};

	var API = {
		KEY_NAME__MENU_WIDTH: "mw",
		KEY_NAME__MENU_STATE: "ms",
		KEY_NAME__TAB_INDEX: "st",
		KEY_NAME__MENU_SCROLL_TOP: "sct",
		KEY_NAME__SEARCH_QUERY: "q",

		init: function() {
			_class.init();
		},

		doBindOpenNextPageWithEncodedStringToLinks: function() {
			_class.doBindOpenNextPageWithEncodedStringToLinks();
		},

		doBindOpenNextPageWithEncodedStringToLinksInClonedNode: function() {
			_class.doBindOpenNextPageWithEncodedStringToLinksInClonedNode();
		},
		
		doBindOpenNextPageWithEncodedStringToLinksInKeywordContextMenu: function() {
			_class.doBindOpenNextPageWithEncodedStringToLinksInKeywordContextMenu();
		},
		
		doDecodeUrl: function() {
			_class.doDecodeUrl();
		},

		getValueByKey: function( key ) {
			return _class.getValueByKey( key );
		},

		addSaveFunc: function( func, key ) {
			_class.addSaveFunc(func, key);
		}
	};

	return API;
})();
/*js/drexplain/drexplain.work-zone-sizer.js*/
DR_EXPLAIN.namespace( 'DR_EXPLAIN.workZoneSizer' );

DR_EXPLAIN.workZoneSizer = (function(){
	var _class = {
		NAV_MIN_WIDTH_DEFAULT: 250,
		ARTICLE_MIN_WIDTH_DEFAULT: 400,
		//IE_SCROLLBAR_SIZE: 24,

		dom: null,
		dataManager: null,

		currNavWidth: 0,
		minNavWidth: null,
		maxNavWidth: null,

		minArticleWidth: null,

		navArticlePaddingSize: 0,

		visibleTabLongestTreeItemWidth: null,

		timeoutId: null,
		WINDOW_RECALCULATING_DELAY: 100,

		init: function() {
			this.doSetCachedDom();
			this.doSetUrlEncoder();
			this.doSetDataManager();
			this.doSetNavArticlePaddingSize();
			this.doSetNavMinWidth();
			this.doSetArticleMinWidth();
			this.doSetSplitter();
			this.doSetFrameModeSizer();
			this.doSetCurrNavWidth();
		},


		doSetCachedDom: function() {
			this.dom = DR_EXPLAIN.dom;
		},

		doSetDataManager: function() {
			this.dataManager = DR_EXPLAIN.dataManager;
		},

		doSetUrlEncoder: function() {
			var that = this;

			this._urlEncoder = DR_EXPLAIN.urlEncoder;
			this._urlEncoder.addSaveFunc(function(){
				return that.currNavWidth;
			}, that._urlEncoder.KEY_NAME__MENU_WIDTH );
		},

		doSetCurrNavWidth: function() {
			var currNavWidth = null;
			if( this._urlEncoder !== null ) {
				currNavWidth = this._urlEncoder.getValueByKey( this._urlEncoder.KEY_NAME__MENU_WIDTH );
				if ( currNavWidth !== null ) {
					this.currNavWidth = currNavWidth;
				}
			}

			if ( currNavWidth === null ) {
				var startingMenuWidth = this.dataManager.getStartingMenuWidth();
				if ( $.isNumeric( startingMenuWidth ) ) {
					this.currNavWidth = startingMenuWidth;
				}
				else {
					this.currNavWidth = this.dom.$workZoneSideNav.width();
				}
			}
		},


		doSetNavArticlePaddingSize: function() {
			var navRightPadding = this.dom.$workZoneSideNav.css( "paddingRight" );
			var articleLeftPadding = this.dom.$workZoneSideArticle.css( "paddingLeft" );

			this.navArticlePaddingSize = navRightPadding + articleLeftPadding;
		},

		getTabWidth: function(tabId)
		{
			var curTab = document.getElementById(tabId);
			if (curTab)
				return $('#' + tabId).outerWidth(true);
			return 0;
		},
		
		doSetNavMinWidth: function() {
			var minWidth = parseInt( this.dom.$workZoneSideNav.css( "minWidth" ), 10 );
			if ( $.isNumeric( minWidth ) && ( minWidth !== 0 ) ) {
				this.minNavWidth = minWidth;
			}
			else {
				this.minNavWidth = this.NAV_MIN_WIDTH_DEFAULT;
			}
			var summaryTabWidth = 0;
			summaryTabWidth += this.getTabWidth("tabSelector_menu");
			summaryTabWidth += this.getTabWidth("tabSelector_index");
			summaryTabWidth += this.getTabWidth("tabSelector_search");
			this.minNavWidth = Math.max(this.minNavWidth, summaryTabWidth + 20);
		},
		
		doSetArticleMinWidth: function() {
			var minWidth = parseInt( this.dom.$workZoneSideArticle.css( "minWidth" ), 10 );
			if ( $.isNumeric( minWidth ) && ( minWidth !== 0 ) ) {
				this.minArticleWidth = minWidth;
			}
			else {
				this.minArticleWidth = this.ARTICLE_MIN_WIDTH_DEFAULT;
			}
		},

		recalculateNavMaxWidth: function() {
			var userLeftSideWidth = 0;
			var userRightSideWidth = 0;
			var isUserLeftSideExistsAndVisible = ( ( this.dom.$pageContentLeft.length ) > 0 && this.dom.isPageLeftVisible() );
			var isUserRightSideExistsAndVisible = ( ( this.dom.$pageContentRight.length ) > 0 && this.dom.isPageRightVisible() );

			if ( isUserLeftSideExistsAndVisible ) {
				userLeftSideWidth = this.dom.$pageContentLeft.outerWidth( true );
			}

			if ( isUserRightSideExistsAndVisible ) {
				userRightSideWidth = this.dom.$pageContentRight.outerWidth( true );
			}

			var windowWidth = $( window ).width();
			var minArticleWidth = this.minArticleWidth;
			if (this.dom.$headerSide__nav.length)
			{
				var headerSide__buttons_width = this.dom.$headerSide__buttons.outerWidth();
				
				var headerSide__nav_paddings = this.dom.$headerSide__nav.outerWidth() - this.dom.$headerSide__nav.width();
				var maxBreadCrumbWidth = 0;
				this.dom.$headerSide__nav__breadCrumbs.find("li").each(function(index) { maxBreadCrumbWidth = Math.max(maxBreadCrumbWidth, $(this).outerWidth()) });
				var headerSide__nav__breadCrumbs_width = maxBreadCrumbWidth;
				var articlePaddings = this.dom.$article.outerWidth() - this.dom.$article.width();
				var workZoneSideArticlePaddings = this.dom.$workZoneSideArticle.outerWidth() - this.dom.$workZoneSideArticle.width();
				var workZoneSideArticleContentPaddings = this.dom.$workZoneSideArticleContent.outerWidth() - this.dom.$workZoneSideArticleContent.width();
				var workZoneNavPaddings = this.dom.$workZoneSideNav.outerWidth() - this.dom.$workZoneSideNav.width();
				
				minArticleWidth = Math.max(minArticleWidth, headerSide__buttons_width + headerSide__nav__breadCrumbs_width +headerSide__nav_paddings + articlePaddings + workZoneSideArticlePaddings + workZoneSideArticleContentPaddings + workZoneNavPaddings);
			}

			var maxContentAreaWidth = this.dom.$internal_wrapper.css("width");
			if (!maxContentAreaWidth || maxContentAreaWidth.length < 2)
			{
				maxContentAreaWidth = windowWidth;
			}
			else
			{
				if (maxContentAreaWidth.charAt(maxContentAreaWidth.length - 1) == '%')
				{
					maxContentAreaWidth = parseInt(maxContentAreaWidth, 10) * windowWidth / 100;
				}
				else
				{
					maxContentAreaWidth = parseInt(maxContentAreaWidth, 10);
				}
			}
			var maxWidth = maxContentAreaWidth - minArticleWidth - userLeftSideWidth - userRightSideWidth;

			if ( maxWidth < this.NAV_MIN_WIDTH_DEFAULT ) {
				maxWidth = this.NAV_MIN_WIDTH_DEFAULT;
			}

			this.maxNavWidth = maxWidth;
			//console.log( [windowWidth,minArticleWidth,userLeftSideWidth,userRightSideWidth, maxWidth, this.minArticleWidth ] );
		},

		recalculateAll: function() {
			this.recalculateNavMaxWidth();
			this.recalculateVisibleTabLongestTreeItemWidth();
			this.dom.$hasHorizontalScrollbar = {};
			this.dom.$hasVerticalScrollbar = {};
			this.setNewNavWidth( this.currNavWidth );
			this.frameModeSizer.recalculateIfEnabled();
			this.setNewNavWidth( this.currNavWidth );			
			this.frameModeSizer.recalculateIfEnabled();
		},

		doBindEvents: function() {
			this.doBindEventsSizer();
			this.splitter.doBindEvents();
			if ( this.dataManager.isFrameModeEnabled() ) {
				this.frameModeSizer.enableRecalculatingEvents();
			}

		},

		doBindEventsSizer: function() {
			var that = this;

			$( document ).on( "newTabSelected.tabs", function(){
				that.recalculateAll();
			});

			$( document ).on( "nodeVisibleChanged", function(){
				that.recalculateAll();
			});

			$( document ).on( "searchCompleteBuildTree", function(){
				that.recalculateAll();
			});

			$( window ).resize(function() {
				clearTimeout( that.timeoutId );
				that.timeoutId = setTimeout( function() {
					that.recalculateAll();
				}, that.WINDOW_RECALCULATING_DELAY );
			});

		},

		doSetSplitter: function() {
			var that = this;

			this.splitter = DR_EXPLAIN.workZoneSizer_Splitter;
			this.splitter.setOnMouseMoveCallback(function( newWidth ) {
				that.changeWorkZoneSizeOnMouseMove( newWidth );
				that.recalculateAll();
			});
			this.splitter.setOnMouseUpCallback(function() {
				that.recalculateAll();
			});
			this.splitter.init();
		},

		doSetFrameModeSizer: function() {
			this.frameModeSizer = DR_EXPLAIN.workZoneSizer_FrameMode;
			this.frameModeSizer.init();
		},

		changeWorkZoneSizeOnMouseMove: function( newWidth ) {
			this.setNewNavWidth( newWidth );
		},

		setNewNavWidth: function( newNavWidth ) {
			var finalNewNavWidth = this.getFinalNewNavWidth( newNavWidth );

			var longestTreeItemWidthWithPaddings = this.visibleTabLongestTreeItemWidth;
			var selectedTab = this.dom.getVisibleTab();
			var tabId = null;
			if (selectedTab)
				tabId = selectedTab.$wrapperItem.prop("id");
			//console.log("setNewNavWidth: this.dom.$hasVerticalScrollbar[" + tabId + "] = " + this.dom.$hasVerticalScrollbar[tabId]);
			
			if ( this.dom.$hasVerticalScrollbar[tabId] )
				longestTreeItemWidthWithPaddings += $.getScrollbarWidth() * 1;
			var dx = finalNewNavWidth - this.dom.$workZoneSideNav.width();
			
			var finalTreeWidth = 0;
			if (selectedTab)
			{
				//console.log("setNewNavWidth: " + tabId);
				var $wrapperItem = selectedTab.$wrapperItem;
			
				finalTreeWidth += $wrapperItem.width();
			}
			finalTreeWidth += dx;
			this.dom.$hasHorizontalScrollbar[tabId] = finalTreeWidth < longestTreeItemWidthWithPaddings;
			//console.log("setNewNavWidth: this.dom.$hasHorizontalScrollbar[" + tabId + "] = " + this.dom.$hasHorizontalScrollbar[tabId]);
			if ( !this.dom.$hasHorizontalScrollbar[tabId] ) {
				this.dom.$treeArr.css( "width", "auto" );
			}
			else {
				this.dom.$treeArr.css({
					width: this.visibleTabLongestTreeItemWidth + "px"
				});
			}

			this.dom.$workZoneSideNav.css( "width", finalNewNavWidth + "px" );
			this.currNavWidth = finalNewNavWidth;
		},

		getFinalNewNavWidth: function( newNavWidth ) {
			var finalNewNavWidth = newNavWidth;

			if ( newNavWidth > this.maxNavWidth ) {
				finalNewNavWidth = this.maxNavWidth;
			}
			else if (newNavWidth < this.minNavWidth ) {
				finalNewNavWidth = this.minNavWidth;
			}


			return finalNewNavWidth;
		},

		recalculateVisibleTabLongestTreeItemWidth: function() {
			var $visibleTab = this.dom.getVisibleItemWrapperInner();
			if ($visibleTab)
			{
				var $tree = $visibleTab.children( "div" );
				var $treeTable = $tree.children( "table" );

				$treeTable.css( "width", "auto" );
				var treeTableWidth = $treeTable.width();
				$treeTable.css( "width", "100%" );
				this.visibleTabLongestTreeItemWidth = treeTableWidth;
			}
			else
			{
				this.visibleTabLongestTreeItemWidth = 0;
			}
		}
	};

	var API = {
		init: function() {
			_class.init();
		},

		recalculateAll: function() {
			_class.recalculateAll();
		},

		doBindEvents: function() {
			_class.doBindEvents();
		}
	};

	return API;
})();
/*js/drexplain/drexplain.work-zone-sizer.splitter.js*/
DR_EXPLAIN.namespace( 'DR_EXPLAIN.workZoneSizer_Splitter' );

DR_EXPLAIN.workZoneSizer_Splitter = (function(){
	var _class = {
		$splitter: null,
		$resizable: null,
		$body: null,
		splitterOldX: 0,
		timeoutId: null,
		dom: null,
		DELAY: 1,

		onMouseDownCallback: null,
		onMouseMoveCallback: null,
		onMouseUpCallback: null,

		init: function() {
			this.doSetDom();
		},

		doSetDom: function() {
			this.dom = DR_EXPLAIN.dom;
			this.$splitter = this.dom.$splitter;
			this.$resizable = this.dom.$workZoneSideNav;
			this.$body = this.dom.$body;
		},


		doBindEvents: function() {
			var that = this;

			$( this.$splitter ).on( "mousedown.splitter", function( e ){
				that.onMouseDown( e );
			});
		},

		bindMouseEvents: function() {
			var that = this;

			$( document ).on( "mouseup.splitter", function(){
				that.onMouseUp();
			});

			$( document ).on( "mousemove.splitter", function( e ){
				clearTimeout( that.timeoutId );
				that.timeoutId = setTimeout(function(){
					that.onMouseMove( e );
				}, that.DELAY );
			});
		},

		unbindMouseEvents: function() {
			$( document ).off( ".splitter" );
		},


		onMouseDown: function( e ) {
			this.splitterOldX = e.pageX;
			this._resizableOldWidth = this.$resizable.width();
			this.addCursorIconToBody();
			this.disableSelection();
			this.bindMouseEvents();
		},


		onMouseUp: function() {
			this.removeCursorIconFromBody();
			this.enableSelection();
			this.unbindMouseEvents();
			return this.onMouseUpCallback();
		},

		onMouseMove: function( e ) {
			// IE mouseup check - mouseup happened when mouse was out of window
			if ($.browser.msie && !(document.documentMode >= 9) && !e.button) {
				return this.onMouseUp();
			}

			var delta = e.pageX - this.splitterOldX;
			var newWidth = this._resizableOldWidth + delta;

			return this.onMouseMoveCallback( newWidth );
		},

		setOnMouseMoveCallback: function( callback ) {
			this.onMouseMoveCallback = callback;
		},

		setOnMouseUpCallback: function( callback ) {
			this.onMouseUpCallback = callback;
		},

		getResizableWidth: function() {
			return parseInt( this.$resizable.css( "width" ), 10 );
		},

		addCursorIconToBody: function() {
			this.$body.css( "cursor", "e-resize" );
		},

		removeCursorIconFromBody: function() {
			this.$body.css( "cursor", "auto" );
		},

		disableSelection: function() {
			this.$body.disableTextSelect();
		},

		enableSelection: function() {
			this.$body.enableTextSelect();
		}
	};

	var API = {
		init: function() {
			_class.init();
		},

		doBindEvents: function() {
			_class.doBindEvents();
		},

		setOnMouseMoveCallback: function( callback ) {
			_class.setOnMouseMoveCallback( callback );
		},

		setOnMouseUpCallback: function( callback ) {
			_class.setOnMouseUpCallback( callback );
		}
	};

	return API;
})();
/*js/drexplain/drexplain.work-zone-sizer.frame-mode.js*/
DR_EXPLAIN.namespace( 'DR_EXPLAIN.workZoneSizer_FrameMode' );
DR_EXPLAIN.workZoneSizer_FrameMode = (function(){
	var _class = {
		dom: null,
		timeoutId: null,
		isRecalculatingEventsEnabled: false,
		recalculateDomNodes: [],
		RESIZE_TIME_INTERVAL: 50,

		init: function() {
			this.doSetDom();
		},

		doSetDom: function( ) {
			this.dom = DR_EXPLAIN.dom;
		},

		doBindEvents: function() {
			this.doBindKeywordContextEvents();
		},

		doBindKeywordContextEvents: function() {
			var that = this;
			this.dom.$keywordContextMenu.on( "show.contextMenu", function(e, $elemInTree ) {
				that.dom.$keywordContextMenu_elemInTree = $elemInTree;
				that.recalculate();
			});

			this.dom.tabs.index.$wrapperItemInner.on( "scroll.contextMenu", function() {
				that.recalculateIndexContextMenu();
			});
		},

		addDomNodeToRecalculate: function( $dom, newValue ) {
			this.recalculateDomNodes.push({
				$dom: $dom,
				value: newValue
			});
		},

		enableRecalculatingEvents: function() {
			this.isRecalculatingEventsEnabled = true;
			this.dom.$html.addClass( this.dom.FRAME_ENABLED_CLASS );
			this.doBindEvents();
		},

		disableRecalculatingEvents: function() {
			this.isRecalculatingEventsEnabled = false;
			$( window ).off( ".contextMenu" );

			this.dom.$html.removeClass( this.dom.FRAME_ENABLED_CLASS );

			this.dom.$workZone.css( "height", "auto" );
			this.dom.$article.css( "height", "auto" );
			this.dom.$articleWrapper.css( "height", "auto" );
			this.dom.$articlePreWrapper.css( "height", "auto" );
			this.dom.$articleInnerWrapper.css( "height", "auto" );
			this.dom.$tabWrapperItemInnerArr.css({ height: "auto", overflow: "auto" });
			this.dom.$treeArr.css( "height", "auto" );
			this.dom.$keywordContextMenu.css( "height", "auto" );
		},

		toggleRecalculatingEvents: function() {
			if ( this.isRecalculatingEventsEnabled ) {
				this.disableRecalculatingEvents();
			}
			else {
				this.enableRecalculatingEvents();
			}
		},

		recalculateIfEnabled: function() {
			if ( this.isRecalculatingEventsEnabled ) {
				this.recalculate();
			}
		},

		recalculate: function() {


			if ( !this.isRecalculatingEventsEnabled ) {
				return false;
			}

			this.recalculateWorkZone();
			this.recalculateArticle();
			this.recalculateNav();
			this.recalculateIndexContextMenu();

			for ( var index = 0; index < this.recalculateDomNodes.length; index += 1 ) {
				var node = this.recalculateDomNodes[ index ];
				if ( typeof( node.value ) === "object" ) {
					node.$dom.css( node.value );
				}
				else {
					node.$dom.css( "height", node.value + "px" );
				}
			}

			this.recalculateDomNodes = [];

			//console.log( this.getHeaderHeight() );
		},

		recalculateIndexContextMenu: function() {
			if ( this.dom.$keywordContextMenu_elemInTree === undefined ) {
				return false;
			}
			var $contextMenu = this.dom.$keywordContextMenu;
			if ( !this.dom.isKeywordContextMenuVisible() ) {
				return false;
			}

			$contextMenu.css({ height: "auto", overflow: "visible" });
			var $workZone = this.dom.getVisibleItemWrapperInner();

			var $elemInTree = this.dom.$keywordContextMenu_elemInTree;
			var elemInTreePos = $elemInTree.offset();
			var elemInTreeHeight = $elemInTree.height();
			var elemInTreePosTop = elemInTreePos.top;

			var workZonePos = $workZone.offset();
			var workZoneHeight = $workZone.height();

			var contextMenuHeight = $contextMenu.height();

			if ( ( elemInTreePosTop < workZonePos.top ) || ( elemInTreePosTop > workZonePos.top + workZoneHeight ) ) {
				 this.dom.$keywordContextMenu_elemInTree.trigger( "click" );
				 return false;
				//elemInTreePosTop = workZonePos.top;
	/*			elemInTreePosTop = workZonePos.top;
				elemInTreePosTop = workZonePos.top + workZoneHeight;*/
			}
			else {
				//$contextMenu.show();
			}

			var topSpaceHeight = elemInTreePosTop - workZonePos.top;
			var bottomSpaceHeight = workZoneHeight - topSpaceHeight - elemInTreeHeight;



			var contextMenuNewTop = 0;
			var contextMenuNewLeft = elemInTreePos.left;
			var contextMenuNewWidth = "auto";
			var contextMenuNewHeight = "auto";
			var contextMenuNewOverflow = "hidden";
			var contextMenuPadding = "0";

			var isContextMenuBiggerThenBottomSpace = ( contextMenuHeight > bottomSpaceHeight );
			var isContextMenuBiggerThenTopSpace = ( contextMenuHeight > topSpaceHeight );
			var isBottomSpaceBiggerThenTopSpace = ( bottomSpaceHeight > topSpaceHeight );
			var isContextMenuHeightInContainer = ( !isContextMenuBiggerThenBottomSpace && !isContextMenuBiggerThenTopSpace );



			if ( !isContextMenuBiggerThenBottomSpace ) {
				contextMenuNewTop = elemInTreePosTop + elemInTreeHeight;
			}
			else if ( !isContextMenuBiggerThenTopSpace )  {
				contextMenuNewTop = elemInTreePosTop - contextMenuHeight;
			}
			else {
				if ( isBottomSpaceBiggerThenTopSpace ) {
					contextMenuNewHeight = bottomSpaceHeight;
					contextMenuNewTop = elemInTreePosTop + elemInTreeHeight;
				}
				else {
					contextMenuNewHeight = topSpaceHeight;
					contextMenuNewTop = elemInTreePosTop - contextMenuNewHeight;
				}
				contextMenuNewOverflow = "scroll";
				contextMenuPadding = "0 20px 0px 0";
			}


			if ( contextMenuNewHeight !== "auto" ) {
				contextMenuNewHeight += "px";
			}



			$contextMenu.css({
				top: contextMenuNewTop  + "px",
				left: contextMenuNewLeft + "px",
				height: contextMenuNewHeight,
				overflow: contextMenuNewOverflow,
				padding: contextMenuPadding
			});
	/*
			this.addDomNodeToRecalculate( $contextMenu, {
				top: contextMenuNewTop  + "px",
				left: contextMenuNewLeft + "px",
				height: contextMenuNewHeight,
				overflow: contextMenuNewOverflow,
				padding: contextMenuPadding
			});*/

		},


		recalculateWorkZone: function() {
			var newWorkZoneHeight = $( window ).height() - this.getHeaderHeight() - this.getFooterHeight();
			//this.dom.$workZone.css( "height", newWorkZoneHeight + "px" );
			this.addDomNodeToRecalculate( this.dom.$workZone, newWorkZoneHeight );
		},

		recalculateArticle: function() {
			var newArticleHeight = $( window ).height()
				- ( this.dom.$article.offset() ).top
				- this.dom.getCssNumericValue( this.dom.$workZoneSideArticleContent, "paddingBottom" )
				- this.dom.getCssNumericValue( this.dom.$article, "paddingBottom" )
				- this.dom.getCssNumericValue( this.dom.$article, "paddingTop" )
				- this.getFooterHeight()
				;

			var newArticleInnerWrapperHeight = $( window ).height()
				- ( this.dom.$articleWrapper.offset() ).top
				- this.dom.getCssNumericValue( this.dom.$articleWrapper, "paddingTop" )
				//- this.dom.getCssNumericValue( this.dom.$articleWrapper, "marginTop" )
				- this.dom.getCssNumericValue( this.dom.$articleWrapper, "paddingBottom" )
				- this.dom.getCssNumericValue( this.dom.$article, "paddingBottom" )
				- this.dom.getCssNumericValue( this.dom.$workZoneSideArticleContent, "paddingBottom" )
				- this.getFooterHeight()
				- 0
				;

			var newArticleWrapperHeight = newArticleHeight
				- this.dom.$articleHeader.outerHeight( true )
				- this.dom.getCssNumericValue( this.dom.$articleWrapper, "borderTopWidth" )
				- this.dom.getCssNumericValue( this.dom.$articleWrapper, "borderBottomWidth" )
			;
			newArticleInnerWrapperHeight = newArticleHeight
				+ this.dom.getCssNumericValue( this.dom.$articleInnerWrapper, "paddingBottom" )
				;


	/*		this.dom.$article.css( "height", newArticleHeight + "px" );
			this.dom.$articlePreWrapper.css( "height", newArticleHeight + "px" );
			this.dom.$articleWrapper.css( "height", newArticleWrapperHeight + "px" );*/

			this.addDomNodeToRecalculate( this.dom.$article, newArticleHeight );
			this.addDomNodeToRecalculate( this.dom.$articlePreWrapper, newArticleHeight );
			this.addDomNodeToRecalculate( this.dom.$articleWrapper, newArticleWrapperHeight );

			//console.log( 'article newArticleHeight', newArticleHeight, 'newArticleInnerWrapperHeight', newArticleInnerWrapperHeight );
		},

		recalculateNav: function() {
			var that = this;

			this.recalculateMenuNav();
			this.recalculateIndexNav();
			this.recalculateSearchNav();

			$( document ).trigger( "navResize" );
		},

		recalculateMenuNav: function() {
			this.recalculateNavWithTreeOnly( this.dom.tabs.menu );
		},

		recalculateIndexNav: function() {
			this.recalculateNavWithTreeOnly( this.dom.tabs.index );
		},

		recalculateSearchNav: function() {
			this.recalculateNavWithTreeAndSearchInput( this.dom.tabs.search );
		},

		recalculateNavWithTreeOnly: function( tab ) {
			var workZoneHeight = this.dom.$workZone.height();

			var navOuterPaddingTop = ( this.dom.$tabWrapperItems.offset() ).top;
			var navOuterPaddingBottom = this.dom.getCssNumericValue( this.dom.$workZoneSideNavContent, "paddingBottom" ) + this.getFooterHeight();

			var navInnerPaddingTop = this.dom.getCssNumericValue( tab.$wrapperItem, "paddingTop" );
			var navInnerPaddingBottom = this.dom.getCssNumericValue( tab.$wrapperItem, "paddingBottom" );

			tab.$tree.css( "height", "auto" );
			var realTreeHeight = tab.$tree.height();

			var newTotalNavHeight = $( window ).height()
				- navInnerPaddingTop
				- navInnerPaddingBottom
				- navOuterPaddingBottom
				- navOuterPaddingTop;
				
			var newNavHeightWithoutHorizontalScrollbar = newTotalNavHeight;
			var tabId = tab.$wrapperItem.prop("id");
			if ( this.dom.$hasHorizontalScrollbar[tabId] )
				newNavHeightWithoutHorizontalScrollbar -= $.getScrollbarWidth() * 1;

			//console.log("recalculateNavWithTreeOnly(" + tabId + "): (realTreeHeight > newNavHeightWithoutHorizontalScrollbar) <=> " + realTreeHeight + " > " + newNavHeightWithoutHorizontalScrollbar);
			this.dom.$hasVerticalScrollbar[tabId] = realTreeHeight > newNavHeightWithoutHorizontalScrollbar;
			//console.log("recalculateNavWithTreeOnly: this.dom.$hasVerticalScrollbar[" + tabId + "] = " + this.dom.$hasVerticalScrollbar[tabId]);
			//console.log("recalculateNavWithTreeOnly: this.dom.$hasHorizontalScrollbar[" + tabId + "] = " + this.dom.$hasHorizontalScrollbar[tabId]);

			if ( !this.dom.$hasVerticalScrollbar[tabId] ) {
				var SCROLL_COMPENSATION = 0;
				if ( this.dom.isIeLessThan9() ) {
					SCROLL_COMPENSATION += 2;
				}
				//tab.$tree.css( "height", newTotalNavHeight - SCROLL_COMPENSATION + "px" );
				this.addDomNodeToRecalculate( tab.$tree, newNavHeightWithoutHorizontalScrollbar - SCROLL_COMPENSATION );
			}
			else {
				//tab.$wrapperItemInner.css( "overflow", "auto" );
				this.addDomNodeToRecalculate( tab.$wrapperItemInner, { overflow: "auto" });
			}

			//tab.$wrapperItemInner.css( "height", newTotalNavHeight + "px" );
			this.addDomNodeToRecalculate( tab.$wrapperItemInner, newTotalNavHeight );
		},

		recalculateNavWithTreeAndSearchInput: function( tab ) {
			if ( !this.dom.isTabVisible( tab ) ) {
				return false;
			}
			var workZoneHeight = this.dom.$workZone.height();

			var navOuterPaddingTop = ( this.dom.$tabWrapperItems.offset() ).top;
			var navOuterPaddingBottom = this.dom.getCssNumericValue( this.dom.$workZoneSideNavContent, "paddingBottom" ) + this.getFooterHeight();

			var navInnerPaddingTop = this.dom.getCssNumericValue( tab.$wrapperItem, "paddingTop" ) + this.dom.$tabSearchFormWrapper.height() + this.dom.getCssNumericValue( this.dom.$tabSearchFormWrapper, "marginBottom" );
			var navInnerPaddingBottom = this.dom.getCssNumericValue( tab.$wrapperItem, "paddingBottom" );

			tab.$tree.css( "height", "auto" );
			var realTreeHeight = tab.$tree.height();

			var newTotalNavHeight = $( window ).height() - navInnerPaddingTop - navInnerPaddingBottom - navOuterPaddingBottom - navOuterPaddingTop;

			var newNavHeightWithoutHorizontalScrollbar = newTotalNavHeight;
			var tabId = tab.$wrapperItem.prop("id");
			if ( this.dom.$hasHorizontalScrollbar[tabId] )
				newNavHeightWithoutHorizontalScrollbar -= $.getScrollbarWidth() * 1;
			this.dom.$hasVerticalScrollbar[tabId] = realTreeHeight > newNavHeightWithoutHorizontalScrollbar;

			if ( !this.dom.$hasVerticalScrollbar[tabId] ) {
				var SCROLL_COMPENSATION = 0;
				if ( this.dom.isIeLessThan9() ) {
					SCROLL_COMPENSATION += 2;
				}
				//tab.$tree.css( "height", newTotalNavHeight - SCROLL_COMPENSATION + "px" );
				this.addDomNodeToRecalculate( tab.$tree, newNavHeightWithoutHorizontalScrollbar - SCROLL_COMPENSATION );
			}
			else {
				//tab.$wrapperItemInner.css( "overflow", "auto" );
				this.addDomNodeToRecalculate( tab.$wrapperItemInner, { overflow: "auto" });
			}

			//tab.$wrapperItemInner.css( "height", newTotalNavHeight + "px" );
			this.addDomNodeToRecalculate( tab.$wrapperItemInner, newTotalNavHeight );
		},

		getHeaderHeight: function() {
			if ( this.dom.isPageHeaderVisible() ) {
				return this.dom.$pageContentHeader.height();
			}
			else {
				return 0;
			}
		},

		getFooterHeight: function() {
			if ( this.dom.isPageFooterVisible() ) {
				return this.dom.$pageContentFooter.height();
			}
			else {
				return 0;
			}
		}
	};

	var API = {
		init: function() {
			_class.init();
		},

		enableRecalculatingEvents: function() {
			_class.enableRecalculatingEvents();
		},

		disableRecalculatingEvents: function() {
			_class.disableRecalculatingEvents();
		},

		recalculateIfEnabled: function() {
			_class.recalculateIfEnabled();
		},

		toggleRecalculatingEvents: function() {
			_class.toggleRecalculatingEvents();
		}
	};

	return API;
})();
/*js/drexplain/nav-tree/drexplain.nav-tree.menu.js*/
DR_EXPLAIN.namespace( 'DR_EXPLAIN.navTree_Menu' );
DR_EXPLAIN.navTree_Menu = (function(){
	var _class = {
	$navTree: null,
	urlEncoder: null,
	dom: null,
	navTreeView: null,

	nodeVisibleStatusArr: [],



	init: function() {
		this.doSetDom();
		this.doSetUtils();
		this.doSetUrlEncoder();
		this.doSetDataManager();
		this.doSetNodeVisibleStatusArrByUrlEncoder();

		var navArr = this.populateTable( this.dataManager.getRootNodesArray(), true);
		var navTreeItemsCollection = this.getNavCollection( navArr, null );
		this.navTreeView = navTreeView = new NavTree__View({ collection: navTreeItemsCollection, $navTree: this.$navTree });

		var flatCollection = new NavTree__ItemsNodes_Collection( this.navTreeView.models );
		flatCollection.on(
			"change:isVisible",
			this.onNodeVisibleChange,
			this
		);
	},

	show: function() {
		this.navTreeView.render();
	},

	doSetDom: function() {
		this.dom = DR_EXPLAIN.dom;
		this.$navTree = this.dom.tabs.menu.$wrapperItem;
	},

	doSetDataManager: function() {
		this.dataManager = DR_EXPLAIN.dataManager;
	},

	doSetUtils: function() {
		this.utils = DR_EXPLAIN.utils;
	},

	doSetUrlEncoder: function() {
		var that = this;

		this.urlEncoder = DR_EXPLAIN.urlEncoder;
		this.urlEncoder.addSaveFunc(
			function() {
				return that.getCurrVisibleStatusArrAsString();
			},
			that.urlEncoder.KEY_NAME__MENU_STATE
		);
	},

	getDefaultNodeVisibleStatusArr: function() {
		var result = new Array( this.dataManager.getNodesCount() );
		if (this.dataManager.getDrexMenuType() == 3 || this.dataManager.getDrexMenuType() == 1)
		{
			for (var i = 0; i < this.dataManager.getDrex().nodes_count; i++)
				result[i] = 1;
		}
		else
		{
			var deep_border = (this.dataManager.getRootNodesArray().length <= 1 ? 1 : 0);
			for (var i = 0; i < this.dataManager.getDrex().nodes_count; i++) {
				result[i] = ( this.dataManager.getNodeDeepByIndex( i ) <= deep_border ? 1 : 0 );
			}

		}
		return result;
	},

	getCurrVisibleStatusArrAsString: function() {
		var menuMinimized = this.getDefaultNodeVisibleStatusArr();
		var VarTOpnd = this.nodeVisibleStatusArr;

		for (var i = 0; i < VarTOpnd.length; i++)
			menuMinimized[i] ^= VarTOpnd[i];

		var bits = new Array();
		for (var i = 0; i < menuMinimized.length; i++)
			bits.push(menuMinimized[i]);


		var bytes = this.utils.bitsToByte(bits, 7);
		bits = new Array();
		for (var i = 0; i < bytes.length; i++)
			if (bytes[i] == 0)
				bits.push(0);
			else
				bits.push(1);


		var result = this.utils.bitsToByte(bits, 7);

		for (var i = 0; i < bytes.length; i++)
			if (bytes[i] != 0)
				result.push(bytes[i]);


		var str = "";
		for (var i = 0; i < result.length; ++i) {
			str += String.fromCharCode(result[i]);
		}

		var menuStateString = str;

		if (menuStateString != "") {
			return menuStateString;
		}
		else {
			return null;
		}
	},

	doSetNodeVisibleStatusArrByUrlEncoder: function() {
		var t = this.urlEncoder.getValueByKey( this.urlEncoder.KEY_NAME__MENU_STATE );
		var VarTOpnd = this.getDefaultNodeVisibleStatusArr();
		if (t !== null )
		{
			var bytes = t;
			bytes = this.utils.str2ar(bytes);
			var bits = this.utils.byteToBits(bytes, 7);

			var prefixLen = this.utils.encodingPrefixLen(VarTOpnd.length, 7);
			var readPos = prefixLen;
			var result = new Array();
			for (var i = 0; i < prefixLen * 8; i++)
				if (bits[i] == 0)
					result.push(0);
				else
					result.push(bytes[readPos++]);

			var MyBB = this.utils.byteToBits(result, 7);
			for (var i = 0; i < VarTOpnd.length; ++i)
				VarTOpnd[i] ^= MyBB[i];
		}

		this.nodeVisibleStatusArr = VarTOpnd;
		this.validateOpenState();
	},


	onNodeVisibleChange: function( model, newValue ) {
		this.nodeVisibleStatusArr[ model.get( "nodeIndex" ) ] = newValue;
		$( document ).trigger( "nodeVisibleChanged" );

	},

	validateOpenState: function()
	{
		var VarTOpnd = this.nodeVisibleStatusArr;

		var rootNodeIndex = this.dataManager.getIndexByNode( this.dataManager.getRootNode() );
		VarTOpnd[ rootNodeIndex ] = 1;
		if ( !this.dataManager.isSelectedNodeExists() ) {
			return;
		}


		var currentNode = this.dataManager.getSelectedNode().parent();
		while (currentNode)
		{
			VarTOpnd[currentNode.node_index] = 1;
			currentNode = currentNode.parent();
		}

		//console.log( 'validateOpenState:', VarTOpnd );
	},

	populateTable: function( nodeArr, isVisible ) {

		if ( nodeArr.length === 0 ) {
			return null;
		}

		var itemArr = [];

		for ( var index = 0; index < nodeArr.length; index += 1 ) {
			var node = nodeArr[ index ];
			var isOpened = this.nodeVisibleStatusArr[ node.node_index ];
			var isNodeVisible = !!(isVisible && isOpened);

			itemArr.push({
				'title': node.title,
				'link': node.link,
				'nodeIndex': node.node_index,
				'childs': this.populateTable( node.children(), isNodeVisible ),
				'isVisible': isNodeVisible,
				'isSelected': ( node.link === this.dataManager.getPageFilename() )
			});
		}
		return itemArr;
	},

	getNavCollection: function( navArr, parentModel ) {
		if ( navArr === null ) {
			return null;
		}

		var that = this;
		var collection = new NavTree__ItemsNodes_Collection( navArr );
		_.each( collection.models, function( model, index ){
			model.set({
				childs: that.getNavCollection( model.get( "childs" ), model ),
				parent: parentModel
			});
		});
		return collection;
	}
	};

	var API = {
		init: function() {
			_class.init();
		},

		show: function() {
			_class.show();
		}
	};

	return API;
})();
/*js/drexplain/nav-tree/drexplain.nav-tree.index.js*/
DR_EXPLAIN.namespace( 'DR_EXPLAIN.navTree_Index' );
DR_EXPLAIN.navTree_Index = (function(){

	var _class = {
		$navTree: null,
		navTreeView: null,

		init: function() {
			this.doSetDom();
			this.doSetDataManager();

			var navArr = this.populateTable( [this.dataManager.getDrex().root_keyword()] );
			var navTreeItemsCollection = this.getNavCollection( navArr, null );
			this.navTreeView = new NavTree__Keywords_View({ collection: navTreeItemsCollection, $navTree: this.$navTree });
		},

		show: function() {
			this.navTreeView.render();
		},

		doSetDom: function() {
			this.dom = DR_EXPLAIN.dom;
			this.$navTree = this.dom.tabs.index.$wrapperItem;
		},

		doSetDataManager: function() {
			this.dataManager = DR_EXPLAIN.dataManager;
		},

		populateTable: function( keywordArr ) {

			if ( keywordArr.length === 0 ) {
				return null;
			}

			var itemArr = [];

			for ( var index = 0; index < keywordArr.length; index += 1 ) {
				var keyword = keywordArr[ index ];

			    if (keyword.isActive())
			    {
					itemArr.push({
						'title': keyword.title,
						'keywordIndex': keyword.keyword_index,
						'childs': this.populateTable( keyword.childrenSorted() ),
						'links': this.getLinks( keyword )
					});
			    }
			    else {
					var childrenArr = this.populateTable( keyword.childrenSorted() );
					if (childrenArr !== null)
			    		itemArr = itemArr.concat(childrenArr);
			    }
			}
			return itemArr;
		},

		getLinks: function( keyword ) {
	        var links = keyword.nodes();
	        var itemArr = [];
	        for (var i = 0; i < links.length; i++) {
				itemArr.push({
					title: links[i].title,
					link: links[i].link,
					nodeIndex: links[i].node_index
				});
	        }

	        return itemArr;

		},

		getNavCollection: function( navArr, parentModel ) {
			if ( navArr === null ) {
				return null;
			}

			var that = this;
			var collection = new NavTree__ItemsKeywords_Collection( navArr );
			_.each( collection.models, function( model, index ){
				model.set({
					childs: that.getNavCollection( model.get( "childs" ), model ),
					links: new NavTree__ItemsNodes_Collection( model.get( "links" ) ),
					parent: parentModel
				});
			});
			return collection;
		}
	};

	var API = {
		init: function() {
			_class.init();
		},

		show: function() {
			_class.show();
		}
	};

	return API;
})();
/*js/drexplain/nav-tree/drexplain.nav-tree.search.js*/
DR_EXPLAIN.namespace( 'DR_EXPLAIN.navTree_Search' );
DR_EXPLAIN.navTree_Search = (function(){
	var _class = {
		$navTree: null,
		navTreeView: null,


		init: function( ) {
			this.doSetDom();
			this.doSetDataManager();
		},

		setNewContentBySearchResults: function( searchResultsArr ) {
			var navArr = this.getNavArr( searchResultsArr );
			var navTreeItemsCollection = this.getNavCollection( navArr, null );
			var navTreeView = new NavTree__Search_View({ collection: navTreeItemsCollection, $navTree: this.$navTree });
			this.navTreeView = navTreeView;
		},

		show: function() {
			this.navTreeView.render();
		},

		doSetDom: function() {
			this.dom = DR_EXPLAIN.dom;
			this.$navTree = this.dom.tabs.search.$wrapperItem;
		},

		doSetDataManager: function() {
			this.dataManager = DR_EXPLAIN.dataManager;
		},

		getNavCollection: function( navArr, parentModel ) {
			if ( navArr === null ) {
				return null;
			}

			var that = this;
			var collection = new NavTree__ItemsNodes_Collection( navArr );
			_.each( collection.models, function( model, index ){
				model.set({
					childs: that.getNavCollection( model.get( "childs" ), model ),
					parent: parentModel
				});
			});
			return collection;
		},

		getNavArr: function( searchResultsArr ) {

			var itemArr = [];

			for ( var index = 0; index < searchResultsArr.length; index++ ) {
				var currNode = searchResultsArr[ index ];
				itemArr.push({
					'title':  currNode[ 0 ],
					'link':  currNode[ 1 ],
					'childs': null,
					'isSelected': (  currNode[ 1 ] === this.dataManager.getPageFilename() )
				});
			}
			return itemArr;
		}
	};

	var API = {
		init: function() {
			_class.init();
		},

		setNewContentBySearchResults: function( searchResultsArr ) {
			_class.setNewContentBySearchResults( searchResultsArr );
		},

		show: function() {
			_class.show();
		}
	};

	return API;
})();
/*js/app.js*/
function initialSizing()
{
	if (1 == 1)
	{
		$("#pageContentMiddle").removeClass("hidden");
		$("#pageContentFooter").removeClass("hidden");
	}
	var app = DR_EXPLAIN;
	app.dataManager.init();

	app.dom.init();

	app.searchManager.init();
	app.searchManager.runCustomButtons();

	app.urlEncoder.init();
	app.urlEncoder.doDecodeUrl();
	
	if (1 == 1)
	{
		app.navTree_Menu.init();
		app.navTree_Index.init();
		app.navTree_Search.init();

		if (app.tabController.isMenuTabShown())
			app.navTree_Menu.show();
		if (app.tabController.isIndexTabShown())
			app.navTree_Index.show();
	}

	app.workZoneSizer.init();
	if (1 == 1)
	{
		app.workZoneSizer.doBindEvents();
	}
	app.workZoneSizer.recalculateAll();
}

function hideContentMiddle()
{
	$( "#pageContentMiddle" ).addClass("hidden");
}
function hideContentFooter()
{
	$( "#pageContentFooter" ).addClass("hidden");
}
function resizeArticle()
{
	var article = $("#article");
	var articleOffset = article.offset();
	var articleOuterHeight = article.outerHeight(true);
	
	var bTabs = $(".b-tabs");
	var bTabsOffset = bTabs.offset();
	var bTabsCurrentLowestPoint = bTabsOffset.top + bTabs.outerHeight(true);

	var articleLowestPointMustBe = bTabsCurrentLowestPoint;
	var articleCurrentLowestPoint = articleOffset.top + articleOuterHeight;
	var articleWrapper = $(".b-article__wrapper");
	var articleMustBeResizedBy = articleLowestPointMustBe - articleCurrentLowestPoint;
	
	var articleWrapperHeight = articleWrapper.height();
	articleWrapper.css("min-height", Math.max(articleWrapperHeight + articleMustBeResizedBy, 0));
}
function initResizeArticleArticle()
{
	resizeArticle();
	
	$( document ).on( "newTabSelected.tabs", function(){
		resizeArticle();
	});
	$( document ).on( "nodeVisibleChanged", function(){
		resizeArticle();
	});
	$( document ).on( "searchCompleteBuildTree", function(){
		resizeArticle();
	});

	var that = { timeoutId: null, DELAY: 1};
	$( DR_EXPLAIN.dom.$splitter ).on( "mousedown.splitter", function(){
	
		$( document ).on( "mousemove.splitter", function( e ){
			clearTimeout( that.timeoutId );
			that.timeoutId = setTimeout(function(){
				resizeArticle();
			}, that.DELAY );
		});
		
		$( document ).on( "mouseup.splitter", function(){
			resizeArticle();
		});
	});
}

$( document ).ready(function(){
	if (1 == 1 && !is_touch_device())
	{
		$("iframe").each(function()
		{
			$(this).prop("real_src", $(this).prop("src"));
			$(this).prop("src", "about:blank");
		});
	}

	var app = DR_EXPLAIN;

	app.dom.init();

	app.urlEncoder.doBindOpenNextPageWithEncodedStringToLinks();

	app.highlightManager.init();

	app.searchManager.runInputSync();
	app.searchManager.runInputPlaceholders();

	app.tabController.init();
	app.tabController.doBindEvents();
	app.tabController.doSetTabIndexByUrlEncoder();

	app.workZoneSizer.init();
	app.workZoneSizer.recalculateAll();

	if (1 == 0)
	{
		app.navTree_Menu.init();
		app.navTree_Index.init();
		app.navTree_Search.init();
		
		if (app.tabController.isMenuTabShown())
			app.navTree_Menu.show();
		if (app.tabController.isIndexTabShown())
			app.navTree_Index.show();
	}
	else
	{
		var $hc = $("#hiddenContent");
		$hc.detach();
		$hc.removeClass("hiddenContent");
		$hc.appendTo("#description_on_page_placeholder");
		$("iframe").each(function()
		{
			$(this).prop("src", $(this).prop("real_src"));
		});
	}


	app.searchManager.doBindEvents();
	app.searchManager.doSearchIfQueryStringNotEmpty();

	app.workZoneSizer.doBindEvents();
	app.workZoneSizer.recalculateAll();

	app.tabController.doSetScrollTopByUrlEncoder();
	
	if (1 == 0 || is_touch_device())
		initResizeArticleArticle();
});
