app.controller('payController',function ($scope,$interval,$location, payService) {



    /*创建订单*/
    $scope.createNative=function () {
        payService.createNative().success(
            function (response) { //map
                $scope.out_trade_no = response.out_trade_no;
                $scope.total_fee=response.total_fee/100;

                //生成二维码展示
                var qrious = new QRious({
                    element:document.getElementById("qrious"),
                    size:250,
                    level:'H',
                    value:response.code_url
                });
                //需要调用方法 查询支付的状态

                //如果超过5分钟就说明超时 3秒钟发送一次请求

                $scope.second = 100;
                time= $interval(function(){

                    if($scope.second>0){
                        $scope.second =$scope.second-1;
                        //调用查询的方法发送请求获取状态
                        payService.queryPayStatus($scope.out_trade_no).success(
                            function (response) {//result
                                if(response.success){
                                    location.href="paysuccess.html#?money="+$scope.total_fee;
                                    //订单支付成功：  更新订单的状态 保存订单到数据库中  清理redis中的订单
                                }else{
                                    //401  402
                                    alert("未支付");
                                }
                            }
                        )
                    }else{
                        //停止调用
                        $interval.cancel(time);
                        //跳转到支付失败页面
                        //超时 需要删除redis中的订单 恢复库存
                        alert("秒杀服务已结束");
                    }
                },3000);
            }
        )
    };

    $scope.getMoney=function () {
        return $location.search()['money'];
    }

});