//v.3.6 build 131023

/*
Copyright DHTMLX LTD. http://www.dhtmlx.com
You allowed to use this component or parts of it under GPL terms
To use it on other terms or get Professional edition of the component please contact us at sales@dhtmlx.com
*/
/*
Copyright DHTMLX LTD. http://www.dhtmlx.com
You allowed to use this component or parts of it under GPL terms
To use it on other terms please contact us at sales@dhtmlx.com
*/

/*_TOPICS_
@0:Initialization
*/

/**
*     @desc: ColorPicker constructor
*     @param: container - id of parent html object
*     @param: isClickOnly - enable/disable limited color selection mode
*     @param: customColors - enable/disable custom colors
*     @param: hide - enable init in hidden state
*     @type: public
*     @topic: 0
*/
function dhtmlXColorPicker(path_img, container, isClickOnly, customColors, hide, fullview)
{
   if (document.all) try { document.execCommand("BackgroundImageCache", false, true); } catch (e){}
   this._cc=customColors;
	
   if (!container){
      container=document.createElement("DIV");
      container.style.position="absolute";
      
      document.body.appendChild (container);
	  
      container._dhx_remove=true;
   }
   if (typeof(container)!="object")
      this.container = document.getElementById(container);
   else
      this.container = container;
   
   dhtmlxEventable(this);
   
   this.path_img = path_img;
   
   this.skinName = dhtmlx.skin || '';
   this.ready = false;
   this.hideOnInit = hide || false;
   this.linkToObjects = [];
   //this.imgURL = dhtmlx.image_path||window.dhx_globalImgPath||"";
   this.imgURL = "../../js/imgs/";
   this.hideSelfOnSelect = true;
   this.hex = "000000";
   this.h = 100;
   this.s = 0.9;
   this.v = 0.1;
   this.r = 0;
   this.g = 0;
   this.b = 0;
   this.old_hue = 0;
   this.old_sat = 0;
   this.fullview = fullview || false;
   this.customColorsCount=(!this.fullview?9:8);
   this.lastUsedColorsCount = 9;
   this.language = null;
   this.elements = new Array();
   this.customColors = new Array();
   this.lastUsedColors = new Array();
   this.alreadyUsedColors = new Array();
   this.restoreFrom = null;
   if (!this.fullview) {
     this.colorSelectH = 119;
     this.colorSelectW = 209;
   } else {
     this.colorSelectH = 255;
     this.colorSelectW = 255;
   }
  

   this.isClickOnly = isClickOnly||false;
    if (window.cs_dechex == null) {
       var hexDigit = new Array(0,1,2,3,4,5,6,7,8,9,'a','b','c','d','e','f');
       window.cs_dechex = new Array();
       for (var i=0; i<256; i++) {
           var retval = hexDigit[i>>4].toString()+hexDigit[i&15].toString();
           if (retval.length < 2)
               retval = '0'+retval;
           window.cs_dechex[i] = retval;
       }
    }

	defLeng = {
		langname:	'en-us',
		labelHue : "Hue",
		labelSat : "Sat",
		labelLum : "Lum",
		labelRed : "Red",
		labelGreen : "Green",
		labelBlue : "Blue",
		btnAddColor : "Add to Custom Colors",
		btnSelect : "Select",
		btnCancel : "Cancel"
	}
  
	if (window.dhtmlxColorPickerObjects) window.dhtmlxColorPickerObjects[window.dhtmlxColorPickerObjects.length] = this;
  else window.dhtmlxColorPickerObjects = [this];
            
	if (!window.dhtmlxColorPickerLangModules) window.dhtmlxColorPickerLangModules = {};
	window.dhtmlxColorPickerLangModules['en-us'] = defLeng;	
	
	return this;
}

