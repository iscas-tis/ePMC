function init(divname) {
	var canvas_div = document.getElementById(divname);
	// FIXME path should be gained automatically

  var path = 'plugins/janigraph/';
	canvas_div.innerHTML = [
  '  <div id = "right-menu"> ',
  '    <img id="menu-select" title="select or move" class="menu-item" src="' + path + 'images/select.png" />',
  '    <img id="menu-location" title="add location" class="menu-item" src="' + path + 'images/location.png" />',
  '    <img id="menu-edge" title="add edge" class="menu-item" src="' + path + 'images/edge.png" />',
  '    <img id="menu-nail" title="add nail on edge" class="menu-item" src="' + path + 'images/nail.png" />',
  '  </div>',
  '  <div id="div_canvas">',
  '    <div id="tab_automata">',
  '      <table border="0" cellspacing="0" cellpadding="0" style="height:20px;" id="table_automata">',
  '        <tr>',
  '          <td class="a1" id = "tab-automaton-show" ></td>',
  '          <td class="a2" id = "tab-automaton-create" title="create new automaton">+</td>',
  '        </tr>',
  '        </table>',
  '    </div>',
  '    <canvas id="canvas">',
  '      <span>Your browser does not support<br/>the HTML5 &lt;canvas&gt;element</span>',
  '    </canvas>',
  '  </div>'].join('\n');

  var html_dialogs = [
  '<div id="editLocation" title="Edit Location">',
  '  <div id="color-choose-location" style="position:absolute;z-index: 10000;left: 40px;top:2px;box-shadow: 0 0 10px gray;"></div>',
  '  Name:&nbsp;&nbsp;<input id="locationName" style="width:250px;margin-top:5px;margin-bottom:5px;" type="text"/><br/>',
  '  Invariant:<br/>',
  '  <textarea id="locationInvariant" rows="4" style="width:310px;margin-bottom:5px;"></textarea><br/>',
  '  Initial<input type="checkbox" id = "isInitial" value="checkbox1"/><br/>',
  '  Color:&nbsp;&nbsp;<input style="width:250px;margin-top: 5px;" id="location-color" value="000000" onclick="showColorChoose(0);"><br/><br/>',
  '  Remark:<br/>',
  '  <textarea id="locationRemark" rows="3" style="width:310px;"></textarea>',
  '</div>',
  '<div id="editEdge"  title="Edit Edge">',
  '  <div id="color-choose-edge" style="position:absolute;z-index: 10000;left: 40px;top:2px;box-shadow: 0 0 10px gray;"></div>',
  '  label:<br/><textarea id="label" style="height:30px;width:310px;" ></textarea><br/>',
  '  rate:<br/><textarea id="rate" style="height:30px;width:310px;" ></textarea><br/>',
  '  guard:<br/><textarea id="guard" style="height:30px;width:310px;"></textarea><br/>',
  '  probability:<br/><textarea id="probability" style="height:30px;width:310px;" ></textarea><br/>',
  '  Color:<input style="margin-top:5px;margin-bottom:10px;width:310px;" id="edge-color" value="000000" onclick="showColorChoose(1);">',
  '  Remark:<br/>',
  '  <textarea id="edgeRemark" style="height:30px;width:310px;"></textarea>',
  '</div>',
  '<div id="edit_automaton"  title="Edit Automaton">',
  '  Automaton Name: <input id="automaton_name"/><br/>',
  '  <button id="delete_automaton"  style="width:200px;height:30px;margin-left: 0px;margin-top: 20px;">delete this automaton</button>',
  '</div>'].join("\n");

  canvas_div.innerHTML += html_dialogs;
	
	var myGraph = new MyGraph(document.getElementById('canvas'),canvas_div);
	// document.getElementById('LeftToRight').onclick = function(){myGraph.codeToGraph();};
	// document.getElementById('RightToLeft').onclick = function(){myGraph.graphToCode();};
	document.getElementById('menu-select').onclick = function(){myGraph.setType(1);};
	document.getElementById('menu-location').onclick = function(){myGraph.setType(2);};
	document.getElementById('menu-edge').onclick = function(){myGraph.setType(3);};
	document.getElementById('menu-nail').onclick = function(){myGraph.setType(4);};
	document.getElementById('menu-select').onmouseout = function(){myGraph.mouse_out('menu-select');};
	document.getElementById('menu-location').onmouseout = function(){myGraph.mouse_out('menu-location');};
	document.getElementById('menu-edge').onmouseout = function(){myGraph.mouse_out('menu-edge');};
	document.getElementById('menu-nail').onmouseout = function(){myGraph.mouse_out('menu-nail');};
	document.getElementById('menu-select').onmouseover = function(){myGraph.mouse_on_menu('menu-select');};
	document.getElementById('menu-location').onmouseover = function(){myGraph.mouse_on_menu('menu-location');};
	document.getElementById('menu-edge').onmouseover = function(){myGraph.mouse_on_menu('menu-edge');};
	document.getElementById('menu-nail').onmouseover = function(){myGraph.mouse_on_menu('menu-nail');};
	document.getElementById('tab-automaton-show').onclick = function(){myGraph.showSelectedAutomaton(0);};
	document.getElementById('tab-automaton-show').ondblclick = function(){myGraph.editAutomatonName(document.getElementById('tab-automaton-show'));};
	document.getElementById('tab-automaton-create').onclick = function(){myGraph.createNewAutomaton();};
	document.getElementById('delete_automaton').onclick = function(){myGraph.automaton_delete();};
	document.getElementById('location-color').onclick = function(){myGraph.showColorChoose(0);};
	document.getElementById('edge-color').onclick = function(){myGraph.showColorChoose(1);};
	
	$( "#editLocation" ).dialog({
	autoOpen: false,
	width: 350,
	modal: true,
	buttons: [
		{
			text: "Ok",
			click: function() {
				myGraph.location_ok();
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
	
	$( "#editEdge" ).dialog({
	autoOpen: false,
	width: 350,
	modal: true,
	buttons: [
		{
			text: "Ok",
			click: function() {
				myGraph.edge_ok();
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
	
	$( "#edit_automaton" ).dialog({
	autoOpen: false,
	width: 350,
	modal: true,
	buttons: [
		{
			text: "Ok",
			click: function() {
				myGraph.automaton_ok();
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
	
	document.getElementById('div_canvas').style.width = Math.round($(canvas_div).width());
	document.getElementById('div_canvas').style.height =  Math.round($(canvas_div).height()-35);
	document.getElementById('canvas').width = Math.round($(canvas_div).width());
	document.getElementById('canvas').height =  Math.round($(canvas_div).height()-65);
	
	myGraph.z_location = new dhtmlXColorPicker(path,"color-choose-location",false,true);
	myGraph.z_location.init();
	myGraph.z_location.setCustomColors("#000000,#777777,#aaaaaa,#ff0000,#ff7f00,#ffff00,#00ff00,#00ffff,#0000ff,#8b00ff");
	myGraph.z_location.setOnSelectHandler(setColor_location);
	myGraph.z_location.hide();
	function setColor_location(color)
	{
		document.getElementById("location-color").value = color;
		document.getElementById("location-color").style.backgroundColor = color;
	}
	
	myGraph.z_edge = new dhtmlXColorPicker(path,"color-choose-edge",false,true);
	myGraph.z_edge.init();
	myGraph.z_edge.setCustomColors("#000000,#777777,#aaaaaa,#ff0000,#ff7f00,#ffff00,#00ff00,#00ffff,#0000ff,#8b00ff");
	myGraph.z_edge.setOnSelectHandler(setColor_edge);
	myGraph.z_edge.hide();
	function setColor_edge(color)
	{
		document.getElementById("edge-color").value = color;
		document.getElementById("edge-color").style.backgroundColor = color;
	}
}

function MyGraph(cnvs,canvas_div)
{
	this.canvas = cnvs;
	this.canvas.tabIndex = 2367;
	canvas.style.outline = "none";
	this.canvas_div = canvas_div;
	
	this.c = canvas.getContext("2d");
	this.font = '16px "Times New Roman", serif';
	this.c.font = this.font;
	
	this.model = new Model();
	this.automaton = new Automaton();
	this.model.automata.push(this.automaton);
	this.nowAutomatonIndex = 0;
	this.locations = this.model.automata[this.nowAutomatonIndex].locations;
	this.edges = this.model.automata[this.nowAutomatonIndex].edges;
	this.variables = this.model.automata[this.nowAutomatonIndex].variables;

	this.currentObject = null;
	this.previousObject = null;
	this.currentType = 1;
	this.nailRadius = 5;
	this.selectedColor = "#ffd700";
	this.defaultColor = '#000000';
	this.currentEdge = null;
	this.isMoving = false;
	this.isAddingEdge = false;
	
	this.selectedMenu = "menu-select";
	this.preWidth = 0;
	this.preHeight = 0;
	this.newWidth = 0;
	this.newHeight = 0;
	this.automatonElement = null;	
	this.r = Math.round($(this.canvas_div).width() * 0.35);

	this.z_location = null;
	this.z_edge = null;
	
	this.addListeners(this.canvas);
}

MyGraph.prototype.showColorChoose = function(n)
{
	if(n==0)
		this.z_location.show();
	else
		this.z_edge.show();
}

MyGraph.prototype.mouse_on_menu = function(id_str)
{
	if(id_str != this.selectedMenu)
	{
		var menu = document.getElementById(id_str);
		menu.style.border = "1px solid #dddddd";
		menu.style.backgroundColor = "#cccccc";
	}
}

MyGraph.prototype.mouse_out = function(id_str)
{
	if(id_str != this.selectedMenu)
	{
		var menu = document.getElementById(id_str);
		menu.style.border = "1px solid #eeeeee";
		menu.style.backgroundColor = "#eeeeee";
	}
}

MyGraph.prototype.setType = function(t)
{
    this.currentType = t;

    var lastSelectedMenu = document.getElementById(this.selectedMenu);
    lastSelectedMenu.style.backgroundColor = "#eeeeee";
    lastSelectedMenu.style.border = "1px solid #eeeeee";

    var menus = document.getElementsByClassName("menu-item");
    menus[t-1].style.backgroundColor = "#bbbbbb";
    menus[t-1].style.border = "1px solid #999999";
    this.selectedMenu = menus[t-1].id;
}

MyGraph.prototype.addListeners = function(canvas)
{
	var myGraph = this;
    canvas.onmousedown = function(e) {
        e = e || window.event;
        var mouse = myGraph.getMousePosition(e);
        myGraph.currentObject = myGraph.getCurrentObject(mouse.x, mouse.y);
        myGraph.isMoving = false;

        switch(myGraph.currentType)
        {
            case 1:
                myGraph.isMoving = true;
                if (myGraph.currentObject != null && myGraph.currentObject.getD) {
                    myGraph.currentObject.getD(mouse.x, mouse.y);
                }
                break;
            case 2:
                myGraph.currentObject = new Location(mouse.x, mouse.y,myGraph);
                myGraph.locations.push(myGraph.currentObject);
                break;
            case 3:
                if(!myGraph.isAddingEdge)
                {
                    if (myGraph.currentObject instanceof Location)
                    {
                        myGraph.currentEdge = new TempEdge(myGraph.currentObject, mouse,new Array(),myGraph);
                        myGraph.previousObject = myGraph.currentObject;
                        myGraph.isAddingEdge = true;
                    }
                }
                break;
            case 4:
                if(myGraph.currentObject instanceof Edge)
                {
                    var l = myGraph.currentObject.findNail(mouse.x,mouse.y);
                    if(l != -1)
                    {
                        var nail = new Nail(mouse.x,mouse.y,myGraph);
                        myGraph.currentObject.nails.splice(l,0,nail);
                        myGraph.currentObject = nail;
                    }
                }
                break;
        }

        myGraph.draw();
    };

    canvas.ondblclick = function(e) {
        if(myGraph.currentType!=1)
            return;

        var mouse = myGraph.getMousePosition(e);
        myGraph.currentObject = myGraph.getCurrentObject(mouse.x, mouse.y);

        if (myGraph.currentObject instanceof Edge) {
            document.getElementById('label').value = myGraph.currentObject.label.content;
            document.getElementById('rate').value = myGraph.currentObject.rate.content;
            document.getElementById('guard').value = myGraph.currentObject.guard.content;
            document.getElementById('probability').value = myGraph.currentObject.probability.content;
            document.getElementById('edgeRemark').value = myGraph.currentObject.remark;
            document.getElementById('edge-color').value = myGraph.currentObject.showColor;
            document.getElementById('edge-color').style.backgroundColor = myGraph.currentObject.showColor;

            $( "#editEdge" ).dialog( "open" );
        } else if (myGraph.currentObject instanceof Location) {
            document.getElementById('locationName').value = myGraph.currentObject.name;
            document.getElementById('locationInvariant').value = myGraph.currentObject.invariant;
            document.getElementById('isInitial').checked = myGraph.currentObject.isInitial;
            document.getElementById('locationRemark').value = myGraph.currentObject.remark;
            document.getElementById('location-color').value = myGraph.currentObject.showColor;
            document.getElementById('location-color').style.backgroundColor = myGraph.currentObject.showColor;

			$( "#editLocation" ).dialog( "open" );
        }
    };

    canvas.onmousemove = function(e) {
        var mouse = myGraph.getMousePosition(e);

        if(myGraph.currentType == 3 && myGraph.isAddingEdge && myGraph.currentEdge != null)
        {
            myGraph.currentEdge.to = mouse;
            myGraph.draw();
        }

        if (myGraph.isMoving) {
            myGraph.currentObject.setFinalPosition(mouse.x, mouse.y);
            myGraph.draw();
        }
    };

    canvas.onmouseup = function(e) {
        myGraph.isMoving = false;

        if(myGraph.isAddingEdge && myGraph.currentType == 3)
        {
            var mouse = myGraph.getMousePosition(e);
            var selected = myGraph.getCurrentObject(mouse.x, mouse.y);

            if(selected == myGraph.previousObject && myGraph.currentEdge.nails.length == 0)
                return;

            if(selected instanceof  Location)
            {
                var nails = myGraph.currentEdge.nails;
                myGraph.currentEdge = new Edge(myGraph.previousObject, selected,nails,myGraph);
                myGraph.edges.push(myGraph.currentEdge);
                myGraph.currentEdge = null;
                myGraph.currentObject = null;
                myGraph.isAddingEdge = false;
                myGraph.draw();
            }
            else
            {
                var nail = new Nail(mouse.x,mouse.y,myGraph);
                myGraph.currentEdge.nails.push(nail);
                myGraph.draw();
            }
        }
    };
	
	canvas.onkeydown = function(e)
	{
		e = e || window.event;
		var key = e.which || e.keyCode;

		if (myGraph.currentObject != null && key == 46)
		{
			for (var i = 0; i < myGraph.locations.length; i++) {
				if (myGraph.locations[i] == myGraph.currentObject) {
					myGraph.locations.splice(i--, 1);
				}
			}
			for (var i = 0; i < myGraph.edges.length; i++) {
				for(var j=0;j<myGraph.edges[i].nails.length;j++)
				{
					if(myGraph.edges[i].nails[j] == myGraph.currentObject)
					{
						myGraph.edges[i].nails.splice(j,1);
					}
				}
				if (myGraph.edges[i] == myGraph.currentObject || myGraph.edges[i].locationA == myGraph.currentObject || myGraph.edges[i].locationB == myGraph.currentObject)
				{
					myGraph.edges.splice(i--, 1);
				}
			}
			myGraph.currentObject = null;
			myGraph.draw();
		}
	}
}

MyGraph.prototype.setHeight = function(height) {
  	document.getElementById('div_canvas').style.width = height;
	document.getElementById('canvas').width = height;
}

MyGraph.prototype.setWidth = function(width) {
	document.getElementById('div_canvas').style.height =  width;
	document.getElementById('tab_automata').style.width = width;
	document.getElementById('canvas').height = width;
}

MyGraph.prototype.drawArrow = function(c, x, y, angle)
{
    var dx = Math.cos(angle);
    var dy = Math.sin(angle);
    c.beginPath();
    c.moveTo(x, y);
    c.lineTo(x - 10 * dx + 3 * dy, y - 10 * dy - 3 * dx);
    c.moveTo(x, y);
    c.lineTo(x - 10 * dx - 3 * dy, y - 10 * dy + 3 * dx);
    c.stroke();
}

MyGraph.prototype.drawText = function(c,text,x,y)
{
    var width = c.measureText(text).width;
    x -= width/2;
    x = Math.round(x);
    y = Math.round(y);
	c.font = this.font;
    c.fillText(text, x, y);
}

MyGraph.prototype.draw = function()
{
    for (var i = 0; i < this.locations.length; i++)
    {
        if(this.locations[i].x+this.locations[i].w+20>document.getElementById('canvas').width)
        {
            document.getElementById('canvas').width = this.locations[i].x+this.locations[i].w+50;
            document.getElementById('tab_automata').style.width  = this.locations[i].x+this.locations[i].w+50;
        }
        if(this.locations[i].y+this.locations[i].h+20>document.getElementById('canvas').height)
            document.getElementById('canvas').height = this.locations[i].y+this.locations[i].h+50;
    }
    for (var i = 0; i < this.edges.length; i++)
    {
        for(var j=0;j<this.edges[i].nails.length;j++)
        {
            if(this.edges[i].nails[j].x+20>document.getElementById('canvas').width)
            {
                document.getElementById('canvas').width = this.edges[i].nails[j].x+50;
                document.getElementById('tab_automata').style.width = this.edges[i].nails[j].x+50;
            }
            if(this.edges[i].nails[j].y+20>document.getElementById('canvas').height)
                document.getElementById('canvas').height = this.edges[i].nails[j].y+50;
        }
    }

    this.c.clearRect(0, 0, this.canvas.width, this.canvas.height);
    this.c.save();
    this.c.translate(0.5, 0.5);
    this.c.lineWidth = 1;
	this.c.font = this.font;
    for (var i = 0; i < this.locations.length; i++) {
        this.locations[i].draw(this.c);
    }
    for (var i = 0; i < this.edges.length; i++) {
        this.edges[i].draw(this.c);
    }
    if (this.currentEdge != null) {
        this.c.fillStyle = this.c.strokeStyle = this.defaultColor;
        this.currentEdge.draw(this.c);
    }

    this.c.restore();
}

MyGraph.prototype.getCurrentObject = function(x,y)
{
    for (var i = 0; i < this.locations.length; i++) {
        if (this.locations[i].contain(x, y)) {
            return this.locations[i];
        }
    }
    for (var i = 0; i < this.edges.length; i++) {
        for(var j=0;j<this.edges[i].nails.length;j++)
        {
            if(this.edges[i].nails[j].contain(x,y))
                return this.edges[i].nails[j];
        }
        if (this.edges[i].contain(x, y)) {
            return this.edges[i];
        }
		if (this.edges[i].label.contain(x, y)) {
            return this.edges[i].label;
        }
		if (this.edges[i].rate.contain(x, y)) {
            return this.edges[i].rate;
        }
		if (this.edges[i].guard.contain(x, y)) {
            return this.edges[i].guard;
        }
		if (this.edges[i].probability.contain(x, y)) {
            return this.edges[i].probability;
        }
    }
    return null;
}

MyGraph.prototype.getMousePosition = function(e)
{
    var x1 = e.pageX || e.clientX + document.body.scrollLeft + document.documentElement.scrollLeft;
    var y1 = e.pageY || e.clientY + document.body.scrollTop + document.documentElement.scrollTop;
    var x2 = 0,y2 = 0;
    var s = e.target || e.srcElement;
    while(s.offsetParent)
    {
        x2 += s.offsetLeft;
        y2 += s.offsetTop;
        s = s.offsetParent;
    }
    var x = x1 - x2 + document.getElementById('div_canvas').scrollLeft;
    var y = y1 - y2 + document.getElementById('div_canvas').scrollTop;

    return {'x':x, 'y':y};
}

MyGraph.prototype.textHeight = function()
{
    var span = document.createElement("span");
    span.style.visibility ="hidden";
    span.style.font = this.c.font;
    document.body.appendChild(span);
	if(typeof span.textContent!="undefined")
		span.textContent = "test";
	else
		span.innerText="test";
	var result = span.offsetHeight;
    span.parentNode.removeChild(span);
    return result;
}

MyGraph.prototype.getEdgeCountBtwLoc = function(location_A,location_B,automaton)
{
    var count = 0;
    for(var i=0;i<this.automaton.edges.length;i++)
    {
        if((this.automaton.edges[i].locationA == location_A && this.automaton.edges[i].locationB == location_B) || (this.automaton.edges[i].locationA == location_B && this.automaton.edges[i].locationB == location_A))
        {
            count++;
        }
    }
    return count;
}

MyGraph.prototype.codeToGraph = function()
{
    this.model = new Model();

    var JANICode = JSON.parse(editor.getValue());

    this.model.name = JANICode.name;
    this.model.alphabet = JANICode.alphabet;
    this.model.constants = JANICode.constants;
    this.model.variables = JANICode.variables;

    for(var i=0;i<JANICode.automata.length;i++)
    {
        var automaton = new Automaton();

        automaton.name = JANICode.automata[i].name;
        automaton.variables = JANICode.automata[i].variables;

        var n = JANICode.automata[i].locations.length;

        var angle = 2*Math.PI/n;
        var x = Math.round($(this.canvas_div).width()/2);
        var y = Math.round($(this.canvas_div).height()/2);
        for(var j=0;j<n;j++)
        {
            var locationX = x + this.r*Math.sin(j*angle);
            var locationY = y - this.r*Math.cos(j*angle);

            var l = new Location(locationX,locationY,this);
            l.name = JANICode.automata[i].locations[j].name;
            l.invariant = JANICode.automata[i].locations[j].invariant;
            automaton.locations.push(l);
        }

        for(var j=0;j<JANICode.automata[i].edges.length;j++)
        {
            var location_A = null;
            for(var s=0;s<automaton.locations.length;s++)
            {
                if(automaton.locations[s].name == JANICode.automata[i].edges[j].location)
                    location_A =  automaton.locations[s];
            }
            if(location_A == null)
            {
                alert('No "' + JANICode.automata[i].edges[j].location + '" location');
                continue;
            }

            for(var k = 0;k<JANICode.automata[i].edges[j].destinations.length;k++)
            {
                var location_B = null;
                for(var s=0;s<automaton.locations.length;s++)
                {
                    if(automaton.locations[s].name == JANICode.automata[i].edges[j].destinations[k].location)
                        location_B =  automaton.locations[s];
                }
                if(location_B == null)
                {
                    alert('No "' + JANICode.automata[i].edges[j].destinations[k].location + '" location');
                    continue;
                }

                var edge = new Edge(location_A,location_B,new Array(),this);

                var count = this.getEdgeCountBtwLoc(location_A,location_B,automaton);
                if(location_A == location_B)
                {
                    var nail1 = new Nail(location_A.x-location_A.w+40*count,location_A.y-location_A.h*2,this);
                    var nail2 = new Nail(location_A.x-location_A.w+40*count+25,location_A.y-location_A.h*2,this);
                    edge.nails.push(nail1);
                    edge.nails.push(nail2);
                }
                else
                {
                    var nail = new Nail((location_A.x+location_B.x)/2+20*count,(location_A.y+location_B.y)/2+20*count,this);
                    edge.nails.push(nail);
                }

                edge.probability.content = JANICode.automata[i].edges[j].destinations[k].probability;
                edge.label.content = JANICode.automata[i].edges[j].label;
                edge.rate.content = JANICode.automata[i].edges[j].rate;
                edge.guard.content = JANICode.automata[i].edges[j].guard;
                edge.assignments = [];
				edge.resetPosition();
                for(var m=0;m<JANICode.automata[i].edges[j].destinations[k].assignments.length;m++)
                {
                    var assignment = new Assignment(JANICode.automata[i].edges[j].destinations[k].assignments[m].variable,JANICode.automata[i].edges[j].destinations[k].assignments[m].newValue);
                    edge.assignments.push(assignment);
                }
                automaton.edges.push(edge);
            }
        }
        this.model.automata.push(automaton);
    }

    this.model.system = JANICode.system;

    this.locations = this.model.automata[this.nowAutomatonIndex].locations;
    this.edges = this.model.automata[this.nowAutomatonIndex].edges;
    this.variables = this.model.automata[this.nowAutomatonIndex].variables;

    this.updateAutomataNameTable();

    this.draw();
}

MyGraph.prototype.graphToCode = function()
{
    var JANICode = {
        'time':this.model.time,
        'alphabet':this.model.alphabet,
        'constants':this.model.constants,
        'variables':this.model.variables,
        'automata':[],
        'system':this.model.system
    };

    for(var i=0;i<this.model.automata.length;i++)
    {
        var automaton = {
            'name':this.model.automata[i].name,
            'variables':this.model.automata[i].variables,
            'locations':[],
            'edges':[]
        };

        for(var j=0;j<this.model.automata[i].locations.length;j++)
        {
            var location = {
                'name': this.model.automata[i].locations[j].name,
                'invariant':this.model.automata[i].locations[j].invariant
            };
            automaton.locations.push(location);
        }

        var flag = [];
        for(var j=0;j<this.model.automata[i].edges.length;j++)
        {
            flag.push(false);
        }
        for(var j=0;j<this.model.automata[i].edges.length;j++)
        {
            if(!flag[j])
            {
                var destinations = [];
                var assignments = [];
                for(var k=0;k<this.model.automata[i].edges[j].assignments.length;k++)
                {
                    var assignment = {
                        "variable":this.model.automata[i].edges[j].assignments[k].variable,
                        "newValue":this.model.automata[i].edges[j].assignments[k].newValue
                    };
                    assignments.push(assignment);
                }
                var destination = {
                    "probability":this.model.automata[i].edges[j].probability.content,
                    "location":this.model.automata[i].edges[j].locationB.name,
                    "assignments":assignments
                };
                destinations.push(destination);
                for(var k=j+1;k<this.model.automata[i].edges.length;k++)
                {
                    if(this.model.automata[i].edges[j].locationA == this.model.automata[i].edges[k].locationA && this.model.automata[i].edges[j].label.content == this.model.automata[i].edges[k].label.content && this.model.automata[i].edges[j].rate.content == this.model.automata[i].edges[k].rate.content && this.model.automata[i].edges[j].guard.content == this.model.automata[i].edges[k].guard.content)
                    {
                        flag[k] = true;
                        var assignments = [];
                        for(var m=0;m<this.model.automata[i].edges[k].assignments.length;m++)
                        {
                            var assignment = {
                                "variable":this.model.automata[i].edges[k].assignments[m].variable,
                                "newValue":this.model.automata[i].edges[k].assignments[m].newValue
                            };
                            assignments.push(assignment);
                        }
                        var destination = {
                            "probability":this.model.automata[i].edges[k].probability.content,
                            "location":this.model.automata[i].edges[k].locationB.name,
                            "assignments":assignments
                        };
                        destinations.push(destination);
                    }
                }
                var edge = {
                    "location":this.model.automata[i].edges[j].locationA.name,
                    "label":this.model.automata[i].edges[j].label.content,
                    "rate":this.model.automata[i].edges[j].rate.content,
                    "guard":this.model.automata[i].edges[j].guard.content,
                    "destinations":destinations
                };
                automaton.edges.push(edge);
            }
        }
        JANICode.automata.push(automaton);
    }

    var str = JSON.stringify(JANICode,null,"\t");
    editor.setValue(str);
}

MyGraph.prototype.saveGraph = function()
{
    var modelGraph = {
        'time':model.time,
        'alphabet':model.alphabet,
        'constants':model.constants,
        'variables':model.variables,
        'automata':[],
        'system':model.system
    };
    for(var i=0;i<model.automata.length;i++)
    {
        var automaton = {
            'name':model.automata[i].name,
            'variables':model.automata[i].variables,
            'locations':model.automata[i].locations,
            'edges':[]
        };
        for(var j=0;j<model.automata[i].edges.length;j++)
        {
            var edge = {
                'locationA':model.automata[i].locations.indexOf(model.automata[i].edges[j].locationA),
                'locationB':model.automata[i].locations.indexOf(model.automata[i].edges[j].locationB),
                'nails':model.automata[i].edges[j].nails,
                'label':model.automata[i].edges[j].label.content,
                'rate':model.automata[i].edges[j].rate.content,
                'guard':model.automata[i].edges[j].guard.content,
                'probability':model.automata[i].edges[j].probability.content,
                'assignments':model.automata[i].edges[j].assignments,
                'remark':model.automata[i].edges[j].remark,
                'showColor':model.automata[i].edges[j].showColor
            };
            automaton.edges.push(edge);
        }
        modelGraph.automata.push(automaton);
    }

    return JSON.stringify(modelGraph);
}

MyGraph.prototype.restoreGraph = function(str)
{
    var modelGraph = JSON.parse(str);

    model = new Model();
    model.time = modelGraph.time;
    model.alphabet = modelGraph.alphabet;
    model.constants = modelGraph.constants;
    model.variables = modelGraph.variables;

    for(var i=0;i<modelGraph.automata.length;i++)
    {
        var automaton = new Automaton();
        automaton.name = modelGraph.automata[i].name;
        automaton.variables = modelGraph.automata[i].variables;

        for(var j=0;j<modelGraph.automata[i].locations.length;j++)
        {
            var l = modelGraph.automata[i].locations[j];
            var location = new Location(l.x, l.y,this);
            location.name = l.name;
            location.invariant = l.invariant;
            location.isInitial = l.isInitial;
            location.remark = l.remark;
            location.showColor = l.showColor;
            automaton.locations.push(location);
        }
        for(var j=0;j<modelGraph.automata[i].edges.length;j++)
        {
            var ed = modelGraph.automata[i].edges[j];
            var edge = new Edge(automaton.locations[ed.locationA],automaton.locations[ed.locationB],new Array(),this);
            edge.label.content = ed.label;
            edge.rate.content = ed.rate;
            edge.guard.content = ed.guard;
            edge.probability.content = ed.probability;
			edge.resetPosition();
            edge.assignments = ed.assignments;
            edge.nails = [];
            edge.remark = ed.remark;
            edge.showColor = ed.showColor;
            for(var k=0;k<ed.nails.length;k++)
            {
                var nail = new Nail(ed.nails[k].x,ed.nails[k].y,this);
                edge.nails.push(nail);
            }
            automaton.edges.push(edge);
        }
        model.automata.push(automaton);
    }

    model.system = modelGraph.system;

    locations = model.automata[nowAutomatonIndex].locations;
    edges = model.automata[nowAutomatonIndex].edges;
    variables = model.automata[nowAutomatonIndex].variables;

    updateAutomataNameTable();

    draw();
}

MyGraph.prototype.updateAutomataNameTable = function()
{
	var myGraph = this;
    var table = document.getElementById("table_automata");
    table.deleteRow(0);
    var row = table.insertRow(0);
    var i=0;
    for(i=0;i<this.model.automata.length;i++)
    {
        var cell = row.insertCell(i);
        cell.innerHTML = this.model.automata[i].name;

        (function()
        {
            var ce = cell;
            cell.ondblclick = function()
            {
                myGraph.editAutomatonName(ce);
            }
        })();

        (function()
        {
            var ii = i;
            cell.onclick = function()
            {
                myGraph.showSelectedAutomaton(ii);
            }
        })();

        if(i==this.nowAutomatonIndex)
            cell.className = "a1";
        else
            cell.className = "a2";
    }

    var cell = row.insertCell(i);
    cell.innerHTML = "+";
    cell.className = "a2";
    cell.title = "create a new automaton";
    (function()
    {
        cell.onclick = function()
        {
            myGraph.createNewAutomaton();
        }
    })();
}

MyGraph.prototype.location_ok = function()
{
    this.currentObject.name = document.getElementById("locationName").value;
    this.currentObject.invariant = document.getElementById("locationInvariant").value;
    this.currentObject.isInitial = document.getElementById("isInitial").checked;
    this.currentObject.remark = document.getElementById("locationRemark").value;
    this.currentObject.showColor = document.getElementById("location-color").value;

    this.draw();
}

MyGraph.prototype.edge_ok = function()
{
    this.currentObject.label.content = document.getElementById("label").value;
    this.currentObject.rate.content = document.getElementById("rate").value;
    this.currentObject.guard.content = document.getElementById("guard").value;
    this.currentObject.probability.content = document.getElementById("probability").value;
    this.currentObject.remark = document.getElementById("edgeRemark").value;
    this.currentObject.showColor = document.getElementById("edge-color").value;

    this.draw();
}

MyGraph.prototype.editAutomatonName = function(element)
{
    document.getElementById("automaton_name").value = element.innerHTML;
    this.automatonElement = element;
    
	$( "#edit_automaton" ).dialog( "open" );
}

MyGraph.prototype.showSelectedAutomaton = function(index)
{
    var table = document.getElementById("table_automata");
    for(var i=0;i<table.rows[0].cells.length;i++)
    {
        if(i!=index)
            table.rows[0].cells[i].className = "a2";
        else
            table.rows[0].cells[i].className = "a1";
    }

    this.nowAutomatonIndex = index;
    this.locations = this.model.automata[this.nowAutomatonIndex].locations;
    this.edges = this.model.automata[this.nowAutomatonIndex].edges;
    this.variables = this.model.automata[this.nowAutomatonIndex].variables;

    this.draw();
}

MyGraph.prototype.automaton_delete = function()
{
    if(confirm("Deletion can not be resumed, confirm to delete?"))
    {
        if(this.model.automata.length == 1)
        {
            this.model.automata = [];
            var automaton = new Automaton();
            this.model.automata.push(automaton);
        }
        else
            this.model.automata.splice(this.automatonElement.cellIndex,1);

        this.nowAutomatonIndex = 0;
        this.locations = this.model.automata[this.nowAutomatonIndex].locations;
        this.edges = this.model.automata[this.nowAutomatonIndex].edges;
        this.variables = this.model.automata[this.nowAutomatonIndex].variables;

        this.updateAutomataNameTable();
        this.draw();
    }

    $( "#edit_automaton" ).dialog( "close" );
}

MyGraph.prototype.automaton_ok = function()
{
    this.automatonElement.innerHTML = document.getElementById("automaton_name").value;
    this.model.automata[this.automatonElement.cellIndex].name = this.automatonElement.innerHTML;
}

MyGraph.prototype.createNewAutomaton = function()
{
    var automata = new Automaton();
    this.model.automata.push(automata);

    this.nowAutomatonIndex = this.model.automata.length - 1;
    this.locations = this.model.automata[this.nowAutomatonIndex].locations;
    this.edges = this.model.automata[this.nowAutomatonIndex].edges;
    this.variables = this.model.automata[this.nowAutomatonIndex].variables;

    this.updateAutomataNameTable();
    this.draw();
}



function Model()
{
    this.time = '';
    this.alphabet = [];
    this.constants = [];
    this.variables = [];
    this.automata = [];
    this.system = '';
}

function Automaton()
{
    this.name = '';
    this.variables = [];
    this.locations = [];
    this.edges = [];
}

function Location(x,y,myGraph)
{
	this.myGraph = myGraph;
    this.x = x;
    this.y = y;
    this.name = '';
    this.invariant='';
    this.isInitial = false;
    this.remark = '';
    this.h = 30;
    this.w = 50;
    this.r = 7;
    this.showColor = myGraph.defaultColor;
    this.d_to_center_x = 0;
    this.d_to_center_y = 0;
}

Location.prototype.draw = function(c)
{
    var a = c.measureText(this.name).width;
    var b = c.measureText(this.invariant).width;
    this.w =  a>b?a/2:b/2;
    this.w += 10;
	
    if(this.w <50)
        this.w = 50;

	if(this == this.myGraph.currentObject)
        c.fillStyle = c.strokeStyle = this.myGraph.selectedColor;
    else
        c.fillStyle = c.strokeStyle = this.showColor;

    c.beginPath();
    c.moveTo(this.x - this.w + this.r,this.y-this.h);
    c.arcTo(this.x+this.w,this.y-this.h,this.x+this.w,this.y+this.h,this.r);
    c.arcTo(this.x+this.w,this.y+this.h,this.x-this.w,this.y+this.h,this.r);
    c.arcTo(this.x-this.w,this.y+this.h,this.x-this.w,this.y-this.h,this.r);
    c.arcTo(this.x-this.w,this.y-this.h,this.x+this.w,this.y-this.h,this.r);
    c.stroke();

    this.myGraph.drawText(c, this.name, this.x, this.y-this.h/2);
    this.myGraph.drawText(c, this.invariant, this.x, this.y+this.h/2);

    if (this.isInitial) {
        c.beginPath();
        var w = this.w-4;
        var h = this.h-4;
        var r = this.r;
        c.moveTo(this.x - w + r,this.y-h);
        c.arcTo(this.x+w,this.y-h,this.x+w,this.y+h,r);
        c.arcTo(this.x+w,this.y+h,this.x-w,this.y+h,r);
        c.arcTo(this.x-w,this.y+h,this.x-w,this.y-h,r);
        c.arcTo(this.x-w,this.y-h,this.x+w,this.y-h,r);
        c.stroke();
    }
}

Location.prototype.getProperPoint = function(x,y)
{
    var dx = x - this.x;
    var dy = y - this.y;
    var resultX,resultY;

    if(Math.abs(dy/dx)>Math.abs(this.h/this.w))
    {
        resultX = this.x + dx*Math.abs(this.h/dy);
        if(dy>0)
            resultY = this.y + this.h;
        else
            resultY = this.y-this.h;
    }
    else
    {
        resultY = this.y + dy*Math.abs(this.w/dx);
        if(dx>0)
            resultX= this.x+this.w;
        else
            resultX= this.x-this.w;
    }
    return {
        'x':resultX,
        'y':resultY
    };
}

Location.prototype.contain = function(x,y)
{
    return Math.abs(x-this.x)<this.w && Math.abs(y-this.y)<this.h;
}

Location.prototype.getD = function(x,y)
{
    this.d_to_center_x = this.x - x;
    this.d_to_center_y = this.y - y;
}

Location.prototype.setFinalPosition = function(x,y)
{
    this.x = x + this.d_to_center_x;
    this.y = y + this.d_to_center_y;
}

function Text(myGraph)
{
	this.myGraph = myGraph;
	this.content = '';
	this.x = 0;
	this.y = 0;
	this.d_to_left_x = 0;
    this.d_to_bottom_y = 0;
}

Text.prototype.contain = function(x,y)
{
	return (x>this.x )&& (x - this.x <  this.myGraph.c.measureText(this.content).width) && (y<this.y) && (this.y-y < this.myGraph.textHeight());
}

Text.prototype.getD = function(x,y)
{
    this.d_to_left_x = this.x - x;
    this.d_to_bottom_y = this.y - y;
}

Text.prototype.setFinalPosition = function(x,y)
{
	this.x = x + this.d_to_left_x;
    this.y = y + this.d_to_bottom_y;
}

Text.prototype.draw = function(c)
{
	var color = c.strokeStyle;
	if(this == this.myGraph.currentObject)
        c.fillStyle = c.strokeStyle = this.myGraph.selectedColor;
	
	c.fillText(this.content, this.x, this.y);
	c.fillStyle = c.strokeStyle = color;
}

function Edge(a,b,nails,myGraph)
{
	this.myGraph = myGraph;
    this.locationA = a;
    this.locationB = b;
    this.nails = nails;
    this.label = new Text(myGraph);
    this.rate = new Text(myGraph);
    this.guard = new Text(myGraph);
    this.probability = new Text(myGraph);
    this.assignments = [];
    this.remark='';
    this.showColor = this.myGraph.defaultColor;
}

Edge.prototype.draw = function(c)
{
	if(this == this.myGraph.currentObject)
        c.fillStyle = c.strokeStyle = this.myGraph.selectedColor;
    else
        c.fillStyle = c.strokeStyle = this.showColor;
	
    c.beginPath();

    var point1,point2;
    if(this.nails.length == 0)
    {
        point1 = this.locationA.getProperPoint(this.locationB.x,this.locationB.y);
        point2 = this.locationB.getProperPoint(this.locationA.x,this.locationA.y);
    }
    else
    {
        point1 = this.locationA.getProperPoint(this.nails[0].x,this.nails[0].y);
        point2 = this.locationB.getProperPoint(this.nails[this.nails.length-1].x,this.nails[this.nails.length-1].y);
    }
    c.moveTo(point1.x,point1.y);
    for(var i=0;i<this.nails.length;i++)
    {
        c.lineTo(this.nails[i].x,this.nails[i].y);
    }
    c.lineTo(point2.x,point2.y);
    c.stroke();

    if(this.nails.length == 0)
        this.myGraph.drawArrow(c, point2.x, point2.y, Math.atan2(point2.y - point1.y, point2.x - point1.x));
    else
        this.myGraph.drawArrow(c, point2.x, point2.y, Math.atan2(point2.y - this.nails[this.nails.length-1].y, point2.x - this.nails[this.nails.length-1].x));

    for(var i=0;i<this.nails.length;i++)
    {
        this.nails[i].draw(c);
    }
    if(this.myGraph.currentObject == this)
    {
        for(var i=0;i<this.nails.length;i++)
        {
            c.beginPath();
            c.arc(this.nails[i].x,this.nails[i].y,this.myGraph.nailRadius,0,2*Math.PI,false);
            c.stroke();
        }
    }

	if(this.label.content =='' && this.rate.content =='' && this.guard.content =='' && this.probability.content=='')
	{
		this.resetPosition();
	}
	else
	{
		this.label.draw(c);
		this.rate.draw(c);
		this.guard.draw(c);
		this.probability.draw(c);
	}
}

Edge.prototype.resetPosition = function()
{
	var height = this.myGraph.textHeight();
	var ox,oy;

	var point1;
    if(this.nails.length == 0)
    {
        point1 = this.locationA.getProperPoint(this.locationB.x,this.locationB.y);
		
		if(this.locationB.x>point1.x)
			ox = point1.x + 3;
		else
			ox = point1.x - 100;
		if(this.locationB.y>point1.y)
			oy = height;
		else
			oy = (-1)*height;
    }
    else
    {
        point1 = this.locationA.getProperPoint(this.nails[0].x,this.nails[0].y);
		
		if(this.nails[0].x>point1.x)
			ox = point1.x + 3;
		else
			ox = point1.x - 100;
		if(this.nails[0].y>point1.y)
			oy = height;
		else
			oy = (-1)*height;
    }
	
	this.label.x = ox;
	this.label.y = point1.y+oy;
	this.rate.x = ox;
	this.rate.y = point1.y+2*oy;
	this.guard.x = ox;
	this.guard.y = point1.y+3*oy;
	this.probability.x = ox;
	this.probability.y = point1.y+4*oy;
}

Edge.prototype.contain = function(x,y)
{
    var x1 = this.locationA.x;
    var y1 = this.locationA.y;
    var x2,y2;
    var s = 3;

    for(var i=0;i<this.nails.length;i++)
    {
        x2 = this.nails[i].x;
        y2 = this.nails[i].y;

        if(x>=(x1<x2?x1:x2)-s && x<=(x1>x2?x1:x2)+s && y>=(y1<y2?y1:y2)-s && y<=(y1>y2?y1:y2)+s)
        {
            var d = Math.abs((y2-y1)*x+(x1-x2)*y+x2*y1-x1*y2)/Math.sqrt((y2-y1)*(y2-y1)+(x1-x2)*(x1-x2));
            if(d<=s)
                return true;
        }
        x1=x2;
        y1=y2;
    }
    x2=this.locationB.x;
    y2=this.locationB.y;

    if(x>=(x1<x2?x1:x2)-s && x<=(x1>x2?x1:x2)+s && y>=(y1<y2?y1:y2)-s && y<=(y1>y2?y1:y2)+s)
    {
        var d = Math.abs((y2-y1)*x+(x1-x2)*y+x2*y1-x1*y2)/Math.sqrt((y2-y1)*(y2-y1)+(x1-x2)*(x1-x2));
        if(d<=s)
            return true;
    }

    return false;
}

Edge.prototype.findNail = function(x,y)
{
    var x1 = this.locationA.x;
    var y1 = this.locationA.y;
    var x2,y2;
    var s = 3;

    for(var i=0;i<this.nails.length;i++)
    {
        x2 = this.nails[i].x;
        y2 = this.nails[i].y;

        if(x>=(x1<x2?x1:x2)-s && x<=(x1>x2?x1:x2)+s && y>=(y1<y2?y1:y2)-s && y<=(y1>y2?y1:y2)+s)
        {
            var d = Math.abs((y2-y1)*x+(x1-x2)*y+x2*y1-x1*y2)/Math.sqrt((y2-y1)*(y2-y1)+(x1-x2)*(x1-x2));
                return i;
        }
        x1=x2;
        y1=y2;
    }
    x2=this.locationB.x;
    y2=this.locationB.y;

    if(x>=(x1<x2?x1:x2)-s && x<=(x1>x2?x1:x2)+s && y>=(y1<y2?y1:y2)-s && y<=(y1>y2?y1:y2)+s)
    {
        var d = Math.abs((y2-y1)*x+(x1-x2)*y+x2*y1-x1*y2)/Math.sqrt((y2-y1)*(y2-y1)+(x1-x2)*(x1-x2));
        if(d<=s)
            return this.nails.length;
    }

    return -1;
}

function TempEdge(from, to,nails,myGraph)
{
	this.myGraph = myGraph;
    this.from = from;
    this.to = to;
    this.nails = nails;
}

TempEdge.prototype.draw = function(c)
{
    c.beginPath();
    var point1;
    if(this.nails.length == 0)
    {
        point1 = this.from.getProperPoint(this.to.x,this.to.y);
    }
    else
    {
        point1 = this.from.getProperPoint(this.nails[0].x,this.nails[0].y);
    }

    c.moveTo(point1.x,point1.y);
    for(var i=0;i<this.nails.length;i++)
    {
        c.lineTo(this.nails[i].x,this.nails[i].y);
    }
    c.lineTo(this.to.x,this.to.y);
    c.stroke();

    if(this.nails.length == 0)
        this.myGraph.drawArrow(c, this.to.x, this.to.y, Math.atan2(this.to.y - this.from.y, this.to.x - this.from.x));
    else
        this.myGraph.drawArrow(c, this.to.x, this.to.y, Math.atan2(this.to.y - this.nails[this.nails.length-1].y, this.to.x - this.nails[this.nails.length-1].x));
}

function Nail(x, y,myGraph)
{
	this.myGraph = myGraph;
    this.x=x;
    this.y=y;
    this.d_to_center_x = 0;
    this.d_to_center_y = 0;
}

Nail.prototype.contain = function(x,y)
{
    return (x-this.x)*(x-this.x) + (y-this.y)*(y-this.y)<this.myGraph.nailRadius*this.myGraph.nailRadius;
}

Nail.prototype.getD = function(x,y)
{
    this.d_to_center_x = this.x - x;
    this.d_to_center_y = this.y - y;
}

Nail.prototype.setFinalPosition = function(x,y)
{
    this.x = x + this.d_to_center_x;
    this.y = y + this.d_to_center_y;
}

Nail.prototype.draw = function(c)
{
    if(this.myGraph.currentObject == this)
    {
		var color = c.strokeStyle;
        c.beginPath();
        c.fillStyle = c.strokeStyle = this.myGraph.selectedColor;
        c.arc(this.x,this.y,this.myGraph.nailRadius,0,2*Math.PI,false);
        c.stroke();
		c.fillStyle = c.strokeStyle = color;
    }
}

function Variable(name,type,initialValue)
{
    this.name = name;
    this.type = type;
    this.initialValue = initialValue;
}

function Assignment(variable,newValue)
{
    this.variable = variable;
    this.newValue = newValue;
}
