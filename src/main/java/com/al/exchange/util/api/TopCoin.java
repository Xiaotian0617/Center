package com.al.exchange.util.api;

import org.springframework.http.MediaType;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * file:spider
 * <p>
 * 文件简要说明
 *
 * @author 19:40  王楷
 * @version 19:40 V1.0
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
public interface TopCoin {

    /**
     * 发送Websocket信息通知
     *
     * @param json
     * @return
     */
    @POST("/ws/flushws")
    @Headers({"Content-Type: " + MediaType.APPLICATION_JSON_UTF8_VALUE})
    Call<String> sendInfo(@Body String json);

}
