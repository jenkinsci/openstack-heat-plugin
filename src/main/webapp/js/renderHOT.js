/**
 * 
 * Copyright 2015 Credit Mutuel Arkea
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */
function generateBundle(div, data, uid) {
	var display = generateProperties(data, uid);
	
	if (data['tags'] !== undefined && isNotEmpty(data['tags'])) {
		display += generateTags(data,uid);
	}
	
	if (data['parameters'] !== undefined && isNotEmpty(data['parameters'])) {
		display += generateParameters(data['parameters'], uid);
	}
	if (data['outputs'] !== undefined && isNotEmpty(data['outputs'])) {
		display += generateOutputs(data['outputs'], uid);
	}
	div.innerHTML = display;
	var arr = div.getElementsByTagName('script')
	for (var n = 0; n < arr.length; n++) {
		try {
			eval(arr[n].innerHTML);
		} catch (exception) {
			alert(exception);
		}
	}
}

function generateProperties(props, uid) {
	var properties = '<table width="100%"><tbody><tr><td colspan="3"><div class="section-header">Properties</div></td></tr>';
	properties += '<tr><td class="setting-leftspace"></td><td class="setting-name">Stack name : </td><td class="setting-main"><input type="text" class="setting-input"';
	if (props["name"] !== '') {
		properties += ' value="' + props["name"] + '" ';
	}
	properties += 'name="name" onchange="updateBundle(\'\', this.name, this.value, \''
			+ uid + '\')"></input></td></tr>';
	properties += '<tr><td colspan="3"><input type="checkbox" name="exist"';
	if (props["exist"] === true) {
		properties += ' checked ';
	}
	properties += ' onchange="updateBundle(\'\', this.name, this.checked, \''
			+ uid
			+ '\')"></input><label class="attach-previous">Delete stack if already exists ?</label></td></tr>';
	properties += '<tr><td colspan="3"><input type="checkbox" name="debug"';
	if (props["debug"] === true) {
		properties += ' checked ';
	}
	properties += ' onchange="updateBundle(\'\', this.name, this.checked, \''
			+ uid
			+ '\')"></input><label class="attach-previous">Debug mode ?</label></td></tr>';
	properties += '</tbody></table>'
	return properties;
}

function generateParameters(params, uid) {
	var param = undefined;
	var update = '';
	var inputs = '<table width="100%"><tbody><tr><td colspan="3"><div class="section-header">Parameters</div></td></tr>';
	for ( var parameter in params) {
		inputs += '<tr><td class="setting-leftspace"></td><td class="setting-name">';
		param = params[parameter];
		if (param["label"] !== '') {
			inputs += param["label"];
		} else {
			inputs += parameter;
		}
		inputs += ' : </td><td class="setting-main">';

		var field = '<input type="text" class="setting-input" name="';
		field += parameter + '"';
		field += ' id="' + parameter + '"';
		if (param["value"] !== '') {
			field += ' value="' + param["value"] + '" ';
		} else if (param["defaultValue"] !== '') {
			field += ' value="' + param["defaultValue"] + '" ';
		}
		if (param["description"] !== '') {
			field += ' title="' + param["description"] + '" ';
		}
		field += ' onchange="updateBundle(\'parameters\', this.name, this.value, \''
				+ uid + '\')" ></input>';

		if (isNotEmpty(param["constraints"])) {
			for ( var element in param["constraints"]) {
				var constraint = param["constraints"][element];
				if (constraint["type"] == "allowed_values") {
					var selectedVal = param["value"];
					field = '<select name="' + parameter + '" ';
					field += ' id="' + parameter + '"';
					if (param["description"] !== '') {
						field += ' title="' + param["description"] + '" ';
					}
					field += ' onchange="updateBundle(\'parameters\', this.name, this.options[this.selectedIndex].value, \''
							+ uid + '\')" >';
					var allowed_values = constraint["allowed_values"];
					for ( var allowed in allowed_values) {
						var aValue = allowed_values[allowed];
						if (aValue == selectedVal) {
							field += '<option selected="selected" value="'
									+ aValue + '">' + aValue + '</option>';
						} else {
							field += '<option value="' + aValue + '">' + aValue
									+ '</option>';
						}
					}
					field += '</select>';
					break;
				} else {
					if (constraint["type"] == "length"
							|| constraint["type"] == "range") {
						if (constraint["limits"]["min"] != undefined) {
							update += 'document.getElementById("';
							update += parameter + '").setAttribute("min","';
							update += constraint["limits"]["min"];
							update += '");'
						}
						if (constraint["limits"]["max"] != undefined) {
							update += 'document.getElementById("';
							update += parameter + '").setAttribute("max","';
							update += constraint["limits"]["max"];
							update += '");'
						}
					}
					if (constraint["type"] == "allowed_pattern") {
						update += 'document.getElementById("';
						update += parameter + '").setAttribute("pattern","';
						update += constraint["allowedPattern"];
						update += '");'
					}
				}
				if (update != '') {
					field += '<script type="text/javascript">';
					field += update + 'document.getElementById("';
					field += parameter
							+ '").setAttribute("onblur","checkConstraints(this)");</script>';
				}
			}
		}
		inputs += field;
		inputs += '</td><tr>';
		inputs += '<td class="setting-leftspace"></td>';
		inputs += '<td class="setting-name"></td>';
		inputs += '<td><label class="setting-name" id="error' + parameter
				+ '"></label></td></tr>';
	}
	inputs += '</tbody></table>';
	return inputs;
}

