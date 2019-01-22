//search
app.controller('searchController',function ($scope,$location,searchService) {

    //先定义一个变量  用于绑定查询到的条件
    $scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':40,'sortField':'','sortType':''};//搜索对象 传送给后台的集合
    //写一个方法  当点击搜索的按钮时候调用 发送请求 获取结果
    $scope.search=function () {
        $scope.searchMap.pageNo=parseInt($scope.searchMap.pageNo);
        searchService.search($scope.searchMap).success(
            function (response) {//Map
                $scope.resultMap = response; // map.put("categoryList",categryList);
                //构建分页的标签 这个resultMap中有分类信息的
                buildPageLabel();
            }
        )
    };

    //添加搜索项 在点击分类 品牌 规格 价格的选项是 要调用的方法
    $scope.addSearchItem=function (key,value) {
        if (key=='category' || key =='brand' || key=='price'){//如果点击的是分类或者是品牌或者是价格
            $scope.searchMap[key]=value;//将搜索方法的value传给searchMap
        }else {
            //为什么要else 因为spec:{}的value是一个对象 这个对象有不同的规格 所以要二层spec:{"spec.text":"spec.option",...}
            $scope.searchMap.spec[key]=value;//如果点击的不是分类和品牌将规格添加到searchMap中
        }
        $scope.searchMap.price='';
        $scope.searchMap.pageNo=1;
        $scope.searchMap.pageSize=40;
        $scope.search();
    };

    //移除复合搜索条件 点击这个方法移除key对应的value
    $scope.removeSearchItem=function (key) {
        if(key=="category"|key=="brand" || key=='price'){//如果是分类或者是品牌或者价格
            $scope.searchMap[key]="";
        }else{//否则是规格
            delete $scope.searchMap.spec[key];//移除此属性

        }
        $scope.searchMap.price='';
        $scope.searchMap.pageNo=1;
        $scope.searchMap.pageSize=40;
        $scope.search();
    }

    //构建分页标签 私有方法
    buildPageLabel=function () {
        $scope.pageLabel=[];//新增分页栏属性
        var maxPageNo = $scope.resultMap.totalPages;//得到总业数
        var firstPage=1;//开始页码
        var lastPage=maxPageNo;//截至页码 初始化为总页数
        if($scope.resultMap.totalPages==null){
            alert("总页数为null");
        }
        if ($scope.resultMap.totalPages>5){//如果总页数大于5页，显示部分页码
            if($scope.searchMap.pageNo<=3){//如果当前页小于等于3
                lastPage=5;//显示前5页 开始默认 截至赋值
            }else if($scope.searchMap.pageNo>=lastPage-2){//如果当前页大于等于最大页码-2
                firstPage=maxPageNo-4;//设置后5页 开始赋值 截至默认
            }else {//显示当前为中心的5页
                firstPage=$scope.searchMap.pageNo-2;//开始页=当前页-2
                lastPage=$scope.searchMap.pageNo+2;//截至也=当前也+2
            }
        }
        //循环产生页码标签 i=开始 i<结束
        for(var i=firstPage;i<=lastPage;i++){
            $scope.pageLabel.push(i);
        }
    }

    //点击的时候调用该方法
    $scope.queryByPage=function (pageNo) {
        //将传递过来的页码 影响变量的值
        //判断 pageno是否是一个数字
        console.log(isNaN(pageNo));//
        if (isNaN(pageNo)==false){//如果是false 表示的数字类型的字符串
            if (pageNo>$scope.resultMap.totalPages){//如果当前页大于 赋值
                $scope.searchMap.pageNo=$scope.resultMap.totalPages;
            }else if(pageNo<1){ //如果当前页为负数 直接赋值1
                $scope.searchMap.pageNo=1;
            }else{
                $scope.searchMap.pageNo=parseInt(pageNo);//点击查询第几页时 赋值 并跳转
            }
            $scope.search();
        }else{
            alert("请输入数字");
        }
    };
    //判断当前页为第一页
    $scope.isTopPage=function () {
        if($scope.searchMap.pageNo==1){
            return true;
        }else{
            return false;
        }
    };

    $scope.clear=function () {
        $scope.searchMap={
            'keywords':$scope.searchMap.keywords,
            'category':'',
            'brand':'',
            'spec':{},
            'price':'',
            'pageNo':1,
            'pageSize':20,
            'sortType':'',
            'sortField':''
        }
    };

    //设置排序规格
    $scope.sortSearch=function (sortField,sortType) {
        $scope.searchMap.sortField=sortField;
        $scope.searchMap.sortType=sortType;
        $scope.search();
    };

    //判断关键字是否包含品牌
    $scope.keywordsIsBrand=function () {
        for(var i =0; i<$scope.resultMap.brandList.length;i++){
            //如果keywords中有遍历出来的品牌字段 indexOf
            if ($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0){
                $scope.searchMap.brand=$scope.resultMap.brandList[i].text;//将该字段赋值给品牌对象
                return true;
            }
        }
        return false;
    };

    //加载查询字符串
    $scope.loadkeywords=function () {
        $scope.searchMap.keywords=$location.search()['keywords'];
        $scope.search();
    }
});