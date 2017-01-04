var DEFAULT_WS_URL = "ws://localhost:8080/";
// recvar is a debug variable
var recvar = null;

function JaniClient() {
  // initializing the jani client
  this.status = "unauthorized";
  this.callback = {};
  this.roles = [];
  this.msgcounter = 0;
  this.buf = [];
  this.block = undefined;

  this.analysis_ongoing = {};
  this.analysis_history = {};

  // initializing web socket
  this.ws = CreateWebSocket(this);
  debug("websocket created.");
  recvar = this;
}

/****************************** JaniClient Sequential Execution ************************/
JaniClient.prototype.exec = function (func, args, sync) {
  if (func == 'login') sync = true;
  this.buf.push({
    func: func,
    args: args,
    sync: sync
  });
}

JaniClient.prototype.execOnce = function () {
  // remove the block flag
  this.block = undefined;
  var cmd = this.buf.shift();
  if (cmd == undefined) {
    // how to continue ?? TODO
    JaniClient.curr = this;
    setTimeout("JaniClient.curr.execOnce()", 50);
    return;
  }
  // console.log(cmd);
  var id = this[cmd.func](cmd.args);
  if (cmd.sync != true) {
    this.execOnce();
  } else {
    // add the block flag
    this.block = id;
  }
}

/********************************** JaniClient APIs *************************************/
JaniClient.prototype.login = function(args) {
  if (this.status == "authorized") {
    // already logged in
    warning("client is already authorized.");
    this.execOnce();
    return;
  }
  if (args.name == null || args.pwd == null) {
    this.call({
      "jani-versions": [1]
    });
  } else {
    this.call({
      "jani-versions": [1],
      "login": args.name,
      "password": args.pwd
    })
  }
}

JaniClient.prototype.logout = function(args) {
  info("server closed.");
  this.call({
    type: 'close',
    reason: "user wanna log out"
  });
}

/******************************************************************************
 * update_params
 *   you should always modify the parameters' value first in this.params, and
 *  then call this function to commit all these modifications to remote server.
 *
 *  @callback the callback function
******************************************************************************/
JaniClient.prototype.update_params = function (args) {
  msgobj = {
    type: "server-parameters",
    parameters: []
  }
  for (var i in this.params) {
    if (this.params[i].value == null) continue;
    msgobj.parameters.push({
      id: this.params[i].id,
      value: this.params[i].value
    });
  }
  this.call(msgobj, function () {
    info('parameters updated.')
  });

  return msgobj.id
}

JaniClient.prototype.update_supported_engines = function (args) {
  msgobj = {
    type: "analysis-engines"
  }
  var client = this;
  this.call(msgobj, function(self, reply) {
    client.analysis_engines = reply.engines;
  });

  return msgobj.id
}

/******************************************************************************
 * TODO untested
 *****************************************************************************/

JaniClient.prototype.update_supported_transformers = function (args) {
  msgobj = {
    type: "model-transformer"
  }
  var client = this;
  this.call(msgobj, function(self, reply) {
    client.model_transformers = reply.transformers;
  });

  return msgobj.id
}

JaniClient.prototype.start_analysis_task = function(args) {
  msgobj = {
    type: "analysis-start",
    engine: args.engine,
    model: args.model,
    parameters: this.params
  }

  // TODO check if args.engine is in supported_engines
  // TODO check if the role 'analyse' is supported by the current server
  var client = this;  
  this.call(msgobj, function(self, reply) {    
    if (self.analysis_ongoing[reply.id] == undefined) {
      self.analysis_ongoing[reply.id] = {result: null, logs: []};
    }    
    switch (reply.type) {
    case 'analysis-results':
      console.log(reply.results);
      self.analysis_ongoing[reply.id].result = reply.results;
      break;
    case 'analysis-message':
      window[reply.severity](reply.message);
      self.analysis_ongoing[reply.id].logs.push(reply.message);
      break;
    case 'analysis-end':      
      // the analysis process is terminated abnormally
      self.analysis_history[reply.id] = self.analysis_ongoing[reply.id];
      delete self.analysis_ongoing[reply.id];
      info('analysis is terminated.');
      break;
    }
  });
   
  return msgobj.id;
}

