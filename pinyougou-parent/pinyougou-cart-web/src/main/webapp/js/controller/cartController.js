app.controller('cartController',function ($scope,cartService) {

    //写一个方法 当页面加载了之后就调用 查询当前用户的购物车的列表
    $scope.findCartList=function () {
        cartService.findCartList().success(
            function (response) {//List<cart>
                $scope.cartList=response;

                $scope.totalNum=0;
                $scope.totalMoney=0;
                for(var i=0;i<$scope.cartList.length;i++){
                    var cart = $scope.cartList[i];

                    for(var j=0;j<cart.orderItemList.length;j++){
                        $scope.totalNum+=cart.orderItemList[j].num;
                        $scope.totalMoney+=cart.orderItemList[j].totalFee;
                    }

                }
            }
        )
    }

    //写一个方法  当点击+  或者-  就相当于向已有购物车添加商品
    $scope.addGoodsToCartList=function (itemId,num) {
        cartService.addGoodsToCartList(itemId,num).success(
            function (response) {//result
                if(response.success){
                    //刷新页面 重新查询购物车的列表
                    $scope.findCartList();
                }else{
                    alert(response.message);
                }

            }
        )
    };

    //选择地址 点击这个方法 将被点击的地址绑定到
    $scope.selectAddress=function (address) {
        $scope.address=address;
    };

    $scope.isSelectedAddress=function(address){
        if(address==$scope.address){
            return true;
        }else {
            return false;
        }
    };

    //订单用户地址选择页 从初始化方法
    $scope.findAddressList=function () {
        cartService.findAddressList().success(
            function (response) { //返回一个list<TbAddress>
                $scope.addressList=response;
                //设置默认的地址
                for (var i=0; i<$scope.addressList.length;i++){
                    //出数据库中拿数据判断
                    if ($scope.addressList[i].isDefault=='1'){
                        $scope.address=$scope.addressList[i];
                        break;
                    }
                }
            }
        )
    };

    $scope.order={paymentType:'1'};

    //选择支付方式
    $scope.selectPayType=function (type) {
        $scope.order.paymentType=type;
    }


    //结算
    $scope.submitOrder=function () {
        //还有封装一些数据
        $scope.order.receiverAreaName=$scope.address.address;//地址
        $scope.order.receiverMobile=$scope.address.mobile;//手机
        $scope.order.receiver=$scope.address.contact;//联系人
        //提交订单
        cartService.submitOrder($scope.order).success(
            function (response) {
                if (response.success){
                    //提交订单 生成成功
                    if($scope.order.paymentType=='1'){
                        //微信
                        location.href="pay.html";
                    }else {
                        location.href="paysuccess.html"
                    }
                }else {
                    alert(response.message);//订单生成失败
                }
            }
        )
    };



});