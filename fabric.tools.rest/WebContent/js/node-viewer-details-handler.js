/*
 * (C) Copyright IBM Corp. 2016
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */
var neighbourTimeout,
    systemTimeout;
var timeoutInterval = 15000;
var systemTimeoutInterval = 3000;

var platform = "TOPOLOGY_VISUALISATION" + Math.floor(Date.now() / 1000);
var thisNodeName,
    nodes,
    parsedResults,
    links,
    oldLinks;

var firstTimeSystem = true;

var latestNodeQueryResult = "",
	latestNeighbourQueryResult = "",
    latestPlatformQueryResult = "",
    latestSystemQueryResult = "";

var tempNodeQueryResult,
	tempNeighbourQueryResult,
    tempPlatformQueryResult,
    tempSystemQueryResult;

function init() {
	this.ws = new WebSocket("ws://"+window.location.hostname+":"+window.location.port+"/fabric.tools.rest/nodeviewer");
	this.startWS();
};

/**
 * Helper functions for sending websocket requests
 */
function initSubscriptions() {
	ws.send("{\"op\":\"register:service-type\",\"type\":\"REGISTRY_UPDATES\",\"mode\":\"input-feed\",\"correl\":\"1\"}");
	ws.send("{\"op\":\"register:system-type\",\"type\":\"REGISTRY\",\"services\":[{\"type\":\"REGISTRY_UPDATES\"}],\"desc\":\"Systemtoreceiveregistryupdates\",\"correl\":\"2\"}");
	ws.send("{\"op\":\"register:platform-type\",\"type\":\"FABRIC_NOTIFICATIONS\",\"desc\":\"Fabricnotifications\",\"correl\":\"3\"}");
	ws.send("{\"op\":\"register:platform\",\"id\":\""+platform+"\",\"type\":\"FABRIC_NOTIFICATIONS\",\"desc\":\"Visualisecurrentnodesandtheirneighbours\",\"correl\":\"4\"}");
	ws.send("{\"op\":\"register:system\",\"id\":\""+platform+"/REGISTRY\",\"type\":\"REGISTRY\",\"correl\":\"5\"}");
	ws.send("{\"op\":\"state:system\",\"id\":\""+platform+"/REGISTRY\",\"state\":\"running\",\"correl\":\"6\"}");
}

function getNodes() {
    ws.send("{\"op\":\"query:nodes\",\"correl\":\"8\"}");
}

function getNeighbours() {
    ws.send("{\"op\":\"query:neighbours\",\"correl\":\"9\"}");
}

function getPlatforms() {
    ws.send("{\"op\":\"query:platforms\",\"correl\":\"10\"}");
}

function getSystems() {
    ws.send("{\"op\":\"query:systems\",\"correl\":\"11\"}");
}

function updateNodes() {
	ws.send("{\"op\":\"query:nodes\",\"correl\":\"12\"}");
}


/**
 * Title update functions
 */
function updateDebugHeader(newText) {
	$('#debug-header').html(newText);
};

function updateTitleBroken(newText) {
	$('#header').html(newText);
	$('#debug-header').html("");
    $( "#progress" ).hide();
    $( "#progressBar" ).hide();
    $( "#progressText" ).hide();
	ws.close();
};


/**
 * Main websocket function which defines message processing etc.
 */