function generateTags(out,uid) {
	
	var outputs = '<table  width="100%"><tbody><tr><td colspan="3"><div class="section-header">Tags</div></td></tr>';
	    outputs += '<tr class="optional-block-start row-set-start" >';
		outputs += '<td  colspan="3">';
		outputs += '<input name="tag" class="" onclick="javascript:updateOptionalBlock(this,true)"';
		if (out["tag"] === true) {
			outputs += ' checked ';
		}
		
		outputs += 'onchange="updateBundle(\'\', this.name, this.checked, \''+ uid + '\')" '; 		
		outputs += 'type="checkbox">';
		outputs += '<label class="attach-previous">Add tags ?</label>';
		outputs += '</td>';
		outputs += '<td class="setting-no-help"></td>';
		outputs += '</tr>';	    
		outputs += '<tr class="rowvg-start" ></tr>';			
		outputs += '<tr><td  class="setting-leftspace"></td><td class="setting-name">';
		outputs += 'Tags';
		outputs += ' : </td><td   class="setting-main"><input type="text"  class="setting-input" name="tags"';
		
		
		outputs += ' title="tags" ';
		if (out['tags'] !== '') {
			outputs += ' value="' + out['tags'] + '" ';
		}
		outputs += ' onchange="updateBundle(\'\', this.name, this.value, \''
				+ uid + '\')" ></input></td></tr>';
		outputs += '<tr class="optional-block-end rowvg-end row-set-end"></tr>';
	outputs += '</tbody></table>';
	return outputs;
}


function generateOutputs(outs, uid) {
	var output = undefined;
	var outputs = '<table width="100%"><tbody><tr><td colspan="3"><div class="section-header">Outputs</div></td></tr>';
	for ( var out in outs) {
		outputs += '<tr><td class="setting-leftspace"></td><td class="setting-name">';
		output = outs[out];
		outputs += out
				+ ' : </td><td class="setting-main"><input type="text" class="setting-input" name="';
		outputs += out + '"';
		outputs += ' title="' + outs[out]['description'] + '" ';
		if (output["value"] !== '') {
			outputs += ' value="' + output["value"] + '" ';
		}
		outputs += ' onchange="updateBundle(\'outputs\', this.name, this.value, \''
				+ uid + '\')" ></input></td></tr>';
	}
	outputs += '</tbody></table>';
	return outputs;
}

function updateBundle(family, name, value, uid) {
	var bundle = JSON.parse(document.getElementById('bundle' + uid).value);
	if (family !== '') {
		bundle[family][name]['value'] = value;
	} else {
		bundle[name] = value;
	}
	document.getElementById('bundle' + uid).value = JSON.stringify(bundle);
}

function checkConstraints(field) {
	var length = field.value.length;
	var error = '';
	if (field.min != undefined && length < field.min) {
		error += ' Length field < ' + field.min + ' !';
	} else if (field.max != undefined && length > field.max) {
		error += ' Length field > ' + field.max + ' !';
	} else if (field.pattern != undefined && field.pattern != '') {
		var regex = new RegExp(pattern, "g");
		if (!regex.test(str)) {
			error += ' Field don'
			't respect the pattern ' + field.pattern + ' !';
		}
	}
	if (error != '') {
		document.getElementById('error' + field.name).innerHTML = '* ' + error;
		document.getElementById('error' + field.name).style.color = 'red';
		document.getElementById(field.id).style.borderColor = 'red';
	} else {
		document.getElementById('error' + field.name).innerHTML = '';
		document.getElementById(field.id).style.borderColor = '#ccc';
	}
}

function isNotEmpty(obj) {
	for ( var data in obj) {
		return true;
	}
	return false;
}

function generateEnv(envs) {
	var display = '';
	if (envs['parameter_defaults'] !== undefined
			&& isNotEmpty(envs['parameter_defaults'])) {
		display = 'parameter_defaults:<br>';
		for ( var param in envs['parameter_defaults']) {
			display += '&nbsp;&nbsp;' + param + ' : '
					+ envs['parameter_defaults'][param] + '<br>';
		}
		display += '<br>';
	}
	if (envs['parameters'] !== undefined && isNotEmpty(envs['parameters'])) {
		display += 'parameters:<br>';
		for ( var param in envs['parameters']) {
			display += '&nbsp;&nbsp;' + param + ' : '
					+ envs['parameters'][param] + '<br>';
		}
	}
	return display;
}

function updateParametersInBundle(uid, data) {
	try {
		var bundle = JSON.parse(document.getElementById('bundle' + uid).value);

		if (data['parameter_defaults'] !== undefined
				&& isNotEmpty(data['parameter_defaults'])) {
			for ( var param in data['parameter_defaults']) {
				if (bundle['parameters'][param] != undefined) {
					bundle['parameters'][param]['value'] = data['parameter_defaults'][param];
				}
			}
		}

		if (data['parameters'] !== undefined && isNotEmpty(data['parameters'])) {
			for ( var param in data['parameters']) {
				if (bundle['parameters'][param] != undefined) {
					bundle['parameters'][param]['value'] = data['parameters'][param];
				}
			}
		}

		document.getElementById('bundle' + uid).value = JSON.stringify(bundle);
	} catch (exception) {
		alert(exception);
	}
}

function updateToolTip(id, data) {
	document.getElementById(id).setAttribute('tooltip', data);
	document.getElementById(id).setAttribute('title', data);
}