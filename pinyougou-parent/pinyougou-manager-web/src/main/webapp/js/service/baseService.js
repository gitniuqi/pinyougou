/创建service
app.service('brandService',function ($http) {
    this.findAll=function () {
        return $http.get('../brand/findAll');
    };
    this.findPage=function (pageNum,pageSize) {
        return  $http.get('../brand/findPage?page='+pageNum+"&rows="+pageSize);
    };

    this.add=function (entity) {
        return $http.post('../brand/add',entity);
    };
    this.update=function (entity) {
        return $http.post('../brand/update',entity);
    };

    this.findOne=function (id) {
        return  $http.get('../brand/findOne?id='+id);
    };

    this.dele=function (ids) {
        return $http.get('../brand/delete?ids='+ids);
    };

    this.search=function (pageNum,pageSize,searchEntity) {
        return $http.post('../brand/search?page='+pageNum+"&rows="+pageSize,searchEntity);
    };

});