dhtmlXColorPicker.prototype.generate = function()
{
X = (document.compatMode!="BackCompat"?"X":"");
   if (!this.fullview) {
	   var html = "<div class='dhtmlxcolorpicker" + (this.skinName != '' ? '_' + this.skinName : '') + "' csid='cs_Content' style='display:none;'>";
	   	//IE selectbox fix
	   if ((document.all)&&((this.container.style.position=="absolute")))
		   html +="<iframe src='"+(this.imgURL||"")+"blank.html' frameBorder='0' style='position:absolute; top:0px; left:0px; width:252px;  height:"+(this._cc?300:244)+"px; z-index:-1;'></iframe>";
	   html += "<table cellpadding='0' cellspacing='0'>";
	   html += "<tr><td style='width:2px;height:2px;background:url("+(this.imgURL||"")+"left_top.gif);'></td><td style='height:2px;background:url("+(this.imgURL||"")+"top.gif);'></td><td style='width:2px;height:2px;background:url("+(this.imgURL||"")+"right_top.gif);'></td></tr>";
	   html += "<tr><td style='width:2px;background:url("+(this.imgURL||"")+"left.gif);'></td><td style='width:"+(document.all?247:243)+"px;height:"+(this._cc?281:240)+"px;'>";
	   html +="<table class='cs_ContentTable' csid='cs_ContentTable' cellpadding='0px' cellspacing='0px'>";
	   html += "<tr>";
	   html += "<td><div class='cs_SelectorDiv_Mini' csid='cs_SelectorDiv'><div class='cs_SelectorVer' csid='cs_SelectorVer' style='left:100px;top:0px;'></div><div class='cs_SelectorHor' csid='cs_SelectorHor' style='left:0px;top:100px;'></div></div></td>";
	   html += "<td><div class='cs_LumSelect_Mini' csid='cs_LumSelect'><div csid='cs_LumSelectArrow'></div><div class='cs_LumSelectLine' csid='cs_LumSelectLine' style='left:0px;top:0px;'></div></div></td>";
	   html += "</tr>";
	   html += "<tr><td style='padding-top:10px;' colspan='2'>";
	
	   html += "<table class='cs_ColorArea_Mini' cellpadding='0px' cellspacing='0px'>";
	   html += "<tr><td rowspan='2' valign='top' style='text-align:left;'><div class='cs_EndColor_Mini' csid='cs_EndColor' ></div></td><td>"+this.language.labelHue+":</td><td><input tabindex='1' class='cs_Input_Mini"+X+"' csid='cs_InputHue' value='' /></td><td>"+this.language.labelRed+":</td><td><input tabindex='4' class='cs_Input_Mini"+X+"' csid='cs_InputRed' value='' /></td></tr>";
	   html += "<tr><td>"+this.language.labelSat+":</td><td><input tabindex='2' class='cs_Input_Mini"+X+"' csid='cs_InputSat' value='' /></td><td>"+this.language.labelGreen+":</td><td><input tabindex='4' class='cs_Input_Mini"+X+"' csid='cs_InputGreen' value='' /></td></tr>";
	   html += "<tr><td style='vertical-align:top;width:80px;text-align:left;'># <input csid='cs_Hex' class='cs_Hex_Mini"+X+"'></td><td>"+this.language.labelLum+":</td><td><input tabindex='2' class='cs_Input_Mini"+X+"' csid='cs_InputLum' value='' /></td><td>"+this.language.labelBlue+":</td><td><input tabindex='4' class='cs_Input_Mini"+X+"' csid='cs_InputBlue' value='' /></td></tr>";   
	
	   if (this._cc){			     
		html +=	"<tr><td colspan='6'><div class='cs_CustomColors_Mini' csid='cs_CustomColors'></div></td></tr>";
		html +=	"<tr><td colspan='6' style='float:left;'>last used color:</td></tr>";
		html +=	"<tr><td colspan='6'><div class='last_used_color' csid='last_used_color'></div></td></tr>";
	   }
	   html +=	"</table>";   
	   html += "</td></tr>";
	   html += "</table>";
	   html += "<table class='cs_ButtonsPanel' cellpadding='0' cellspacing='0' style='width:100%;'><tr><td style='width:100%;'><input tabindex='9' class='cs_ButtonCancel' csid='cs_ButtonCancel' type='button' value='"+this.language.btnCancel+"' /><input tabindex='8' class='cs_ButtonOk' csid='cs_ButtonOk' type='button' value='"+this.language.btnSelect+"' /></td></tr></table>";
	   html += "</td><td style='width:2px;background:url("+(this.imgURL||"")+"right.gif);'></td></tr>";
	   html += "<tr><td style='width:2px;height:2px;background:url("+(this.imgURL||"")+"left_bottom.gif);'></td><td style='height:2px;background:url("+(this.imgURL||"")+"bottom.gif);'></td><td style='width:2px;height:2px;background:url("+(this.imgURL||"")+"right_bottom.gif);'></td></tr>";
	   html += "</table>";
	   html +="</div>";
   } else {
	   var html = "<div class='dhtmlxcolorpicker" + (this.skinName != '' ? '_' + this.skinName : '') + "' csid='cs_Content' style='display:none;'>";
	   	//IE selectbox fix
	   if ((document.all)&&((this.container.style.position=="absolute")))
		   html +="<iframe src='"+(this.imgURL||"")+"blank.html' style='position:absolute; top:0px; left:0px; width:320px;  height:"+(this._cc?454:407)+"px; z-index:-1;'></iframe>";
	   html += "<table cellpadding='0' cellspacing='0'>";
	   html += "<tr><td style='width:2px;height:2px;background:url("+(this.imgURL||"")+"left_top.gif);'></td><td style='height:2px;background:url("+(this.imgURL||"")+"top.gif);'></td><td style='width:2px;height:2px;background:url("+(this.imgURL||"")+"right_top.gif);'></td></tr>";
	   html += "<tr><td style='width:2px;background:url("+(this.imgURL||"")+"left.gif);'></td><td style='width:316px;height:"+(this._cc?446:392)+"px;background:#E6E5E5;'>";
	   html +="<table class='cs_ContentTable' csid='cs_ContentTable' cellpadding='0px' cellspacing='0px'>";
	   html += "<tr>";
	   html += "<td><div class='cs_SelectorDiv' csid='cs_SelectorDiv'><div class='cs_SelectorVer' csid='cs_SelectorVer' style='left:100px;top:0px;'></div><div class='cs_SelectorHor' csid='cs_SelectorHor' style='left:0px;top:100px;'></div></div></td>";
	   html += "<td><div class='cs_LumSelect' csid='cs_LumSelect'><div class='cs_LumSelectArrow' csid='cs_LumSelectArrow' style='left:16px;top:124px;'></div><div class='cs_LumSelectLine' csid='cs_LumSelectLine' style='left:0px;top:0px;'></div></div></td>";
	   html += "</tr>";
	   html += "<tr><td style='padding-top:10px;' colspan='2'>";
	
	   html += "<table class='cs_ColorArea' cellpadding='0px' cellspacing='0px'>";
	   html += "<tr><td rowspan='3'><div class='cs_EndColor' csid='cs_EndColor'></div></td><td valign='top' style='text-align:center;width:80px;'># <input csid='cs_Hex' class='cs_Hex'></td><td>"+this.language.labelHue+":</td><td><input tabindex='1' class='cs_Input' csid='cs_InputHue' value='' /></td><td>"+this.language.labelRed+":</td><td><input tabindex='4' class='cs_Input' csid='cs_InputRed' value='' /></td></tr>";
	   html += "<tr><td></td><td>"+this.language.labelSat+":</td><td><input tabindex='2' class='cs_Input' csid='cs_InputSat' value='' /></td><td>"+this.language.labelGreen+":</td><td><input tabindex='4' class='cs_Input' csid='cs_InputGreen' value='' /></td></tr>";
	   html += "<tr><td></td><td>"+this.language.labelLum+":</td><td><input tabindex='2' class='cs_Input' csid='cs_InputLum' value='' /></td><td>"+this.language.labelBlue+":</td><td><input tabindex='4' class='cs_Input' csid='cs_InputBlue' value='' /></td></tr>";   
	
	   if (this._cc){
		html +=	"<tr><td>";
		html +=	"<div class='cs_CustomColors' csid='cs_CustomColors'></div>";
		html +=	"</td><td></td><td colspan='4' valign='top'><input tabindex='7' class='cs_CustomColorAdd' csid='cs_CustomColorAdd' type='button' value='"+this.language.btnAddColor+"'></td></tr>";
	   }
	   html +=	"</table>";   
	   html += "</td></tr>";
	   html += "</table>";
	   html += "<table cellpadding='0' cellspacing='0' style='width:100%;'><tr><td style='height:49px;background:url("+(this.imgURL||"")+"buttons_panel.gif);'><input tabindex='9' class='cs_ButtonCancel' csid='cs_ButtonCancel' type='button' value='"+this.language.btnCancel+"' /><input tabindex='8' class='cs_ButtonOk' csid='cs_ButtonOk' type='button' value='"+this.language.btnSelect+"' /></td></tr></table>";
	   html += "</td><td style='width:2px;background:url("+(this.imgURL||"")+"right.gif);'></td></tr>";
	   html += "<tr><td style='width:2px;height:2px;background:url("+(this.imgURL||"")+"left_bottom.gif);'></td><td style='height:2px;background:url("+(this.imgURL||"")+"bottom.gif);'></td><td style='width:2px;height:2px;background:url("+(this.imgURL||"")+"right_bottom.gif);'></td></tr>";
	   html += "</table>";
	   html +="</div>";	   
   }
   if (!this.fullview)
		this.container.style.width="252px";
	else   
	   this.container.style.width="320px";

   this.container.innerHTML = html;

   this._initCsIdElement();
    if (!this.fullview)
        this.elements['cs_SelectorDiv'].style.backgroundImage="url(' "+this.path_img+"colorpicker/imgs/colormini.png')";
    else
        this.elements['cs_SelectorDiv'].style.backgroundImage="url('"+this.path_img+"colorpicker/imgs/color.png')";

    this.elements['cs_SelectorDiv'].z = this;
    this.elements['cs_LumSelect'].z = this;
    this.elements['cs_LumSelectArrow'].z = this;
    this.z = this;
   this._drawLum();
   this._colorizeLum(this.old_hue, this.old_sat);
   this._initEvents();
   if (this._cc)
      this._initCustomColors();
   this.ColorNum = 0;
   this.usedColorNum = 0;

   this.restoreFromHSV();
   this._drawValues();
}

