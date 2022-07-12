package com.example.seckill.vo;

import com.example.seckill.pojo.Goods;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 商品公共返回对象
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoodsVo extends Goods {

    //由于继承了Goods,所以Goods的属性,这里也有
    private BigDecimal seckillPrice;
    private Integer stockCount;
    private Date startDate;
    private Date endDate;
}
