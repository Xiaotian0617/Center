package com.al.exchange.task;


import com.al.exchange.dao.domain.MarketKey;
import com.al.exchange.dao.domain.MarketVO;
import com.al.exchange.service.DataProvideService;
import com.al.exchange.service.management.MarketCapManagement;
import com.al.exchange.util.OperationFileUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CalcCenter
 * file:SaveMsgToHardDriveTask
 * <p>  定时保存重要信息到硬盘
 *
 * @author mr.wang
 * @version 2018年03月08日10:39:53 V1.0
 * @par 版权信息：
 * 2018 copyright 河南艾鹿网络科技有限公司 all rights reserved.
 */
@Slf4j
@Component
public class SaveMsgToHardDriveTask {

    @Autowired
    MarketCapManagement marketCapManagement;

    @Autowired
    OperationFileUtils operationFileUtils;

    @Autowired
    MarketInfoEnhanceTask marketInfoEnhanceTask;

    @Autowired
    DataProvideService dataProvideService;

    public static final String marketCapDirectoryName = "";

    public static final String marketCapFileName = "marketCap.json";

    public static final String marketDirectoryName = "markets/";

    public static final String marketFileName = "market_%s.json";

    public static final String onlyKeyDirectoryName = "";

    public static final String onlyKeyFileName = "onlyKey.json";

    //@Scheduled(cron = "${scheduled.savemarketcaptodisk}")
    private void saveMarketCap() {
        try {
            if (marketCapManagement.getMarketCaps().size() == 0) {
                log.warn("市值Map中无数据，无法保存！");
                return;
            }
            String content = JSONArray.toJSONString(marketCapManagement.getMarketCaps());
            if (!operationFileUtils.writeFile(marketCapDirectoryName, marketCapFileName, content)) {
                log.error("保存市值信息至硬盘出错，时间为" + new Date());
            }
            log.debug("读硬盘中市值信息内容为{}", operationFileUtils.readFile(marketCapDirectoryName, marketCapFileName));
        } catch (Throwable e) {
            log.error("定时保存市值信息至硬盘出错，错误信息为：", e);
        }
    }

    //@Scheduled(cron = "${scheduled.savemarkettodisk}")
    private void saveMarketByMinute() {
        try {
            //由于系统中保存的最新Map的机制决定，所以在每分钟40秒时执行保存当前分钟的Map（由于需要新建List防止那边变动过快，所以没取整点）
            if (marketInfoEnhanceTask.getMarketMap().size() == 0) {
                //如果当前Map数据有误，即无数据，有可能是在初始化时，进行预警即可
                //无需担心此时会出现无写入文件导致到时候查询时涨跌幅无法计算，到时候会往前推进或查询数据库
                log.warn("当前市场Map中无数据，无法进行文件保存！");
                return;
            }
            //TODO 此处循环可能需要枷锁
            Map<String, MarketVO> marketVOS = new HashMap<>(marketInfoEnhanceTask.getMarketMap());
            String content = JSONArray.toJSONString(marketVOS);
            Date nowDate = new Date();
            String fileName = String.format(marketFileName, nowDate.getTime());
            if (!operationFileUtils.writeFile(marketDirectoryName, fileName, content)) {
                log.error("保存行情信息至硬盘出错，时间为" + nowDate);
            }
            //删除掉Market文件夹中最老的一个文件
            operationFileUtils.deleteMarketOldestFile();
            log.debug("读硬盘中当前分钟的行情信息内容为{}", content);
        } catch (Throwable e) {
            log.error("定时保存行情信息至硬盘出错，错误信息为：", e);
        }
    }

    //@Scheduled(cron = "${scheduled.saveonlykeytodisk}")
    private void saveOnlyKeyByMinute() {
        try {
            //在每分钟55秒时从文件中拿到刚保存的行情信息，用于遍历
//            File file = operationFileUtils.getMarketLastFile();
//            String marketContent = operationFileUtils.readFile(file.toPath());
//            if (marketContent==null){
//                log.error("从文件中未找到最新的行情信息！将跳过本次OnlyKey保存！");
//                return;
//            }
            List<MarketKey> list = dataProvideService.getAllOnlyKeysFromDB();
            if (!operationFileUtils.writeFile(onlyKeyDirectoryName, onlyKeyFileName, JSONObject.toJSONString(list))) {
                log.error("保存行情信息至硬盘出错，时间为" + new Date());
            }
            //log.debug("读硬盘中当前分钟的OnlyKey信息内容为{}",operationFileUtils.readFile(onlyKeyDirectoryName,onlyKeyFileName));
        } catch (Throwable e) {
            log.error("定时保存OnlyKey信息至硬盘出错，错误信息为：", e);
        }
    }


}
