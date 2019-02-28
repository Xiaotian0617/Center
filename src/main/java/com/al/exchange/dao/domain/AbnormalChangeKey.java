package com.al.exchange.dao.domain;

public class AbnormalChangeKey {

    private Long time;

    private String coin;

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getCoin() {
        return coin;
    }

    public void setCoin(String coin) {
        this.coin = coin == null ? null : coin.trim();
    }
}