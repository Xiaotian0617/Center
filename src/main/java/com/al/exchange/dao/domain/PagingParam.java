package com.al.exchange.dao.domain;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

/**
 * 分页参数
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 30/01/2018 15:53
 */
@Data
public class PagingParam {
    public static final int LIMIT_DEFAULT = 100;
    @Range(min = 0, max = LIMIT_DEFAULT, message = "0~100")
    private Integer offset;
    @Range(min = 1, max = LIMIT_DEFAULT, message = "1~100")
    private Integer limit;

    public PagingParam(Integer offset, Integer limit) {
        this.offset = offset;
        this.limit = limit;
    }

    public PagingParam() {
    }
}
