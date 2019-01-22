package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.AddressService;
import com.pinyougou.mapper.TbAddressMapper;
import com.pinyougou.pojo.TbAddress;
import com.pinyougou.pojo.TbAddressExample;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private TbAddressMapper addressMapper;

    /**
     * 通过用户id查找这个用户的地址集合
     * @param userId
     * @return
     */
    @Override
    public List<TbAddress> findListByUserId(String userId) {
        TbAddressExample example = new TbAddressExample();
        TbAddressExample.Criteria criteria = example.createCriteria();
        criteria.andUserIdEqualTo(userId);
        List<TbAddress> tbAddresses = addressMapper.selectByExample(example);
        return tbAddresses;
    }
}
