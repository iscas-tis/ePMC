ace.define("ace/mode/prism_highlight_rules",["require","exports","module","ace/lib/oop","ace/mode/text_highlight_rules"], function(require, exports, module) {
"use strict";

var oop = require("../lib/oop");
var TextHighlightRules = require("./text_highlight_rules").TextHighlightRules;

var prismHighlightRules = function() {

    this.$rules = { start: 
       [ { include: '#keyword' },
         { include: '#operator' },
         { include: '#number' },
         { include: '#string' },
         { include: '#comment' } ],
      '#bracket': 
       [ { token: 'keyword.bracket.prism',
           regex: '\\(|\\)|\\[|\\]|,' } ],
      '#comment': 
       [ { token: 
            [ 'punctuation.definition.comment.prism',
              'comment.line.number-sign.prism' ],
           regex: '(#)(?!\\{)(.*$)'} ],
      '#keyword':
       [ { token: 'keyword.structure.prism',
           regex: '\\b(?:ctmc|dtmc|mdp|smg|module|endmodule|formula|nondeterministic|probabilistic|pta|stochastic|invariant|endinvariant|rewards|endrewards|init|endinit|system|endsystem|player|endplayer)\\b' },
         { token: 'keyword.bool.prism',
           regex: '\\b(?:true|false)\\b' },
         { token: 'keyword.variableType.prism',
           regex: '\\b(?:bool|clock|const|double|global|int|rate|label|filter|func)\\b' },
         { token: 'keyword.function.prism',
           regex: '\\b(?:max|min)\\b' } ],
      '#number': 
       [ { token: 'constant.numeric.prism',
           regex: '\\b0(?:x|X)[0-9a-fA-F]*|(?:\\b[0-9]+\\.?[0-9]*|\\.[0-9]+)(?:(?:e|E)(?:\\+|-)?[0-9]*)?(?:im)?|\\bInf(?:32)?\\b|\\bNaN(?:32)?\\b|\\btrue\\b|\\bfalse\\b' } ],
      '#operator': 
       [ { token: 'keyword.operator.update.prism',
           regex: '=|:=|\\+=|-=|\\*=|/=|//=|\\.//=|\\.\\*=|\\\\=|\\.\\\\=|^=|\\.^=|%=|\\|=|&=|\\$=|<<=|>>=' },
         { token: 'keyword.operator.ternary.prism', regex: '\\?|:' },
         { token: 'keyword.operator.boolean.prism',
           regex: '\\|\\||&&|!' },
         { token: 'keyword.operator.arrow.prism', regex: '->|<-|-->' },
         { token: 'keyword.operator.relation.prism',
           regex: '>|<|>=|<=|==|!=|\\.>|\\.<|\\.>=|\\.>=|\\.==|\\.!=|\\.=|\\.!|<:|:>' },
         { token: 'keyword.operator.range.prism', regex: ':' },
         { token: 'keyword.operator.shift.prism', regex: '<<|>>' },
         { token: 'keyword.operator.bitwise.prism', regex: '\\||\\&|~' },
         { token: 'keyword.operator.arithmetic.prism',
           regex: '\\+|-|\\*|\\.\\*|/|\\./|//|\\.//|%|\\.%|\\\\|\\.\\\\|\\^|\\.\\^' },
         { token: 'keyword.operator.isa.prism', regex: '::' },
         { token: 'keyword.operator.dots.prism',
           regex: '\\.(?=[a-zA-Z])|\\.\\.+' },
         { token: 'keyword.operator.interpolation.prism',
           regex: '\\$#?(?=.)' },
         { token: [ 'variable', 'keyword.operator.transposed-variable.prism' ],
           regex: '([\\w\\xff-\\u218e\\u2455-\\uffff]+)((?:\'|\\.\')*\\.?\')' },
         { token: 'text',
           regex: '\\[|\\('},
         { token: [ 'text', 'keyword.operator.transposed-matrix.prism' ],
            regex: "([\\]\\)])((?:'|\\.')*\\.?')"} ],
      '#string': 
       [ { token: 'punctuation.definition.string.begin.prism',
           regex: '\'',
           push: 
            [ { token: 'punctuation.definition.string.end.prism',
                regex: '\'',
                next: 'pop' },
              { include: '#string_escaped_char' },
              { defaultToken: 'string.quoted.single.prism' } ] },
         { token: 'punctuation.definition.string.begin.prism',
           regex: '"',
           push: 
            [ { token: 'punctuation.definition.string.end.prism',
                regex: '"',
                next: 'pop' },
              { include: '#string_escaped_char' },
              { defaultToken: 'string.quoted.double.prism' } ] },
         { token: 'punctuation.definition.string.begin.prism',
           regex: '\\b[\\w\\xff-\\u218e\\u2455-\\uffff]+"',
           push: 
            [ { token: 'punctuation.definition.string.end.prism',
                regex: '"[\\w\\xff-\\u218e\\u2455-\\uffff]*',
                next: 'pop' },
              { include: '#string_custom_escaped_char' },
              { defaultToken: 'string.quoted.custom-double.prism' } ] },
         { token: 'punctuation.definition.string.begin.prism',
           regex: '`',
           push: 
            [ { token: 'punctuation.definition.string.end.prism',
                regex: '`',
                next: 'pop' },
              { include: '#string_escaped_char' },
              { defaultToken: 'string.quoted.backtick.prism' } ] } ] }
    
    this.normalizeRules();
};

prismHighlightRules.metaData = { fileTypes: [ 'jl' ],
      firstLineMatch: '^#!.*\\bprism\\s*$',
      foldingStartMarker: '^\\s*(?:if|while|for|begin|function|macro|module|baremodule|type|immutable|let)\\b(?!.*\\bend\\b).*$',
      foldingStopMarker: '^\\s*(?:end)\\b.*$',
      name: 'prism',
      scopeName: 'source.prism' }


oop.inherits(prismHighlightRules, TextHighlightRules);

exports.prismHighlightRules = prismHighlightRules;
});

