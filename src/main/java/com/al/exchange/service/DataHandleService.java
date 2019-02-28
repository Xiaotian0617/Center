package com.al.exchange.service;

import com.al.exchange.dao.domain.OnlyKey;

import java.util.List;

/**
 * NOTE:
 * 根据不同设置处理不同来源的数据，最终入库
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author mr.wang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018/6/22 10:15
 */
public interface DataHandleService {

    void saveDatasToDB(List<?> datas);

    void saveDataToDB(OnlyKey data);

}