var startWS = function() {
	ws.onopen = function() {
		console.log("WS connected.");
	};
	
	ws.onmessage = function(evt) {
		var received_msg = evt.data;
        var jsonMessage = evt.data;
        try {
            jsonMessage = JSON.parse(received_msg);
        } catch(e) {
            console.log(e);
        }
		
		//For debugging purposes, log out all received messages
	 	console.log("Received: ", received_msg);

		/**
		* When the MQTT client is connected, update front end and send request
		* for neighbour information.
		*/
		if (received_msg.indexOf("WSConnected") >= 0) {
            updateProgressBar(8, "Websocket Connected");
            //Find out who we are first
			ws.send("{\"op\":\"query:local-node\"}");
            
			//"Heartbeat" to keep web socket alive
			var intervalID = setInterval(function(){ws.send("{\"op\":\"query:local-node\"}");}, 60000);
		}
        
        //First time we find out about the local node, save the node name and initialise our subscriptions
        if(jsonMessage.op=="query-result:local-node" && thisNodeName == undefined) {
            var json = $.parseJSON(received_msg);
            thisNodeName = json.id;
            updateProgressBar(16, "Local node " + thisNodeName + " discovered");
            initSubscriptions();
		}
        
        //Correl 1-7 are related to registering systems and setting up subscriptions
		if (jsonMessage.correl==1) {
			if(jsonMessage.msg != "OK") {
				updateTitleBroken("Subscription did not complete correctly. Please refresh page.");
			} else {
				updateProgressBar(24, "Service Type registered");
			}
		}
        
        if (jsonMessage.correl==2) {
			if(jsonMessage.msg != "OK") {
				updateTitleBroken("Subscription did not complete correctly. Please refresh page.");
			} else {
				updateProgressBar(32, "System Type registered");
			}
		}
        
        if (jsonMessage.correl==3) {
			if(jsonMessage.msg != "OK") {
				updateTitleBroken("Subscription did not complete correctly. Please refresh page.");
			} else {
				updateProgressBar(40, "Platform Type registered");
			}
		}
        
        if (jsonMessage.correl==4) {
			if(jsonMessage.msg != "OK") {
				updateTitleBroken("Subscription did not complete correctly. Please refresh page.");
			} else {
				updateProgressBar(48, "Platform registered");
			}
		}
        
        if (jsonMessage.correl==5) {
			if(jsonMessage.msg != "OK") {
				updateTitleBroken("Subscription did not complete correctly. Please refresh page.");
			} else {
				updateProgressBar(56, "System registered");
			}
		}
		
		//Correl 6 is us starting up the service to subscribe to updates.
		//We need to know that this has started before subscribing
		if (jsonMessage.correl==6) {
			if(jsonMessage.msg != "OK") {
				updateTitleBroken("Subscription did not complete correctly. Please refresh page.");
			} else {
                updateProgressBar(64, "System started");
				ws.send("{\"op\":\"subscribe\",\"output-feeds\":[\"$fab/$reg/$updates\"],\"input-feed\":\""+platform+"/REGISTRY/REGISTRY_UPDATES\",\"correl\":\"7\"}");
			}
		}
		
		//Correl 7 is our subscription. We need to know that this is set up correctly before running the initial query for neighbours
		if (jsonMessage.correl==7) {
			if (received_msg.indexOf(platform) >= 0 && received_msg.indexOf("\"output-feeds\":[]") == -1) {
				//Now, and only now we can send our query
                updateProgressBar(72, "Querying for nodes");
                getNodes();
			} else {
				updateTitleBroken("Subscription did not complete correctly. Please refresh page.");
			}
			//Register a refresh event when the 'r' key is pressed, but only 
			//once we have drawn the graph for the first time
            $(document).keypress(function(e) {
                if (e.charCode == 114) {
                    console.log(e);
                    getNodes();
                }
            });
		}
		
        //Result from get nodes
        if (jsonMessage.correl==8) {
            tempNodeQueryResult = received_msg;
            if (compareStrings(tempNodeQueryResult, latestNodeQueryResult) == false) {
                latestNodeQueryResult = received_msg;
            }
            
            updateProgressBar(80, "Querying for neighbours");
            getNeighbours();
        }
        
        //Result from get neighbours
        if (jsonMessage.correl==9) {
            //This is the minimum we need to draw a graph
            tempNeighbourQueryResult = received_msg;
            if (compareStrings(tempNeighbourQueryResult, latestNeighbourQueryResult) == false) {
                latestNeighbourQueryResult = received_msg;
            }
            
            parsedResults = parseNeighbours(latestNeighbourQueryResult);
            updateProgressBar(88, "Querying for platforms");
            getPlatforms();
        }
        
        //Result from get platforms
        if (jsonMessage.correl==10) {
            tempPlatformQueryResult = received_msg;
            if (compareStrings(tempPlatformQueryResult, latestPlatformQueryResult) == false) {
                latestPlatformQueryResult = received_msg;
            }
            
            parsedResults = parsePlatforms(JSON.stringify(parsedResults), latestPlatformQueryResult);
            updateProgressBar(96, "Querying for systems");
            getSystems();
        }
        
        //Result from get systems
        if (jsonMessage.correl==11) {
        	//Hide progress bars as we are nearly ready to display graph
            $( "#progress" ).hide();
            $( "#progressBar" ).hide();
            $( "#progressText" ).hide();
            
            tempSystemQueryResult = received_msg;
            if (compareStrings(tempSystemQueryResult, latestSystemQueryResult) == false) {
                latestSystemQueryResult = received_msg;
            }
            parsedResults = parseSystems(JSON.stringify(parsedResults), latestSystemQueryResult);
            
            var tempLinks = links;
            links = computeLinks(parsedResults);

            //This is to 'un-d3' the links (meta data gets added that needs to be removed)
            if (oldLinks == undefined) {
                oldLinks = links;
            } else {
                oldLinks = cleanLinks(tempLinks);
            }
            
            //Only redraw if our newly parsed results are different to the old set
            if (compareStrings(JSON.stringify(links), JSON.stringify(oldLinks)) == false || firstTimeSystem) {
                firstTimeSystem = false;
                redrawAllTheThings();
            }
            
            updateDebugHeader("Logging for " + thisNodeName);
        }
        
        //Incomming feed-message processing
        var latestNeighbourQueryResultBackup = latestNeighbourQueryResult;
        //When we get a neighbour message, remove knowledge of the neighbour from our neighbour set,
        //then recalculate using the other known data
        if (jsonMessage.op=="feed-message" && jsonMessage.msg.table=="NODE_NEIGHBOURS") {
            var date = new Date(jsonMessage.msg.timestamp);
            var nodeIds = jsonMessage.msg.id.split("/");
            if (nodeIds[1].indexOf(":") >= 0) {
                var tempIds = nodeIds[1].split(":");
                nodeIds[1] = tempIds[0];
            }

            //If we are deleting or marking a node as 'unavialable', remove it from our neighbour set
            if (jsonMessage.msg.action=="DELETE" || (jsonMessage.msg.action=="UPDATE" && jsonMessage.msg.availability=="UNAVAILABLE")) {
                latestNeighbourQueryResult = removeNeighbour(latestNeighbourQueryResult, received_msg);

                $('#debug').prepend($('<div class="debug-message"> ' + 
                '<span class="debug-message-date">'+date+'</span>' +
                '<span class="debug-message-action">Action: <text class=remove-node>Loss</text></span>' +
                '<span class="debug-message-source">'+nodeIds[0]+' has lost contact with '+nodeIds[1]+'</span>' +
                '</div>'));
            }
            
          //If we are inserting or marking a node as 'available', add it to our neighbour set
            if (jsonMessage.msg.action=="INSERT" || (jsonMessage.msg.action=="UPDATE" && jsonMessage.msg.availability=="AVAILABLE")) {
                latestNeighbourQueryResult = addNeighbour(latestNeighbourQueryResult, received_msg);

	 		    $('#debug').prepend($('<div class="debug-message"> ' + 
                '<span class="debug-message-date">'+date+'</span>' +
                '<span class="debug-message-action">Action: <text class=insert-node>Discovery</text></span>' +
                '<span class="debug-message-source">'+nodeIds[0]+' has discovered '+nodeIds[1]+'</span>' +
                '</div>'));
            }
            
            //Only redraw the whole thing if we've actually changed something
            if(compareStrings(latestNeighbourQueryResultBackup, latestNeighbourQueryResult) == false) {
                  //Update the nodes so that we have IP's to allocate to nodes
                  updateNodes();
            }
            
            //Clear the timeout and run the get neighbours query after the timeout period
            clearTimeout(neighbourTimeout);
            neighbourTimeout = setTimeout(function() {
                getNodes();
            }, timeoutInterval);
        }
        
        //Correl 12 only comes from updateNodes() which is only called if we needed to add something from a feed message
        if (jsonMessage.correl==12) {
        	redrawAllTheThings();
        }

        //Here we would add the new system and services to our model, however as we currently do not get passed the 
        //node ID with the message, we will just have to trigger the getPlatforms() query instead. Leave a delay of 3 seconds,
        //incase more than one comes along and we end up redrawing multiple times.
        if (jsonMessage.op=="feed-message" && jsonMessage.msg.table=="SYSTEMS") {
            
            //Clear the timeout and run the get neighbours query after the timeout period
            clearTimeout(systemTimeout);
            systemTimeout = setTimeout(function() {
                getNeighbours();
            }, systemTimeoutInterval);
        }
	};

	ws.onclose = function() {
        updateTitleBroken("Websocket lost. Please refresh page.");
		console.log("WS disconnected.");
	};

	ws.onerror = function(evt) {
		console.log("WS Error: ",evt);
	};
};

