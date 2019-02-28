package com.al.exchange.controller;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.al.exchange.dao.domain.*;
import com.al.exchange.service.DataProvideService;
import com.al.exchange.service.management.HighLowChangeManagement;
import com.al.exchange.service.management.MarketCapManagement;
import com.al.exchange.service.management.OnlyKeyManagement;
import com.al.exchange.service.management.PriceChangeManagement;
import com.al.exchange.task.MarketInfoEnhanceTask;
import com.al.exchange.task.PriceCheckTask;
import com.al.exchange.task.PriceRateTask;
import com.al.exchange.util.PoiExcel;
import com.al.exchange.util.redis.ObjectRedisService;
import com.al.exchange.web.WebResult;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * file:topcoin
 * <p>
 * 数据提供接口
 *
 * @author 14:56  王楷
 * @version 14:56 V1.0
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
@Slf4j
@Controller
@RequestMapping(value = "data")
public class DataController {

    @Resource
    private DataProvideService dataProvideService;

    @Autowired
    MarketCapManagement marketCapManagement;

    @Autowired
    private PriceChangeManagement priceChangeManagement;

    @Autowired
    MarketInfoEnhanceTask marketInfoEnhanceTask;

    @Autowired
    PriceRateTask priceRateTask;

    @Autowired
    PriceCheckTask priceCheckTask;

    @Autowired
    HighLowChangeManagement highLowChangeManagement;

    @Autowired
    ObjectRedisService objectRedisService;

    @Resource
    OnlyKeyManagement onlyKeyManagement;

    /**
     * 供下方返回百分比时相乘使用
     */
    final BigDecimal HUNDRED = new BigDecimal(100);

    /**
     * 获得所有库中拥有的交易所列表
     *
     * @return
     */
    @RequestMapping("getAllOnlyKeys")
    @ResponseBody
    public WebResult getAllOnlyKeys() {
        try {
            return WebResult.okResult(dataProvideService.getAllOnlyKeys());
        } catch (Exception e) {
            log.error("获取所有交易所失败！", e);
            return WebResult.failResult(6000);
        }
    }

    @RequestMapping("downloadAllSymbolByExchange")
    public void downloadAllSymbolByExchange(String exchanges, HttpServletResponse response) {
        try {
            Collection<MarketKey> allOnlyKeys = dataProvideService.getAllOnlyKeys();
            Set<Symbols> symbols = getSymbols(exchanges, allOnlyKeys);
            PoiExcel.exportExcel(symbols.stream().collect(Collectors.toList()), "各大交易所币种列表", "币种列表", Symbols.class, "各大交易所币种列表.xls", response);
            //return WebResult.okResult(symbols);
        } catch (Exception e) {
            log.error("获取所有交易所失败！", e);
            //return WebResult.failResult(6000);
        }
    }

    private Set<Symbols> getSymbols(String exchanges, Collection<MarketKey> allOnlyKeys) {
        Set<Symbols> symbols;
        symbols = allOnlyKeys.stream().filter(marketKey -> {
            if (StringUtils.isEmpty(exchanges)) {
                return true;
            }
            return exchanges.contains(marketKey.exchange());
        }).map(marketKey -> {
            Symbols symbols1 = new Symbols();
            symbols1.setExchange(marketKey.exchange());
            symbols1.setSymbol(marketKey.symbol());
            symbols1.setUnit(marketKey.unit());
            return symbols1;
        }).collect(Collectors.toSet());
        return symbols;
    }

    @RequestMapping("getAllSymbolByExchange")
    @ResponseBody
    public WebResult getAllSymbolByExchange(@RequestBody JSONObject jsonObject, HttpServletResponse response) {
        try {
            Collection<MarketKey> allOnlyKeys = dataProvideService.getAllOnlyKeys();
            String exchanges = jsonObject.getString("exchanges");
            Set<Symbols> symbols = getSymbols(exchanges, allOnlyKeys);
            //PoiExcel.exportExcel(symbols.stream().collect(Collectors.toList()), "各大交易所币种列表","币种列表",Symbols.class,"各大交易所币种列表",response);
            return WebResult.okResult(symbols);
        } catch (Exception e) {
            log.error("获取所有交易所失败！", e);
            return WebResult.failResult(6000);
        }
    }

    @Data
    class Symbols {
        @Excel(name = "交易所", orderNum = "0")
        private String exchange;
        @Excel(name = "币种", orderNum = "1")
        private String symbol;
        @Excel(name = "市场", orderNum = "2")
        private String unit;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Symbols symbols = (Symbols) o;
            return Objects.equals(exchange, symbols.exchange) &&
                    Objects.equals(symbol, symbols.symbol) &&
                    Objects.equals(unit, symbols.unit);
        }

