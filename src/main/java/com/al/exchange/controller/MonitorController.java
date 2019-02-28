package com.al.exchange.controller;

import com.al.exchange.dao.domain.InformationOwnDto;
import com.al.exchange.dao.domain.Market;
import com.al.exchange.dao.domain.MarketCache;
import com.al.exchange.dao.domain.OnlyKeysConf;
import com.al.exchange.service.ExchangeRealtimeMarketService;
import com.al.exchange.service.management.OnlyKeyManagement;
import com.al.exchange.service.management.SourceSettingManagement;
import com.al.exchange.task.MarketInfoEnhanceTask;
import com.al.exchange.util.InfluxResultExt;
import com.al.exchange.web.WebResult;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 12/03/2018 18:04
 */
@Slf4j
@RestController
@RequestMapping(value = "/monitor", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class MonitorController {
    @Autowired
    SourceSettingManagement sourceSettingManagement;
    @Autowired
    OnlyKeyManagement onlyKeyManagement;

    @Autowired
    MarketInfoEnhanceTask marketInfoEnhanceTask;

    @Autowired
    private ExchangeRealtimeMarketService exchangeRealtimeMarketService;

    @RequestMapping(value = "/market/raw/now/{onlykey}", method = RequestMethod.POST, produces = "application/json")
    public WebResult getMarketRawNow(@PathVariable("onlykey") String onlykey) {
        try {
            Market now = exchangeRealtimeMarketService.getNow(onlykey);
            return WebResult.okResult(now);
        } catch (Exception e) {
            return WebResult.failResult(9999, e);
        }
    }

    @RequestMapping(value = "/market/raw/nows", method = RequestMethod.POST, produces = "application/json")
    public WebResult getMarketRawNow() {
        try {
            List<MarketCache> nows = exchangeRealtimeMarketService.getNowMarkets();
            return WebResult.okResult(nows);
        } catch (Exception e) {
            return WebResult.failResult(9999, e);
        }
    }

    @PutMapping(value = "/onlykeys/{hours}", produces = "application/json")
    public WebResult refreshOnlykeys(@PathVariable("hours") Integer hours) {
        try {
            onlyKeyManagement.refreshOnlykeys(hours);
            return WebResult.okResult();
        } catch (Exception e) {
            return WebResult.failResult(9999, e);
        }
    }

    @RequestMapping(value = "/settings", method = RequestMethod.POST, produces = "application/json")
    public WebResult getSourceSettingManagement() {
        try {
            Map<String, SourceSettingManagement.OnlykeySetted> onlykeySettedMap = sourceSettingManagement.getOnlykeySettedMap();
            return WebResult.okResult(onlykeySettedMap);
        } catch (Exception e) {
            return WebResult.failResult(9999, e);
        }
    }

    @RequestMapping(value = "/settings/{onlykey}", method = RequestMethod.POST, produces = "application/json")
    public WebResult getSourceSettingManagement(@PathVariable String onlykey) {
        try {
            SourceSettingManagement.OnlykeySetted onlykeySetted = sourceSettingManagement.getOnlykeySettedMap().get(onlykey);
            return WebResult.okResult(onlykeySetted);
        } catch (Exception e) {
            return WebResult.failResult(9999, e);
        }
    }

    @RequestMapping(value = "/marketCapSettings", method = RequestMethod.POST, produces = "application/json")
    public WebResult getMarketCapSourceSettingManagement() {
        try {
            Map<String, SourceSettingManagement.MarketCapSetted> marketCapSettedMap = sourceSettingManagement.getMarketCapSettedMap();
            HashMap<String, String> capName = onlyKeyManagement.getCoinMarketCapNameMap();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("marketCapSettedMap",marketCapSettedMap);
            jsonObject.put("capName",capName);
            return WebResult.okResult(jsonObject);
        } catch (Exception e) {
            return WebResult.failResult(9999, e);
        }
    }

    @RequestMapping(value = "/marketCapSettings/{onlykey}", method = RequestMethod.POST, produces = "application/json")
    public WebResult getMarketCapSourceSettingManagement(@PathVariable String onlykey) {
        try {
            SourceSettingManagement.MarketCapSetted marketCapSettedMap = sourceSettingManagement.getMarketCapSettedMap().get(onlykey);
            return WebResult.okResult(marketCapSettedMap);
        } catch (Exception e) {
            return WebResult.failResult(9999, e);
        }
    }


    @RequestMapping(value = "/getAdminSetted/", method = RequestMethod.POST, produces = "application/json")
    public WebResult getAdminSetted() {
        try {
            Map<String, InformationOwnDto> informationOwnDtoHashMap = onlyKeyManagement.getInformationOwns();
            Map<String, OnlyKeysConf> onlyKeysConfMap = onlyKeyManagement.getOnlyKeysConfMap();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("informationOwnDtoHashMap",informationOwnDtoHashMap);
            jsonObject.put("onlyKeysConfMap",onlyKeysConfMap);
            return WebResult.okResult(jsonObject);
        } catch (Exception e) {
            return WebResult.failResult(9999, e);
        }
    }

    @RequestMapping(value = "/getAdminSetted/{onlykey}", method = RequestMethod.POST, produces = "application/json")
    public WebResult getAdminSetted(@PathVariable String onlykey) {
        try {
            InformationOwnDto informationOwnDtoHashMap = onlyKeyManagement.getInformationOwns().get(onlykey);
            OnlyKeysConf onlyKeysConfMap = onlyKeyManagement.getOnlyKeysConfMap().get(onlykey);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("informationOwnDtoHashMap",informationOwnDtoHashMap);
            jsonObject.put("onlyKeysConfMap",onlyKeysConfMap);
            return WebResult.okResult(jsonObject);
        } catch (Exception e) {
            return WebResult.failResult(9999, e);
        }
    }

    /**
     * 根据币种全称获取对应的市值数据
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "calcPriceChange", method = RequestMethod.POST, produces = "application/json")
    public WebResult calcPriceChange() {
        try {
            marketInfoEnhanceTask.calcPriceChange();
            return WebResult.okResult();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return WebResult.failException();
    }

    /**
     * 根据币种全称获取对应的市值数据
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "getInfluxCache", method = RequestMethod.POST, produces = "application/json")
    public WebResult getInfluxCache() {
        try {
            return WebResult.okResult(InfluxResultExt.getClassFieldCache());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return WebResult.failException();
    }

}
