//cartpayService
app.service('payservice',function ($http) {
    this.createNative=function () {
        return $http.get("/pay/createNative.do")
    };

    //调用查询支付状态的service
    this.queryPayStatus=function (out_trade_no) {
        return $http.get('/pay/queryPayStatus.do?out_trade_no='+out_trade_no);
    };

})