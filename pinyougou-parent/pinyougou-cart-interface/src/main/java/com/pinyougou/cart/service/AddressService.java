package com.pinyougou.cart.service;

import com.pinyougou.pojo.TbAddress;

import java.util.List;

public interface AddressService {
    /**
     * findListByUserId 查找集合 通过用户id
     * @param userId
     * @return
     */
    public List<TbAddress> findListByUserId(String userId );

}
