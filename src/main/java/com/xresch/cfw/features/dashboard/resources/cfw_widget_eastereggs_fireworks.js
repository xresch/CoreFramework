(function (){
	
	/******************************************************************
	 * 
	 ******************************************************************/
	//CFW.dashboard.registerCategory("fas fa-desktop", "Monitoring");
		
	/******************************************************************
	 * 
	 ******************************************************************/
	CFW.dashboard.registerWidget("cfw_easteregg_fireworks",
		{
			category: "Easter Eggs",
			menuicon: "fas fa-bomb",
			menulabel: CFWL('cfw_widget_fireworks', "Fireworks"),
			description: CFWL('cfw_widget_fireworks_desc', "Adds some explosions to your dashboard."),
			defaulttitle: "",
			defaultwidth: 4,
			defaultheight: 4,
			createWidgetInstance: function (widgetObject, params, callback) {		
				
				var fwTemplate = $('#fireworks-template');
				if(fwTemplate.length == 0){
					$('body').append('<div id="fireworks-template">'
						    +'<div id="fw" class="firework"></div>'
						    +'<div id="fp" class="fireworkParticle"><i class="fas fa-circle"></i></div>'
						 +'</div>'
						+'<div id="fireContainer"></div>');
				}

				var html = 
					 '<button class="btn btn-sm btn-danger fas fa-bomb" onclick="cfw_widget_easteregg_toggleFireworks('+widgetObject.JSON_SETTINGS.discolevel+')" style="height: 100%; width:100%;"></button>'
					;
				callback(widgetObject, html);
				
			},
			getEditForm: function (widgetObject) {
				return CFW.dashboard.getSettingsForm(widgetObject);
			},
			onSave: function (form, widgetObject) {
				widgetObject.JSON_SETTINGS = CFW.format.formToObject(form);
				return true;
			}
			
		}
	);
})();

var GLOBAL_FWC;
var GLOBAL_FIREWORKS_INTERVAL_ID = null;

/************************************************************ 
 * 
 ************************************************************/
function cfw_widget_easteregg_toggleFireworks(){
	if(GLOBAL_FWC == null){
		GLOBAL_FWC = new FireworksController();
		GLOBAL_FWC.init();
		addEventHandler(window,'resize',GLOBAL_FWC.getWindowCoords);
		addEventHandler(window,'scroll',GLOBAL_FWC.getWindowCoords);
		addEventHandler(window,'unload',GLOBAL_FWC.destructor);
		
	}
	
	if(GLOBAL_FIREWORKS_INTERVAL_ID != null){
		 window.clearInterval(GLOBAL_FIREWORKS_INTERVAL_ID);
		 GLOBAL_FIREWORKS_INTERVAL_ID = null;
	}else{
		GLOBAL_FIREWORKS_INTERVAL_ID = window.setInterval( function() {	
			
			window.setTimeout(function(){	
				var blastRadiusPercent =  Math.ceil(15 +(Math.random()*20));
				var particleCount =  Math.ceil(12 +(Math.random()*150));
				var circles =  Math.ceil(2 +(Math.random()*6));
				var colorType =  Math.ceil(1 +(Math.random()*6));
				
				createFirework(blastRadiusPercent,
						particleCount,
						circles,
						colorType,
						null,null,null,null, 
						Math.random()>0.5, 
						Math.random()>0.5);
				//createFirework(28,112,5,3,null,null,null,null, Math.random()>0.5, Math.random()>0.5);
			},
			(500 + parseInt(Math.random()*1000)));
			
		}, 1000);
	}
		
	
}

/************************************************************ 
 * fireworks.js: A JavaScript animation experiment
 * -----------------------------------------------
 * http://schillmania.com/projects/fireworks/
 *
 * Provided "as-is", free and without warranty
 * Originally written in 2005. Old code ahead.
 *
 * Includes SoundManager 2 API (BSD).
 * http://schillmania.com/projects/soundmanager2/
 *
 * v0.9.1.20110703
 *************************************************************/

/*jslint white: false, onevar: false, undef: true, nomen: false, eqeqeq: false, plusplus: false, bitwise: true, regexp: false, newcap: true, immed: true */
/*global window, document, navigator, setTimeout, setInterval, clearInterval, enableDebugMode, writeDebug, soundManager, FireworkParticle, attachEvent */