dhtmlXColorPicker.prototype._initCsIdElement = function(obj)
{
   if (obj == null)
      obj = this.container;
   var childs = obj.childNodes;
   var l = childs.length;
   for (var i=0; i<l; i++) {
      if (childs[i].nodeType == 1) {
         var attr = childs[i].getAttribute('csid');
         if (attr !== null) {
               this.elements[attr] = childs[i];
         }
         if (childs[i].childNodes.length > 0) {
            this._initCsIdElement(childs[i]);
         }
      }
   }
}

dhtmlXColorPicker.prototype._initEvents = function()
{
   this.elements['cs_SelectorDiv'].onmousedown = this._startMoveColor;
   this.elements['cs_SelectorDiv'].ondblclick = this.clickOk;   
   this.elements['cs_LumSelect'].onmousedown = this._startMoveLum;
   this.elements['cs_LumSelect'].ondblclick = this.clickOk;      
   this.elements['cs_LumSelectArrow'].onmousedown = this._startMoveLum;

   var inputs = new Array('cs_InputHue', 'cs_InputRed', 'cs_InputSat', 'cs_InputGreen', 'cs_InputLum', 'cs_InputBlue');
   for (var i=0; i<inputs.length; i++) {
      this.elements[inputs[i]].z = this;
      this.elements[inputs[i]].onchange = i%2?this._changeValueRGB:this._changeValueHSV;
   }
   this.elements['cs_Hex'].z = this;
   this.elements['cs_Hex'].onchange = this._changeValueHEX;

   this.elements['cs_ButtonOk'].z = this;
   this.elements['cs_ButtonOk'].onclick = this.clickOk;
   this.elements['cs_ButtonOk'].onmouseover = this.hoverButton;
   this.elements['cs_ButtonOk'].onmouseout = this.normalButton;
   this.elements['cs_ButtonCancel'].z = this;
   this.elements['cs_ButtonCancel'].onclick = this.clickCancel;
   this.elements['cs_ButtonCancel'].onmouseover = this.hoverButton;
   this.elements['cs_ButtonCancel'].onmouseout = this.normalButton;
}

dhtmlXColorPicker.prototype.resetHandlers = function(ev)
{
}


dhtmlXColorPicker.prototype.clickOk = function(ev)
{
   var z = this.z;
   var t=z.getSelectedColor();
   
   //////////////////////////////////////////////////////////////////////////////
	var rgb = z.r+','+z.g+','+z.b;
	
	var already_use = false;
	for(var i=0;i<z.alreadyUsedColors.length;i++)
	{
		if(z.alreadyUsedColors[i] == rgb)
		{
			already_use = true;
			break;
		}
	}
	
	if(!already_use)
	{
		z.alreadyUsedColors.push(rgb);
		
		for(var i=z.usedColorNum;i>0;i--)
		{
			var d = z.lastUsedColors[i];
			d.color = z.lastUsedColors[i-1].color;
			var backrgb = d.color.split(',');
			var r = window.cs_dechex[backrgb[0]]||'00';
			var g = window.cs_dechex[backrgb[1]]||'00';
			var b = window.cs_dechex[backrgb[2]]||'00';
			d.style.backgroundColor = '#'+r+g+b;
		}
		
		var d = z.lastUsedColors[0];        
		d.color = rgb;

		if(z.usedColorNum<z.lastUsedColorsCount-1)
			z.usedColorNum = z.usedColorNum +1;

		var backrgb = d.color.split(',');
		var r = window.cs_dechex[backrgb[0]]||'00';
		var g = window.cs_dechex[backrgb[1]]||'00';
		var b = window.cs_dechex[backrgb[2]]||'00';
		d.style.backgroundColor = '#'+r+g+b;
	}
		
   ///////////////////////////////////////////////////////////////////////////////////
   
   
	z.callEvent ("onSelect", [t[0]]);

	if (z.hideSelfOnSelect)
    z.hide();
}