        @Override
        public int hashCode() {

            return Objects.hash(exchange, symbol, unit);
        }
    }

    /**
     * 获得所有库中拥有的交易所列表
     */
    @RequestMapping("getAllEXchange")
    @ResponseBody
    public WebResult getAllEXchange() {
        Collection<Exchange> list;
        try {
            list = dataProvideService.getAllEXchange();
            return WebResult.okResult(list);
        } catch (Exception e) {
            log.error("获取所有交易所失败！", e);
            return WebResult.failResult(6000);
        }
    }

    /**
     * 获得所有市值信息 未做排序
     */

    @ResponseBody
    @RequestMapping(value = "getAllMarketCaps", method = RequestMethod.POST, produces = "application/json")
    public WebResult getAllMarketCaps(@Valid PagingParam pagingParam) {
        if (pagingParam.getOffset() == null || pagingParam.getLimit() == null) {
            return WebResult.okResult(marketCapManagement.getMarketCaps());
        }
        List<MarketCap> list;
        try {
            list = marketCapManagement.getMarketCaps(pagingParam.getOffset(), pagingParam.getLimit());
            return WebResult.okResult(list);
        } catch (Exception e) {
            log.error("获取所有市值失败！", e);
            return WebResult.failResult(6000);
        }
    }

    /**
     * 根据某个币种全称查询币种市值信息
     *
     * @param obj 币种全称
     *            例：{
     *            "ename":"bitcoin"
     *            }
     */

    @ResponseBody
    @RequestMapping(value = "getMarketCapById", method = RequestMethod.POST, produces = "application/json")
    public WebResult getMarketCapById(@RequestBody String obj) {
        if (StringUtils.isEmpty(obj)) {
            return WebResult.failResult(1000);
        }
        MarketCap marketCap;
        try {
            String id = JSONObject.parseObject(obj).getString("ename");
            if (StringUtils.isEmpty(id)) {
                return WebResult.failResult(1000);
            }
            //marketCap = dataProvideService.getMarketCapById(id);
            marketCap = dataProvideService.getMarketCapFromMapById(id);
            return WebResult.okResult(marketCap);
        } catch (Exception e) {
            log.error("获取某个币种市值失败！", e);
            return WebResult.failResult(6000);
        }
    }

    /**
     * 获得所有市值和24小时价格走势信息 未做排序
     *
     * @param obj 排名小于传入数据的值例：{
     *            "limit":2
     *            }
     *            例如：传入 2 ，则查询出来的为市值排名前2位的币种的价格走势
     */

    @ResponseBody
    @RequestMapping(value = "getAllMarketCapVo", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public WebResult getAllMarketCapVo(@RequestBody String obj) {
        if (StringUtils.isEmpty(obj)) {
            return WebResult.failResult(1000);
        }
        List<DataProvideService.MarketCapVo> list;
        try {
            int limit = JSONObject.parseObject(obj).getIntValue("limit");
            if (limit == 0) {
                return WebResult.failResult(1000);
            }
            list = dataProvideService.getAllMarketCapVo(limit);
            return WebResult.okResult(list);
        } catch (Exception e) {
            log.error("获取所有市值加价格信息失败！", e);
            return WebResult.failResult(6000);
        }
    }


    @ResponseBody
    @RequestMapping(value = "getAllMarketBySymbol", method = RequestMethod.POST, produces = "application/json")
    public WebResult getAllMarketBySymbol(@RequestBody JSONObject obj) {
        if (MapUtils.isEmpty(obj)) {
            return WebResult.failResult(1000);
        }
        List<MarketVO> list;
        try {
            String coinName = obj.getString("coinName");
            Integer offset = obj.getInteger("offset");
            Integer limit = obj.getInteger("limit");
            if (StringUtils.isEmpty(coinName)) {
                return WebResult.failResult(1000);
            }
            list = dataProvideService.getAllMarketBySymbol(new PagingParam(offset, limit), coinName);
            return WebResult.okResult(list);
        } catch (Exception e) {
            log.error("通过币种获取所有信息失败！", e);
            return WebResult.failResult(6000);
        }
    }


    @ResponseBody
    @RequestMapping(value = "getAllMarketByExchange", method = RequestMethod.POST, produces = "application/json")
    public WebResult getAllMarketByExchange(@RequestBody JSONObject obj) {
        if (MapUtils.isEmpty(obj)) {
            return WebResult.failResult(1000);
        }
        List<MarketVO> list;
        try {
            String exchangeName = obj.getString("exchangeName");
            Integer offset = obj.getInteger("offset");
            Integer limit = obj.getInteger("limit");
            if (StringUtils.isEmpty(exchangeName)) {
                return WebResult.failResult(1000);
            }
            list = dataProvideService.getAllMarketByExchange(new PagingParam(offset, limit), exchangeName);
            return WebResult.okResult(list);
        } catch (Exception e) {
            log.error("通过交易所获取信息失败！", e);
            return WebResult.failResult(6000);
        }
    }

    /**
     * 提供大盘交易获取多个交易所行情使用
     *
     * @param obj
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "getAllMarketByExchangeList", method = RequestMethod.POST, produces = "application/json")
    public WebResult getAllMarketByExchangeList(@RequestBody JSONObject obj) {
        if (MapUtils.isEmpty(obj)) {
            return WebResult.failResult(1000);
        }
        List<MarketVO> list = new ArrayList<>();
        try {
            String exchangeName = obj.getString("exchangeName");
            Integer offset = obj.getInteger("offset");
            Integer limit = obj.getInteger("limit");
            if (StringUtils.isEmpty(exchangeName)) {
                return WebResult.failResult(1000);
            }
            String[] split = exchangeName.split(",");
            for (int i = 0; i < split.length; i++) {
                list.addAll(dataProvideService.getAllMarketByExchange(new PagingParam(offset, limit), split[i]));
            }
            return WebResult.okResult(list);
        } catch (Exception e) {
            log.error("通过交易所获取信息失败！", e);
            return WebResult.failResult(6000);
        }
    }


    @ResponseBody
    @RequestMapping(value = "getAllMarketByExchangeOrSymbol", method = RequestMethod.POST, produces = "application/json")
    public WebResult getAllMarketByExchangeOrSymbol(@RequestBody JSONObject obj) {
        if (MapUtils.isEmpty(obj)) {
            return WebResult.failResult(1000);
        }
        List<MarketVO> list;
        try {
            String name = obj.getString("name");
            Integer offset = obj.getInteger("offset");
            Integer limit = obj.getInteger("limit");
            String columnName = obj.getString("columnName");
            String sortDirection = obj.getString("sortDirection");
            if (!StringUtils.hasText(name)) {
                return WebResult.failResult(1000);
            }
            list = dataProvideService.getAllMarketByExchangeOrSymbol(new PagingParam(offset, limit), name,columnName,sortDirection);
            return WebResult.okResult(list);
        } catch (Exception e) {
            log.error("通过交易所获取信息失败！", e);
            return WebResult.failResult(6000);
        }
    }


    @ResponseBody
    @RequestMapping(value = "getKlineByOnlyKey", method = RequestMethod.POST, produces = "application/json")
    public WebResult getKlineByOnlyKey(@RequestBody String obj) {
        if (StringUtils.isEmpty(obj)) {
            return WebResult.failResult(1000);
        }
        List<KLine> list;
        try {
            JSONObject jsonObject = JSONObject.parseObject(obj);
            String onlyKey = jsonObject.getString("onlyKey");
            String[] keys = onlyKey.split("_");
            if (Objects.equals(onlyKey, null) || Objects.equals(onlyKey, "") || keys.length != 3) {
                return WebResult.failResult(1000);
            }
            if (jsonObject.getLong("lastTime") == null || jsonObject.getLong("lastTime") == 0L) {
                list = dataProvideService.getSettedKlineByOnlyKey(onlyKey, keys[0], keys[1], keys[2], jsonObject.getString("timeType"), jsonObject.getIntValue("limitNum"));
            } else {
                list = dataProvideService.getSettedKlineByOnlyKey(onlyKey, keys[0], keys[1], keys[2], jsonObject.getString("timeType"), jsonObject.getLong("lastTime"), jsonObject.getIntValue("limitNum"));
            }
            return WebResult.okResult(list);
        } catch (Throwable e) {
            log.error("获取K线数据失败！", e);
            return WebResult.failResult(6000);
        }
    }

    @ResponseBody
    @RequestMapping(value = "getKlineByOnlyKeys", method = RequestMethod.POST, produces = "application/json")
    public WebResult getKlineByOnlyKeys(@RequestBody JSONObject obj) {
        if (null == obj || StringUtils.isEmpty(obj.getString("onlyKeys"))) {
            return WebResult.failResult(1000);
        }
        try {
            List<String> onlyKeys = JSONArray.parseArray(obj.getString("onlyKeys"), String.class);
            Object collect = onlyKeys.stream().map(onlyKey -> {
                String[] keys = onlyKey.split("_");
                if (Objects.equals(onlyKey, null) || Objects.equals(onlyKey, "") || keys.length != 3) {
                    return null;
                }
                List list;
                if ((obj.getLong("lastTime") == null) || (obj.getLong("lastTime") == 0L)) {
                    list = dataProvideService.getKlineByOnlyKey(onlyKey, keys[0], keys[1], keys[2], obj.getString("timeType"), obj.getIntValue("limitNum"));
                    return new KeyKline(onlyKey, list);
                } else {
                    list = dataProvideService.getKlineByOnlyKey(onlyKey, keys[0], keys[1], keys[2], obj.getString("timeType"), obj.getLong("lastTime"), obj.getIntValue("limitNum"));
                    return new KeyKline(onlyKey, list);
                }
            }).filter(keyKline -> keyKline != null).collect(Collectors.toMap(KeyKline::getOnlyKey, KeyKline::getKLines));
            return WebResult.okResult(collect);
        } catch (Throwable e) {
            log.error("获取K线数据失败！", e);
            return WebResult.failResult(6000);
        }
    }

    @Data
    class KeyKline {
        private String onlyKey;
        private List<KLine> kLines;

        public KeyKline(String onlyKey, List<KLine> kLines) {
            this.onlyKey = onlyKey;
            this.kLines = kLines;
        }
    }

    @ResponseBody
    @RequestMapping(value = "getRateKlineByTime", method = RequestMethod.POST, produces = "application/json")
    public WebResult getRateKlineByTime(@RequestBody JSONObject obj) {
        if (obj == null) {
            return WebResult.failResult(1000);
        }
        List<PriceVo> list;
        try {
            Long startTime = obj.getLong("startTime");
            Long endTime = obj.getLong("endTime");
            Integer num = obj.getInteger("num");
            String symbol = obj.getString("symbol");
            String unit = obj.getString("unit");
            String type = obj.getString("type");
            list = dataProvideService.getRateKlineByTime(startTime, endTime, num, symbol, unit, type);
            if (list == null) {
                return WebResult.okResult(new ArrayList<>());
            }
            return WebResult.okResult(list);
        } catch (Throwable e) {
            log.error("获取K线数据失败！", e);
            return WebResult.failResult(6000);
        }
    }

    /**
     * 获得现在服务器所有的数据汇率
     */

    @ResponseBody
    @RequestMapping(value = "getAllPriceRate", method = RequestMethod.POST)
    public WebResult getAllPriceRate() {
        return WebResult.okResult(priceRateTask.getMap());
    }

    /**
     * 根据传值获取数据汇率
     *
     * @param obj json类型的传入方式
     */

    @ResponseBody
    @RequestMapping(value = "getPriceRateByKey", method = RequestMethod.POST, produces = "application/json")
    public WebResult getPriceRateByKey(@RequestBody String obj) {
        if (StringUtils.isEmpty(obj)) {
            return WebResult.failResult(1000);
        }
        JSONObject jsonObject = JSONObject.parseObject(obj);
        return WebResult.okResult(priceRateTask.getPriceRate(jsonObject.getString("key")));
    }


    /**
     * 根据key值修改K线数据
     */

    @ResponseBody
    @RequestMapping(value = "editKlineByOnlyKey", method = RequestMethod.POST, produces = "application/json")
    public WebResult editKlineByOnlyKey(@RequestBody String obj) {
        if (StringUtils.isEmpty(obj)) {
            return WebResult.failResult(1000);
        }
        JSONObject jsonObject = JSONObject.parseObject(obj);
        if (StringUtils.isEmpty(jsonObject.getString("onlyKey"))
                || StringUtils.isEmpty(jsonObject.getString("timeType"))
                || StringUtils.isEmpty(jsonObject.getString("timestamp"))) {
            return WebResult.failResult(1000);
        }
        try {
            boolean result = dataProvideService.editKlineByOnlyKey(jsonObject);
            if (result) {
                return WebResult.okResult();
            } else {
                return WebResult.failResult(6001);
            }
        } catch (Exception e) {
            log.error("修改K线数据失败，", e);
            e.printStackTrace();
            return WebResult.failResult(6001);
        }
    }

    /**
     * 根据onlyKey获取最新的市场信息
     *
     * @param obj
     */
    @ResponseBody
    @RequestMapping(value = "getLastMarketByOnlyKey", method = RequestMethod.POST, produces = "application/json")
    public WebResult getLastMarketByOnlyKey(@RequestBody String obj) {
        if (StringUtils.isEmpty(obj)) {
            return WebResult.failResult(1000);
        }
        try {
            JSONObject jsonObject = JSONObject.parseObject(obj);
            List<String> keys = jsonObject.getJSONArray("onlyKeys").toJavaList(String.class);
            List<MarketVO> list = dataProvideService.getLastMarketByOnlyKey(keys);
            return WebResult.okResult(list);
        } catch (Exception e) {
            e.printStackTrace();
            return WebResult.failResult(9999, e);
        }
    }

    /**
     * 获取Map中最新的市场信息
     */
    @ResponseBody
    @RequestMapping(value = "getFinalMarketInfo", method = RequestMethod.POST, produces = "application/json")
    public WebResult getFinalMarketInfo(@RequestBody List<String> onlyKeys) {
        HashMap<Object, Object> hashMap = Maps.newHashMap();
        if ("All".equals(onlyKeys.get(0))) {
            onlyKeys.forEach(o -> hashMap.putAll(marketInfoEnhanceTask.getMarketMap()));
        } else {
            onlyKeys.forEach(o -> hashMap.put(o, marketInfoEnhanceTask.getMarketMap().get(o)));
        }
        return WebResult.okResult(hashMap);
    }

    /**
     * 获取此时24小时前的最后一条数据
     */
    @ResponseBody
    @RequestMapping(value = "getLast24HourMarket", method = RequestMethod.POST, produces = "application/json")
    public WebResult getLast24HourMarket(@RequestBody List<String> onlyKeys) {
        HashMap<Object, Object> hashMap = Maps.newHashMap();
        if ("All".equals(onlyKeys.get(0))) {
            onlyKeys.forEach(o -> hashMap.putAll(marketInfoEnhanceTask.getLast24HourMarket()));
        } else {
            onlyKeys.forEach(o -> hashMap.put(o, marketInfoEnhanceTask.getLast24HourMarket().get(o)));
        }
        return WebResult.okResult(hashMap);
    }

    /**
     * 获取今日凌晨前的最后一条数据
     */
    @ResponseBody
    @RequestMapping(value = "getLastZeroTimeMarket", method = RequestMethod.POST, produces = "application/json")
    public WebResult getLastZeroTimeMarket(@RequestBody List<String> onlyKeys) {
        HashMap<Object, Object> hashMap = Maps.newHashMap();
        if (onlyKeys.get(0).equals("All")) {
            onlyKeys.forEach(o -> hashMap.putAll(marketInfoEnhanceTask.getLastZeroTimeMarket()));
        } else {
            onlyKeys.forEach(o -> hashMap.put(o, marketInfoEnhanceTask.getLastZeroTimeMarket().get(o)));
        }
        return WebResult.okResult(hashMap);
    }

    /**
     * 获取每分钟的比较结果
     */
    @ResponseBody
    @RequestMapping(value = "getCheckResult", method = RequestMethod.POST, produces = "application/json")
    public WebResult getCheckResult(@RequestBody List<String> onlyKeys) {
        HashMap<Object, Object> hashMap = Maps.newHashMap();
        if (onlyKeys.get(0).equals("All")) {
            onlyKeys.forEach(o -> hashMap.putAll(priceCheckTask.marketCheckResultMap()));
        } else {
            onlyKeys.forEach(o -> hashMap.put(o, priceCheckTask.marketCheckResultMap().get(o)));
        }
        return WebResult.okResult(hashMap);
    }

    /**
     * 获取当前最新的市场增强信息（目前仅：涨跌幅）
     */
    @ResponseBody
    @RequestMapping(value = "getPriceChangeMap", method = RequestMethod.POST, produces = "application/json")
    public WebResult getPriceChangeMap(@RequestBody List<String> onlyKeys) {
        HashMap<Object, Object> changeMap = Maps.newHashMap();
        HashMap<Object, Object> highLowMap = Maps.newHashMap();
        if (onlyKeys.get(0).equals("All")) {
            onlyKeys.forEach(o -> changeMap.putAll(priceChangeManagement.getPriceChangeMap()));
            onlyKeys.forEach(o -> highLowMap.putAll(highLowChangeManagement.getHighLowMap()));
        } else {
            onlyKeys.forEach(o -> changeMap.put(o, priceChangeManagement.get(o)));
            onlyKeys.forEach(o -> highLowMap.put(o,highLowChangeManagement.getHighLowMap().get(o)));
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("change",changeMap);
        jsonObject.put("highLow",highLowMap);
        return WebResult.okResult(jsonObject);
    }

    /**
     * 获取一定时间段内的交易信息
     */
    @ResponseBody
    @RequestMapping(value = "getTradesForTimeByOnlyKey", method = RequestMethod.POST, produces = "application/json")
    public WebResult getTradesForTimeByOnlyKey(@RequestBody JSONObject jsonObject) {
        if (jsonObject == null) {
            return WebResult.failResult(1000);
        }
        return WebResult.okResult(dataProvideService.getTradesForTimeByOnlyKey(jsonObject));
    }

    /**
     * 获取一定时间段内的交易信息
     */
    @ResponseBody
    @RequestMapping(value = "getTradesForTimeByExchange", method = RequestMethod.POST, produces = "application/json")
    public WebResult getTradesForTimeByExchange(@RequestBody JSONObject jsonObject) {
        if (jsonObject == null) {
            return WebResult.failResult(1000);
        }
        return WebResult.okResult(dataProvideService.getTradesForTimeByExchange(jsonObject));
    }


    /**
     * 获取一定时间段内的行情信息
     */
    @ResponseBody
    @RequestMapping(value = "getMarketsForTimeByOnlyKey", method = RequestMethod.POST, produces = "application/json")
    public WebResult getMarketsForTimeByOnlyKey(@RequestBody JSONObject jsonObject) {
        if (jsonObject == null) {
            return WebResult.failResult(1000);
        }
        return WebResult.okResult(dataProvideService.getMarketsForTimeByOnlyKey(jsonObject));
    }

    /**
     * 获取一定时间段内的行情信息
     */
    @ResponseBody
    @RequestMapping(value = "getMarketsForTimeByExchange", method = RequestMethod.POST, produces = "application/json")
    public WebResult getMarketsForTimeByExchange(@RequestBody JSONObject jsonObject) {
        if (jsonObject == null) {
            return WebResult.failResult(1000);
        }
        return WebResult.okResult(dataProvideService.getMarketsForTimeByExchange(jsonObject));
    }

    /**
     * 获取当前最新的市场增强信息（目前仅：涨跌幅）
     */
    @ResponseBody
    @RequestMapping(value = "getLastMarketByTerrace", method = RequestMethod.POST, produces = "application/json")
    public WebResult getLastMarketByTerrace(@RequestBody String obj) {
        if (StringUtils.isEmpty(obj)) {
            return WebResult.failResult(1000);
        }
        HashMap<String, Market> hashMap = Maps.newHashMap();
        switch (JSONObject.parseObject(obj).getString("type")) {
            case "Exchange":
                hashMap.putAll(marketInfoEnhanceTask.getExchangeMarketMap());
                break;
            case "Quintar":
                hashMap.putAll(marketInfoEnhanceTask.getQuintarMarketMap());
                break;
            case "Aicoin":
                hashMap.putAll(marketInfoEnhanceTask.getAicoinMarketMap());
                break;
            case "Mytoken":
                hashMap.putAll(marketInfoEnhanceTask.getMyTokenMarketMap());
                break;
            default:
                break;
        }
        return WebResult.okResult(hashMap);
    }


    /**
     * 获取最新市场最高和最低价格接口
     */
    @ResponseBody
    @RequestMapping(value = "getMaxAndMinPriceByOnlyKey", method = RequestMethod.POST, produces = "application/json")
    public WebResult getMaxAndMinPriceByOnlyKey(@RequestBody JSONObject jsonObject) {
        if (jsonObject == null) {
            return WebResult.failResult(1000);
        }
        try {
            List<MarketMaxMinVO> marketMaxMinVO = dataProvideService.getMaxAndMinPriceByOnlyKey(jsonObject);
            if (marketMaxMinVO.size() == 0) {
                return WebResult.failResult(1000);
            }
            return WebResult.okResult(marketMaxMinVO.get(0));
        } catch (Throwable e) {
            log.error("Trading 等获取最新市场最高和最低价格接口报错！", e);
        }
        return WebResult.failResult(1000);
    }

    @Value("#{${eliminateExchange}}")
    private Map<String,String> eliminateExchange;

    /**
     * 根据币种简称查询全部交易所下的该比重的交易比例
     */
    @ResponseBody
    @RequestMapping(value = "getExchangeSymbolVolRateBySymbol", method = RequestMethod.POST, produces = "application/json")
    public WebResult getExchangeSymbolVolRateBySymbol(@RequestBody JSONObject jsonObject) {
        if (jsonObject == null) {
            return WebResult.failResult(1000);
        }
        try {
            String symbol = jsonObject.getString("symbol");
            String onlyKey = jsonObject.getString("onlyKey");
            //2018年9月6日10:11:46 新增一个逻辑就是如果前台传入的是一个OnlyKey的话，我需要查找到其对应的库简称并进行接下来的操作
            if (StringUtils.hasText(onlyKey)&&onlyKey.split("_").length==3){
                OnlyKeysConf onlyKeysConfMap = onlyKeyManagement.getOnlyKeysConfMap(onlyKey);
                if (onlyKeysConfMap==null){
                    return WebResult.failResult(1000);
                }
                symbol = onlyKeysConfMap.getAllName();
            }
            if (!StringUtils.hasText(symbol)){
                return WebResult.failResult(1000);
            }

            Collection<Exchange> allEXchange = dataProvideService.getAllEXchange();
            //首先先将每个交易所的数据进行一次计算综合返回一个list
            List<SymbolVolRateVO> collect = getSymbolExchangeSum(allEXchange, symbol);
            //根据上一步的计算结果，求出全部总量
            BigDecimal totalSum = collect.stream().filter(jobj -> !eliminateExchange.containsKey(jobj.getExchange()))
                    .collect(Collectors.reducing(BigDecimal.ZERO, SymbolVolRateVO::getSum, BigDecimal::add));
            List<SymbolVolRateVO> finalResult = collect.stream().filter(symbolVolRateVO -> totalSum.compareTo(BigDecimal.ZERO) != 0)
                    .filter(jobj -> !eliminateExchange.containsKey(jobj.getExchange()))
                    .map(symbolVolRateVO -> {
                symbolVolRateVO.setTotalSum(totalSum);
                symbolVolRateVO.setRate(symbolVolRateVO.getSum().divide(totalSum, 4, BigDecimal.ROUND_DOWN).multiply(HUNDRED));
                return symbolVolRateVO;
            }).filter(symbolVolRateVO -> BigDecimal.ZERO.compareTo(symbolVolRateVO.rate)!=0).collect(Collectors.toList());
            return WebResult.okResult(finalResult);
        } catch (Throwable e) {
            log.error("根据币种简称查询全部交易所下的该比重的交易比例出错！", e);
        }
        return WebResult.failResult(1000);
    }

    /**
     * 根据币种简称查询对应币种市值信息
     */
    @GetMapping("getMarketCapsByName")
    @ResponseBody
    public WebResult getMarketCapsByName(String names) {
        try {
            List<MarketCap> marketCapLsit = new ArrayList<>();
            String[] nameStr = names.split(",");
            for (String name : nameStr) {
                String capName = onlyKeyManagement.getCoinMarketCapNameMapByName(name);
                MarketCap marketCap;
                if (StringUtils.hasText(capName)){
                    marketCap = marketCapManagement.getMarketCaps(capName);
                }else {
                    marketCap = marketCapManagement.getMarketCapsMapByName(name);
                }
                marketCapLsit.add(marketCap);
            }
            if (marketCapLsit.size() == 0) {
                return WebResult.okResult(marketCapLsit);
            }
            return WebResult.okResult(marketCapLsit);
        } catch (Exception e) {
            log.error("根据币种简称查询对应币种市值信息出错！", e);
        }
        return WebResult.failResult(1000);
    }

    /**
     * 获取内存中数据
     *
     * @return
     */
    @GetMapping("getMarketValueMap")
    @ResponseBody
    public WebResult getMarketCapsMap() {
        Map<String, PriceChange> allDataForMarketValue = priceChangeManagement.getAllDataForMarketValue();
        Map<String, HighLowChange> marketValueHighLowMap = highLowChangeManagement.getMarketValueHighLowMap();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("highLow",marketValueHighLowMap);
        jsonObject.put("allData",allDataForMarketValue);
        return WebResult.okResult(jsonObject);
    }

    /**
     * 根据币种全称获取对应的市值数据
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "getMarkCapFor7d", method = RequestMethod.POST, produces = "application/json")
    public WebResult getMarkCapFor7d(@RequestBody JSONObject obj) {
        String coinName = obj.get("coinName").toString();
        if (!org.springframework.util.StringUtils.hasText(coinName) || obj == null) {
            return WebResult.failResult(1000);
        }
        try {
            List<RiseVo> riseVoList = dataProvideService.getResiFor7dByCoinName(coinName);
            return WebResult.okResult(riseVoList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据币种全称获取对应的市值数据
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "get30DayVolumes", method = RequestMethod.POST, produces = "application/json")
    public WebResult get30DayVolumes(@RequestBody JSONObject obj) {
        if (obj == null) {
            return WebResult.failResult(1000);
        }
        String exchangeName = obj.getString("exchangeName");
        String coinName = obj.getString("coinName");
        try {
            //为返回格式统一，这里做一个拼接Map
            Map<String,Map<String,List<VolumeVo>>> mapMap = new HashMap<>(200);
            if (StringUtils.hasText(exchangeName)&&StringUtils.hasText(coinName)){
                Map<String, List<VolumeVo>> volumesMap = new HashMap<>(1);
                List<VolumeVo> volumeVos = MarketInfoEnhanceTask.getVolumesFor30Day(exchangeName).get(coinName);
                volumesMap.put(coinName,volumeVos);
                mapMap.put(exchangeName,volumesMap);
                return WebResult.okResult(mapMap);
            }
            if (StringUtils.hasText(exchangeName)){
                Map<String, List<VolumeVo>> volumesMap = MarketInfoEnhanceTask.getVolumesFor30Day(exchangeName);
                mapMap.put(exchangeName,volumesMap);
                return WebResult.okResult(mapMap);
            }
            return WebResult.okResult(Collections.EMPTY_MAP);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据币种和交易所将其交易量求和并返回
     *
     * @param allEXchange
     * @param symbol
     * @return
     */
    private List<SymbolVolRateVO> getSymbolExchangeSum(Collection<Exchange> allEXchange, String symbol) {
        return allEXchange.stream().map(exchange -> {
            BigDecimal symbolSum;
            //由于要兼容之前版本，所以这个地方还需要兼容APP传简称过来这个地方会判断传过来的第2个字符，如果是大写的话，认为是简称
            if (Character.isLowerCase(symbol.charAt(1))){
                symbolSum = getAllNameExchangeSpotVolSum(symbol, exchange);
            }else {
                symbolSum = getSymbolExchangeSpotVolSum(symbol, exchange);
            }
            SymbolVolRateVO symbolVolRateVO = new SymbolVolRateVO();
            symbolVolRateVO.setExchange(exchange.getEName());
            symbolVolRateVO.setSum(symbolSum);
            symbolVolRateVO.setSymbol(symbol);
            return symbolVolRateVO;
        }).collect(Collectors.toList());
    }

    private BigDecimal getAllNameExchangeSpotVolSum(String allName, Exchange exchange) {
        List<MarketVO> marketsResult = dataProvideService.getAllMarketByExchange(new PagingParam(),exchange.getEName());
        return marketsResult.stream()
                .filter(marketVO -> allName.equals(marketVO.getSymName()) && marketVO.getVolume() != null)
                .filter(marketVO -> removeFuturesSymbol(exchange,marketVO))
                .collect(Collectors.reducing(BigDecimal.ZERO, MarketVO::getVolume, BigDecimal::add));
    }

    private boolean removeFuturesSymbol(Exchange exchange, MarketVO marketVO) {
        if (!"Okex".equals(exchange.getEName())){
            return true;
        }
        if (marketVO.getSymbol().contains("THISWEEK")){
            return false;
        }
        if (marketVO.getSymbol().contains("NEXTWEEK")){
            return false;
        }
        if (marketVO.getSymbol().contains("QUARTER")){
            return false;
        }
        return true;
    }

    private BigDecimal getSymbolExchangeSpotVolSum(String symbol, Exchange exchange) {
        List<MarketVO> marketsResult = dataProvideService.getAllMarketByExchangeAndSymbol(exchange.getEName(),symbol);
        return marketsResult.stream()
                .filter(marketVO -> symbol.equals(marketVO.getSymbol()) && marketVO.getVolume() != null)
                .filter(marketVO -> removeFuturesSymbol(exchange,marketVO))
                .collect(Collectors.reducing(BigDecimal.ZERO, MarketVO::getVolume, BigDecimal::add));
    }


    @Data
    class SymbolVolRateVO {
        /**
         * 交易所名称
         */
        private String exchange;
        /**
         * 币种简称
         */
        private String symbol;
        /**
         * 该币种在该交易所下的总量
         */
        private BigDecimal sum;
        /**
         * 该币种在全部交易所下的总量
         */
        private BigDecimal totalSum;
        /**
         * 该交易所下的总量占总量的比例  sum/totalSum
         */
        private BigDecimal rate;
    }

}
