var global_Y;
var vsedit_modelid = null;

var availableFields = [
    {
        iconClass: 'diagram-node-task-icon',
        label: 'Location',
        type: 'location'
    },
    {
        iconClass: 'diagram-node-state-icon',
        label: 'Label',
        type: 'label'
    }
];

var CustomConnector = function () {
};

function setJANI(content, id) {
    var builder = document.getElementById("diagramBuilder");
    builder.removeChild(builder.childNodes[0]);

    automata = eval("(" + content + ")");
    vsedit_modelid = id;
    refresh_JANI();
}

function getJANI() {
    var object = diagramBuilder.toJSON();

    console.log(object);

    var locations = [];
    var edges = [];

    object.nodes.forEach(function (node) {
        if (node.type == "location") {
            locations.push({
                name: node.name,
                invariants: node.invariants == "" ? null : C2A(node.invariants),
                x: node.xy[0],
                y: node.xy[1]
            });
            node.transitions.forEach(function (transition) {
                if (transition.target.split("_")[0] == "label") {
                    var pos = edges.map(function (e) {
                        return e.label
                    }).indexOf(transition.target);
                    if (pos == -1) {
                        edges.push({
                            label: transition.target,
                            location: node.name,
                            sourceXY: transition.sourceXY,
                            targetXY: transition.targetXY,
                            action: transition.connector.action,
                            rate: transition.connector.rate == "" ? null : C2A(transition.connector.rate),
                            guard: transition.connector.guard == "" ? null : C2A(transition.connector.guard),
                            destinations: []
                        });
                    }
                    else {
                        edges[pos].location = node.name;
                        edges[pos].sourceXY = transition.sourceXY;
                        edges[pos].targetXY = transition.targetXY;
                        edges[pos].action = transition.connector.action;
                        edges[pos].rate = transition.connector.rate == "" ? null : C2A(transition.connector.rate);
                        edges[pos].guard = transition.connector.guard == "" ? null : C2A(transition.connector.guard);
                        edges[pos].destinations = [];
                    }
                }
                else {
                    edges.push({
                        location: node.name,
                        action: transition.connector.action,
                        rate: transition.connector.rate == "" ? null : C2A(transition.connector.rate),
                        guard: transition.connector.guard == "" ? null : C2A(transition.connector.guard),
                        destinations: [{
                            location: transition.target,
                            sourceXY: transition.sourceXY,
                            targetXY: transition.targetXY,
                            probability: transition.connector.probability == "" ? null : C2A(transition.connector.probability),
                            assignments: transition.connector.assignments == "" ? null : transition.connector.assignments.split(",").map(function (e) {
                                return {ref: e.split("' =")[0], value: C2A(e.split("' =")[1])};
                            }),
                            rewards: transition.connector.rewards == "" ? null : transition.connector.rewards.split(",").map(function (e) {
                                return {ref: e.split("' =")[0], value: C2A(e.split("' =")[1])};
                            })
                        }]
                    });
                }
            });
        }
        else if (node.type == "label") {
            node.transitions.forEach(function (transition) {
                var pos = edges.map(function (e) {
                    return e.label
                }).indexOf(transition.source);
                if (pos == -1) {
                    edges.push({
                        label: transition.source,
                        x: node.xy[0],
                        y: node.xy[1],
                        destinations: [{
                            location: transition.target,
                            sourceXY: transition.sourceXY,
                            targetXY: transition.targetXY,
                            probability: transition.connector.probability == "" ? null : C2A(transition.connector.probability),
                            assignments: transition.connector.assignments == "" ? null : transition.connector.assignments.split(",").map(function (e) {
                                return {ref: e.split("' =")[0], value: C2A(e.split("' =")[1])};
                            }),
                            rewards: transition.connector.rewards == "" ? null : transition.connector.rewards.split(",").map(function (e) {
                                return {ref: e.split("' =")[0], value: C2A(e.split("' =")[1])};
                            })
                        }]
                    });
                }
                else {
                    edges[pos].x = node.xy[0];
                    edges[pos].y = node.xy[1];
                    edges[pos].destinations.push({
                        location: transition.target,
                        sourceXY: transition.sourceXY,
                        targetXY: transition.targetXY,
                        probability: transition.connector.probability == "" ? null : C2A(transition.connector.probability),
                        assignments: transition.connector.assignments == "" ? null : transition.connector.assignments.split(",").map(function (e) {
                            return {ref: e.split("' =")[0], value: C2A(e.split("' =")[1])};
                        }),
                        rewards: transition.connector.rewards == "" ? null : transition.connector.rewards.split(",").map(function (e) {
                            return {ref: e.split("' =")[0], value: C2A(e.split("' =")[1])};
                        })
                    });
                }
            });
        }
    });

    edges.forEach(function (edge) {
        edge.label = null;
    });

    automata = {locations: locations, edges: edges};

    return JSON.stringify(automata, null, "\t");
}

