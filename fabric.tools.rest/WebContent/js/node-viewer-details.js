/*
 * (C) Copyright IBM Corp. 2016
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */


/**
 * Based on the following d3 example:
 * https://bl.ocks.org/mbostock/1153292
 */

var debugMode = false;
var graphDrawn = false;
var smallScreenWidth = 1080;

var svg;
var path;
var circle;
var text;
var force;

var width = window.innerWidth - 315,
    height = window.innerHeight - 80;
var linkDistance = 175,
    charge = -1000;

function drawGraph(links) {
	var nodes = computeNodes(links);
    graphDrawn = true;

    //Set the 'charge' and 'distance' parameters differently if the type is 'neighbour'
	force = d3.layout.force()
		.nodes(d3.values(nodes))
		.links(links)
		.size([width, height])
		.linkDistance(
            function(d) {
                if (d.type == "neighbour") {
                    return linkDistance;
                } else {
                    return linkDistance/2;
                }
            })
		.charge(
	        function(d) {
                if (d.type == "neighbour") {
                    return charge*2;
                } else {
                    return charge;
                }
	         })
		.on("tick", tick)
		.start();

    svg = d3.select("#topology").append("svg")
		.attr("width", width)
		.attr("height", height);

    /**
     * Append markers (arrows), as they don't inherit styles.
     */
	svg.append("defs").selectAll("marker")
		.data(["available","unavailable"])
	    .enter().append("marker")
		.attr("id", function(d) { return d; })
		.attr("viewBox", "0 -5 10 10")
		.attr("refX", 32)
		.attr("refY", -1)
		.attr("markerWidth", 4)
		.attr("markerHeight", 4)
		.attr("orient", "auto")
	    .append("path")
		.attr("d", "M0,-5L10,0L0,5");

	path = svg.append("g").selectAll("path")
		.data(force.links())
	    .enter().append("path")
	    .attr("id", function(d) { return d.source.name + ":" + d.target.name; })
		.attr("class", function(d) {
            //If we are a neighbour, base link colour off of availability. If not, use the type
            if(d.source.type == "neighbour" && d.target.type == "neighbour"){
                return "link " + d.availability; 
            }
            return "link " + d.type; 
        })
		.attr("marker-end", function(d) { 
            //Stop marker end from showing up if we only have one node
            if(d.source.name == d.target.name){
                return "url(#" + "NA" + ")"; 
            } else {
                return "url(#" + d.availability + ")"; 
            }
        });

    circle = svg.append("g").selectAll("circle")
        .data(force.nodes())
        .enter()
        .append("svg:a")
        .attr("xlink:href", function(d){
            //Create clickable link to other pages, only for neighbours. Assumes that web server
        	//is available on the same IP with a port of 8080
            var nodeQueryResults = JSON.parse(tempNodeQueryResult);
        	var nodeIP;
        	
	        $.each(nodeQueryResults.nodes, function(key, value) {
		         if(nodeQueryResults.nodes[key].id == d.name){
		        	 nodeIP = nodeQueryResults.nodes[key].interfaces[0].address;
		        	 nodeIP = nodeIP.substring(5,nodeIP.length).split(":");
		        	 nodeIP = nodeIP[0];
		        	 return false;
		         }
		    });
            
	        if (nodeIP !== undefined){
	            if (d.type == "neighbour") {
	                if (d.name == thisNodeName) {
	                	return "http://"+nodeIP+":8080/fabric.tools.rest/services-viewer.html"
	                } else {
	                	return "http://"+nodeIP+":8080/fabric.tools.rest/node-viewer-details.html"
	                }
	            }
	        }
        })
        .attr("xlink:show", "new")
        .append("circle")
        .attr("id", function(d) { return d.name; })
        .attr("r", nodeSizeTransform)
        .attr("class", function(d) { return "circle " + d.type; })
        .call(force.drag);

    //Add labels to the nodes
	text = svg.append("g").selectAll("text")
		.data(force.nodes())
	    .enter()
        .append("text")
	    .attr("id", function(d) { return d.name; })
        .attr("class", function(d) { 
            if (d.name == thisNodeName) {
                return "this-node";
            }
        })
		.attr("x", 22)
		.attr("y", ".31em")
		.text(function(d) { 
			//Split up string and only display suffix if required
            if (d.name.indexOf("/") == -1) {
                return d.name; 
            } else {
                var str = d.name.split("/");
                return str[str.length-1];
            }
        });
}