JaniClient.prototype.end_analysis_task = function(args) {
  msgobj = {
    type: "analysis-stop",
    id: args.id
  }

  this.call(msgobj, function(self, reply){
    info(reply);
  });

  return msgobj.id
}

/******************************************************************************
* Low level functions from now on
******************************************************************************/

JaniClient.prototype.send = function(msg) {
  if (this.ws.readyState == 3) {
    // connection failed, need to retry
    this.ws = CreateWebSocket(this);
    return;
  }
  // debug("[sendmsg] " + msg);  
  this.ws.send(msg);
}

JaniClient.prototype.call = function(obj, callback) {
  obj.id = this.msgcounter;
  this.msgcounter ++;

  if (callback != undefined) {
    this.callback[obj.id] = callback;
  }
  this.send(JSON.stringify(obj));
}

// definition of web sockets
function CreateWebSocket(client) {
  var ws = new WebSocket(DEFAULT_WS_URL);
  ws.onopen = function() {
    info("websocket opened.");
    // start Execution
    client.execOnce();
  }
  ws.onmessage = function(evt) {
    var obj = JSON.parse(evt.data);
    // debug(evt.data);
    if (obj.type == "close") {
      warning("server is closing because " + obj.reason);
    } else {
      // if the server returns a non-closing message and the client hasn't been
      // authorized yet, it means that authorization finishes
      if (client.status == "unauthorized") {
        client.status = "authorized";
        client.params = obj.parameters;
        // initalizing the parameters
        for (var i in client.params) {
          client.params[i].value = client.params[i]["default-value"];
        }
        client.name = obj.name;
        client.roles = obj.roles;
        console.log(obj);

        // authorization finished, start working
        client.execOnce();
      } else {
        // interactive state        
        if (obj.id in client.callback) {
          func = client.callback[obj.id];
          func(client, obj);
          if (obj.id == client.block) {
            client.execOnce();
          }
          // TODO since task messsage requires multiple responses 
          // delete client.callback[obj.id];
        } else {
          console.log(obj);
        }
      }
    }
  }
  ws.onclose = function () {
    error("websocket closed.");
    client.status = "unauthorized";
    client.callback = {};
  }
  ws.onerror = function(evt) {
    error("websocket error.")
    console.log(evt.data);
  }
  return ws;
}

/******************************** log functions ****************************/
// 对Date的扩展，将 Date 转化为指定格式的String
// 月(M)、日(d)、小时(h)、分(m)、秒(s)、季度(q) 可以用 1-2 个占位符，
// 年(y)可以用 1-4 个占位符，毫秒(S)只能用 1 个占位符(是 1-3 位的数字)
// 例子：
// (new Date()).Format("yyyy-MM-dd hh:mm:ss.S") ==> 2006-07-02 08:09:04.423
// (new Date()).Format("yyyy-M-d h:m:s.S")      ==> 2006-7-2 8:9:4.18
Date.prototype.Format = function(fmt)
{ //author: meizz
  var o = {
    "M+" : this.getMonth()+1,                 //月份
    "d+" : this.getDate(),                    //日
    "h+" : this.getHours(),                   //小时
    "m+" : this.getMinutes(),                 //分
    "s+" : this.getSeconds(),                 //秒
    "q+" : Math.floor((this.getMonth()+3)/3), //季度
    "S"  : this.getMilliseconds()             //毫秒
  };
  if(/(y+)/.test(fmt))
    fmt=fmt.replace(RegExp.$1, (this.getFullYear()+"").substr(4 - RegExp.$1.length));
  for(var k in o)
    if(new RegExp("("+ k +")").test(fmt))
  fmt = fmt.replace(RegExp.$1, (RegExp.$1.length==1) ? (o[k]) : (("00"+ o[k]).substr((""+ o[k]).length)));
  return fmt;
}