function clearJANI() {
    var builder = document.getElementById("diagramBuilder");
    builder.removeChild(builder.childNodes[0]);
    diagramBuilder = new global_Y.DiagramBuilder({
        availableFields: availableFields,
        boundingBox: '#diagramBuilder',
        connector: CustomConnector,
        fields: [],
        render: true
    }).render();

    diagramBuilder.connectAll([]);
}

function refresh_JANI() {
    var locations = automata.locations;
    var edges = automata.edges;

    var fields = [];
    var connectors = [];

    locations.forEach(function (location) {
        fields.push({
            name: location.name,
            type: "location",
            invariants: location.invariants == null ? "" : A2C(location.invariants),
            xy: [location.x, location.y]
        });
    });

    var label_index = 0;
    var link_index = 0;

    edges.forEach(function (edge) {

        if (edge.destinations.length == 1) {
            connectors.push({
                connector: {
                    type: 3,
                    name: "link_" + link_index,
                    action: edge.action == null ? "" : edge.action,
                    rate: A2C(edge.rate),
                    guard: A2C(edge.guard),
                    probability: A2C(edge.destinations[0].probability),
                    assignments: edge.destinations[0].assignments == null ? "" : edge.destinations[0].assignments.map(function (e) {
                        return e.ref + "' = " + A2C(e.value)
                    }).toString(),
                    rewards: edge.destinations[0].rewards == null ? "" : edge.destinations[0].rewards.map(function (e) {
                        return e.ref + "' = " + A2C(e.value)
                    }).toString()
                },
                source: edge.location,
                target: edge.destinations[0].location,
                sourceXY: edge.destinations[0].sourceXY,
                targetXY: edge.destinations[0].targetXY
            });
            ++link_index;
            return;
        }

        edge.name = "label_" + label_index;
        ++label_index;
        fields.push({
            name: edge.name,
            type: "label",
            xy: [edge.x, edge.y]
        });

        connectors.push({
            connector: {
                type: 1,
                name: "link_" + link_index,
                action: edge.action == null ? "" : edge.action,
                rate: A2C(edge.rate),
                guard: A2C(edge.guard)
            },
            source: edge.location,
            target: edge.name,
            sourceXY: edge.sourceXY,
            targetXY: edge.targetXY
        });
        ++link_index;

        edge.destinations.forEach(function (destination) {
            connectors.push({
                connector: {
                    type: 2,
                    name: "link_" + link_index,
                    probability: A2C(destination.probability),
                    assignments: destination.assignments == null ? "" : destination.assignments.map(function (e) {
                        return e.ref + "' = " + A2C(e.value)
                    }).toString(),
                    rewards: destination.rewards == null ? "" : destination.rewards.map(function (e) {
                        return e.ref + "' = " + A2C(e.value)
                    }).toString()
                },
                source: edge.name,
                target: destination.location,
                sourceXY: destination.sourceXY,
                targetXY: destination.targetXY
            });
            ++link_index;
        });
    });

    diagramBuilder = new global_Y.DiagramBuilder({
        availableFields: availableFields,
        boundingBox: '#diagramBuilder',
        connector: CustomConnector,
        fields: fields,
        render: true
    }).render();

    diagramBuilder.connectAll(connectors);
}

