// initialization of the tabs at the right side of our page  
// initialization of the tabs at the right side of our page  
$("#attr-tabs").tabs();
$("#main-tabs").tabs();


$('#term_dialog').dialog({
  autoOpen: false,
  width: 800,
  height: 500,
  modal: true
});

// initialization of nav menu
$("#nav").menu();
// initialization of prop panel
// FIXME a little problem: an property line can be
// dragged under the new empty line, that should not
// be enabled
$('#table-prop tbody').sortable();

// initialization of terminal
// NOTE: this function will be overwritten in js/explorer/terminal.js
var terminalfunc = function (command, term) {
  var result = window.eval(command);
  if (result != undefined) {
    term.echo(String(result));
  }
}

try {
  // TODO why there's always an error?
  // seems there's something wrong dealing with the history
  $('#terminal').terminal(function (command, term) {
    terminalfunc(command, term);
  },
  {
    history: false,
    prompt: 'EPMC> ',
    greetings: 'Welcome to EPMC Online Terminal!',
  }
  );
} catch (e) {
  console.log(e.message);
}

// adjust of heights of container & editors
// maybe this should be put in a individual js file which would be loaded in every html
window.onresize = function () {
  resize();
};

$(document).ready(function () {
  resize();
});

function resize() {
  var wheight = $(this).height();
  var offset = 120;
  var tabheader_offset = 70;
  $("#container").height(wheight);
  $("#main-tabs").height(wheight-offset);
  $("#attr-tabs").height(wheight-offset);
  var validheight = wheight-offset-tabheader_offset;
  $(".model-editor").height(validheight);
  if (0.5 * validheight < 150) {
    $("#table-prop-div").height(validheight);
    $("#prop-editor").height(0);
    $("#prop-grapheditor").height(0);
  } else {
    $("#table-prop-div").height(0.5 * validheight);
    $("#prop-editor").height(0.5 * validheight);
    $("#prop-grapheditor").height(0.5 * validheight);
  }
}