/**
 * D3 specifics
 */
function tick() {
  path.attr("d", linkArc);
  circle.attr("transform", transform);
  text.attr("transform", transformText);
}

function linkArc(d) {
  var dx = d.target.x - d.source.x,
	  dy = d.target.y - d.source.y;
  if (d.type == "neighbour") {
      dr = Math.sqrt(dx * dx + dy * dy)*1.5;
  } else {
      dr = Math.sqrt(dx * dx + dy * dy)*1000;
  }
	  
  return "M" + d.source.x + "," + d.source.y + "A" + dr + "," + dr + " 0 0,1 " + d.target.x + "," + d.target.y;
}

function transformText(d) {
  return "translate(" + textOffsetX(d) + "," + d.y + ")";
}

function transform(d) {
  return "translate(" + d.x + "," + d.y + ")";
}

/**
 * Overrides the hiding of the debug menu when the screen width is too small by pressing 'd'.
 */
$(document).keypress(function(e) {
  //Toggle debug mode, stop from accidentally messing up display
  if (e.charCode == 100 || e.charCode == 68) {
      debugMode = !debugMode;
      if (debugMode) {
          $("#debug").show();
      } else {
          if(window.width<smallScreenWidth){
              $("#debug").hide();
          }
      }
  }
});

/**
 * Called whenever the graph needs to be redrawn. Will compute nodes/links, remove the old
 * graph and draw the new one in its place.
 */
function redrawAllTheThings() {
    parsedResults = parseNeighbours(latestNeighbourQueryResult);
    parsedResults = parsePlatforms(JSON.stringify(parsedResults), latestPlatformQueryResult);
    parsedResults = parseSystems(JSON.stringify(parsedResults), latestSystemQueryResult);
    links = computeLinks(parsedResults);

    d3.selectAll("svg").remove();
    drawGraph(links);
}


/**
 * Transformation functions help to size and align text and nodes
 */
function nodeSizeTransform(d) {
    var nodeSize = 15;
    
      switch (d.type) {
          case "neighbour":
            return 22.5;

          case "platform":
            return nodeSize/1.5;

          case "system":
            return nodeSize/2.5;
              
          default:
            return 22.5;
      }
}

function textOffsetX(d) {
    switch (d.type) {
      case "neighbour":
        return d.x+3;

      case "platform":
        return d.x-9;

      case "system":
        return d.x-13;

      default:
    	  return d.x+1;
  }
}

/**
 * Aligns everything on screen according to height and width
 */
var resizeTimeout;
function updateHeightandWidth() {
    clearTimeout(resizeTimeout);
    
    if(window.innerWidth < smallScreenWidth) {
        if (!debugMode) {
            $("#debug").hide();
        }
        width = window.innerWidth;
        height = window.innerHeight;
    } else {
        $("#debug").show();
        width = window.innerWidth - 315;
        height = window.innerHeight - 80;
    }
    
    var topologyWidth = $("#topology").width();
    var progressWidth = $("#progress").width();
    progressBarLeftPos = (width - progressWidth) / 2;
    $("#progress").css({left: progressBarLeftPos, top: (height/3+100)});
    $("#progressText").css({left: progressBarLeftPos, top: (height/3+100)});
    
	//This prevents the redraw from happening lots of times on a single resize
    resizeTimeout = setTimeout(function() {
        if (graphDrawn) {
            redrawAllTheThings();
        }        
    }, 500);
}

/**
 * Fills the progress bar to a given percentage and displays an informative message
 */
function updateProgressBar(percentage, message) {
    $('#progressBar').width(percentage + "%");
    $('#progressText').html(message);
}
