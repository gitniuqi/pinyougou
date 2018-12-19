 //控制层
 //在模板控制中 我用到了品牌服务层中的方法 所有需要依赖注入品牌服务
 //这个控制器因为用到要查询所有的规格名字 要用到规格的service所以要引入规格的服务 specificationService
app.controller('typeTemplateController' ,function($scope,$controller,typeTemplateService,brandService,specificationService){
	
	$controller('baseController',{$scope:$scope});//继承

	//新增属性行
	$scope.addTableRow=function () {
		$scope.entity.customAttributeItems.push({});
    }
	//删除属性行
	$scope.deleTableRow=function (index) {
		$scope.entity.customAttributeItems.splice(index,1);
	}


	//定义品牌列表数组
	$scope.brandList={data:[]};
	//读取品牌列表的方法
	$scope.findBrandList=function () {
		//触发这个方法 初始化触发 要拿到所以的品牌嘛 调用service的对应方法 返回success的回调函数
		brandService.selectOptionList().success(
			function (response) {
				$scope.brandList={data:response};
            }
		)
    }

    //定义规格列表数组
	$scope.specificationList={data:[]};
	//读取规格列表的方法
	$scope.findSpecificationList=function () {
		//触发这个方法 init触发 因为也要拿到所有的规格列表 会调用service对应的方法
        specificationService.selectSOptionList().success(
        	//成功的回调函数
			function (response) {
				//因为select2要这个样子的数据
				$scope.specificationList={data:response};
            }
		)
    }

    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		typeTemplateService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		typeTemplateService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		typeTemplateService.findOne(id).success(
			function(response){
				$scope.entity= response;
				/*因为要对象数据 这是返回的string数据 会别转译*/
				/*品牌列表*/
				$scope.entity.brandIds=angular.fromJson($scope.entity.brandIds);
				$scope.entity.specIds=angular.fromJson($scope.entity.specIds);
				$scope.entity.specIds=angular.fromJson($scope.entity.specIds);
				$scope.entity.customAttributeItems=angular.fromJson($scope.entity.customAttributeItems);


			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=typeTemplateService.update( $scope.entity ); //修改  
		}else{
			serviceObject=typeTemplateService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	$scope.reloadList();//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		typeTemplateService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		typeTemplateService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
    
});	
