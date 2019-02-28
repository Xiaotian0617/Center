package com.al.exchange.dao.domain;

/**
 * 消息源类型
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018-01-31
 */
public enum MarketSourceType {
    Exchange(""),
    Mytoken("t"),
    Alcoin("a"),
    Quintar("q"),
    Other("o"),
    Web("w");

    public final String value;

    MarketSourceType(String value) {
        this.value = value;
    }
}
