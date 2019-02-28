package com.al.exchange.util.api;

import retrofit2.Call;
import retrofit2.http.*;

/**
 * file:topcoin
 * <p>
 * 文件简要说明
 *
 * @author 17:23  王楷
 * @version 17:23 V1.0
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
public interface PriceApi {

    //https://api.fixer.io/latest?base=USD&symbols=USD,CNY

    /**
     * 获取汇率方法接口 接口从Fixer查询，查询从2018年6月12日 13:49:35起改为加入Appkey以应对其接口变化
     *
     * AppKey：25081458
     * AppSecret：1e86bd934106c4621740a85ffb9b3455
     * AppCode：c156dd9993ef41c6b40d07a99195de98
     *
     * @param from  换算前的币种
     * @param to 要换算的币种
     * @return
     */
    @POST("/exchange/currency")
    @FormUrlEncoded
    @Headers({
            "Authorization:APPCODE c156dd9993ef41c6b40d07a99195de98"
    })
    Call<String> getRates(@Field("from") String from, @Field("to") String to);

}