var automata;

// and/or &&/|| are both supported instead of symbols
// -- is not supported
// ceil floor are supported instead of symbols

function A2B(object) {
    if (object == null)
        return;
    if (object.op != null) {
        if (object.op == "?:") {
            var args = object.args;
            if (args.length != 3) {
                console.log("wrong");
                return;
            }
            return {
                type: "ConditionalExpression",
                test: A2B(args[0]),
                consequent: A2B(args[1]),
                alternate: A2B(args[2]),
                operator: object.op
            };
        }
        else if (object.op == "!") {
            var args = object.args;
            if (args.length != 1) {
                console.log("wrong");
                return;
            }
            return {
                type: "UnaryExpression",
                prefix: true,
                argument: A2B(args[0]),
                operator: object.op
            };
        }
        else if (object.op == "||" ||
            object.op == "&&" ||
            object.op == "or" ||
            object.op == "and") {
            var args = object.args;
            if (args.length != 2) {
                console.log("wrong");
                return;
            }
            if (object.op == "or")
                object.op = "||";
            if (object.op == "and")
                object.op = "&&";
            return {
                type: "LogicalExpression",
                left: A2B(args[0]),
                right: A2B(args[1]),
                operator: object.op
            };
        }
        else if (object.op == "=") {
            var args = object.args;
            if (args.length != 2) {
                console.log("wrong");
                return;
            }
            return {
                type: "AssignmentExpression",
                left: A2B(args[0]),
                right: A2B(args[1]),
                operator: object.op
            };
        }
        else if (object.op == "<" ||
            object.op == "<=" ||
            object.op == ">" ||
            object.op == ">=" ||
            object.op == "+" ||
            object.op == "-" ||
            object.op == "*" ||
            object.op == "/" ||
            object.op == "%") {
            var args = object.args;
            if (args.length != 2) {
                console.log("wrong");
                return;
            }
            return {
                type: "BinaryExpression",
                left: A2B(args[0]),
                right: A2B(args[1]),
                operator: object.op
            };
        }
        else if (object.op == "min" ||
            object.op == "max" ||
            object.op == "abs" ||
            object.op == "sgn" ||
            object.op == "trc" ||
            object.op == "ceil" ||
            object.op == "floor" ||
            object.op == "pow" ||
            object.op == "exp" ||
            object.op == "log") {
            return {
                type: "CallExpression",
                callee: {name: object.op, type: "Identifier"},
                arguments: object.args.map(function (e) {
                    return A2B(e);
                })
            };
        }

    }
    else if (typeof(object) == "string") {
        return {name: object, type: "Identifier"};
    }
    else if (typeof(object) == "number" || typeof(object) == "boolean") {
        return {value: object, type: "Literal", raw: object.toString()};
    }
    else {
        console.log("sth wrong");
    }
}

function B2A(object) {
    if (object.type == "Literal") {
        return object.value;
    }
    else if (object.type == "Identifier") {
        return object.name;
    }
    else if (object.type == "ConditionalExpression") {
        return {
            op: "?:",
            args: [B2A(object.test),
                B2A(object.consequent),
                B2A(object.alternate)]
        };
    }
    else if (object.type == "UnaryExpression") {
        return {
            op: object.operator,
            args: [B2A(object.argument)]
        };
    }
    else if (object.type == "LogicalExpression" ||
        object.type == "AssignmentExpression" ||
        object.type == "BinaryExpression") {
        var op = object.operator;
        if (op == "&&")
            op = "and";
        if (op == "||")
            op = "or";
        return {
            op: op,
            args: [B2A(object.left),
                B2A(object.right)]
        };
    }
    else if (object.type == "CallExpression") {
        return {
            op: object.callee.name,
            args: object.arguments.map(function (e) {
                return B2A(e);
            })
        };
    }
    else {
        console.log("sth wrong");
    }
}

function A2C(object) {
    if ((m = A2B(object)) == null)
        return "";
    var str = escodegen.generate(m);
    str = str.replace("||", "or");
    str = str.replace("&&", "and");
    return str;
}
function C2A(string) {
    var str = string;
    str = str.replace("and", "&&");
    str = str.replace("or", "||");
    return B2A(esprima.parse(str).body[0].expression);
}