dhtmlXColorPicker.prototype.clickCancel = function(ev)
{
  var z = this.z;
  z.callEvent("onCancel",[]);
  if (this.z.hideSelfOnSelect)
    this.z.hide();
}

dhtmlXColorPicker.prototype._setCrossPos = function(ev)
{
   var z = this.z;
   var offset = z._getOffset(this.elements['cs_SelectorDiv']);
   var y = ev.clientY - offset[0];
   if (y < 0)
      y = 0;
   else if (y > this.colorSelectH)
      y = this.colorSelectH;
    var x = ev.clientX - offset[1];
   if (x < 0)
      x = 0;
   else if (x > this.colorSelectW)
      x = this.colorSelectW;
    z.elements['cs_SelectorVer'].style.left = x+'px';
    z.elements['cs_SelectorHor'].style.top = y+'px';
   var H = x==this.colorSelectW?0:360*x/this.colorSelectW;
   var S = 1-y/this.colorSelectH;
   return [H, S];
}
dhtmlXColorPicker.prototype._getScrollers = function(){
   return     ([(document.body.scrollLeft||document.documentElement.scrollLeft),(document.body.scrollTop||document.documentElement.scrollTop)]);
}
dhtmlXColorPicker.prototype._setLumPos = function(ev)
{
   var z = this.z;
   var offset = z._getOffset(z.elements['cs_LumSelect']);
   var y = ev.clientY - offset[0] - 4;
   if (y < -3)
      y = -3;
   if (y > this.colorSelectH-4)
      y = this.colorSelectH-4;
    z.elements['cs_LumSelectArrow'].style.top = y+'px';
    z.elements['cs_LumSelectLine'].style.top = y+4+'px';
    var V = (y+3)/this.colorSelectH;
    return V;
}


dhtmlXColorPicker.prototype._startMoveColor = function(ev)
{
   var z = this.z;
   if (ev == null)
      ev = event;
   z.elements['cs_SelectorDiv'].onmousedown = null;

   z.b_move = document.body.onmousemove;
   z.b_up = document.body.onmouseup;

    var self=z;
   document.body.onmousemove = function(e) { if (e ==null) e = event; self._mouseMoveColor(e); };
   document.body.onmouseup = function(e) { if (e ==null) e = event; self._stopMoveColor(e); self = null;};
   z.elements['cs_SelectorDiv'].onmousemove = z._mouseMoveColor;
   z.elements['cs_SelectorDiv'].onmouseup = z._stopMoveColor;
   var HS = z._setCrossPos(ev);
   z.h = HS[0];
   z.s = HS[1];
   var col = z._calculateColor();
   z._colorizeLum(col[0], col[1]);
}

dhtmlXColorPicker.prototype._mouseMoveColor = function(ev)
{
   var z = this.z;
   if (ev == null)
      ev = event;
   var HS = z._setCrossPos(ev);
   if (z.isClickOnly)
      return;
   z.h = HS[0];
   z.s = HS[1];
   var col = z._calculateColor();
   z._colorizeLum(col[0], col[1]);
}

dhtmlXColorPicker.prototype._stopMoveColor = function(ev)
{
   var z = this.z;
   if (ev == null)
      ev = event;
   z.elements['cs_SelectorDiv'].onmousedown = z._startMoveColor;
   z.elements['cs_SelectorDiv'].onmousemove = null;
   z.elements['cs_SelectorDiv'].onmouseup = null;
   document.body.onmousemove = z.b_move;
   document.body.onmouseup = z.b_up;
   var HS = z._setCrossPos(ev);
   z.h = HS[0];
   z.s = HS[1];
   var col = z._calculateColor();
   z._colorizeLum(col[0], col[1]);
}


dhtmlXColorPicker.prototype._startMoveLum = function(ev)
{
   var z = this.z;
   if (ev == null)
      ev = event;
   z.elements['cs_LumSelect'].onmousedown = null;
   z.elements['cs_LumSelectArrow'].onmousedown = null;
   z.elements['cs_LumSelect'].onmousemove = z._mouseMoveLum;
   z.elements['cs_LumSelect'].onmouseup = z._stopMoveLum;

   z.b_move = document.body.onmousemove;
   z.b_up = document.body.onmouseup;
   z.b_selstart = document.body.onselectstart;
   var self=z;
   document.body.onmousemove = function(e) {if (e == null) e = event; self._mouseMoveLum(e);}
   document.body.onmouseup = function(e) {if (e == null) e = event; self._stopMoveLum(e); self = null;}
   document.body.onselectstart = function() {return false}

   z.v = z._setLumPos(ev);
   z._calculateColor();
}

dhtmlXColorPicker.prototype._mouseMoveLum = function(ev)
{   
   var z = this.z;
   if (ev == null)
      ev = event;
   z.v = z._setLumPos(ev);
   if (z.isClickOnly)
      return;

   z._calculateColor();
}

