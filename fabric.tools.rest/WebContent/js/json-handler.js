/*
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

var ws;

function init(){
	this.ws = new WebSocket("ws://"+window.location.hostname+":"+window.location.port+"/fabric.tools.rest/json");
	this.startWS();
};

var startWS = function() {
	ws.onopen = function() {
		console.log("WS connected.");	
	};
	
	ws.onmessage = function(evt) {
		var received_msg = evt.data;
		console.log("WS Received: ", received_msg);
		var now = new Date();
		var date = now.getDate() + "/" +(now.getMonth()+1) + "/" + now.getFullYear() 
			+ "@" + now.getHours() + ":" + now.getMinutes()+":"+now.getSeconds();
	
		$('#incoming').append($("<tr>\n")
				 .append("<td>"+received_msg+"</td>\n")
				 .append("<td>"+date+"</td>\n")
				 .append("</tr>"));
	 	console.log("Received: ", received_msg);
	};
	
	ws.onclose = function() {
		console.log("WS disconnected.");
		setTimeout(function() { this.init(); }, 3000);
	};

	ws.onerror = function(evt) {
		console.log("WS Error: ",evt);
	};
};

this.init();

function sendJson() {
	var jsonList = document.getElementById("jsonInput").value;
	var depthCount = 0;
	var start = 0;
	var end = 0;
	var jsonArray = [];
	
	for(json in jsonList){
		
		if(jsonList[json] === "{"){
			if(depthCount === 0){
				start = json;
			}
			depthCount++;
		}
		
		if(jsonList[json] === "}"){
			depthCount--;
			if(depthCount === 0){
				end = json;
				jsonArray.push(jsonList.slice(start, ++end));
			}
		}
	}
	
	for(json in jsonArray){
		ws.send(jsonArray[json]);
	}
};

function clearTable() {
	$('#incoming tbody').remove();
};
