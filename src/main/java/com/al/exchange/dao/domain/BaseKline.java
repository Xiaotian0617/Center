package com.al.exchange.dao.domain;

import lombok.Data;

/**
 * NOTE:
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author mr.wang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018/6/30 9:30
 */
@Data
public class BaseKline {

    private String measurement;

    public BaseKline(String measurement) {
        this.measurement = measurement;
    }
}