function Animator() {

  var self = this;
  writeDebug('Animator()');
  this.tweens = [];
  this.tweens['default'] = [1,2,3,4,5,6,7,8,9,10,9,8,7,6,5,4,3,2,1];
  this.tweens.blast = [16,12,11,10,9,9,8,7,6,5,4,3,2,1];
  this.tweens.blastreverse = [1,2,3,4,5,6,7,8,9,10,10,11,12,12];
  this.tweens.fade = [10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10];
  this.queue = [];
  this.queue.IDs = [];
  this.active = false;
  this.timer = null;

  this.createTween = function(start,end,type) {
    // return array of tween coordinate data (start->end)
    type = type||'default';
    var tween = [start];
    var tmp = start;
    var diff = end-start;
    var x = self.tweens[type].length;
    for (var i=0; i<x; i++) {
      tmp += diff*self.tweens[type][i]*0.01;
      tween[i] = {};
      tween[i].data = tmp;
      tween[i].event = null;
    }
    return tween;
  };

  this.enqueue = function(o,fMethod,fOnComplete) {
    // add object and associated methods to animation queue
    // writeDebug('animator.enqueue()');
    var i;
    if (!fMethod) {
      writeDebug('animator.enqueue(): missing fMethod');
    }
    if (typeof(self.queue.IDs[o.oID])=='undefined') {
      // writeDebug('animator.enqueue(): added '+o.oID);
      i = self.queue.length;
      self.queue.IDs[o.oID] = i;
      self.queue[i] = o;
    } else {
      // writeDebug('animator.enqueue(): '+o.oID+' already queued');
      i = self.queue.IDs[o.oID]; // retrieve queue index
      self.queue[i].active = true;
      self.queue[i].frame = 0;
    }
    o.active = true; // flag for animation
    self.queue[i]._method = fMethod;
    self.queue[i]._oncomplete = fOnComplete?fOnComplete:null;
  };

  this.animate = function() {
    var active = 0;
    for (var i=self.queue.length; i--;) {
      if (self.queue[i].active) {
        self.queue[i]._method();
        active++;
      }
    }
    if (active===0 && self.timer) {
      // all animations finished
      self.stop();
    } else {
      // writeDebug(active+' active');
    }
  };

  this.start = function() {
    if (self.timer || self.active) {
      // writeDebug('animator.start(): already active');
      return false;
    }
    // writeDebug('animator.start()'); // report only if started
    self.active = true;
    self.timer = setInterval(self.animate,GLOBAL_FWC.intervalRate);
  };

  this.stop = function() {
    // writeDebug('animator.stop()',true);
    clearInterval(self.timer);
    self.timer = null;
    self.active = false;
    self.queue = [];
    self.queue.IDs = [];
  };

}

function FireworksController() {

  var self = this;
  this.intervalRate = 60; // rate (ms) to run animation at, general best default = 20
  this.DEBUG = true; // debug mode disabled by default
  this.oFW = null;
  this.isIE = !!(navigator.userAgent.match(/msie/i));
  this.isOpera = !!(navigator.userAgent.match(/opera/i));
  if (this.isOpera) {
    this.isIE = false; // no impersonation allowed here!
  }
  this.fireworks = [];
  this.animator = null;
  this.gOID = 0; // global object ID counter (for animation queue)
  this.particleTypes = 6;
  this.particleXY = 10;
  this.tweenFade = [100,90,80,70,60,50,40,30,20,10,0];
  this.isSafari = !!(navigator.appVersion.match(/webkit/i));
  this.canvasX = null;
  this.canvasY = null;
  this.screenY = null; // screen area (not entire page)
  self.scrollY = null;

  self.getWindowCoords = function() {
    self.canvasX = (document.documentElement.clientWidth||document.body.clientWidth||document.body.scrollWidth);
    self.canvasY = (document.documentElement.clientHeight||document.body.clientHeight||document.body.scrollHeight);
    self.screenY = self.canvasY;
    self.scrollY = parseInt(window.scrollY||document.documentElement.scrollTop||document.body.scrollTop, 10);
    self.canvasY += self.scrollY;
  };

  this.getWindowCoordsAlt = function() {
    self.canvasX = window.innerWidth-16;
    self.canvasY = window.innerHeight;
    self.screenY = self.canvasY;
    self.scrollY = parseInt(window.scrollY||document.documentElement.scrollTop||document.body.scrollTop, 10);
    self.canvasY += self.scrollY;
  };

  this.getPanX = function(x) {
    x = parseInt(x, 10);
    var pos = x/self.canvasX;
    if (pos<0.4) {
      pos *= -1;
    } else if (pos >= 0.4 && pos <= 0.6) {
      pos = 0.5;
    }
    pos = parseInt(pos*100, 10);
    // writeDebug('getPanX('+x+'): '+pos+'%');
    return pos;
  };

  this.isEmpty = function(o) {
    // needs further hacking
    return (typeof(o)=='undefined'||(o===null&&o!==0)||(o===''&&o!==0)||o=='null');
  };

  this.init = function() {
    self.oFW = document.getElementById('fw');
    self.oFP = document.getElementById('fp');
    if (typeof(enableDebugMode)!='undefined' && (self.DEBUG||window.location.toString().toLowerCase().indexOf('debug')>=0)) {
      enableDebugMode();
    }
    self.getWindowCoords();
    self.animator = new Animator();
  };

  this.destructor = function() {
    for (var i=self.fireworks.length; i--;) {
      self.fireworks[i] = null;
    }
    self.fireworks = null;
  };

  if (this.isSafari || this.isOpera) {
    this.getWindowCoords = this.getWindowCoordsAlt;
  }

}

