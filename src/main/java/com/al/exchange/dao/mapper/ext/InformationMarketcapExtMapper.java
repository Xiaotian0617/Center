package com.al.exchange.dao.mapper.ext;

import com.al.exchange.dao.domain.MarketCapDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * NOTE:
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author mr.wang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018 2018/9/5 11:10
 */
@Mapper
public interface InformationMarketcapExtMapper {

    @Update("<script>" +
            "   insert IGNORE into information_marketcap (name_self, symbol)\n" +
            "    values\n" +
            "    <foreach collection=\"list\" item=\"item\" separator=\",\">\n" +
            "      (#{item.id,jdbcType=VARCHAR}, #{item.symbol,jdbcType=VARCHAR})\n" +
            "    </foreach>" +
            " </script>")
    void updateOrAddInfomationMarketCaps(@Param("list") List<MarketCapDTO> marketCapDTOS);

}
