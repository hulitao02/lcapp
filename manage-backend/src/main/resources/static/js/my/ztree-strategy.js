function getAndMenuTree(data) {
	var root = {
		id : 0,
		name : "同时满足的策略",
		open : true,
		children: []
	};
	$.each(data, function(i,item){
		var node = createNode(item);
		root.children.push(node);
	});
	return root;
}

function getOrMenuTree(data) {
	var root = {
		id : 0,
		name : "满足其一的策略",
		open : true,
		children: []
	};
	$.each(data, function(i,item){
		var node = createNode(item);
		root.children.push(node);
	});
	return root;
}
function initMenuDatas(configid,type){
	$.ajax({
		type : 'get',
		url : domainName + '/api-p/Strategy/getCurHasStrategy?type='+type+'&configid=' + configid,
		success : function(data) {
			var treeObj;
			if(type == 'andType')
				treeObj = $.fn.zTree.getZTreeObj("treeDemo");
			else
				treeObj = $.fn.zTree.getZTreeObj("treeDemos");
			var length = data.length;
			if(length > 0){//选中root
				var node = treeObj.getNodeByParam("id", 0, null);
				treeObj.checkNode(node, true, false);
			}
			
			for(var i=0; i<length; i++){//选中节点
				var node = treeObj.getNodeByParam("name", data[i].name, null);
				treeObj.checkNode(node.getParentNode(), true, false);
				treeObj.checkNode(node, true, false);

			}
		}
	});
}

//获取选中的节点id
function getCheckedMenuIds(type){
	var treeObj;
	if(type == 'andType')
		treeObj = $.fn.zTree.getZTreeObj("treeDemo");
	else
		treeObj = $.fn.zTree.getZTreeObj("treeDemos");
	var nodes = treeObj.getCheckedNodes(true);
	var length = nodes.length;
	var ids = [];
	for(var i=0; i<length; i++){
		var n = nodes[i];
		if(typeof(n.children) == "undefined" )
		{
			var id = n['id'];
			ids.push(id);
		}
	}
	return ids;
}

function createNode(d) {
	var id = d['id'];
	var pId = d['parentId'];
	var name = d['name'];
	var child = d['child'];

	var node = {
		open : true,
		id : id,
		name : name,
		pId : pId,
	};

	if (child != null) {
		var length = child.length;
		if (length > 0) {
			var children = [];
			for (var i = 0; i < length; i++) {
				children[i] = createNode(child[i]);
			}

			node.children = children;
		}

	}
	return node;
}

function initParentMenuSelect(){
	$.ajax({
        type : 'get',
        url : domainName + '/api-b/menus/all',
        async : false,
        success : function(data) {
            var select = $("#parentId");
            select.append("<option value='0'>root</option>");
            for(var i=0; i<data.length; i++){
                var d = data[i];
              //  if(d['parentId'] == 0){
                	var id = d['id'];
                	var name = d['name'];
                	
                	select.append("<option value='"+ id +"'>" +name+"</option>");
             //   }
            }
        }
    });
}

function getSettting(type) {
	var setting = {
		check : {
			enable : true,
			chkboxType : {
				"Y" : "ps",
				"N" : "ps"
			}
		},
		async : {
			enable : true,
		},
		data : {
			simpleData : {
				enable : true,
				idKey : "id",
				pIdKey : "pId",
				rootPId : 0
			}
		},
		callback : {
			onCheck : (andType == type)?zTreeAndOnCheck:zTreeOrCheck
		}
	};
	return setting;
}
function zTreeAndOnCheck(event, treeId, treeNode) {
	if(treeNode.checked)
	{
		var treeObj = $.fn.zTree.getZTreeObj("treeDemos");
		var node;
		if(treeNode.id!=0)
			node=treeObj.getNodeByParam("name", treeNode.name, null);
		else
			node=treeObj.getNodeByParam("id", 0, null);
		if(node.checked)
		{
			treeObj.checkNode(node, false, false);
		}
	}


	console.log("AND");
	console.log(treeNode.id + ", " + treeNode.name + "," + treeNode.checked
			+ "," + treeNode.pId);
	console.log(JSON.stringify(treeNode));
	checkedMenu();
}
function zTreeOrCheck(event, treeId, treeNode) {
	if(treeNode.checked)
	{
		var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
		var node;
		if(treeNode.id!=0)
			node=treeObj.getNodeByParam("name", treeNode.name, null);
		else
			node=treeObj.getNodeByParam("id", 0, null);
		if(node.checked)
		{
			treeObj.checkNode(node, false, false);
		}
	}
	checkedMenu();
	console.log("OR");
	console.log(treeNode.id + ", " + treeNode.name + "," + treeNode.checked
			+ "," + treeNode.pId);
	console.log(JSON.stringify(treeNode));
}
//检测菜单是否正确
function checkedMenu(){
	var treeObj_and=$.fn.zTree.getZTreeObj("treeDemo");
	var	treeObj_or = $.fn.zTree.getZTreeObj("treeDemos");
	var nodes_and_false = treeObj_and.getCheckedNodes(false);
	var nodes_and_true = treeObj_and.getCheckedNodes(true);
	var nodes_or_false = treeObj_or.getCheckedNodes(false);
	var nodes_or_true = treeObj_or.getCheckedNodes(true);
	// and false
	for (var i = 0; i < nodes_and_false.length; i++) {
		if(typeof(nodes_and_false[i].children) != "undefined" )
		{
			for (var j = 0; j < nodes_and_false[i].children.length; j++) {
				treeObj_and.checkNode(nodes_and_false[i].children[j], false, false);
				if(typeof(nodes_and_false[i].children[j].children) != "undefined" )
				{
					for (var k = 0; k < nodes_and_false[i].children[j].children.length; k++) {
						treeObj_and.checkNode(nodes_and_false[i].children[j].children[k], false, false);
					}
				}
			}
		}
	}
	//and true
	for (var i = 0; i < nodes_and_true.length; i++) {
		if(typeof(nodes_and_true[i].children) != "undefined" )
		{
			var isTure = false;
			for (var j = 0; j < nodes_and_true[i].children.length; j++) {
				if(nodes_and_true[i].children[j].checked)
				{
					isTure=true;
					break;
				}
			}
			if(!isTure)
			{
				treeObj_and.checkNode(nodes_and_true[i], false, false);
			}
		}
	}



	// or false
	for (var i = 0; i < nodes_or_false.length; i++) {
		if(typeof(nodes_or_false[i].children) != "undefined" )
		{
			for (var j = 0; j < nodes_or_false[i].children.length; j++) {
				treeObj_or.checkNode(nodes_or_false[i].children[j], false, false);
				if(typeof(nodes_or_false[i].children[j].children) != "undefined")
				{
					for (var k = 0; k < nodes_or_false[i].children[j].children.length; k++) {
						treeObj_or.checkNode(nodes_or_false[i].children[j].children[k], false, false);
					}
				}
			}
		}
	}
	//or true
	for (var i = 0; i < nodes_or_true.length; i++) {
		if(typeof(nodes_or_true[i].children) != "undefined" )
		{
			var isTure = false;
			for (var j = 0; j < nodes_or_true[i].children.length; j++) {
				if(nodes_or_true[i].children[j].checked)
				{
					isTure=true;
					break;
				}
			}
			if(!isTure)
			{
				treeObj_or.checkNode(nodes_or_true[i], false, false);
			}
		}
	}
}