function Firework(oC,startX,startY,burstX,burstY,burstType,nRadius,nParticles,nCircles,allowRandom,obeyBoundaries) {
  var self = this;
  this.oID = 'fp'+(GLOBAL_FWC.gOID++); // may be unneeded
  var p = '';
  for (var i=0; i<arguments.length-1; i++) {
    p += arguments[i]+',';
  }
  p += arguments[i];
  writeDebug('firework('+p+')');
  this.oC = oC;
  this.o = GLOBAL_FWC.oFW.cloneNode(!GLOBAL_FWC.isIE?true:false);
  this.particles = [];
  this.vX = -1;
  this.vY = -4;
  this.x = startX;
  this.y = startY;
  this.allowRandom = allowRandom;
  this.obeyBoundaries = obeyBoundaries;
  this.frame = 0;
  this.tween = [];
  this.active = false;

  this.moveTo = function(x,y) {
    self.o.style.left = x+'px';
    self.o.style.top = y+'px';
    self.x = x;
    self.y = y;
  };

  this.slideTo = function(x,y) {
    self.tween = [GLOBAL_FWC.animator.createTween(self.x,x,'blast'),GLOBAL_FWC.animator.createTween(self.y,y,'blast')];
    GLOBAL_FWC.animator.enqueue(self,self.animate,self.aniExplode);
  };

  this.aniExplode = function() {
    // called from animation finish
    self.o.style.background = 'none';
    self.o.style.border = 'none';
    for (var i=self.particles.length; --i;) {
      self.particles[i].o.style.display = 'block';
      GLOBAL_FWC.animator.enqueue(self.particles[i],self.particles[i].animate);
    }
    // attach oncomplete event handler to last particle
    self.particles[i].o.style.display = 'block';
    GLOBAL_FWC.animator.enqueue(self.particles[i],self.particles[i].animate,self.beginFade);
    var sID = 'boom'+parseInt(Math.random()*8, 10);

  };

  this.beginFade = function() {
    // writeDebug('beginFade');
    self.tween = GLOBAL_FWC.animator.createTween(1,0,'fade');
    GLOBAL_FWC.animator.enqueue(self,self.aniFade,self.destructor);
  };

  this.aniFade = function() {
    // writeDebug('firework.aniFade('+self.tween[self.frame].data+')');
    for (var i=self.particles.length; i--;) {
      self.particles[i].moveRel();
      self.particles[i].nextState();
      self.particles[i].setOpacity(GLOBAL_FWC.tweenFade[self.frame]+20);
    }
    if (self.frame++>=self.tween.length) {
      self.active = false;
      self.frame = 0;
      if (self._oncomplete) {
        self._oncomplete();
      }
      self._oncomplete = null;
      return false;
    }
    return true;
  };

  this.destructor = function() {
    writeDebug('firework.destructor()');
    // for (var i=0; i<self.particles.length; i++) {
    for (var i=self.particles.length; i--;) {
      self.particles[i].destructor();
      self.particles[i] = null;
    }
    self.particles = null;
    self.oC.removeChild(self.o);
    self.o = null;
    self.oC = null;
  };

  this.animate = function() {
    // generic animation method
    self.moveTo(self.tween[0][self.frame].data,self.tween[1][self.frame].data,'burst');
    if (self.frame++>=self.tween[0].length-1) {
      self.active = false;
      self.frame = 0;
      if (self._oncomplete) {
        self._oncomplete();
      }
      self._oncomplete = null;
      return false;
    }
    return true;
  };

  this.createBurst = function(circles,nMax,rMax,type) {
    // c: # of circles, n: # of particles per circle, r: max radius
    writeDebug('firework.createBurst('+circles+','+nMax+','+rMax+','+type+')');
    var i=0, j=0;
    var tmp = 0;
    var radiusInc = rMax/circles;
    var radius = radiusInc;
    var angle = 0;
    var angleInc = 0; // per-loop increment
    var radiusOffset = (self.allowRandom?(0.33+Math.random()):1);
    var particlesPerCircle = [];
    var isRandom = Math.random()>0.5;
    var circleTypes = [type,circles>1?parseInt(Math.random()*GLOBAL_FWC.particleTypes, 10):type];
    var thisType = null;

    for (i=0; i<circles; i++) {
      particlesPerCircle[i] = parseInt(nMax*(i+1)/circles*1/circles, 10)||1; // hack - nMax*(i+1)/circles;
      angle = angleInc; // could be offset as well
      angleInc = 360/particlesPerCircle[i];
      thisType = circleTypes[i%2];
      for (j=0; j<particlesPerCircle[i]; j++) {
        self.particles[tmp] = new FireworkParticle(self.o,self.allowRandom,thisType,burstX,burstY,self.obeyBoundaries);
        self.particles[tmp].slideTo(radius*Math.cos(angle*Math.PI/180),radius*radiusOffset*Math.sin(angle*Math.PI/180));
        angle += angleInc;
        tmp++;
      }
      radius += radiusInc; // increase blast radius
    }
  };

  // startX,startY,burstX,burstY,burstType,nRadius,nParticles,nCircles

  self.oC.appendChild(self.o);
  self.moveTo(self.x,self.y);
  self.createBurst(nCircles,nParticles,nRadius,burstType); // create an explosion
  self.slideTo(burstX,burstY);
  var sID = 'fire'+parseInt(Math.random()*2, 10);

  GLOBAL_FWC.animator.start();

}

