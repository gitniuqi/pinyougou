//user服务
app.service('loginService',function ($http) {
    //showname的service方法
    this.showName=function () {
        return $http.get("../login/name.do");
    }
});