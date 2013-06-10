/*
 * jQuery UI Simplepager 1.0
 *
 * Copyright (c) 2009 Ian Tyndall 
 * Dual licensed under the MIT (MIT-LICENSE.txt)
 * and GPL (GPL-LICENSE.txt) licenses.
 *
 * http://ianty.com/simplePager
 *
 * Depends:
 *	ui.core.js
 *
*/

(function($) { // hide the namespace

$.widget("ui.simplepager", {
    _init: function() {
        this.close();
        this._doOpts();
    },
    _doOpts : function(){
        if (this.options.totalRecords > 0) {
            this._setPages();
            if (!this.contentSet) {
                this._setContent();
		        this._setLinkDisplay(this.options.currentPage);
				this._doObservers();        
            }
            this._setCurrent();
        }
        this.open();
    },
    _doObservers : function() {
        if (this.contentSet == false) { return; }
        var pg = this;
		pg.element.children('span:not(.disabled)').bind("click", function(e) {
			pg._switchPage(e, $(this).attr('pg'));
		}).hover(
            function() { $(this).addClass('state-hover'); },
			function() { $(this).removeClass('state-hover'); }
		);
    },
    _generateHTML : function() {
        var html  = '<span class="pager-seek-first" pg="first">&lt;&lt;</span><span class="pager-seek-prev" pg="prev">&lt;</span>';
		for (var i = this.startPage; i <= this.endPage; i++) html += '<span pg="' + i + '">' + i + '</span>';
        html += '<span class="pager-seek-next" pg="next">&gt;</span><span class="pager-seek-end" pg="end">&gt;&gt;</span>';
        return html;
    },
    _setPages : function() {
        this.totalPages = Math.ceil(this.options.totalRecords / this.options.perPage);
		this.startPage = Math.floor((this.options.currentPage - 1) / this.options.windowSize) * this.options.windowSize + 1;
		this.endPage = Math.min(this.totalPages, this.startPage + this.options.windowSize - 1);
    },
    _setContent : function() {
        $(this.element).addClass('pagination ui-widget').empty().append(this._generateHTML());
        this.contentSet = true;
    },
    _setLinkDisplay : function(page) {
        var pg   = this;
        $.each(['first','prev','next','end'],function(i) {
            var disable = (i > 1) ? (page > Math.floor(pg.totalPages / pg.options.windowSize) * pg.options.windowSize) : (page <= pg.options.windowSize);
            pg.element.find('.pager-seek-' + this)[disable ? 'addClass' : 'removeClass']('disabled');
        });
    },
    _decipherPage: function(page) {
        var reqPage = 0;
        switch(page) {
            case 'next':
                reqPage = this.startPage + this.options.windowSize;
            break;
            case 'prev':
                reqPage = this.startPage - this.options.windowSize;
            break;
            case 'first':
                reqPage = 1;
            break;
            case 'end':
                reqPage = this.totalPages;
            break;
            default:
                if(!isNaN(page)) { reqPage = page; }
        }
        return reqPage;        
    },
    _switchPage : function(event,page) {
        var reqPage = this._decipherPage(page);
        if ((reqPage > 0) && (reqPage <= this.totalPages)) {
			this._setLinkDisplay(reqPage);
			if (reqPage >= this.startPage && reqPage <= this.endPage) {
				this.reset();
			} else {
				this._setCurrent(reqPage);
			}
			this._trigger('switchPage', event, reqPage);
        }
    },
    _setCurrent : function() {
		var pg = this;
        var curr = (arguments.length > 0) ? arguments[0] : pg.options.currentPage;
        pg.options.currentPage = curr;
		pg.element.find('span.current').removeClass('current'); pg.element.find('span[pg=' + curr + ']').addClass('current');
    },
    current : function(){
        if (arguments.length == 1) {
            var reqPage = this._decipherPage(arguments[0]);
            this._setLinkDisplay(reqPage);
			if (reqPage >= this.startPage && reqPage <= this.endPage) {
				this.reset();
			} else {
				this._setCurrent(reqPage);
			}
        }
        return this.options.currentPage;
    },
    close : function() {
        this.element.hide();
        return this;
    },
    open : function() {
        this.element.show();
        return this;
    },
    reset : function() {
		this.contentSet = false;
		this.element.children('span:not(.disabled)').unbind("click");
        this._doOpts();
        return this;
    }
});

$.extend($.ui.simplepager, {
    version: "1.7.2",
    defaults: {
        perPage: 15,
	    currentPage: 1,
        totalRecords: 0,
		windowSize: 10
    },
    totalPages: 1,
	startPage: 1,
	endPage: 1,
    contentSet: false
});

})(jQuery);
