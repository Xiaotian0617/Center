package com.al.exchange.dao.domain;


import lombok.Data;
import lombok.EqualsAndHashCode;
import org.influxdb.annotation.Measurement;

@Data
@EqualsAndHashCode(of = "onlyKey")
@Measurement(name = "pairs")
public class PairsDTO implements OnlyKey {

    private String measurement = "pairs";

    //中文名
    private String coinMarketName;

    //币种简称
    private String coinMarketCode;

    //币种英文名称
    private String coinMarketEn;

    //交易所名称
    private String marketName;

    //交易所代号
    private String marketCode;

    //交易所本项目枚举中的名称
    private String exchange; //交易所

    private String symbol; //币种

    private String unit; //单位

    /**
     * 这个币种在这个交易所所对应的货币单位
     * 比如 coinMarketCode = ETH
     * marketCode = huobipro
     * currency = BTC
     * 则onlykey为 Huobi_ETH_BTC
     */
    private String currency;

    // TODO 应该是各个交易所这个币种的可用状态（待验证）
    private Integer status;

    private Long timestamp;

    private String onlyKey;//市场的唯一标识 例如：Okex_ETH_BTC

    @Override
    public String onlyKey() {
        return onlyKey;
    }

    @Override
    public String symbol() {
        return this.symbol;
    }

    @Override
    public String exchange() {
        return this.exchange;
    }

    @Override
    public String unit() {
        return this.unit;
    }

    public PairsDTO() {
    }

    public PairsDTO(String exchange, String symbol, String unit) {
        this.exchange = exchange;
        setSymbol(symbol);
        setUnit(unit);
        setOnlyKey(exchange, this.symbol, this.unit);
    }

    private void setOnlyKey(String exchange, String symbol, String unit) {
        if (onlyKey == null) {
            onlyKey = String.format("%s_%s_%s", exchange, symbol, unit);
        }
    }
}