/**
 * Compute the distinct nodes from the links.
 */
function computeNodes(links) {
	var nodes = {};
	if (links.length == 0) {
		//If we're still empty, return just one
		nodes = {thisNodeName:{"name":thisNodeName}};
	} else {
		links.forEach(function(link) {
		  link.source = nodes[link.source] || (nodes[link.source] = {name: link.source, type: link.type});
		  link.target = nodes[link.target] || (nodes[link.target] = {name: link.target, type: link.type});
            
		  //Infer availability based on whether or not we have a 2-way link
		  //i.e. a node is reachable            
	 	  links.some(function(link2) {
	 		if (link.source !== undefined && link2.source !== undefined &&
	 				link.target !== undefined && link2.target!== undefined) {
                
                if (link.source.type == "neighbour" && link.target.type == "neighbour") {
                    if (link.source.name === link2.target.name && link.target.name === link2.source.name ||
                       link.source.name === link2.target && link.target.name === link2.source) {
                        link.availability = "available";
                        return true;
                    }
                } else {
                    link.availability = "NA";
                    return true;
                }
			}
		  });
		});
	}
	
	return nodes;
}

/**
 * Functions to parse the incoming neighbour, platform and system data
 */
function parseNeighbours(neighbours) {
    neighbours = JSON.parse(neighbours);
    if (neighbours.nodes.length > 0) {
        $.each(neighbours.nodes, function(key, value) {
            neighbours.nodes[key].platforms = new Array();
        });
    } else {
        //If we only have one, use the node name from query local
        neighbours.nodes.push({"id":thisNodeName,"neighbours":[{"neighbour":thisNodeName,"neighbourInterface":"eth0","nodeInterface":"eth0"}],"platforms":[]});
    }
    
    return neighbours;
}