dhtmlXColorPicker.prototype._stopMoveLum = function(ev)
{
   var z = this.z;
   if (ev == null)
      ev = event;
   z.elements['cs_LumSelect'].onmousedown = z._startMoveLum;
   z.elements['cs_LumSelectArrow'].onmousedown = z._startMoveLum;
   z.elements['cs_LumSelect'].onmousemove = null;
   z.elements['cs_LumSelect'].onmouseup = null;
   z.v = z._setLumPos(ev);
   z._calculateColor();

   document.body.onmousemove = z.b_move;
   document.body.onmouseup = z.b_up;
   document.body.onselectstart = z.b_selstart;
}

dhtmlXColorPicker.prototype._getOffset = function(node)
{
   var top = this._getOffsetTop(node);
   var left = this._getOffsetLeft(node);
   var mod=this._getScrollers();
   return [top-mod[1], left-mod[0]];
}

dhtmlXColorPicker.prototype._getOffsetTop = function(node)
{
   var offset = 0;
   if (node.offsetParent)
      offset += node.offsetTop+this._getOffsetTop(node.offsetParent);
   return offset;
}

dhtmlXColorPicker.prototype._getOffsetLeft = function(node)
{
   var offset = 0;
   if (node.offsetParent)
      offset += node.offsetLeft+this._getOffsetLeft(node.offsetParent);
   return offset;
}



dhtmlXColorPicker.prototype._calculateColor = function()
{   
    if (this.restoreFrom == 'RGB') {
      var rgb = [this.r, this.g, this.b];
    }
    else {
     var rgb = this._hsv2rgb(this.h, this.s, 1-this.v);
     this.r = rgb[0];
     this.g = rgb[1];
     this.b = rgb[2];
    }  
    
   this.hex = this._getColorHEX(rgb);
   this.elements['cs_EndColor'].style.backgroundColor = '#'+this.hex;
   this._drawValues();
   this.restoreFrom = null;
   return [this.h, this.s, 1-this.v];
}

dhtmlXColorPicker.prototype._drawValues = function()
{	
   this.elements['cs_Hex'].value = this.hex;
   this.elements['cs_InputHue'].value = Math.floor(this.h);
   this.elements['cs_InputSat'].value = Math.floor(this.s*100);
   this.elements['cs_InputLum'].value = Math.floor((1-this.v)*100);
   this.elements['cs_InputRed'].value = Math.floor(this.r);
   this.elements['cs_InputGreen'].value = Math.floor(this.g);
   this.elements['cs_InputBlue'].value = Math.floor(this.b);
}

dhtmlXColorPicker.prototype.saveColor = function(rgb, num)
{
   var date = new Date();
   var datevalue = date.valueOf()+2678400000;
   date = new Date(datevalue);
   var str = 'color_'+num+'='+rgb+'; expires='+date.toGMTString();
   document.cookie = str;
}

dhtmlXColorPicker.prototype.restoreColor = function(num) {
	var rgb = false;
	var name = 'color_'+num;
	if (document.cookie.length > 0) {
	   var pos = document.cookie.indexOf(name+'=');
	   if (pos != -1) {
	      var pos2 = document.cookie.indexOf(';', pos);
	      if (pos2 == -1)
	         pos2 = document.cookie.length;
	      var pos3 = document.cookie.indexOf('=', pos)+1;
	      var rgb = document.cookie.substr(pos3, pos2-pos3);
	   }
	}
	return rgb;
}



dhtmlXColorPicker.prototype._hsv2rgb = function(h, s, v)
{
   Hi = Math.floor((h/60))%6;
   f = h/60-Hi;
   p = v*(1-s);
   q = v*(1-f*s);
   t = v*(1-(1-f)*s);
   var r = 0;
   var g = 0;
   var b = 0;
   switch(Hi) {
      case 0:
         r = v; g = t; b = p;
         break;
      case 1:
         r = q; g = v; b = p;
         break;
      case 2:
         r = p; g = v; b = t;
         break;
      case 3:
         r = p; g = q; b = v;
         break;
      case 4:
         r = t; g = p; b = v;
         break;
      case 5:
         r = v; g = p; b = q;
         break;
   }
   r = Math.floor(r*255);
   g = Math.floor(g*255);
   b = Math.floor(b*255);
   return [r, g, b];
}

dhtmlXColorPicker.prototype._rgb2hsv = function(r, g, b)
{
   R = r/255;
   G = g/255;
   B = b/255;
   var MAX = Math.max(R, G, B);
   var MIN = Math.min(R, G, B);
   var V = MAX;
   var S = MAX==0?0:(1-MIN/MAX);
    var H = 0;
   if (MAX == MIN) {
      H = 0;
   } else if (MAX == R && G>=B) {
      H = 60*(G-B)/(MAX-MIN)+0;
   } else if (MAX == R && G < B) {
        H = 60*(G-B)/(MAX-MIN)+360;
   } else if (MAX == G) {
      H = 60*(B-R)/(MAX-MIN)+120;
   } else if (MAX == B) {
      H = 60*(R-G)/(MAX-MIN)+240;
   }
   return [H, S, V];
}




dhtmlXColorPicker.prototype._drawLum = function()
{
   var color = this.colorSelectH;
   var lumElemCount = (!this.fullview?30:64);
   var colorInc = (!this.fullview?8:4);
   for (var i=0; i<lumElemCount; i++) {
      var d = document.createElement('div');
      var c = this._dec2hex(color);
      d.style.backgroundColor = '#'+c+c+c;
      d.className = 'cs_LumElement';
      color-=colorInc;
      this.elements['cs_LumSelect'].appendChild(d);
   }
}

dhtmlXColorPicker.prototype._colorizeLum = function(hue, sat)
{
   this.old_hue = hue;
   this.old_sat = sat;
   var lum = 255;
   var colorInc = (!this.fullview?8:4);
   var l = this.elements['cs_LumSelect'].childNodes.length;
   for (var i=2; i<l; i++) {
      var v = (lum>255)?1:(lum/255);
       var rgb = this._hsv2rgb(hue, sat, v);
       var rgb = '#'+this._getColorHEX(rgb);
       this.elements['cs_LumSelect'].childNodes[i].style.backgroundColor = rgb;
       lum-=colorInc;
   }
   this.callEvent("onChange", [this.getSelectedColor()])
}


