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
});