// ace.define("ace/mode/folding/cstyle",["require","exports","module","ace/lib/oop","ace/range","ace/mode/folding/fold_mode"], function(require, exports, module) {
// "use strict";

// var oop = require("../../lib/oop");
// var Range = require("../../range").Range;
// var BaseFoldMode = require("./fold_mode").FoldMode;

// var FoldMode = exports.FoldMode = function(commentRegex) {
//     if (commentRegex) {
//         this.foldingStartMarker = new RegExp(
//             this.foldingStartMarker.source.replace(/\|[^|]*?$/, "|" + commentRegex.start)
//         );
//         this.foldingStopMarker = new RegExp(
//             this.foldingStopMarker.source.replace(/\|[^|]*?$/, "|" + commentRegex.end)
//         );
//     }
// };
// oop.inherits(FoldMode, BaseFoldMode);

// (function() {
    
//     this.foldingStartMarker = /(\{|\[)[^\}\]]*$|^\s*(\/\*)/;
//     this.foldingStopMarker = /^[^\[\{]*(\}|\])|^[\s\*]*(\*\/)/;
//     this.singleLineBlockCommentRe= /^\s*(\/\*).*\*\/\s*$/;
//     this.tripleStarBlockCommentRe = /^\s*(\/\*\*\*).*\*\/\s*$/;
//     this.startRegionRe = /^\s*(\/\*|\/\/)#?region\b/;
//     this._getFoldWidgetBase = this.getFoldWidget;
//     this.getFoldWidget = function(session, foldStyle, row) {
//         var line = session.getLine(row);
    
//         if (this.singleLineBlockCommentRe.test(line)) {
//             if (!this.startRegionRe.test(line) && !this.tripleStarBlockCommentRe.test(line))
//                 return "";
//         }
    