dhtmlXColorPicker.prototype._dec2hex = function(dec)
{
   return window.cs_dechex[dec]||'00';
}

dhtmlXColorPicker.prototype._hex2dec = function(hex)
{
   return parseInt(hex, 16);
}


dhtmlXColorPicker.prototype._initCustomColors = function()
{
   var colors = this.elements['cs_CustomColors'];
   for (var i=0; i<this.customColorsCount; i++) {
      var d = document.createElement('div');
      d.className = (this.fullview?'cs_CustomColor':'cs_CustomColor_Mini');
        d.color_num = i;
        if (document.all) {
          if (navigator.appName=='Opera') {
		 	if (!this.fullview)	{
	           d.style.width = '18px';
    	       d.style.height = '18px';
    	    } else {
   	           d.style.width = '14px';
    	       d.style.height = '14px';
    	    }   
    	}
		else
          	if (!this.fullview)	{
      		if (document.compatMode != "BackCompat")
		        d.style.width = '18px'; //XHTML
	        else 
	    	    d.style.width = '20px';           
    	    d.style.height = '20px';
    	    } else {
   	           d.style.width = '16px';
    	       d.style.height = '16px';
    	    }
        }

        d.z = this;
        d.onclick=this._selectCustomColor;
        d.ondblclick=this.clickOk;
        var color = this.restoreColor(i)||'0,0,0';
        d.color = color;
        var rgb = color.split(',');
        d.style.backgroundColor = '#'+this._getColorHEX(rgb);
        this.customColors.push(d);
        colors.appendChild(d);
   }
   
   var colors_used = this.elements['last_used_color'];
   for (var i=0; i<this.lastUsedColorsCount; i++) {
      var d = document.createElement('div');
      d.className = (this.fullview?'cs_CustomColor':'cs_CustomColor_Mini');
        d.color_num = i;
        if (document.all) {
          if (navigator.appName=='Opera') {
		 	if (!this.fullview)	{
	           d.style.width = '18px';
    	       d.style.height = '18px';
    	    } else {
   	           d.style.width = '14px';
    	       d.style.height = '14px';
    	    }   
    	}
		else
          	if (!this.fullview)	{
      		if (document.compatMode != "BackCompat")
		        d.style.width = '18px'; //XHTML
	        else 
	    	    d.style.width = '20px';           
    	    d.style.height = '20px';
    	    } else {
   	           d.style.width = '16px';
    	       d.style.height = '16px';
    	    }
        }

        d.z = this;
		d.onclick=this._selectLastUsedColor;
		d.ondblclick=this.clickOk;
        this.lastUsedColors.push(d);
        colors_used.appendChild(d);
   }

}

dhtmlXColorPicker.prototype._reinitCustomColors = function()
{
   var colors = this.elements['cs_CustomColors'];
   for (var i=0; i<this.customColorsCount; i++) {
        var d = this.customColors[i];
        var color = this.restoreColor(i)||'0,0,0';
        d.color = color;
        var rgb = color.split(',');
        d.style.backgroundColor = '#'+this._getColorHEX(rgb);
        this.customColors[i] = d;
   }
}

dhtmlXColorPicker.prototype._getColorHEX=function(rgb){
        var r = this._dec2hex(rgb[0]);
        var g = this._dec2hex(rgb[1]);
        var b = this._dec2hex(rgb[2]);
        return (r+g+b);
}

dhtmlXColorPicker.prototype._selectCustomColor = function(ev)
{
    if (ev == null)
       ev = event;
   var z = this.z;
   if (!this.selected) {
       for (var i = 0; i< z.customColors.length; i++)
           z.customColors[i].style.border = '1px solid gray';
       //this.style.border = '1px dashed black';
       this.selected = true;
       if (z.selectedColor != null) {
           z.customColors[z.selectedColor].style.border = '1px solid gray';
           z.customColors[z.selectedColor].selected = false;
       }
   }
   z.selectedColor = this.color_num;
   z.ColorNum = this.color_num;

    var color = this.color.split(',');
   z.r = color[0];
   z.g = color[1];
   z.b = color[2];
   z.restoreFromRGB();
}

dhtmlXColorPicker.prototype._selectLastUsedColor = function(ev)
{
    if (ev == null)
       ev = event;
   var z = this.z;
   
   if(this.color == null)
		return;

   var color = this.color.split(',');
   z.r = color[0];
   z.g = color[1];
   z.b = color[2];
   z.restoreFromRGB();
}

dhtmlXColorPicker.prototype.addCustomColor = function()
{
   var z = this.z;
   if (z.selectedColor != null) {
      var d = z.customColors[z.selectedColor];
      var n = z.selectedColor;
      d.style.border = '1px solid gray';
      z.selectedColor = null;
   } else {
      var d = z.customColors[z.ColorNum];
        var n = z.ColorNum;
		d.style.border = '1px solid gray';        
   }
   var rgb = z.r+','+z.g+','+z.b;
   z.saveColor(rgb, n);
   d.color = rgb;
   z.ColorNum = z.ColorNum==9?0:z.ColorNum+1;
   z.customColors[z.ColorNum].style.border = '1px dashed red';
   d.style.backgroundColor = z.elements['cs_EndColor'].style.backgroundColor;
}

dhtmlXColorPicker.prototype.restoreFromRGB = function()
{
   this.restoreFrom = 'RGB';
   var HSV = this._rgb2hsv(this.r, this.g, this.b);
   this.h = HSV[0];
   this.s = HSV[1];
   this.v = 1-HSV[2];
   if (this.ready)   
	   this.redraw();
}