function FireworkParticle(oC,isRandom,type,baseX,baseY,obeyBoundaries) {

  var self = this;
  this.oC = oC;
  this.oID = 'fp'+(GLOBAL_FWC.gOID++); // may be unneeded
  this.o = GLOBAL_FWC.oFP.cloneNode(true);
  this.obeyBoundaries = obeyBoundaries;
  // set type: index becomes Y offset (for background image)
  this.type = null;

  this.oImg = this.o.getElementsByTagName('i')[0];
  this.o.style.display = 'none';
  this.baseX = baseX;
  this.baseY = baseY;
  this.x = 0;
  this.y = 0;
  this.vx = 0;
  this.vy = 0;
  this.frame = 0;
  this.tween = [];
  this.active = null;
  this.tweenType = 'blast';
  this.states = [];
  this.state = parseInt(Math.random()*3, 10);
  this.isRandom = isRandom;
  this._mt = 5;

  this.moveTo = function(x,y) {
    self.o.style.left = x+'px';
    self.o.style.top = y+'px';
    self.vx = x-self.x;
    self.vy = y-self.y;
    self.x = x;
    self.y = y;
  };

  this.moveRel = function() {
    // continue last moveTo() pattern, bouncing off walls if applicable
    var toX = self.x+self.vx;
    var toY = self.y+self.vy;
    if (self.obeyBoundaries) {
      var xMax = GLOBAL_FWC.canvasX-self.baseX-GLOBAL_FWC.particleXY;
      var yMax = GLOBAL_FWC.canvasY-self.baseY-GLOBAL_FWC.particleXY;
      var yMin = GLOBAL_FWC.scrollY;
      if (self.vx>=0) {
        if (toX>=xMax) {
          self.vx *= -1;
        }
      } else if (self.vx<0 && toX+self.baseX<=0) {
        self.vx *= -1;
      }
      if (self.vy>=0) {
        if (toY>=yMax) {
          self.vy *= -1;
        }
      } else if (self.vy<0) {
        if (toY+self.baseY-yMin<=0) {
          self.vy *= -1;
        }
      }
    }
    self.moveTo(self.x+self.vx,self.y+self.vy);
  };

  this.setOpacity = function(n) { // where n = 0..100
	  self.oImg.style.opacity = 0.15+1/( (101-n)/2);
	  if	  (n > 90) { self.oImg.style.fontSize = '6px';}
	  else  if(n > 70) { self.oImg.style.fontSize = '5px';}
	  else  if(n > 50) { self.oImg.style.fontSize = '4px';}
	  else  if(n > 30) { self.oImg.style.fontSize = '3px';}
	  else  if(n > 10) { self.oImg.style.fontSize = '2px';}
	  else  { self.oImg.style.fontSize = '2px';}
    //self.oImg.style.marginLeft = -100+(n*GLOBAL_FWC.particleXY/10)+'px';
  };

  this.nextState = function() {
    var vis = self.o.style.visibility;
    if (self.state == 2 && vis != 'hidden') {
      self.o.style.visibility = 'hidden';
    } else if (self.state != 2 && vis == 'hidden') {
      self.o.style.visibility = 'visible';
    }
    self.state = parseInt(Math.random()*3, 10);
  };

  this.slideTo = function(x1,y1) {
    // writeDebug('slideTo (x/y): '+x1+','+y1);
    if (self.isRandom) {
      // randomize a bit
      x1 += (x1*0.2*(Math.random()>0.5?1:-1));
      y1 += (y1*0.2*(Math.random()>0.5?1:-1));
    }
    self.tween = [GLOBAL_FWC.animator.createTween(self.x,x1,self.tweenType),GLOBAL_FWC.animator.createTween(self.y,y1,self.tweenType)];
    // prevent X overflow (scrolling)
    var xMax = GLOBAL_FWC.canvasX-GLOBAL_FWC.particleXY;
    var yMax = GLOBAL_FWC.canvasY-GLOBAL_FWC.particleXY;
    var xMin = GLOBAL_FWC.particleXY-self.baseX;
    var yMin = GLOBAL_FWC.scrollY;
    var toX = null;
    var toY = null;
    if (self.obeyBoundaries) {
      for (var i=self.tween[0].length; i--;) {
        // bounce off walls where applicable
        toX = self.tween[0][i].data+self.baseX;
        toY = self.tween[1][i].data+self.baseY;
        if (toX>=xMax) {
          self.tween[0][i].data -= (toX-xMax)*2;
          // self.tween[0][i].event = 'bounce';
        } else if (toX<0) {
          self.tween[0][i].data -= (toX*2);
          // self.tween[0][i].event = 'bounce';
        }
        if (toY>=yMax) {
          self.tween[1][i].data -= (toY-yMax)*2;
          // self.tween[1][i].event = 'bounce';
        } else if (toY-yMin<=0) {
          self.tween[1][i].data -= (toY-yMin)*2;
          // self.tween[1][i].event = 'bounce';
        }
      }
    }
  };

  this.animate = function() {
    var f0 = self.tween[0][self.frame].data;
    var f1 = self.tween[1][self.frame].data;
    self.moveTo(f0,f1);
    // possible bounce event/sound hooks

    if (self.frame++>=self.tween[0].length-1) {
      if (self._oncomplete) {
        self._oncomplete();
      }
      self._oncomplete = null;
      self.active = false;
      self.frame = 0;
      return false;
    } else if (self.frame>10) {
      self.nextState();
    }
    return true;
  };

  this.destructor = function() {
    self.oImg = null;
    self.oC.removeChild(self.o);
    self.oC = null;
    self.o = null;
  };

  this.setType = function(t) {
    self.type = t;
    switch(t){
    	case 1: 	self.oImg.style.color = "white"; 	break;
    	case 2: 	self.oImg.style.color = "red"; 		break;
    	case 3: 	self.oImg.style.color = "yellow"; 	break;
    	case 4: 	self.oImg.style.color = "green"; 	break;
    	case 5: 	self.oImg.style.color = "blue"; 	break;
    	case 6: 	self.oImg.style.color = "cyan"; 	break;
    	case 7: 	self.oImg.style.color = "magenta"; 	break;
    }
    //self.oImg.style.marginTop = -(GLOBAL_FWC.particleXY*t)+'px';
  };

  self.setType(type);
  self.oC.appendChild(self.o);

}

