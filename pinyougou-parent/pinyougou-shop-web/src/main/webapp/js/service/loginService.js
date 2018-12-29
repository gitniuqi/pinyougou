//登陆的服务层 参数只有http就好了 不必要socpe
app.service('loginService',function ($http) {
    //读取登陆用户名的方法
    this.loginName=function () {
        return $http.get('../login/name.do')
    }
})