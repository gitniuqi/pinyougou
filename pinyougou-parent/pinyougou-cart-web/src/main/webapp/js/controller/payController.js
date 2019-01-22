//payController 引用服务@scope 引入location服务用他的seach方法截取money参数
//引入interval定时任务调用后台主动发送查询订单是否支付成功的微信api
app.controller('paycontroller',function ($scope,$location,$interval, payservice) {

    //初始化调用方法获得 map集合
    $scope.createNative=function () {
        payservice.createNative().success(
            function (response) { //map
                $scope.money = (response.total_fee/100).toFixed(2) ;	//金额元
                $scope.out_trade_no = response.out_trade_no;//订单号
                alert(response.out_trade_no);
                //code_url 用QRious js
                var qr = new QRious({
                    element:document.getElementById('qrious'),
                    size:250,
                    level:'H',
                    value:response.code_url
                });
                //查询支付状态 后台没有定时重复查询 需要定时发送请求 页面加载 发送“统一下单”api“处理逻辑 定时调用查看支付状态api
                $scope.queryPayStatus($scope.out_trade_no);
            }
        )
    };

    //查询支付状态的controller 返回result
    $scope.queryPayStatus=function (out_trade_no) {

        $scope.second = 100;
        //定时任务
        time = $interval(function () {
            if($scope.second>0){
                $scope.second=$scope.second-1;//定时器
                //发送请求
                payservice.queryPayStatus(out_trade_no).success(
                    function (response) {
                        if (response.success){ //返回成功 跳转成功
                            location.href="paysuccess.html#?money="+$scope.money;//用localtion服务拿参数
                        }else if (response.success==401){ //支付失败系统错误 超时不管
                            location.href="payfail.html"
                        }
                        else if(response.success==402){//为查询到支付成功信息
                            alert("未支付");
                        }
                    }
                )
            }else {
                //订单超时 几分钟懒得算
                $interval.cancel(time); //停止调用
                alert("订单超时 订单失效");
            }
        },3000);
    };

    //调用这个方法返回url的money参数值
    $scope.getMoney=function () {
       return $location.search()['money'];
    }



});