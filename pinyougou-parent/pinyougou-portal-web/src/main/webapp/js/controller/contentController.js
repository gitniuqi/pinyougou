//portal
app.controller("contentController",function ($scope, contentService) {

    //广告id集合
    $scope.contentList=[];
    $scope.findByCategoryId=function (categoryId) {
        contentService.findByCategoryId(categoryId).success(
            function (response) {//List<TbContent>
                $scope.contentList[categoryId]=response;
            }
        )
    }

    //搜索跳转
    $scope.search=function () {
        window.location.href="http://localhost:9104/search.html#?keywords="+$scope.keywords;
    }
});