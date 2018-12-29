  //控制层 将uploadService注入到goodsController服务中 引入$location服务
app.controller('goodsController' ,function($scope,$controller,$location   ,goodsService,itemCatService,uploadService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承

    //写一个方法  点击复选框的时候调用
    /**
     *$scope.entity.goodsDesc.specificationItems=
     * [{"attributeValue":["移动3G","移动4G"],"attributeName":"网络"},
     {"attributeValue":["16G","32G"],"attributeName":"机身内存"}]
     */
	//处理规格选项 定义goodsDesc的实体对象    要在下面定义这个属性
    $scope.entity={goodsDesc:{itemImages:[],specificationItems:[]}};
    // name是 规格id的名字 "网络 value是选中的规格id对应的option 选项{"id":98,"optionName":"移动3G","orders":1,"specId":27}
	//中的optionName=移动3G
    $scope.updateSpecAttribute=function($event,name,value){
    	//又调用了base中的方法 查询 第一个参数为规格选项 第二参数为属性名? 第三个参数是规格id
		//他的作用 就是找到对应 name的规格的那一个集合 有规格名和对应的规格选项
        var object= $scope.searchObjectByKey(
            $scope.entity.goodsDesc.specificationItems,'attributeName', name);

        if(object!=null){
            if($event.target.checked){
                object.attributeValue.push(value);
            }else{//取消勾选
                object.attributeValue.splice( object.attributeValue.indexOf(value ) ,1);//移除选项
                //如果选项都取消了，将此条记录移除
                if(object.attributeValue.length==0){
                    $scope.entity.goodsDesc.specificationItems.splice(
                        $scope.entity.goodsDesc.specificationItems.indexOf(object),1);
                }
            }
        }else{
            $scope.entity.goodsDesc.specificationItems.push(
                {"attributeName":name,"attributeValue":[value]});
        }
    };

    //创建sku列表
    /**
	 * 当点击事件触发时  拿到该点击事件对应的参数 然后拼接成tbItem
     */
	$scope.createItemList=function () {
		//先初始化
		$scope.entity.itemList=[{spec:{},price:0,num:99999,status:0,isDefault:0}];
		//拿到将规格项 [{"attributeValue":["移动3G","移动4G"],"attributeName":"网络"},
		//             {"attributeValue":["16G","32G"],"attributeName":"机身内存"}]
        var items = $scope.entity.goodsDesc.specificationItems;
        //遍历该规格项的长度
		for (var i=0;i<items.length;i++){
			//调用方法了 第一个参数是刚创建的一个集合 第二个参数是在每一个goodsDesc.specificationItems规格项中的attributeName
			//第三个参数是在每一个goodsDesc.specificationItems规格项中的attributeValue
			//给了这三个元素 得到了一个sku列表集合
			$scope.entity.itemList = addColumn( $scope.entity.itemList,items[i].attributeName,items[i].attributeValue);
		}
    };

    /**
     *
     * @param list  [{spec:{},price:0,num:1,status:'1',isDefault:'0'}];
	 *
	 * [{"attributeValue":["移动3G","移动4G"],"attributeName":"网络"},
       {"attributeValue":["16G","32G"],"attributeName":"机身内存"}]

     * @param columnName   "网络"
     * @param attributeValues  ["移动3G","移动4G"]
     * @returns {Array}
     */
    //因为是在本类中调用的方法所以不要用$scope
    //添加列值内部    做拼接  克隆
    addColumn=function (list,attributeName,attributeValue) {
    	var newList=[];//新的集合
		for (var i=0;i<list.length;i++){
			//将久的itemList放入oldRow中
			var oldRow = list[i];
			for (var j=0;j<attributeValue.length;j++){
                var newRow= JSON.parse(JSON.stringify(oldRow));//深克隆
                newRow.spec[attributeName]=attributeValue[j];
                newList.push(newRow);
			}
		}
        return newList;
    };



	//面包屑 下拉框 三级联动
	$scope.selectItemCat1List=function () {
		//因为是三级联动的一级 所以查询到0的所以集合
		itemCatService.findByParentId(0).success(
			function (response) { //返回的一个list集合 存到是itemcat对象
				$scope.itemCat1List=response;
            }
		)
    }

    //读取三级标题的第二级 $watch是作用是$ watch方法用于监控某个变量的值，当被监控的值发生变化，就自动执行相应的函数。
	$scope.$watch('entity.goods.category1Id',function (newValue, oldValue) {
        //根据选择的值，查询二级分类
        itemCatService.findByParentId(newValue).success(
            function(response){
                $scope.itemCat2List=response;
            }
        );
    })

    //读取三级分类
    $scope.$watch('entity.goods.category2Id', function(newValue, oldValue) {
        //根据选择的值，查询二级分类
        itemCatService.findByParentId(newValue).success(
            function(response){
                $scope.itemCat3List=response;
            }
        );
    });

    //三级分类选择后  读取模板ID
    $scope.$watch('entity.goods.category3Id', function(newValue, oldValue) {
        itemCatService.findOne(newValue).success(
            function(response){
                $scope.entity.goods.typeTemplateId=response.typeId; //更新模板ID
            }
        );
    });

    //模板id别选择后 更新模板对象
    //三级联动确定模板后 拿到模板id去(typetemplate)模板id表中查所以的品牌信息 品牌的下拉框 域改变也要拿到数据
    $scope.$watch('entity.goods.typeTemplateId',function (newValue, oldValue) {
        //typeTemplateService.findBrandList() 这个是拿系统中所以可以用的品牌
        //通过品牌id拿到这个品牌
        typeTemplateService.findOne(newValue).success(
            function (response) {// response是这个id的整条数据。该id下的品牌是json数据[{"id":1,"text":"联想"},{"id":12,"text":"锤子"}]
                $scope.typeTemplate=response;//将整个模板id数据赋值给typeTemplate
                $scope.typeTemplate.brandIds=JSON.parse($scope.typeTemplate.brandIds)//将这个json对象转化成对象
				//三级联动确定模板后 还可以拿到这个模板的扩展属性
				if ($location.search()['id']==null){//那url的参数
                    $scope.entity.goodsDesc.customAttributeItems=JSON.parse($scope.typeTemplate.customAttributeItems);
				}
            }
        );
        //查询规格列表
		typeTemplateService.findSpecList(newValue).success(
			function (response) {
                $scope.specList=response; //一个数组 在后台我们写了
            }
		);
    });



    //上传图片 uploadService的service
	$scope.uploadFile=function () {
		uploadService.uploadFile().success(
			function (response) {
				if (response.success){ //上传成功还要拿到这个图片的地址
					$scope.image_entity.url=response.message;
				}else {
					alert(response.message)
				}
            }
		).error(function () {
			alert("上传方式错误");
        });
    };

	//#########################################看这里 要在这里把实体封装传递到后台########################################
    $scope.entity={goods:{isEnableSpec:0},goodsDesc:{itemImages:[],customAttributeItems:[],specificationItems:[]}};//定义页面实体结构
	//添加图片列表
    $scope.add_image_entity=function () {
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity)
    };

    //移除待上传的图片
	$scope.remove_image_entity=function (index) {
		$scope.entity.goodsDesc.itemImages.splice(index,1);
    };


    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	};
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	};

	//kindeditor富文本编辑器


	//保存 
	/*$scope.add=function(){
            $scope.entity.goodsDesc.introduction=editor.html();
			goodsService.add( $scope.entity  ).success(
			function(response){
				if(response.success){
					//重新查询
					//不需要修改 只需要增加
					alert("保存成功")
					alert($scope.entity.goodsDesc.introduction)
					$scope.entity={};//将页面清空
					editor.html('');
				}else{
					alert(response.message);
				}
			}
		);
	};*/


    //保存
    $scope.save=function(){
        var serviceObject;//服务层对象
        if($scope.entity.goods.id!=null){//如果有ID
            //1.先获取富文本编辑器中的内容（html）
            var introduction = editor.html();
            //2.内容赋值给entity中的属性 introduction
            $scope.entity.goodsDesc.introduction=introduction;
            serviceObject=goodsService.update( $scope.entity ); //修改
        }else{
            //1.先获取富文本编辑器中的内容（html）
            var introduction = editor.html();
            //2.内容赋值给entity中的属性 introduction
            $scope.entity.goodsDesc.introduction=introduction;
            serviceObject=goodsService.add( $scope.entity  );//增加
        }
        serviceObject.success(
            function(response){
                if(response.success){
                    editor.html('');
                    //跳转到商品的列表页面
                    location.href="goods.html";//跳转到商品列表页
                }
            }
        );
    }



    //查询实体
    $scope.findOne=function(){
        //search(search,[paramValue]);getter/setter，返回当前url的参数的序列化json对象。
        var id= $location.search()['id'];//获取参数值
        if(id==null){
            return ;
        }

        goodsService.findOne(id).success(
            function(response){
                $scope.entity= response;
                //向富文本编辑器添加商品介绍

                //editor.html($scope.entity.goodsDesc.introduction);
                //将数据展示到富文本编辑器中
                editor.html("22");
                $scope.entity.goodsDesc.itemImages=JSON.parse($scope.entity.goodsDesc.itemImages);
                //显示扩展属性
                $scope.entity.goodsDesc.customAttributeItems=  JSON.parse($scope.entity.goodsDesc.customAttributeItems);

                $scope.entity.goodsDesc.specificationItems=JSON.parse($scope.entity.goodsDesc.specificationItems);

                var itemList = $scope.entity.itemList;//[{spec:{\\},{} ]
				for (var i =0;i<itemList.length;i++){
					itemList[i].spec=angular.fromJson(itemList[i].spec);
				}

            }
        );
    };
    //根据规格名称和选项名称返回是否被勾选
	$scope.checkAttributeValue=function (specName, optionName) {
		//规格 specificationItems
		var items=$scope.entity.goodsDesc.specificationItems;
		var object = $scope.searchObjectByKey(items,'attributeName',specName);
		if (object == null){
			return false;
		}else {
			if (object.attributeValue.indexOf(optionName) != -1){
				return true;
			}else {
				return false;
			}
		}
    };


	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
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
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	//解决状态名称显示 定义state数组
	$scope.status=['未审核','以审核','审核未通过','关闭'];//商品状态
    //解决商品分类名称显示
	$scope.itemCatList=[];//商品分类列表
	//加载商品分类列表
	$scope.findItemCatList=function () {
		itemCatService.findAll().success(
			function (response) {
				//查询到有多少个itemCat
				for (var i=0; i<response.length;i++){
					//将每个itemcat的id给itemCatList的下标并对应赋值name
					$scope.itemCatList[response[i].id]=response[i].name;
				}
            }
		)
    }
});	
