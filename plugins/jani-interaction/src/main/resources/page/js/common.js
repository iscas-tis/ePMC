// some functions to construct html elements
function td (e) { return "<td>" + e + "</td>"; }

// 根据相对路径获取绝对路径
function getPath(relativePath,absolutePath){
  var reg = new RegExp("\\.\\./","g");
  var uplayCount = 0; // 相对路径中返回上层的次数。
  var m = relativePath.match(reg);
  if(m) uplayCount = m.length;
  var lastIndex = absolutePath.length-1;
  for(var i=0;i<=uplayCount;i++){
    lastIndex = absolutePath.lastIndexOf("/",lastIndex);
  }
  return absolutePath.substr(0,lastIndex+1) + relativePath.replace(reg,"");
}

function include(jssrc){
  // 先获取当前a.js的src。a.js中调用include,直接获取最后1个script标签就是a.js的引用。
  var scripts = document.getElementsByTagName("script");
  var lastScript = scripts[scripts.length-1];
  var src = lastScript.src;
  if(src.indexOf("http://")!=0 && src.indexOf("/") !=0){
    // a.js使用相对路径,先替换成绝对路径
    var url = location.href;
    var index = url.indexOf("?");
    if(index != -1){
      url = url.substring(0, index-1);
    }
    src = getPath(src,url);
  }
  var jssrcs = jssrc.split("|"); // 可以include多个js，用|隔开
  for(var i=0;i<jssrcs.length;i++){
    // 使用juqery的同步ajax加载js.
    // 使用document.write 动态添加的js会在当前js的后面，可能会有js引用问题
    // 动态创建script脚本，是非阻塞下载，也会出现引用问题
    console.log(jssrc + src);
    $.ajax({type:'GET',url:getPath(jssrc,src),async:false,dataType:'script'});
  }
}

Array.prototype.remove = function(b) {
  var a = this.indexOf(b);
  if (a >= 0) {
    this.splice(a, 1);
    return true;
  }
  return false;
}; 

// FIXME in local version, the function is of no use

// FIXME the following code is only used in demo version
var testmodel = "\
ctmc\n\
\n\
const double lambda = 1.0; // one of 1.0, 2.0, 3.0, 4.0, 5.0, 6.0\n\
const double gamma = 3.0;\n\
const double mu = 100.0;\n\
\n\
const int n = 200;\n\
\n\
module three_valued_abstraction\n\
  // places\
  p1: [0..n] init 0; // idle\n\
  p2: [0..n] init 3; // busy\n\
  p3: [0..n] init 0; // queue\n\
\n\
  // transitions\n\
  [] (p1>0) & (p2<n) -> lambda : (p1'=p1-1) & (p2'=p2+1);\n\
  [] (p2>0) & (p1<n) -> gamma : (p1'=p1+1) & (p2'=p2-1);\n\
  [] (p1>0) & (p3>0) & (p2<n) -> mu : (p1'=p1-1) & (p2'=p2+1) & (p3'=p3-1);\n\
  [] (p2>=3) & (p3<n) -> lambda : (p3'=p3+1);\n\
\n\
endmodule\n\
";

// definition of the dialog
$('#dialog').dialog({
  autoOpen: false,
  width: 400,
  modal: true,
  buttons: [
  {
    text: "Ok",
    click: function() {
      $( this ).dialog( "close" );
    }
  },
  {
    text: "Cancel",
    click: function() {
      $( this ).dialog( "close" );
    }
  }
  ]
});

function alert(message) {
  $('#dialog').html(message);
  $('#dialog').attr("title", "alert");
  $('#dialog').dialog("open");
}
