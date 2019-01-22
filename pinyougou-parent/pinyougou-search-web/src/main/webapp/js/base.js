var app = angular.module('pinyougou', []);//定义模块

app.filter('trustHtml',function ($sce) {
    return function (data) {
        return $sce.trustAsHtml(data);
    }
});