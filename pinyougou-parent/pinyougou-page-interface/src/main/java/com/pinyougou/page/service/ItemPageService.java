package com.pinyougou.page.service;

public interface ItemPageService {

    /**
     * 根据商品信息  生成静态页面 模板+数据集=html
     * @param id
     */
    public void genHtml(Long id);


}