// console.log(C2A("r=min(1,2,pow(!c,d or 2))"));
// console.log(A2C(C2A("r=min(1,2,pow(!c,d or 2))")));


YUI({filter: 'raw'}).use('aui-diagram-builder', function (Y) {

    global_Y = Y;

    Y.DiagramNodeLocation = Y.Component.create({

        NAME: 'diagram-node',

        ATTRS: {
            type: {
                value: 'location'
            },
            // width:{
            //   value: 60
            // },
            // height:{
            //   value: 60
            // },
            invariants: {
                validator: Y.Lang.isString,
                value: ''
            }
        },

        EXTENDS: Y.DiagramNodeTask,

        prototype: {

            initializer: function () {
                this.SERIALIZABLE_ATTRS.push('invariants');

                var instance = this;

                instance.on({
                    nameChange: instance._onNameChange,
                    invariantsChange: instance._onInvariantsChange
                });
            },


            getPropertyModel: function () {
                var instance = this;

                var model = Y.DiagramNodeTask.superclass.getPropertyModel.apply(instance, arguments);

                var pos = model.map(function (e) {
                    return e.name;
                }).indexOf("Description");

                model.splice(pos, 1);


                var pos = model.map(function (e) {
                    return e.name;
                }).indexOf("Type");

                model.splice(pos, 1);

                model.push({
                    attributeName: 'invariants',
                    name: 'Invariants'
                });

                return model;
            },

            _afterRender: function () {
                var instance = this;

                instance.setStdModContent(Y.WidgetStdMod.BODY, '', Y.WidgetStdMod.AFTER);
                instance._renderGraphic();
                instance._renderControls();
                instance._renderLabel();
                instance._uiSetHighlighted(instance.get('highlighted'));
            },

            _renderLabel: function () {
                var instance = this;

                instance.labelNode = Y.Node.create(
                    Y.Lang.sub(instance.LABEL_TEMPLATE, {
                        label: (instance.get('name') + "<br>" + instance.get('invariants') )
                    })
                );

                instance.get('contentBox').prepend(instance.labelNode);
            },

            _uiSetName: function (val) {
                var instance = this;
                var boundingBox = instance.get('boundingBox');

                boundingBox.setAttribute('data-nodeId', Y.Escape.html(Y.DiagramNode.buildNodeId(val)));

                if (instance.get('rendered')) {
                    instance.labelNode.setContent(val + "<br>" + instance.get('invariants'));
                }
            },

            _onInvariantsChange: function (event) {
                var instance = this;
                instance.labelNode.setContent(instance.get('name') + "<br>" + event.newVal);
            },
            connectOverTarget: function () {
                var instance = this;
                var builder = instance.get('builder');


                builder.publishedTarget = instance;

            }

        }
    });

    Y.DiagramBuilder.types['location'] = Y.DiagramNodeLocation;


    Y.DiagramNodeLabel = Y.Component.create({

        NAME: 'diagram-node',

        ATTRS: {
            type: {
                value: 'label'
            }
        },

        EXTENDS: Y.DiagramNodeState,

        prototype: {

            initializer: function () {

                var instance = this;

                instance.on({
                    nameChange: instance._onNameChange
                });
            },


            getPropertyModel: function () {
                var instance = this;

                var model = Y.DiagramNodeTask.superclass.getPropertyModel.apply(instance, arguments);

                var pos = model.map(function (e) {
                    return e.name;
                }).indexOf("Description");

                model.splice(pos, 1);


                var pos = model.map(function (e) {
                    return e.name;
                }).indexOf("Type");

                model.splice(pos, 1);

                var pos = model.map(function (e) {
                    return e.name;
                }).indexOf("Name");

                model.splice(pos, 1);

                return model;
            },

            _afterRender: function () {
                var instance = this;

                instance.setStdModContent(Y.WidgetStdMod.BODY, '', Y.WidgetStdMod.AFTER);
                instance._renderGraphic();
                instance._renderControls();
                instance._renderLabel();
                instance._uiSetHighlighted(instance.get('highlighted'));
            },

            _renderLabel: function () {
                var instance = this;

                instance.labelNode = Y.Node.create(
                    Y.Lang.sub(instance.LABEL_TEMPLATE, {
                        label: ("")
                    })
                );

                instance.get('contentBox').prepend(instance.labelNode);
            },

            _uiSetName: function (val) {
                var instance = this;
                var boundingBox = instance.get('boundingBox');

                boundingBox.setAttribute('data-nodeId', Y.Escape.html(Y.DiagramNode.buildNodeId(val)));

                if (instance.get('rendered')) {
                    instance.labelNode.setContent("");
                }
            }


        }
    });

    Y.DiagramBuilder.types['label'] = Y.DiagramNodeLabel;


///           connectors


    CustomConnector.ATTRS = {
        type: {
            valueFn: function () {
                return '';
            }
        },
        action: {
            valueFn: function () {
                return '';
            }
        },
        rate: {
            valueFn: function () {
                return '';
            }
        },
        guard: {
            valueFn: function () {
                return '';
            }
        },
        probability: {
            valueFn: function () {
                return '';
            }
        },
        assignments: {
            valueFn: function () {
                return '';
            }
        },
        rewards: {
            valueFn: function () {
                return '';
            }
        }
    };

    CustomConnector.prototype.initializer = function () {
        var instance = this;

        var attrs = ["type", "action", "rate", "guard", "probability", "assignments", "rewards"];

        attrs.forEach(function (attr) {
            if (instance.SERIALIZABLE_ATTRS.indexOf(attr) == -1)

                instance.SERIALIZABLE_ATTRS.push(attr);
        });


        instance.after({
            nameChange: instance._afterNameChange,
            typeChange: instance._afterChange,
            actionChange: instance._afterChange,
            rateChange: instance._afterChange,
            guardChange: instance._afterChange,
            probabilityChange: instance._afterChange,
            assignmentsChange: instance._afterChange,
            rewardsChange: instance._afterChange,
            p1Change: instance.draw,
            p2Change: instance.draw,
            selectedChange: instance._afterSelectedChange,
            showNameChange: instance._afterShowNameChange,
            visibleChange: instance._afterVisibleChange
        });


        if (instance.get('type') == 1) {
            // from location to fork
            var str = "";
            var attrs = ["action", "rate", "guard"];
            attrs.forEach(function (attr) {
                var name = instance.get(attr);
                if (name != "") {
                    if (str != "") {
                        str += "<br>";
                    }
                    str += attr;
                    str += ": ";
                    str += name;
                }
            });
            if (str == "")
                str = "null";
            instance._uiSetName(str);
        }

        else if (instance.get('type') == 2) {
            // from fork to location
            var str = "";
            var attrs = ["probability", "assignments", "rewards"];
            attrs.forEach(function (attr) {
                var name = instance.get(attr);
                if (name != "") {
                    if (str != "") {
                        str += "<br>";
                    }
                    str += attr;
                    str += ": ";
                    str += name;
                }
            });
            if (str == "")
                str = "null";
            instance._uiSetName(str);
        }

        else if (instance.get('type') == 3) {
            var str = "";
            var attrs = ["action", "rate", "guard", "probability", "assignments", "rewards"];
            attrs.forEach(function (attr) {
                var name = instance.get(attr);
                if (name != "") {
                    if (str != "") {
                        str += "<br>";
                    }
                    str += attr;
                    str += ": ";
                    str += name;
                }
            });
            if (str == "")
                str = "null";
            instance._uiSetName(str);
        }
        else {
            instance._uiSetName("null");
        }


    };


    // CustomConnector.prototype._afterNameChange = function(event) {

    //   var instance = this;

    //   instance._uiSetName(event.newVal + "<br>" + instance.get('guard'));

    //   console.log(event.newVal);

    //   instance.draw();

    // };

    CustomConnector.prototype._afterChange = function (event) {

        var instance = this;

        if (instance.get('type') == 1) {
            // from location to fork
            var str = "";
            var attrs = ["action", "rate", "guard"];
            attrs.forEach(function (attr) {
                var name = instance.get(attr);
                if (name != "") {
                    if (str != "") {
                        str += "<br>";
                    }
                    str += attr;
                    str += ": ";
                    str += name;
                }
            });
            if (str == "")
                str = "null";
            instance._uiSetName(str);
        }

        else if (instance.get('type') == 2) {
            // from fork to location
            var str = "";
            var attrs = ["probability", "assignments", "rewards"];
            attrs.forEach(function (attr) {
                var name = instance.get(attr);
                if (name != "") {
                    if (str != "") {
                        str += "<br>";
                    }
                    str += attr;
                    str += ": ";
                    str += name;
                }
            });
            if (str == "")
                str = "null";
            instance._uiSetName(str);
        }

        else if (instance.get('type') == 3) {
            var str = "";
            var attrs = ["action", "rate", "guard", "probability", "assignments", "rewards"];
            attrs.forEach(function (attr) {
                var name = instance.get(attr);
                if (name != "") {
                    if (str != "") {
                        str += "<br>";
                    }
                    str += attr;
                    str += ": ";
                    str += name;
                }
            });
            if (str == "")
                str = "null";
            instance._uiSetName(str);
        }
        else {
            instance._uiSetName("null");
        }

        instance.draw();

    };

    CustomConnector.prototype._uiSetName = function (val) {
        var instance = this;

        instance.get('nodeName').html(val);
    };


    CustomConnector.prototype.getPropertyModel = function () {
        var instance = this;

        if (instance.get('type') == 1) {
            console.log("sss");
            return [
                {
                    attributeName: 'type',
                    editor: new Y.TextCellEditor(),
                    name: 'Type'
                },
                {
                    attributeName: 'action',
                    editor: new Y.TextCellEditor(),
                    name: 'Action'
                },
                {
                    attributeName: 'rate',
                    editor: new Y.TextCellEditor(),
                    name: 'Rate'
                },
                {
                    attributeName: 'guard',
                    editor: new Y.TextCellEditor(),
                    name: 'Guard'
                }
            ];
        }

        else if (instance.get('type') == 2) {
            return [
                {
                    attributeName: 'type',
                    editor: new Y.TextCellEditor(),
                    name: 'Type'
                },
                {
                    attributeName: 'probability',
                    editor: new Y.TextCellEditor(),
                    name: 'Probability'
                },
                {
                    attributeName: 'assignments',
                    editor: new Y.TextCellEditor(),
                    name: 'Assignments'
                },
                {
                    attributeName: 'rewards',
                    editor: new Y.TextCellEditor(),
                    name: 'Rewards'
                }
            ];
        }
        else if (instance.get('type') == 3) {
            return [
                {
                    attributeName: 'type',
                    editor: new Y.TextCellEditor(),
                    name: 'Type'
                },
                {
                    attributeName: 'action',
                    editor: new Y.TextCellEditor(),
                    name: 'Action'
                },
                {
                    attributeName: 'rate',
                    editor: new Y.TextCellEditor(),
                    name: 'Rate'
                },
                {
                    attributeName: 'guard',
                    editor: new Y.TextCellEditor(),
                    name: 'Guard'
                },
                {
                    attributeName: 'probability',
                    editor: new Y.TextCellEditor(),
                    name: 'Probability'
                },
                {
                    attributeName: 'assignments',
                    editor: new Y.TextCellEditor(),
                    name: 'Assignments'
                },
                {
                    attributeName: 'rewards',
                    editor: new Y.TextCellEditor(),
                    name: 'Rewards'
                }
            ];
        }
        else {
            return [
                {
                    attributeName: 'type',
                    editor: new Y.TextCellEditor(),
                    name: 'Type'
                }
            ];
        }

    };

    Y.Base.mix(Y.Connector, [CustomConnector]);


    var diagramBuilder = new Y.DiagramBuilder({
        availableFields: availableFields,
        boundingBox: '#diagramBuilder',
        connector: CustomConnector,
    }).render();


});