dhtmlXColorPicker.prototype.restoreFromHSV = function()
{
   this.restoreFrom = 'HSV';
   var RGB = this._hsv2rgb(this.h, this.s, this.v);
   this.r = RGB[0];
   this.g = RGB[1];
   this.b = RGB[2];
   this.redraw();
}

dhtmlXColorPicker.prototype.restoreFromHEX = function()
{
   this.r = this._hex2dec(this.hex.substr(0,2));
   this.g = this._hex2dec(this.hex.substr(2,2));
   this.b = this._hex2dec(this.hex.substr(4,2));
   
   this.restoreFromRGB();
}

dhtmlXColorPicker.prototype.redraw = function()
{
   var x = this.colorSelectW*this.h/360;
   var y =   (1-this.s)*this.colorSelectH;
   var top = (this.v)*this.colorSelectH;
   this.elements['cs_SelectorHor'].style.top = y+"px";
   this.elements['cs_SelectorVer'].style.left = x+"px";
   this.elements['cs_LumSelectArrow'].style.top = top-3+"px";
   this.elements['cs_LumSelectLine'].style.top = top+1+"px";
   var col = this._calculateColor();
   this._colorizeLum(col[0], col[1]);
}

dhtmlXColorPicker.prototype._changeValueHSV = function()
{
   var z = this.z;
   var H = parseInt(z.elements['cs_InputHue'].value)||0;
   var S = parseInt(z.elements['cs_InputSat'].value)||0;
   var V = parseInt(z.elements['cs_InputLum'].value)||0;
   if (H < 0 || H > 359)
      H = 0;
   if (S < 0 || S > 100)
      S = 0;
   if (V < 0 || V > 100)
      V = 0;
   z.elements['cs_InputHue'].value = H;
   z.elements['cs_InputSat'].value = S;
   z.elements['cs_InputLum'].value = V;
   z.h = H;
   z.s = S/100;
   z.v = 1-V/100;
   z.restoreFromHSV();
}

dhtmlXColorPicker.prototype._changeValueRGB = function()
{
   var z = this.z;
   var R = parseInt(z.elements['cs_InputRed'].value)||0;
   var G = parseInt(z.elements['cs_InputGreen'].value)||0;
   var B = parseInt(z.elements['cs_InputBlue'].value)||0;
   if (R < 0 || R > 255)
      R = 0;
   if (G < 0 || G > 255)
      G = 0;
   if (B < 0 || B > 255)
      B = 0;
   z.elements['cs_InputRed'].value = R;
   z.elements['cs_InputGreen'].value = G;
   z.elements['cs_InputBlue'].value = B;
   z.r = R;
   z.g = G;
   z.b = B;
   z.restoreFromRGB();
}

dhtmlXColorPicker.prototype._changeValueHEX = function()
{
   var z = this.z;
   var hex = (z.elements['cs_Hex'].value)||000000;  
   hex = hex.replace(/[^a-fA-F0-9]/gi,"0");
   if (hex.length>6)
	 hex = hex.substr(0,6)
   else while (hex.length<6)
   	 hex += '0';
   z.elements['cs_Hex'].value = hex;
   z.hex = hex;
   z.restoreFromHEX();
}
/**
*     @desc: define user's custom colors
*     @param: colors - array with predefined colors (like #F8560A)
*     @type: public
*     @topic: 0
*/
dhtmlXColorPicker.prototype.setCustomColors = function(colors)
{
   colors = colors.split(",");
   for (var i=0; i<colors.length; i++) {
      var rgb = colors[i];
        if (rgb.substr(0, 1) == '#') {
            rgb = rgb.substr(1);
        }
        var R = this._hex2dec(rgb.substr(0, 2));
        var G = this._hex2dec(rgb.substr(2, 2));
        var B = this._hex2dec(rgb.substr(4, 2));
        rgb = R+','+G+','+B;
        this.saveColor(rgb, i);
   }
}

/**
*     @desc: select defined color
*     @param: rgb - array with decimal values of Red, Green and Blue or string with hex color
*     @type: public
*     @topic: 0
*/
dhtmlXColorPicker.prototype.setColor = function(rgb)
{
   if (typeof(rgb) != 'string') {
      var R = rgb[0];
      var G = rgb[1];
      var B = rgb[2];
	} else if (rgb.indexOf('rgb')!=-1) {
	  var rgbArr = rgb.substr(rgb.indexOf("(")+1,rgb.lastIndexOf(")")-rgb.indexOf("(")-1).split(",");
	  var R = rgbArr[0];
	  var G = rgbArr[1];
	  var B = rgbArr[2];
   } else {
      if (rgb.substr(0, 1) == '#') {
         rgb = rgb.substr(1);
      }
        var R = this._hex2dec(rgb.substr(0, 2));
        var G = this._hex2dec(rgb.substr(2, 2));
        var B = this._hex2dec(rgb.substr(4, 2));
   }
   R = parseInt(R)||0;
   G = parseInt(G)||0;
   B = parseInt(B)||0;
   if (R < 0 || R > 255)
      R = 0;
   if (G < 0 || G > 255)
      G = 0;
   if (B < 0 || B > 255)
      B = 0;
   this.r = R;
   this.g = G;
   this.b = B;
   this.restoreFromRGB();
}


/**
*     @desc: close ColorPicker
*     @type: public
*     @topic: 0
*/
dhtmlXColorPicker.prototype.close = function()
{
    this.elements['cs_SelectorDiv'].z = null;
    this.elements['cs_LumSelect'].z = null;
    this.elements['cs_LumSelectArrow'].z = null;
   this.elements['cs_ButtonOk'].z = null;
   this.elements['cs_ButtonCancel'].z = null;

   this.container.innerHTML="";
   if (this.container._dhx_remove)
      this.container.parentNode.removeChild(this.container);
}

