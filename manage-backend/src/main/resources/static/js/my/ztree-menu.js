function getMenuTree() {
	var root = {
		id : 0,
		name : "root",
		open : true,
		children: []
	};

	$.ajax({
		type : 'get',
		url : domainName + '/api-b/menus/tree',
		contentType : "application/json; charset=utf-8",
		async : false,
		success : function(data) {
			$.each(data, function(i,item){
				var node = createNode(item);
				root.children.push(node);
			});
		}
	});

	return root;
}

function getKPTree() {
	var root = {
		id : 0,
		name : "root",
		open : true,
		children: []
	};

	$.ajax({
		type : 'get',
		url : domainName + '/api-b/knowledgepoints/tree',
		contentType : "application/json; charset=utf-8",
		async : false,
		success : function(data) {
			$.each(data, function(i,item){
				var node = createNode(item);
				root.children.push(node);
			});
		}
	});

	return root;
}


function initMenuDatas(roleId){
	$.ajax({
		type : 'get',
		url : domainName + '/api-b/menus?roleId='+roleId,
		success : function(data) {
			var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
			var length = data.length;
			if(length > 0){//选中root
				var node = treeObj.getNodeByParam("id", 0, null);
				treeObj.checkNode(node, true, false);
			}

			for(var i=0; i<length; i++){//选中节点
				var node = treeObj.getNodeByParam("id", data[i], null);
				treeObj.checkNode(node, true, false);
			}
		}
	});
}

//treeDate
function treeArrTrans(arr,value,key){
	// 第一遍先加children
	for (let j in arr) {
		arr[j].children = [];
		arr[j].title = arr[j][value];
		arr[j].id = arr[j][key];
	}
	for (let i = arr.length - 1; i >= 0; i--) {
		if (arr[i].parentId) {
			findChild(arr, arr[i].parentId, arr[i], i);
		}
	}
	return arr;
};
function findChild(arr, parentId, item, index){
	for (let i = arr.length - 1; i >= 0; i--) {
		if (arr[i].id == parentId) {
			arr[i].children.push(item);
			arr.splice(index, 1);
		} else if (arr[i].children.length) {
			findChild(arr[i].children, parentId, item, i);
		}
	}
};

//获取选中的节点id
function getCheckedMenuIds(){
	var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
	var nodes = treeObj.getCheckedNodes(true);

	var length = nodes.length;
	var ids = [];
	for(var i=0; i<length; i++){
		var n = nodes[i];
		var id = n['id'];
		ids.push(id);
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
function initSysDepartmentListSelect(){
	$.ajax({
		type : 'get',
		url : domainName + '/api-u/findSysDepartmentList',
		async : false,
		success : function(data) {
			var arr = treeArrTrans(data,"dname","id");
			layui.use(["customSelect"], function () {
				var select = layui.customSelect;
				select.render({
					el: "departmentId",
					data: arr,
					type: "radio",
					line: true,
					id:departmentId,
					accordion: true,    
					checked: function (obj) {
						departmentId = obj.combData.datas.id;
					},
				});
				if(departmentId){
					$("#departmentId").val($('div[data-id="' + departmentId +'"]').find('>.layui-tree-entry').find('.layui-tree-txt').text())
				}
			});

		}
	});
}



function initSysDepartmentSelect(){
    $.ajax({
        type : 'get',
        url : domainName + '/api-u/sysDepartment/all',
        async : false,
        success : function(data) {
            var arr = treeArrTrans(data,"dname","id");
            layui.use(["customSelect"], function () {
                var select = layui.customSelect;
                select.render({
                    el: "parentId",
                    data: arr,
                    type: "radio",
                    line: true,
                    id:parentId,
                    accordion: true,
                    checked: function (obj) {
                        parentId = obj.combData.datas.id;
                    },
                });
            });
        }
    });
}



function initPositionSelect(){
	$.ajax({
		type : 'get',
		url : domainName + '/api-u/position/findUserPosition',
		async : false,
		success : function(data) {
			var select = $("#positionId");
			// select.append("<option value='0'>root</option>");
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

function initCountrySelect(){
	$.ajax({
		type : 'get',
		url : domainName + '/api-b/CountryDictAll',
		async : false,
		success : function(data) {
			var select = $("#countrySelect");
			select.append("<option value='0'>无</option>");
			for(var i=0; i<data.length; i++){
				var d = data[i];
				//  if(d['parentId'] == 0){
				var id = d['id'];
				var name = d['name'];

				select.append("<option value='"+name +"'>" +name+"</option>");
				//   }
			}
		}
	});
}
function initParentPointSelect(){
	$.ajax({
		type : 'get',
		url : domainName + '/api-b/knowledgepoints/all',
		async : false,
		success : function(data) {
			var select = $("#parentId");
			select.append("<option value='0'>root</option>");
			for(var i=0; i<data.length; i++){
				var d = data[i];
				//  if(d['parentId'] == 0){
				var id = d['id'];
				var pointName = d['pointName'];

				select.append("<option value='"+ id +"'>" +pointName+"</option>");
				//   }
			}
		}
	});
}

function getSettting() {
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
			onCheck : zTreeOnCheck
		}
	};

	return setting;
}

function zTreeOnCheck(event, treeId, treeNode) {
//	console.log(treeNode.id + ", " + treeNode.name + "," + treeNode.checked
//			+ "," + treeNode.pId);
//	console.log(JSON.stringify(treeNode));
}
