/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

var hostname = window.location.hostname;
var port = window.location.port;
var ws = new WebSocket("ws://"+hostname+":"+port+"/rest/mqtt");
var host = "";

ws.onopen = function() {
	console.log("Connected to websocket");
	setPlaceHolderBrokerURI();
};

ws.onmessage = function(evt) {
	var received_msg = evt.data;
	onMessageArrived(received_msg);
};

ws.onclose = function() {
	console.log("Connection is closed.");
};

function connectToBroker(){
	host = $('#brokerURI').val();
	if(validateHost(host)){
		ws.send(host);
		refreshBrokerTitle();
	} else{
		alert('Invalid hostname ' + host + ' , please try again: tcp://hostname:port');
	}
};

function onMessageArrived(message) {
	try{
		var obj = $.parseJSON(message);
		addNode(obj.topic, obj.payloadString);
	} catch(e){
		console.log(e);
	}
};

function refreshBrokerTitle(){
	$('#brokerURI').hide();
	$('#connect').hide();
	$('#title').html("Connected to: " + host);
	toggleSubscriptionOption(true);
};

function setPlaceHolderBrokerURI(){
	var brokerURI = "tcp://"+this.hostname+":1883";
	if(validateHost(brokerURI)){
		$('#brokerURI').val(brokerURI);
	}
	toggleSubscriptionOption(false);
};

function subscribeToTopic(){
	topic = $('#subscribeToTopic').val();
	if(validateTopic(topic)){
		ws.send(topic);
		alert('Subscription attempt to topic ' + topic + ' has been sent');
	} else {
		alert('Invalid topic name: ' + topic + ', please try again e.g. topic/#');
	}
};

function toggleSubscriptionOption(displayOption){
	if(displayOption){
		$('#subscribeToTopic').show();
		$('#subscribeButton').show();
	} else {
		$('#subscribeToTopic').hide();
		$('#subscribeButton').hide();
	}
};

function validateTopic(topic){
	if(topic.length == 0){
		return false;
	}
	return true;
};

function validateHost(hostname){
	if(hostname.indexOf("tcp://")!=0){
		console.log(hostname.indexOf("tcp://"));
		return false;
	} 
	var port = hostname.split(":")[2].trim();
	if(port == undefined || port.match(/^[0-9]+$/) == null ||  port.length < 2){
		console.log(port.match("/^[0-9]+$/"));
		return false;
	}
	
	return true;
};