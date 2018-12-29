//控制层 goods
app.controller('baseController' ,function($scope){
    //重新加载列表 数据
    $scope.reloadList=function(){
        //切换页码
        $scope.search( $scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    };
    
	//分页控件配置 
	$scope.paginationConf = {
         currentPage: 1,
         totalItems: 10,
         itemsPerPage: 10,
         perPageOptions: [10, 20, 30, 40, 50],
         onChange: function(){
        	 $scope.reloadList();//重新加载
     	 }
	}; 
	
	$scope.selectIds=[];//选中的ID集合 

	//更新复选
    $scope.updateSelection = function($event, id) {
        if($event.target.checked){//如果是被选中,则增加到数组
            $scope.selectIds.push( id);
        }else{
            var idx = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(idx, 1);//删除
        }
    }

    //定义方法是将json转换成string
    $scope.jsonToString=function (list, key) {
        //将字符传转换 成json对象（数组）
        var fromJson = angular.fromJson(list);
        var str="";
        for(var i=0;i<fromJson.length;i++){// [{"id":27,"text":"网络"},{"id":32,"text":"机身内存"}]
            var obj = fromJson[i];
            str +=obj[key]+",";
        }
        if (str.length>=1){
            str=str.substring(0,str.length-1);
        }
        return str;
    }

    //又调用了base中的方法 查询 第一个参数为规格选项是一个数组 第二参数为属性名？ 第三个参数是规格"text":"网络
    //从集合中按照key查询对象    //  key="'attributeName'" keyValue="网络"
    $scope.searchObjectByKey=function(list,key,keyValue){
        for(var i=0;i<list.length;i++){
            if(list[i][key]==keyValue){
                return list[i];
            }
        }
        return null;
    }

});	