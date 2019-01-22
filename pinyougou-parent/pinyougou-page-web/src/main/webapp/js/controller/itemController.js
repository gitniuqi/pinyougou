app.controller('itemController',function($scope,$http){
	
	$scope.num=1;
	//写一个方法 当点击的时候 影响变量 数字的值
	
	$scope.add=function(x){
		x=parseInt(x);
		$scope.num=parseInt($scope.num);
		
		$scope.num=$scope.num+x;
		
		if($scope.num<1){
			$scope.num=1;
		}
	}
	
	$scope.specificationItems=angular.fromJson(angular.toJson(skuList[0].spec));//深克隆
	
	
	//用来当点击的时候调用  影响变量specificationItems
	$scope.selectSpecificationItems=function(name,value){
		$scope.specificationItems[name]=value;
		$scope.search();
	}
	
	
	
	//写一个方法 用来判断 点击的规格是否在当前的规格对象中存在，如果存在就勾选
	
	$scope.isSelected=function(name,value){
		if($scope.specificationItems[name]==value){
			return true;
		}
		return false;
	}
	
	$scope.sku=skuList[0];
	
	//写一个方法 要在点击的时候调用  判断当前的规格 是否在SKU列表中存在，如果存在，列表中的对象赋值给SKU变量
	
	$scope.search=function(){
		for(var i=0;i<skuList.length;i++){
			var obj = skuList[i];
			if(angular.toJson(obj.spec)==angular.toJson($scope.specificationItems)){
				$scope.sku=obj;
			}
		}
	}
	//添加商品到购物车 不用service层了 所以引用$http服务
	$scope.addToCat=function () { /*cors跨域请求解决方案*/
		$http.get('http://localhost:9107/cart/addGoodsToCartList.do?itemId='
			+$scope.sku.id+'&num='+$scope.num,{'withCredentials':true}).success(
				function (response) {
					if (response.success){
                        location.href='http://localhost:9107/cart.html';//跳转到购物车页面
					}else {
						alert(response.message);
					}
                }
		)

    };
	
	
});