package com.al.exchange.util.api;

import com.alibaba.fastjson.support.retrofit.Retrofit2ConverterFactory;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Converter;
import retrofit2.Retrofit;

@Configuration
public class AAPI {

    @Autowired
    private OkHttpClient okHttpClient;

    public <T> T build(String baseUrl, Class<T> clz) {
        return build(baseUrl, clz, new CustomConvertFactory(), new Retrofit2ConverterFactory());
    }

    private <T> T build(String baseUrl, Class<T> clz, Converter.Factory... factory) {
        Retrofit.Builder builder = new Retrofit.Builder();
        for (Converter.Factory f : factory) {
            builder.addConverterFactory(f);
        }
        return builder
                .client(okHttpClient)
                .baseUrl(baseUrl)
                .build()
                .create(clz);
    }

    @Bean
    public TopCoin topCoin(@Value("${websocket.topcoinws.url}") String url) {
        return build(url, TopCoin.class);
    }

    @Bean
    public PriceApi priceApi(@Value("http://exchange.market.alicloudapi.com") String url) {
        return build(url, PriceApi.class);
    }

}
