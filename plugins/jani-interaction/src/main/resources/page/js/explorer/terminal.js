// overwrite the interpreter func of the terminal

// term_cbmap : function map for callback in terminal
// term_cbmap[a] = f indicates that if a command in terminal is a,
// then function b(args) will be executed
var term_cbmap = {};

function terminalfunc(command, term) {
  // check if the command is in callback list
  // term.echo("[HANDLED BY TERMINAL.JS");
  var args = command.split(' ');
  if (args[0] in term_cbmap) {
    term_cbmap[args[0]](args, term);
  } else {
    var result = window.eval(command);
    if (result != undefined) {
      term.echo(String(result));
    }
  }
}


term_cbmap['ls'] = function(args, term) {
  if (args[1] == 'buf') {
    term.echo("Currently there're " + currmodels.length + " models opened.");
    for (var i = 0; i < currmodels.length; i ++) {
      term.echo(i + " - " + currmodels[i].path + currmodels[i].name + "\n");
    }
  }
}
