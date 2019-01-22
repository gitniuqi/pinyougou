package com.pinyougou.sellergoods.service.impl;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.pojogroup.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;

	@Autowired
	private TbGoodsDescMapper goodsDescMapper;

	//通过品牌id查询名称
	@Autowired
	private TbBrandMapper brandMapper;

	//通过分类id
	@Autowired
	private TbItemCatMapper itemCatMapper;

	@Autowired
	private TbSellerMapper sellerMapper;

	@Autowired
	private TbItemMapper itemMapper;


	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
		try {
			//调用tbgoods的方法 插入goodes
			TbGoods tbgoods = goods.getGoods();
			//设置默认状态
			tbgoods.setAuditStatus("0");
			//设置tbgoods的is_delete为1
			tbgoods.setIsDelete(false);
			//到insert的sql语句修改 主键返回
			goodsMapper.insert(tbgoods);
			//拿到goods的id 赋值给页面传递来的goodsdesc中
			goods.getGoodsDesc().setGoodsId(tbgoods.getId());
			//将数据添加到数据库中 主键也对应了赋值了
			goodsDescMapper.insert(goods.getGoodsDesc());//插入商品扩展数据
			//遍历每个规格集合 向数据库sku表中添加遍历出来的每一天
			//TODO
			saveItems(tbgoods, goods.getGoodsDesc(), goods.getItemList());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void saveItems(TbGoods tbGoods, TbGoodsDesc goodsDesc, List<TbItem> itemList) {
        String isEnableSpec = tbGoods.getIsEnableSpec();

        if ("1".equals(tbGoods.getIsEnableSpec())){
			//启用规格
			for (TbItem item : itemList) {
				//标题
				String title = tbGoods.getGoodsName();
				Map<String, Object> specMap = JSON.parseObject(item.getSpec());
				for (String key : specMap.keySet()) {
					title += "" + specMap.get(key);
				}
				item.setTitle(title);
				item.setGoodsId(tbGoods.getId());//商品SPU编号
				item.setSellerId(tbGoods.getSellerId());//商家编号
				item.setCategoryid(tbGoods.getCategory3Id());//商品分类编号（3级）
				item.setCreateTime(new Date());//创建日期
				item.setUpdateTime(new Date());//修改日期
				//品牌名称
				TbBrand brand = brandMapper.selectByPrimaryKey(tbGoods.getBrandId());
				item.setBrand(brand.getName());
				//分类名称
				TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id());
				item.setCategory(itemCat.getName());
				//商家名称
				TbSeller seller = sellerMapper.selectByPrimaryKey(tbGoods.getSellerId());
				item.setSeller(seller.getNickName());
				//图片地址（取spu的第一个图片）
				List<Map> imageList = JSON.parseArray(goodsDesc.getItemImages(), Map.class);
				if (imageList.size() > 0) {
					item.setImage((String) imageList.get(0).get("url"));
				}
				itemMapper.insert(item);
			}
		}else {
			//不启用规格
			//页面没有sku传过来
			TbItem tbItem = new TbItem();
			tbItem.setTitle(tbGoods.getGoodsName());
			tbItem.setPrice(tbGoods.getPrice());
			tbItem.setNum(999); //这个是在规格里传过来的没有规格了就写死~
			tbItem.setStatus("1");//商品状态
			tbItem.setIsDefault("1");
			tbItem.setSpec("{}");//没有sku了为空

			//图片
			String itemImages = goodsDesc.getItemImages();
			List<Map> list = JSON.parseArray(itemImages, Map.class);
			String url = (String) list.get(0).get("url");
			tbItem.setImage(url);

			//设置商品id
			TbItemCat tbItemCat = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id());
			tbItem.setCategoryid(tbItemCat.getId());
			tbItem.setCategory(tbItemCat.getName());


			//设置创建时间
			tbItem.setCreateTime(new Date());
			tbItem.setUpdateTime(tbItem.getCreateTime());

			tbItem.setGoodsId(tbGoods.getId());


			//设置卖家

			TbSeller seller = sellerMapper.selectByPrimaryKey(tbGoods.getSellerId());
			tbItem.setSellerId(seller.getSellerId());
			tbItem.setSeller(seller.getNickName());//店铺名

			//设置品牌名称
			TbBrand tbBrand = brandMapper.selectByPrimaryKey(tbGoods.getBrandId());
			tbItem.setBrand(tbBrand.getName());
			itemMapper.insert(tbItem);
		}

	}

	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){
		//更新spu
		TbGoods goods1 = goods.getGoods();
		goods1.setAuditStatus("0");
		int i = goodsMapper.updateByPrimaryKey(goods1);
		System.out.println(i);
		//更新描述
		goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());
		//更新sku列表
		TbItemExample example = new TbItemExample();
		example.createCriteria().andGoodsIdEqualTo(goods1.getId());
		itemMapper.deleteByExample(example);//delete
		//insert
		saveItems(goods1,goods.getGoodsDesc(),goods.getItemList());
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
		Goods goods = new Goods();
		TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
		goods.setGoods(tbGoods);
		TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
		goods.setGoodsDesc(tbGoodsDesc);
		//3添加商品的sku列表信息
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(id);//select * from x where goodsid = id
		List<TbItem> tbItems = itemMapper.selectByExample(example);
		goods.setItemList(tbItems);
		return goods;
	}

	/**
	 * 批量删除 逻辑删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
			tbGoods.setIsDelete(true);//设置isdalete为true
			goodsMapper.updateByPrimaryKey(tbGoods);//更新
		}		
	}
	
	
		@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();

		//添加判断isdelete是否为true
		criteria.andIsDeleteEqualTo(false);
		if(goods!=null){			
			if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				/*criteria.andSellerIdLike("%"+goods.getSellerId()+"%");*/
				criteria.andSellerIdEqualTo(goods.getSellerId());
			}
			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}
			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}
			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}
			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}
			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}
			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}
	
		}
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	//品优购修改商品状态
	@Override
	public void updateStatus(Long[] ids, String status) {
		for (Long id : ids) {
			TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);//查询
			tbGoods.setAuditStatus(status);
			goodsMapper.updateByPrimaryKey(tbGoods);
		}
	}

	/**
	 * 根据商品id和查询状态为1的itemList 是sku
	 * @param goodsIds
	 * @return
	 */

	public List<TbItem> findItemListByGoodsId(Long[] goodsIds) {
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andStatusEqualTo("1");
		List<Long> longs = Arrays.asList(goodsIds);//（T... a）返回这个t类型的list集合
		criteria.andGoodsIdIn(longs);
		List<TbItem> itemList = itemMapper.selectByExample(example);//select * from tb_item where goods_id in (1,2,3) and status=1
		return itemList;
	}

}
