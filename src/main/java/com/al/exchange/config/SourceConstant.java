package com.al.exchange.config;

import java.util.Objects;

/**
 * NOTE:
 * 数据来源枚举类 表示抓取的方式 例如网页或API
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author mr.wang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018/6/22 11:38
 */
public enum SourceConstant {

    Api("api"),
    Web("web"),
    Calc("calc"),
    None("none");


    private final String value;

    SourceConstant(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }

    public static SourceConstant valueExist(String value) {
        SourceConstant[] sourceConstants = values();
        for (SourceConstant sourceConstant : sourceConstants) {
            if (Objects.equals(sourceConstant.value, value)) {
                return sourceConstant;
            }
        }
        return null;
    }

}