/**
*     @desc: show ColorPicker
*     @type: public
*     @topic: 0
*/
dhtmlXColorPicker.prototype.show = function()
{                
  this.callEvent("onShow",[]);
	if (this.container.innerHTML=="") return;
   this.elements['cs_Content'].style.display = '';
   this.elements['cs_InputHue'].focus()
}

/**
*     @desc: show ColorPicker
*     @param: x - (int) position left
*     @param: x - (top) position top
*     @type: public
*     @topic: 0
*/
dhtmlXColorPicker.prototype.setPosition = function(x,y)
{ 
  this.container.style.position='absolute';
  this.container.style.top=(y>0?y:10)+"px";
  this.container.style.left=x+"px";
}

/**
*     @desc: hide ColorPicker
*     @type: public
*     @topic: 0
*/
dhtmlXColorPicker.prototype.hide = function()
{
  this.resetHandlers();
  if (this.elements['cs_Content'])
    this.elements['cs_Content'].style.display = 'none';
}

/**
*     @desc: set function called when color selected (before closing)
*     @param: func - event handling function (or its name)
*     @type: public
*     @topic: 0
*     @event: onColorSelect
*     @eventdesc: Event raised immideatly after color selected, before closing
*/
dhtmlXColorPicker.prototype.setOnSelectHandler = function(func){
   this.attachEvent ("onSelect", func);
}

/**
*		@desc: set function called when selection canceled
*		@param: func - event handling function (or its name)
*   @type: public
*   @topic: 0
*   @event: onColorSelect
*   @eventdesc: Event raised immideatly after selection canceled, before closing
*/
dhtmlXColorPicker.prototype.setOnCancelHandler = function(func){
   this.attachEvent ("onCancel", func);
}

/**
*     @desc: get selected color
*     @type: public
*     @topic: 0
*     @returns: (array) first - hex rgb value, second - rgb component array, third - hsl component array
*/
dhtmlXColorPicker.prototype.getSelectedColor = function(){
   var rgb_dec = new Array(this.r, this.g, this.b);
   var rgb_hex = this._dec2hex(this.r)+this._dec2hex(this.g)+this._dec2hex(this.b);
   var hsl = new Array(this.h, this.s, this.v);
   return ["#"+rgb_hex,rgb_dec,hsl];
}

/**
*     @desc: link color picker to some area
*     @param: obj1 - color target
*     @param: obj2 - action element
*	  @param: obj3 - color value target
*     @type: public
*     @topic: 0
*/
dhtmlXColorPicker.prototype.linkTo = function(obj1,obj2,obj3){
   if (typeof(obj1)!="object")
      obj1=document.getElementById(obj1);
   if (typeof(obj2)!="object")
      obj2=document.getElementById(obj2);
   if (typeof(obj3)!="object")
      obj3=document.getElementById(obj3);
   this.linkToObjects = arguments;
   var self=this;
   obj2.onclick=function(){
      var z=self._getOffset(obj1);
      var s=self._getScrollers();
      var x=z[1]+s[0];
			var y=z[0]+s[1];
      self.setPosition(x+obj1.offsetWidth,y);
      self.isVisible() ? self.hide() : self.show();
   };
   this.setOnSelectHandler(function(color){
      obj1.style.backgroundColor=color;
      if (obj3)  obj3.value=color;
   });
   this.close=this.hide;
   this.hide();
}

dhtmlXColorPicker.prototype.hideOnSelect = function(value) {
  	this.hideSelfOnSelect = value;
}

/**
*     @desc: set path to images
*     @type: public
*     @topic: 0
*/
dhtmlXColorPicker.prototype.setImagePath = function(path){
	this.imgURL = path;
}

/**
*     @desc: object initialization
*     @type: public
*     @topic: 0
*/
dhtmlXColorPicker.prototype.init = function(){
   if (!this.language)
   	this.loadUserLanguage ("en-us");
   this.generate();
   this.ready = true;
   if (this._cc)
	  this._reinitCustomColors();
   if (this.linkToObjects.length>0)
      this.linkTo(this.linkToObjects[0],this.linkToObjects[1],this.linkToObjects[2]);
   if (!this.hideOnInit)
      this.show();	  
}

/**
*  @desc:  Set new language interface for colorpicker
*  @param: lang - language (ex: en-us|ru)
*  @type: public
*  @topic: 0
*/
dhtmlXColorPicker.prototype.loadUserLanguage = function(lang) {
  if (!window.dhtmlxColorPickerLangModules [lang])
  	return;
  this.language = window.dhtmlxColorPickerLangModules [lang];
  if (this.ready) {
  	this.generate();
    this.show();
  }
}

/**
*  @desc: set skin to colorpicker
*  @param: skin - {String} skin name
*  @type: public
*/
dhtmlXColorPicker.prototype.setSkin = function (skin) {
  this.skinName = skin;
  if (this.elements['cs_Content'])
	  this.elements['cs_Content'].className = 'dhtmlxcolorpicker' + (skin ? '_' + skin : skin);
}
/** 
* @desc: return state of colorPicker visibility
* @type: public
* @topic: 0
*/ 
dhtmlXColorPicker.prototype.isVisible = function () {
  return !(this.elements['cs_Content'].style.display == 'none');
};

dhtmlXColorPicker.prototype.hoverButton = function () {
  this.className += '_Hover';
};

dhtmlXColorPicker.prototype.normalButton = function () {
  this.className = this.className.substr(0, this.className.length-6)
};

//colorpicker
(function(){
	dhtmlx.extend_api("dhtmlXColorPicker",{
		_init:function(obj){
			return [obj.parent, obj.click, obj.colors, obj.hide, obj.full ];
		},
		show:"showA",
		link:"linkTo",
		image_path:"setImagePath",
		color:"setColor"
	},{
		showA:function(){
			this.init();
			this.show();
		}
	});
})();