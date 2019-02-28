package com.al.exchange.dao.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * file:topcoin
 * <p>
 * 交易所对象
 *
 * @author 20:03  王楷
 * @version 20:03 V1.0
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
@Data
@EqualsAndHashCode(of = "eName")
public class Exchange {

    //中文名
    private String cName;

    //英文名
    private String eName;

    private String img;

    //TODO 等等其他属性

    public Exchange(String eName) {
        this.eName = eName;
    }

    public Exchange() {
    }

}