function parsePlatforms(neighbours, platforms) {
    neighbours = JSON.parse(neighbours);
    platforms  = JSON.parse(platforms);
    var platformCount = 0;
    
    $.each(neighbours.nodes, function(nkey, neighbour) {
        
        $.each(platforms.platforms, function(pkey, platform) {
            if (neighbour.id == platform.node) {
                neighbours.nodes[nkey].platforms.push(platform);
                neighbours.nodes[nkey].platforms[platformCount].systems = new Array();
                platformCount++;
            }
        });
        platformCount = 0;
    });
    
    return neighbours;
}

function parseSystems(neighbours, systems) {
    neighbours = JSON.parse(neighbours);
    systems    = JSON.parse(systems);
    
    var systemCount = 0;
    $.each(neighbours.nodes, function(nkey, neighbour) {
        $.each(neighbour.platforms, function(pkey, platform) {
            $.each(systems.systems, function(skey, system) {
                if (platform.id == system.platform) {
                    system.id = system.platform + "/" + system.id;
                    if (neighbours.nodes[nkey].platforms[pkey].systems != undefined) {
                        neighbours.nodes[nkey].platforms[pkey].systems.push(system);
                    }
                    systemCount++;
                }
            });
            systemCount = 0;
        });
        platformCount = 0;
    });
    
    return neighbours;
}

/**
 * Compute the links that we will actually show. All links/nodes with a name prefix included
 * in the toHide array will not be displayed.
 */
