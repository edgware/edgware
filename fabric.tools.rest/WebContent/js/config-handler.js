/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

var dc_node = "#default_config";
var nc_node = "#node_config";
var default_table = "DEFAULT_CONFIG";
var node_table = "NODE_CONFIG";
var default_config_json = "{\"op\":\"sql-select\",\"sql-table\":\""+ default_table + "\"}";
var node_config_json = "{\"op\":\"sql-select\",\"sql-table\":\""+ node_table + "\"}";

function init(){
	this.ws = new WebSocket("ws://"+window.location.hostname+":"+window.location.port+"/rest/json");
	this.startWS();
};

var startWS = function() {
	ws.onopen = function() {
		console.log("WS connected");	
		addClickHandlers();
		updateDefaultConfig();
	};

	ws.onmessage = function(evt) {
		var data = $.parseJSON(evt.data);
		if(data.hasOwnProperty("default_config")){
			addDatabaseInformationToTable(data.default_config, true);
			updateNodeConfig();
		} else if(data.hasOwnProperty("node_config")){
			addDatabaseInformationToTable(data.node_config, false);
		} else{
			wasUpdateSuccessful(data);
		}
	};

	ws.onclose = function() {
		console.log("WS disconnected");
		setTimeout(function() { this.init(); }, 3000);
	};
	
	ws.onerror = function(evt) {
		console.log("WS Error: ",evt);
	};
}

this.init();

function addClickHandlers(){
	$("#submit").click(function(){
		var table = $('#table_name').val();
		var node = $('#node_value').val();
		var row = $('#key_value').val().split(":");
		updateSQL(node, row[0], table, false, row[1]);
	});
};

function addDatabaseInformationToTable(data, isDefault){
	if(Object.keys(data).length == 0){
		placeEmptyDataSetMessage(isDefault);
	}
	$.each(data, function(key, value){
		$.each(value, function(k, v){
			if(k != undefined){
				if(!isDefault){
					$.each(v, function(node_key, node_value){
						addRow(node_key, node_value, isDefault, key);
					});
				} else {
					addRow(k, v, isDefault);
				}
			} 
		});
	});
};

function placeEmptyDataSetMessage(isDefault){
	var html = "This table is currently empty";
	if(isDefault){
		$("#dc_empty").html(html);
	} else {
		$("#nc_empty").html(html);
	}
	
}
function updateDefaultConfig(){
	updating_dc = true;
	ws.send(default_config_json);
};

function updateNodeConfig(){
	updating_nc = true;
	ws.send(node_config_json);
};

function deleteRow(node, key, table){
	if(confirm('Are you sure you want to delete row: '+node)){
		updateSQL(node, key, table, true);
	}
};

function updateSQL(node, key, table, isDelete, entry_value){
	var value = $(node).html();
	if(value == undefined || value.length == 0){
		if(entry_value == undefined){
			var id = "#"+node;
			value = $(id).html();
			node = node.split("_")[0];
			console.log(node, value);
		} else {
			value = entry_value;
		}
	}
	if(table == default_table){
		if(isDelete){
			ws.send("{\"op\":\"sql-delete\", \"sql-table\":\""+table+"\", \"sql-key\":\""+key+"\", \"sql-value\":\""+value+"\"}");
		} else{
			ws.send("{\"op\":\"sql-update\", \"sql-table\":\""+table+"\", \"sql-key\":\""+key+"\", \"sql-value\":\""+value+"\"}");
		}
	} else {
		if(isDelete){
			ws.send("{\"op\":\"sql-delete\", \"sql-table\":\""+table+"\", \"sql-node\":\""+node+"\", \"sql-key\":\""+key+"\", \"sql-value\":\""+value+"\"}");
		} else {
			ws.send("{\"op\":\"sql-update\", \"sql-table\":\""+table+"\", \"sql-node\":\""+node+"\", \"sql-key\":\""+key+"\", \"sql-value\":\""+value+"\"}");
		}
	}
};

function wasUpdateSuccessful(obj){
	if(obj['sql-update-result'] !== "true"){
		alert('There was an error updating that row, please try again');
	} else {
		console.log("Successfully affected row - refreshing");
		location.reload();
	}
}

function addRow(key, value, isDefault, fabricNode){
	var id = key;
	if(key.indexOf(".") != -1){
		id = key.replace(".", "_");
	} 
	node = "#"+id;
	if(isDefault){
		addRowToDefaultConfigTable(id, node, key, value);
	} else {
		addRowToNodeConfigTable(id, node, key, value, fabricNode);
	}
};

function addRowToDefaultConfigTable(id, node, key, value){
	$(dc_node).append($("<tr>\n")
		.append("<th width='30px'><span title='Delete' onclick=\"deleteRow('"+node+"','"+key+"','"+default_table+"')\">&#10005;</span></th>\n") 
		.append("<td>"+key+"</td>\n")
		.append("<td id='"+id+"' class='editableRow' contentEditable='true'>"+value+"</td>\n")
		.append("<th width='30px'><span title='Submit change' onclick=\"updateSQL('"+node+"','"+key+"','"+default_table+"', false)\">&#10003;</span></th>\n")
		.append("</tr>"));
};

function addRowToNodeConfigTable(id, node, key, value, fabricNode){
	var id = fabricNode+"_"+id;
	$(nc_node).append($("<tr>\n")
		.append("<th width='30px'><span title='Delete' onclick=\"deleteRow('"+id+"','"+key+"','"+node_table+"')\">&#10005;</span></th>\n")
		.append("<td>"+fabricNode+"</td>")
		.append("<td>"+key+"</td>\n")
		.append("<td id='"+id+"' class='editableRow' contentEditable='true'>"+value+"</td>\n")
		.append("<th width='30px'><span title='Submit change' onclick=\"updateSQL('"+id+"','"+key+"','"+node_table+"', false)\">&#10003;</span></th>\n")
		.append("</tr>"));
};
