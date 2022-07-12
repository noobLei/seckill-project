package com.example.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.seckill.pojo.Goods;
import com.example.seckill.vo.GoodsVo;

import java.util.List;

/**
 * <p>
 * 商品表 Mapper 接口
 * </p>
 *
 * 加油，冲冲冲
 *
 * @author qll
 * @since 2022-06-16
 */
public interface GoodsMapper extends BaseMapper<Goods> {


    /**
     * 获取商品列表
     * @return
     */
    public List<GoodsVo> findGoodsVo();

    /**
     * 获取商品详情
     * @return
     */
    GoodsVo findGoodsVoByGoodsId(Long goodsId);
}
