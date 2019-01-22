//usercontroller
app.controller('userController',function ($scope,userService) {

    //生成短信验证码
    $scope.createSmsCode=function () {
        if($scope.entity.phone==null){
            alert("请输入手机号！");
            return ;
        }
        userService.createSmsCode($scope.entity.phone).success(
            function (response) {
                alert(response.message);
            }
        )
    };

    //验证验证码并添加用户
    $scope.register=function () {
        userService.add($scope.entity,$scope.code).success(
            function (response) {
                alert(response.message)
            }
        )
    }
});