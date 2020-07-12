package com.wayn.admin.api.service.shop;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.admin.api.domain.shop.Goods;
import com.wayn.admin.api.domain.vo.GoodsSaveRelatedVO;
import com.wayn.common.util.R;

import java.util.Map;

/**
 * <p>
 * 商品基本信息表 服务类
 * </p>
 *
 * @author wayn
 * @since 2020-07-06
 */
public interface IGoodsService extends IService<Goods> {


    /**
     * 查询商品分页列表
     *
     * @param page  分页对象
     * @param goods 查询参数
     * @return goods分页列表
     */
    IPage<Goods> listPage(Page<Goods> page, Goods goods);

    /**
     * 获取商品详情（包含货品，规格，属性，分类）
     *
     * @param goodsId 商品ID
     * @return 商品详情
     */
    Map<String, Object> getGoodsInfoById(Long goodsId);

    /**
     * 保存商品相关对象
     * @param goodsSaveRelatedVO 商品保存关联VO对象
     * @return R
     */
    R saveGoodsRelated(GoodsSaveRelatedVO goodsSaveRelatedVO);

    /**
     * 校验商品名称是否唯一
     *
     * @param goods 栏目信息
     * @return 状态码 0 唯一 1 不唯一
     */
    String checkGoodsNameUnique(Goods goods);
}