//         var fw = this._getFoldWidgetBase(session, foldStyle, row);
    
//         if (!fw && this.startRegionRe.test(line))
//             return "start"; // lineCommentRegionStart
    
//         return fw;
//     };

//     this.getFoldWidgetRange = function(session, foldStyle, row, forceMultiline) {
//         var line = session.getLine(row);
        
//         if (this.startRegionRe.test(line))
//             return this.getCommentRegionBlock(session, line, row);
        
//         var match = line.match(this.foldingStartMarker);
//         if (match) {
//             var i = match.index;

//             if (match[1])
//                 return this.openingBracketBlock(session, match[1], row, i);
                
//             var range = session.getCommentFoldRange(row, i + match[0].length, 1);
            
//             if (range && !range.isMultiLine()) {
//                 if (forceMultiline) {
//                     range = this.getSectionRange(session, row);
//                 } else if (foldStyle != "all")
//                     range = null;
//             }
            
//             return range;
//         }

//         if (foldStyle === "markbegin")
//             return;

//         var match = line.match(this.foldingStopMarker);
//         if (match) {
//             var i = match.index + match[0].length;

//             if (match[1])
//                 return this.closingBracketBlock(session, match[1], row, i);

//             return session.getCommentFoldRange(row, i, -1);
//         }
//     };
    
//     this.getSectionRange = function(session, row) {
//         var line = session.getLine(row);
//         var startIndent = line.search(/\S/);
//         var startRow = row;
//         var startColumn = line.length;
//         row = row + 1;
//         var endRow = row;
//         var maxRow = session.getLength();
//         while (++row < maxRow) {
//             line = session.getLine(row);
//             var indent = line.search(/\S/);
//             if (indent === -1)
//                 continue;
//             if  (startIndent > indent)
//                 break;
//             var subRange = this.getFoldWidgetRange(session, "all", row);
            
//             if (subRange) {
//                 if (subRange.start.row <= startRow) {
//                     break;
//                 } else if (subRange.isMultiLine()) {
//                     row = subRange.end.row;
//                 } else if (startIndent == indent) {
//                     break;
//                 }
//             }
//             endRow = row;
//         }
        
//         return new Range(startRow, startColumn, endRow, session.getLine(endRow).length);
//     };
//     this.getCommentRegionBlock = function(session, line, row) {
//         var startColumn = line.search(/\s*$/);
//         var maxRow = session.getLength();
//         var startRow = row;
        
//         var re = /^\s*(?:\/\*|\/\/|--)#?(end)?region\b/;
//         var depth = 1;
//         while (++row < maxRow) {
//             line = session.getLine(row);
//             var m = re.exec(line);
//             if (!m) continue;
//             if (m[1]) depth--;
//             else depth++;

//             if (!depth) break;
//         }

//         var endRow = row;
//         if (endRow > startRow) {
//             return new Range(startRow, startColumn, endRow, line.length);
//         }
//     };

// }).call(FoldMode.prototype);

// });

//ace.define("ace/mode/prism",["require","exports","module","ace/lib/oop","ace/mode/text","ace/mode/prism_highlight_rules","ace/mode/folding/cstyle"], function(require, exports, module) {
ace.define("ace/mode/prism",["require","exports","module","ace/lib/oop","ace/mode/text","ace/mode/prism_highlight_rules"], function(require, exports, module) {

"use strict";

var oop = require("../lib/oop");
var TextMode = require("./text").Mode;
var prismHighlightRules = require("./prism_highlight_rules").prismHighlightRules;
//var FoldMode = require("./folding/cstyle").FoldMode;

var Mode = function() {
    this.HighlightRules = prismHighlightRules;
    //this.foldingRules = new FoldMode();
};
oop.inherits(Mode, TextMode);

(function() {
    this.lineCommentStart = "#";
    this.blockComment = "";
    this.$id = "ace/mode/prism";
}).call(Mode.prototype);

exports.Mode = Mode;
});