function computeLinks(results) {
	var toHide = ["MQTT","WSA","TOPOLOGY","MA","HA"];
    var parsedResults = [];
    var link;
    var add = 1;

    $.each(results.nodes, function(nokey,node) {
        $.each(node.neighbours, function(nekey,neighbour) {
            if (node.id != null && neighbour != null) {
                link = {"source":node.id, "target":neighbour.neighbour, "availability":"unavailable", "type":"neighbour"};
            }
            parsedResults.push(link);
        });
        
        if (node.platforms != undefined) {
            $.each(node.platforms, function(pkey,platform) {
                //Assume that we want to add the platform until we find out otherwise
                add = 1;

                //Check if we need to add or not, based on the types that we want to hide
                $.each(toHide, function(key, toHide) {
                    if (node.id.indexOf(toHide) != -1 || platform.id.indexOf(toHide) != -1) {
                        add = 0;
                    }
                });
                
                if(add == 1) {
                    link = {"source":node.id, "target":platform.id, "availability":"unavailable", "type":"platform"};
                    parsedResults.push(link);
                }
                add = 1;

                if (platform.systems != undefined) {
                    $.each(platform.systems, function(skey,system) {
                    	//Assume that we want to add the system until we find out otherwise
                        add = 1;
                        //Check if we need to add or not, based on the types that we want to hide
                        $.each(toHide, function(key, toHide) {
                            if(platform.id.indexOf(toHide) != -1 || system.id.indexOf(toHide) != -1){
                                add = 0;
                            }
                        });
                        if(add == 1) {
                            link = {"source":platform.id, "target":system.id, "availability":"unavailable", "type":"system"};
                            parsedResults.push(link);
                        }
                        add = 1;
                    });
                }
            });
        }
    });
    
    return parsedResults;
}

/**
 * Helper functions for deciding whether or not we need to redraw based on new information
 */

function getCharacterCount(string) {
    var count = 0;
    if (string == undefined) {
        return count;
    }
    
    for (var x=0; x<string.length; x++) {
        count += string.charCodeAt(x);
    }
    
    return count;
}

function compareStrings(string1, string2) {
    var result1 = getCharacterCount(string1);
    var result2 = getCharacterCount(string2);
    
    if (result1 == result2) {
        return true;
    } else {
        return false;
    }
}

/**
 * Functions to add or remove neighbours
 */

function removeNeighbour(neighbourMessage, message) {
    var neighbours = JSON.parse(neighbourMessage);
    message = JSON.parse(message);
    
    var nodeIds = message.msg.id.split("/");
    var nodeToRemove = nodeIds[0];
    var neighbourToRemove = nodeIds[1];
    if (neighbourToRemove.indexOf(":") >= 0) {
        var tempIds = nodeIds[1].split(":");
        neighbourToRemove = tempIds[0];
    }
    $.each(neighbours.nodes, function(nokey, node) {
        if (node.id == nodeToRemove) {
            $.each(node.neighbours, function(nekey, neighbour) {
                if (neighbour != undefined) {
                    if (neighbour.neighbour == neighbourToRemove) {
                        neighbours.nodes[nokey].neighbours.splice(nekey,1);
                    }
                }
            });
        }
    });
    
    neighbourMessage = JSON.stringify(neighbours);
    return neighbourMessage;
}


function addNeighbour(neighbourMessage, message) {
    var needToAddNode = -1;
    var needToAddNeighbour = -1;
    var neighbours = JSON.parse(neighbourMessage);
    message = JSON.parse(message);
    
    var nodeIds = message.msg.id.split("/");
    var nodeToAdd = nodeIds[0];
    var neighbourToAdd = nodeIds[1];
    if (neighbourToAdd.indexOf(":") >= 0) {
        var tempIds = nodeIds[1].split(":");
        neighbourToAdd = tempIds[0];
    }
    $.each(neighbours.nodes, function(nokey, node) {
        if (node.id == nodeToAdd) {
            needToAddNode = nokey;
            $.each(node.neighbours, function(nekey, neighbour) {
                if (neighbour != undefined) {
                    if (neighbour.neighbour == neighbourToAdd) {
                        needToAddNeighbour = nekey;
                    }
                }
            });
        }
    });
    
    if (needToAddNode == -1) {
        needToAddNode = (neighbours.nodes.push({"id":nodeToAdd,"neighbours":[]}) - 1);
    }
    
    if (needToAddNeighbour == -1) {
        neighbours.nodes[needToAddNode].neighbours.push({"neighbour":neighbourToAdd,"neighbourInterface":"eth0","nodeInterface":"eth0"});
    }
    
    neighbourMessage = JSON.stringify(neighbours);
    return neighbourMessage;
}


/**
 * Cleans links that have meta data that we no longer want
 */

function cleanLinks(linksWithData) {
    var links = [];
    
    //Check to see if we truly are 'dirty' i.e. have xy coordinates etc.
    if (linksWithData[0].source.name != undefined) {
        $.each(linksWithData, function(key,link) {
            links.push({source:link.source.name,target:link.target.name,availability:"unavailable",type:link.target.type});
        });

        return links;
    } else {
        return linksWithData;
    }
}