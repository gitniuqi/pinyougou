//服务层
app.service('specificationService',function($http){

	//获得规格列表 只发请求 查一张表返回 一个名字和id的map集合
	this.selectSOptionList=function () {
		return $http.get('../specification/selectSOptionList.do');
    }
	    	
	//读取列表数据绑定到表单中
	this.findAll=function(){
		return $http.get('../specification/findAll.do');		
	}
	//分页 
	this.findPage=function(page,rows){
		return $http.get('../specification/findPage.do?page='+page+'&rows='+rows);
	}
	//查询实体
	this.findOne=function(id){
		return $http.get('../specification/findOne.do?id='+id);
	}
	//增加 
	this.add=function(entity){
		return  $http.post('../specification/add.do',entity );
	}
	//修改 
	this.update=function(entity){
		return  $http.post('../specification/update.do',entity );
	}
	//删除
	this.dele=function(ids){
		return $http.get('../specification/delete.do?ids='+ids);
	}
	//搜索
	this.search=function(page,rows,searchEntity){
		return $http.post('../specification/search.do?page='+page+"&rows="+rows, searchEntity);
	}    	
});
