//form序列化为json
$.fn.serializeObject = function()
{
    var o = {};
    var a = this.serializeArray();
    $.each(a, function() {
        if (o[this.name] !== undefined) {
            if (!o[this.name].push) {
                o[this.name] = [o[this.name]];
            }
            o[this.name].push(this.value || '');
        } else {
            o[this.name] = this.value || '';
        }
    });
    return o;
};

//获取url后的参数值
function getUrlParam(key) {
	var href = window.location.href;
	var url = href.split("?");
	if(url.length <= 1){
		return "";
	}
	var params = url[1].split("&");

	for(var i=0; i<params.length; i++){
		var param = params[i].split("=");
		if(key == param[0]){
			return param[1];
		}
	}
    return "";
}

//treeDate
function treeArrTrans(arr){
    // 第一遍先加children
    for (var j in arr) {
        arr[j].children = [];
        arr[j].title = arr[j].pointName;
        arr[j].id = arr[j].id;
    }
    for (var i = arr.length - 1; i >= 0; i--) {
        if (arr[i].parentId) {
            findChild(arr, arr[i].parentId, arr[i], i);
        }
    }
    return arr;
};
function findChild(arr, parentId, item, index){
    for (var i = arr.length - 1; i >= 0; i--) {
        if (arr[i].id == parentId) {
            arr[i].children.push(item);
            arr.splice(index, 1);
        } else if (arr[i].children.length) {
            findChild(arr[i].children, parentId, item, i);
        }
    }
};
