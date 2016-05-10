/*
 * (C) Copyright IBM Corp. 2016
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

var w = window.innerWidth;
var h = window.innerHeight;
var diameter = 900,
    radius = diameter / 2,
    innerRadius = radius / 2;

var translateWidth = 2.1;
var translateHeight = 2.1;

var json_msg = undefined;
    
function updateTitleBroken(newText){
	$('#header').html(newText);
	$('#debug-header').html("");
    $( "#loader-wrapper" ).hide();
    $( "#loader" ).hide();
	ws.close();
};
    
function drawGraph(serviceWiring){
    function mouseovered(d) {
      node
          .each(function(n) { n.target = n.source = false; });

      link
          .classed("link--target", function(l) { if (l.target === d) return l.source.source = true; })
          .classed("link--source", function(l) { if (l.source === d) return l.target.target = true; })
        .filter(function(l) { return l.target === d || l.source === d; })
          .each(function() { this.parentNode.appendChild(this); });

      node
          .classed("node--target", function(n) { return n.target; })
          .classed("node--source", function(n) { return n.source; });
    }

    function mouseouted(d) {
      link
          .classed("link--target", false)
          .classed("link--source", false);

      node
          .classed("node--target", false)
          .classed("node--source", false);
    }
    
    d3.selectAll("svg").remove();

    var cluster = d3.layout.cluster()
        .size([360, innerRadius])
        .sort(null)
        .value(function(d) { return d.size; });

    var bundle = d3.layout.bundle();

    var line = d3.svg.line.radial()
        .interpolate("bundle")
        .tension(0.95)
        .radius(function(d) { return d.y; })
        .angle(function(d) { return d.x / 180 * Math.PI; });

    var svg = d3.select("#topology").append("svg")
        .attr("width", w)
        .attr("height", h)
        .append("g")
        .attr("transform", "translate(" + w/translateWidth + "," + h/translateHeight + ")");

    var link = svg.append("g").selectAll(".link"),
        node = svg.append("g").selectAll(".node");

    var classes = computeClasses(serviceWiring);
        classes = sortClasses(classes);

    styleClasses(classes);

    var nodes = cluster.nodes(packageHierarchy(classes)),
      links = packageImports(nodes);

    link = link
      .data(bundle(links))
      .enter().append("path")
      .each(function(d) { d.source = d[0], d.target = d[d.length - 1]; })
      .attr("class", "link")
      .attr("d", line);

    node = node
      .data(nodes.filter(function(n) { return !n.children; }))
      .enter().append("text")
      .attr("class", function(d){
            if(d.imports.length == 0){
                return "node-with-neighbours";
            }
            return "node";
        })
      .attr("dy", ".31em")
      .attr("transform", function(d) { return "rotate(" + (d.x - 90) + ")translate(" + (d.y + 8) + ",0)" + (d.x < 180 ? "" : "rotate(180)"); })
      .style("text-anchor", function(d) { return d.x < 180 ? "start" : "end"; })
      .text(function(d) { 
        //console.log(d.key);
        return d.key; 
        })
      .on("mouseover", mouseovered)
      .on("mouseout", mouseouted);
    
    
  d3.select(self.frameElement).style("height", diameter + "px");
}




// Lazily construct the package hierarchy from class names.
function packageHierarchy(classes) {
  var map = {};

  function find(name, data) {
    var node = map[name], i;
    if (!node) {
      node = map[name] = data || {name: name, children: []};
      if (name.length) {
        node.parent = find(name.substring(0, i = name.lastIndexOf(".")));
        node.parent.children.push(node);
        node.key = name.substring(i + 1);
      }
    }
    return node;
  }

  classes.forEach(function(d) {
    find(d.name, d);
  });

  return map[""];
}

// Return a list of imports for the given array of nodes.
function packageImports(nodes) {
  var map = {},
      imports = [];

  // Compute a map from name to node.
  nodes.forEach(function(d) {
    map[d.name] = d;
  });

  // For each import, construct a link from the source to target node.
  nodes.forEach(function(d) {
    if (d.imports) d.imports.forEach(function(i) {
      imports.push({source: map[d.name], target: map[i]});
    });
  });

  return imports;
}

function computeClasses(queryResult){
    var toHide = ["MQTT","WSA","TOPOLOGY","$"];
    var computedClasses = new Array();

    $.each(queryResult["service-wiring"], function(key,value){
        var add = 1;

        $.each(toHide, function(hkey,hideValue){
            if(value["from-system"].indexOf(hideValue) != -1 || value["to-system"].indexOf(hideValue) != -1){
                //HIDE IT!
                add = 0;
            }
        });

        if(add == 1){
            var from = value["from-platform"] + "/" + value["from-system"] + "/" + value["from-interface"];
            var to = value["to-platform"] + "/" + value["to-system"] + "/" + value["to-interface"];

            computedClasses.push({"name":from,"imports":[to]});
            computedClasses.push({"name":to,"imports":[]});
        }

        add = 1;
    });
    return computedClasses;
}
    
function sortClasses(classes){
    var sortedClasses = [];
    
    classes.sort(function (a, b) {
        
      a=a.name;
      b=b.name;

      if (a > b) {
        return 1;
      }
      if (a < b) {
        return -1;
      }
      // a must be equal to b
      return 0;
    });
    
    classes.reverse();
    
    return classes;
}
    
    
function styleClasses(classes){
    $.each(classes, function(key,thisClass){
        if(thisClass.imports.length > 0){
        }
    });
}

function init(){
    updateHeightandWidth();
	this.ws = new WebSocket("ws://"+window.location.hostname+":"+window.location.port+"/fabric.tools.rest/nodeviewer");
	this.startWS();
};
    
    
var startWS = function() {
	ws.onopen = function() {
		console.log("WS connected.");
	};
	
	ws.onmessage = function(evt) {
		var received_msg = evt.data;
		
		//Debug - Log all messages received
	 	console.log("Received: ", received_msg);

		/**
		* When the MQTT client is connected, update front end and send request for wiring info.
		*/
		if(received_msg.indexOf("WSConnected") >= 0){
            //Find out who we are first
			ws.send("{\"op\":\"query:service-wiring\"}");
		}
        
        //Draw the wiring and hide the loader
		if(received_msg.indexOf("query-result:service-wiring") >= 0){
                json_msg = JSON.parse(received_msg);

                drawGraph(json_msg);
                var wiringRefresh = setTimeout(function(){
                    ws.send("{\"op\":\"query:service-wiring\"}");
                }, 15000);
            
                $( "#loader-wrapper" ).hide();
                $( "#loader" ).hide();
		}
	};
	
	
	/**
	* Todo - Make this update the front end so user doesn't have to refresh?
	*/
	ws.onclose = function() {
        updateTitleBroken("Websocket lost. Please refresh page.");
		console.log("WS disconnected.");
		//setTimeout(function() { this.init(); }, 3000);
	};

	ws.onerror = function(evt) {
		console.log("WS Error: ",evt);
	};
};



//This is the method called whenever the window is resized
var resizeTimeout;
function updateHeightandWidth(){
    w = window.innerWidth;
    h = window.innerHeight;
    diameter=h/1.5;
    radius = diameter / 2,
    innerRadius = radius / 2;
    
    $("#loader").css({left: (w/2.2), top: (h/3), position:'absolute'});
    console.log("drawGraph");
    if(json_msg!=undefined){
        drawGraph(json_msg);
    }
}
