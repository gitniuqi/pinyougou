//userservice
app.service('userService',function ($http) {

    this.createSmsCode=function (phone) {
        return $http.get("../user/createSmsCode.do?phone="+phone);
    };
    this.add=function (entity, code) {
        return $http.post("../user/add.do?code="+code,entity);
    }
});