function createFirework(nRadius,nParticles,nCircles,nBurstType,startX,startY,burstX,burstY,allowRandom,obeyBoundaries) {

  // check all arguments, supply random defaults if needed
  var tmp = '';
  for (var i in arguments) {
    if (arguments.hasOwnProperty(i)) {
      tmp += i+',';
    }
  }
  writeDebug('createFirework('+tmp+')');

  if (GLOBAL_FWC.isEmpty(startX)) {
    startX = parseInt(Math.random()*GLOBAL_FWC.canvasX, 10);
  } else {
    startX = parseInt(GLOBAL_FWC.canvasX*startX/100, 10);
  }

  if (GLOBAL_FWC.isEmpty(startY)) {
    startY = GLOBAL_FWC.canvasY-GLOBAL_FWC.particleXY;
  } else {
    startY = GLOBAL_FWC.canvasY-GLOBAL_FWC.screenY+parseInt(GLOBAL_FWC.screenY*startY/100, 10);
  }

  if (GLOBAL_FWC.isEmpty(burstX)) {
    burstX = parseInt(GLOBAL_FWC.canvasX*0.1+(Math.random()*GLOBAL_FWC.canvasX*0.8), 10);
  } else {
    burstX = parseInt(GLOBAL_FWC.canvasX*burstX/100, 10);
  }

  if (GLOBAL_FWC.isEmpty(burstY)) {
    burstY = GLOBAL_FWC.canvasY-parseInt(Math.random()*GLOBAL_FWC.screenY, 10);
  } else {
    burstY = GLOBAL_FWC.canvasY-parseInt(GLOBAL_FWC.screenY*(100-burstY)/100, 10);
  }

  if (GLOBAL_FWC.isEmpty(nBurstType)) {
    nBurstType = parseInt(Math.random()*GLOBAL_FWC.particleTypes, 10);
  }

  if (GLOBAL_FWC.isEmpty(nRadius)) {
    nRadius = 64+parseInt(Math.random()*GLOBAL_FWC.screenY*0.75, 10);
  } else if (nRadius.toString().indexOf('%')>=0) {
    nRadius = parseInt(parseInt(nRadius, 10)/100*GLOBAL_FWC.screenY, 10);
  } else if (nRadius.toString().indexOf('.')>=0) {
    nRadius = parseInt(nRadius*GLOBAL_FWC.screenY, 10);
  } else {
    nRadius = parseInt(nRadius*GLOBAL_FWC.screenY/100, 10);
  }

  if (GLOBAL_FWC.isEmpty(nParticles)) {
    nParticles = 4+parseInt(Math.random()*64, 10);
  }

  if (GLOBAL_FWC.isEmpty(nCircles)) {
    nCircles = Math.random()>0.5?2:1;
  }

  if (GLOBAL_FWC.isEmpty(allowRandom)) {
    allowRandom = Math.random()>0.5;
  }

  if (GLOBAL_FWC.isEmpty(obeyBoundaries)) {
    obeyBoundaries = Math.random()>0.5;
  }  

  // update screen coordinates
  GLOBAL_FWC.getWindowCoords();

  GLOBAL_FWC.fireworks[GLOBAL_FWC.fireworks.length] = new Firework(document.getElementById('fireContainer'),startX,startY,burstX,burstY,nBurstType,nRadius,nParticles,nCircles,allowRandom,obeyBoundaries);

}

// create null objects if APIs not present
if (typeof(writeDebug)=='undefined') {
  window.writeDebug = function() {
    return false;
  };
}

function addEventHandler(o,evtName,evtHandler) {
  return (typeof(attachEvent)=='undefined'?o.addEventListener(evtName,evtHandler,false):o.attachEvent('on'+evtName,evtHandler));
}

function removeEventHandler(o,evtName,evtHandler) {
  return (typeof(attachEvent)=='undefined'?o.removeEventListener(evtName,evtHandler,false):o.detachEvent('on'+evtName,evtHandler));
}