function debug(msg) {
  var dt = new Date();
  console.log(dt.Format("hh:mm:ss") + " [debug] " + msg);
}

function warning(msg) {
  var dt = new Date();
  console.log(dt.Format("hh:mm:ss") + " [ warn] " + msg);
}

function info(msg) {
  var dt = new Date();
  console.log(dt.Format("hh:mm:ss") + " [ info] " + msg);
}

function error(msg) {
  var dt = new Date();
  console.log(dt.Format("hh:mm:ss") + " [error] " + msg);
}

/************************************* login part ************************************************/
var conn = null;

function connect(username, password) {
  if (conn == null) {
    conn = new JaniClient();
  }  
}

function restoreLogin() {
  if ("uid" in window.sessionStorage && "pwd" in window.sessionStorage) {
    // there's a previous login
    login(window.sessionStorage.uid, window.sessionStorage.pwd);
  }
}

// TODO remove this debug code
window.sessionStorage.uid = null;
window.sessionStorage.pwd = null;

$(document).ready(function () {
  connect();
  info('Waiting for Connection ...');
});

function test() {
  conn.exec('login', {});
  conn.exec('update_supported_engines', {}, true);
  conn.exec('start_analysis_task', {engine: "explorer", model: testmodel});
}

/************************************* testing data *******************************************/
testmodel =
{
	"jani-version": 1,
	"name": "cell",
	"type" : "ctmc",
	"actions" : [],
	"variables" : [],
	"rewards" : [],
	"properties" : [],
	"automata" : [
		{
			"name" : "asdf",
			"variables" : [
				{
					"name" : "n",
					"type" : {
						"kind": "bounded",
						"base": "int",
						"lower-bound" : 0,
						"upper-bound" : 6
					},
					"initial-value" : 0
				}
			],
			"locations" : [
				{
					"name" : "asdf"
				}
			],
			"initial-locations" : ["asdf"],
			"edges" : [
				{
					"location" : "asdf",
					"rate" : {
            "exp": 49
          },
					"guard" : {
            "exp": {
              "op" : "<",
              "args" : [
                "n",
                {
                  "op" : "*",
                  "args" : [
                    6,
                    0.8
                  ]
                }
              ]
            }
          },
					"destinations" : [
						{
							"probability" : {
                "exp": 1
              },
							"location" : "asdf",
							"assignments" : [
								{
									"ref" : "n",
									"value" : {
										"op" : "+",
										"args" : [
											"n",
											1
										]
									}
								}
							]
						}
					]
				},
				{
					"location" : "asdf",
					"rate" : {
            "exp": 21
          },
					"guard" : {
            "exp": {
              "op" : "<",
              "args" : [
                "n",
                6
              ]
            }
          },
					"destinations" : [
						{
							"probability" : {
                "exp": 1
              },
							"location" : "asdf",
							"assignments" : [
								{
									"ref" : "n",
									"value" : {
										"op" : "+",
										"args" : [
											"n",
											1
										]
									}
								}
							]
						}
					]
				},
				{
					"location" : "asdf",
					"rate" : {
            "exp": 1
          },
					"guard" : {
            "exp": {
              "op" : "<",
              "args" : [
                "n",
                0
              ]
            }
          },
					"destinations" : [
						{
							"probability" : {
                "exp": 1
              },
							"location" : "asdf",
							"assignments" : [
								{
									"ref" : "n",
									"value" : {
										"op" : "-",
										"args" : [
											"n",
											1
										]
									}
								}
							]
						}
					]
				}
			]
		}
	],
	"system" : {
    "elements": [
      {
        "automaton": "asdf"
      }
    ],
    "syncs": [
      {
        "synchronise": [ null ]
      }
    ]
  }
}
