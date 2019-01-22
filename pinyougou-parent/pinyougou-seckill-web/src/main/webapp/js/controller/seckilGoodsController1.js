//引入$location服务
app.controller('seckillGoodsController',function ($scope,$location,$interval,seckillGoodsService) {

    /*查询所有的秒杀商品*/
    $scope.findAll=function () {
        seckillGoodsService.findAll().success(
            function (response) {//返回list集合
                $scope.list=response;
            }
        )
    };

    /*根据url的id从redis中查询商品*/
    $scope.findOne=function () {
        var id = $location.search()['id'];
        seckillGoodsService.findOne(id).success(
            function (response) {//一条 tbseckgoods
                $scope.entity=response;
                var enddate = new Date(response.endTime).getTime();//getTime 是获取毫秒值
                var currenttime = new Date().getTime();//获取现在的毫秒值

                //结算时间-当前时间 换成秒
                $scope.allSecond = Math.floor((enddate-currenttime)/1000);

                //定时器
                time=$interval(function () {
                    //将秒换成时间
                    $scope.timeString = convertTimeString($scope.allSecond);
                    if ($scope.allSecond>0){
                        $scope.allSecond=$scope.allSecond-1;
                    }else {
                        $interval.cancel(time);
                        alert("秒杀活动已经结束")
                    }
                },1000) /*每秒*/
            }
        )
    };

    //转换秒为   天小时分钟秒格式  XXX天 10:22:33
    convertTimeString=function(allsecond){
        var days= Math.floor( allsecond/(60*60*24));//天数
        var hours= Math.floor( (allsecond-days*60*60*24)/(60*60) );//小时数
        var minutes= Math.floor(  (allsecond -days*60*60*24 - hours*60*60)/60    );//分钟数
        var seconds= allsecond -days*60*60*24 - hours*60*60 -minutes*60; //秒数
        if(days>0){
            days=days+"天 ";
        }
        if(hours<10){
            hours="0"+hours;
        }
        if(minutes<10){
            minutes="0"+minutes;
        }
        if(seconds<10){
            seconds="0"+seconds;
        }
        return days+hours+":"+minutes+":"+seconds;
    };

    /*下订单*/
    $scope.submitOrder=function(){
        seckillGoodsService.submitOrder($scope.entity.id).success(
            function(response){
                if(response.success){
                    alert("下单成功，请在5分钟内完成支付");
                    location.href="pay.html";
                }else if (response.message=='用户没有登陆'){
                   var url = window.location.href;
                   window.location.href="/page/login.do?url"+encodeURIComponent(url); //让用户登录成功跳转到页面要id和参数的
                    //这个方法保留的url参数 ？
                }
                else{
                    alert(response.message);
                }
            }
        );
    }

})