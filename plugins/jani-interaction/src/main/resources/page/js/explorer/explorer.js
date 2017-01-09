/* setting the events for the explorer */
var debug;

// when we switch to different folders, this will be updated
var path = "/";

$("tr.modelline td.caption a").click(function (event) {
  var modelname = event.target.innerHTML;
  // TODO this is only a temp solution
  $.get("resources/" + modelname, {}, function(result) {
    opentab(path, modelname, result);
  }, "text");  
});

$("tr.folderline td.caption a").click(function (event) {
  var foldername = event.target.innerHTML;
  alert('Going to folder ' + path + foldername);
});

$("a[href=#tabs-visual-editor]").click(function(event) {
  openVisualEditor();
});

$("a[href!=#tabs-visual-editor]").click(function(event) {
  exitVisualEditor();
});


/* currmodels: every tab has a corresponding element in currmodels */
var currmodels = new Array();
var activatedid = null;
var activateditem = null;

function searchmodel(identifier) {
  for (var i = 0; i < currmodels.length; i ++) {
    if (currmodels[i].id == identifier) {
      return currmodels[i];
    }
  }
  return null;
}

/*************************************************************************
 * SECTION: TABS/MODELS
 ************************************************************************/

/*
  function: opentab
  -- path: where the model is located (localhost)
           the folder tree (online)
  -- modelname: trivial
  
  dependence: websocket/websocket.js
*/

function opentab(path, modelname, modelcontent) {
  // fetch model content
  // var model = wsgetmodel(path, modelname);
  var id = 'model' + (path + modelname).replace('/', '-').replace('.', '');
  var model = new Object({
    name: modelname,
    path: path,
    // make sure that ther're no invalid characters
    id: id,
    // FIXME using real model instead
    model: modelcontent,
    // FIXME using real model type instead
    type: "mdp",
    // FIXME using real properties instead
    prop: [
      {
        id: 0,
        formula: "Pmax=?[F(\"done\" & \"even\")]"
      },
      {
        id: 1,
        formula: "Pmax=?[F(\"done\" & \"even\")]"
      },
      {
        id: 2,
        formula: "Pmax=?[F(\"done\" & \"even\")]"
      },
      {
        id: 3,
        formula: "Pmax=?[F(\"done\" & \"even\")]"
      }
    ]
  });
  
  // check if this model is already opened
  if (searchmodel(model.id) != null) {
    // existed already
    gototab(model.id);
    return;
  }  
  // create tab headers
  console.log(model.id);
  $("#main-tabs ul").append(
    "<li id='menuitem-" + model.id + "'>" +
    "<a href='#tabs-" + model.id + "' targetid='" + model.id + "' class = 'btn-model'>" + model.name + "</a>" +
    "<a class='close' href='#' targetid='" + model.id + "'>×</a>" + 
    "</li>"
  );
  // create tab bodys
  var tabc = "<div id='tabs-" + model.id + "' style='padding: 0px;'><pre class='model-editor' id='#editor-" + model.id + "' width='100%'></pre></div>";
  $("#main-tabs").append(tabc);
  // refresh tabs instance
  $("#main-tabs").tabs("refresh");
  setup_tabs_events();
  // we may resize before run the ace.edit()
  resize();
  $('#tabs-' + model.id).find("pre").html(model.model);
  var $p = $('#tabs-' + model.id).find("pre");  
  // create editor instance for model/properties
  model.editor = ace.edit($p[0]);
  model.editor.setTheme("ace/theme/github");
  model.editor.getSession().setMode("ace/mode/prism");
  // I don't know why but this line is required during a runtime warning
  model.editor.$blockScrolling = Infinity;
  // set font-size
  model.editor.setFontSize(14);
  // finally, goto the new tab
  currmodels.push(model);
  gototab(model.id);  
}

