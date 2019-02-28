package com.al.exchange.dao.domain;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.util.concurrent.locks.ReentrantLock;

/**
 * 最近两次行情,用未赋值的 market 初始化队列
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 31/01/2018 11:25
 */
public class MarketCache {
    private final OnlyKey onlyKey;
    private CircularFifoQueue<Market> markets = new CircularFifoQueue<>(2);
    ReentrantLock lock = new ReentrantLock();

    public MarketCache(OnlyKey onlyKey) {
        this.onlyKey = onlyKey;
        init();
    }

    private void init() {
        for (int i = 0; i < 2; i++) {
            markets.offer(new Market());
        }
    }

    /**
     * 上一次行情
     *
     * @return
     */
    public Market last() {
        lock.lock();
        try {
            Market market = markets.peek();
            return market;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 更新最新行情
     *
     * @param market
     * @return
     */
    public void updateNow(Market market) {
        lock.lock();
        try {
            markets.offer(market);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 最新行情
     *
     * @return
     */
    public Market now() {
        lock.lock();
        try {
            return markets.get(1);
        } finally {
            lock.unlock();
        }
    }

    public OnlyKey getOnlyKey() {
        return onlyKey;
    }

    public boolean isReady() {
        return !(now().getLast() == null || last().getLast() == null);
        //return now()!=null&&now().getLast() != null;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MarketCache{");
        sb.append("markets=").append(markets);
        sb.append('}');
        return sb.toString();
    }
}