function gototab(identifier) {
  // since we're using gototab to switch some certain tab
  // including a model, we'd like to shoud the attr panel
  $("#attr-cell").show();
  if (identifier != null) 
  {  
    // get the index of our target tab
    var index = $('#main-tabs a[href="#tabs-' + identifier + '"]').parent().index();
    // active this tab
    $('#main-tabs').tabs('option', 'active', index);
    // find the corresponding model and fill its properties
    var m = searchmodel(identifier);
    // reload props
    // 1. refresh, remove all the existing props
    var lst = $('#table-prop tbody');
    lst.html('');
    // 2. add all properties and create its containers (ace editors)
    for (var i = 0; i < m.prop.length; i ++) {
      // append the html div
      var editorid = identifier + "-" + m.prop[i].id;
      var item = "<input id='" + editorid + "' value='" + m.prop[i].formula + "' />";
      item = "<tr>" + td(item) + td(propext) + "</tr>";
      lst.append(item);
    }
    lst.append("<tr><td><input onchange='addprop(event);' placeholder='[new property]' /></td></tr>");
    activatedid = identifier;
    activateditem = m;
    
    if (activateditem.name.includes('.jani')) {
      $('#btn-visualeditor').show();
    } else {
      $('#btn-visualeditor').hide();
    }
  }
}

function removetab(identifier) {
  $('#tabs-' + identifier).remove();
  $('#menuitem-' + identifier).remove();
  if (identifier == activatedid) {
    gotofileexplorer();
  }
}

function gotofileexplorer() {
  $("#attr-cell").hide();
  $('#main-tabs').tabs('option', 'active', 0);
  // since we're going to the file explorer, no model is activated
  // we're going to assign the relative variable to null
  activatedid = null;
  activateditem = null;
}

function setup_tabs_events() {
  $('#btn-modellist').click(function (e){
    gotofileexplorer();
  });

  $(".btn-model").click(function (e){    
    var id = e.target.getAttribute("targetid");
    gototab(id);
  });
  
  $("a.close").click(function (e){
    var id = e.target.getAttribute("targetid");
    var item = searchmodel(id);
    if (item == null) {
      alert("Something is wrong. Maybe some model is opened by not yet appended to the Array currmodels.");
      return;
    } else {
      // TODO save the models
      alert("You're going to close the model " + item.name + "/Save operation to be done.");
      // free the object (not sure if it works)
      // NOTE if a file is repeatedly opened and closed, some bugs may be raised
      item.editor = null;
      removetab(item.id);
      currmodels.remove(item);
    }
  });
}

setup_tabs_events();

/*************************************************************************
 * SECTION: PROPOSITIONS
 ************************************************************************/

var propext = "<span class='ui-icon ui-icon-carat-2-n-s'></span>"
var proppre = "<span class='ui-icon ui-icon-closethick'></span>";

function addprop(evt) {
  debug = evt;
  // expand current line a valid property (no saving here)
  // TODO FIXME
  $(evt.target.parentNode.parentNode).append(td(propext));
  $(evt.target.parentNode.parentNode.parentNode).append(
      // add a new line, making it able to continue adding props via
      // a empty input field
      "<tr><td><input onchange='addprop(event);' placeholder='[new property]' /></td></tr>"
      // FIXME TAB is not captured currently, which means when we finish inputing
      // new property and press TAB, we can add new empty field but this field
      // has no cursor (however, it does have focus)
    );
  $('#table-prop tbody tr:last td:first input').focus();
}

/*************************************************************************
 * SECTION: VISUAL EDITORS
 ************************************************************************/

function openVisualEditor() {
  $('#main-cell').hide();
  setJANI(activateditem.model, activatedid);
}

function exitVisualEditor() {
  /*  when the visual editor is activated, there's no way to directly open
      the model explorer. so we assume that when exiting the visual editor,
      we'll update the textual format at once
   */
  if (activateditem != null) {
    $('#main-cell').show();
    var jani = getJANI();
    activateditem.model = jani;
    activateditem.editor.getSession().setValue(jani);
  }
}
