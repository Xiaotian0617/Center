package com.al.exchange.dao.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OnlyKeysConfExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public OnlyKeysConfExample() {
        oredCriteria = new ArrayList<Criteria>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<Criterion>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andOnlyKeyIsNull() {
            addCriterion("only_key is null");
            return (Criteria) this;
        }

        public Criteria andOnlyKeyIsNotNull() {
            addCriterion("only_key is not null");
            return (Criteria) this;
        }

        public Criteria andOnlyKeyEqualTo(String value) {
            addCriterion("only_key =", value, "onlyKey");
            return (Criteria) this;
        }

        public Criteria andOnlyKeyNotEqualTo(String value) {
            addCriterion("only_key <>", value, "onlyKey");
            return (Criteria) this;
        }

        public Criteria andOnlyKeyGreaterThan(String value) {
            addCriterion("only_key >", value, "onlyKey");
            return (Criteria) this;
        }

        public Criteria andOnlyKeyGreaterThanOrEqualTo(String value) {
            addCriterion("only_key >=", value, "onlyKey");
            return (Criteria) this;
        }

        public Criteria andOnlyKeyLessThan(String value) {
            addCriterion("only_key <", value, "onlyKey");
            return (Criteria) this;
        }

        public Criteria andOnlyKeyLessThanOrEqualTo(String value) {
            addCriterion("only_key <=", value, "onlyKey");
            return (Criteria) this;
        }

        public Criteria andOnlyKeyLike(String value) {
            addCriterion("only_key like", value, "onlyKey");
            return (Criteria) this;
        }

        public Criteria andOnlyKeyNotLike(String value) {
            addCriterion("only_key not like", value, "onlyKey");
            return (Criteria) this;
        }

        public Criteria andOnlyKeyIn(List<String> values) {
            addCriterion("only_key in", values, "onlyKey");
            return (Criteria) this;
        }

        public Criteria andOnlyKeyNotIn(List<String> values) {
            addCriterion("only_key not in", values, "onlyKey");
            return (Criteria) this;
        }

        public Criteria andOnlyKeyBetween(String value1, String value2) {
            addCriterion("only_key between", value1, value2, "onlyKey");
            return (Criteria) this;
        }

        public Criteria andOnlyKeyNotBetween(String value1, String value2) {
            addCriterion("only_key not between", value1, value2, "onlyKey");
            return (Criteria) this;
        }

        public Criteria andExchangeIsNull() {
            addCriterion("exchange is null");
            return (Criteria) this;
        }

        public Criteria andExchangeIsNotNull() {
            addCriterion("exchange is not null");
            return (Criteria) this;
        }

        public Criteria andExchangeEqualTo(String value) {
            addCriterion("exchange =", value, "exchange");
            return (Criteria) this;
        }

        public Criteria andExchangeNotEqualTo(String value) {
            addCriterion("exchange <>", value, "exchange");
            return (Criteria) this;
        }

        public Criteria andExchangeGreaterThan(String value) {
            addCriterion("exchange >", value, "exchange");
            return (Criteria) this;
        }

        public Criteria andExchangeGreaterThanOrEqualTo(String value) {
            addCriterion("exchange >=", value, "exchange");
            return (Criteria) this;
        }

        public Criteria andExchangeLessThan(String value) {
            addCriterion("exchange <", value, "exchange");
            return (Criteria) this;
        }

        public Criteria andExchangeLessThanOrEqualTo(String value) {
            addCriterion("exchange <=", value, "exchange");
            return (Criteria) this;
        }

        public Criteria andExchangeLike(String value) {
            addCriterion("exchange like", value, "exchange");
            return (Criteria) this;
        }

        public Criteria andExchangeNotLike(String value) {
            addCriterion("exchange not like", value, "exchange");
            return (Criteria) this;
        }

        public Criteria andExchangeIn(List<String> values) {
            addCriterion("exchange in", values, "exchange");
            return (Criteria) this;
        }

        public Criteria andExchangeNotIn(List<String> values) {
            addCriterion("exchange not in", values, "exchange");
            return (Criteria) this;
        }

        public Criteria andExchangeBetween(String value1, String value2) {
            addCriterion("exchange between", value1, value2, "exchange");
            return (Criteria) this;
        }

        public Criteria andExchangeNotBetween(String value1, String value2) {
            addCriterion("exchange not between", value1, value2, "exchange");
            return (Criteria) this;
        }

        public Criteria andSymbolIsNull() {
            addCriterion("symbol is null");
            return (Criteria) this;
        }

        public Criteria andSymbolIsNotNull() {
            addCriterion("symbol is not null");
            return (Criteria) this;
        }

        public Criteria andSymbolEqualTo(String value) {
            addCriterion("symbol =", value, "symbol");
            return (Criteria) this;
        }

        public Criteria andSymbolNotEqualTo(String value) {
            addCriterion("symbol <>", value, "symbol");
            return (Criteria) this;
        }

        public Criteria andSymbolGreaterThan(String value) {
            addCriterion("symbol >", value, "symbol");
            return (Criteria) this;
        }

        public Criteria andSymbolGreaterThanOrEqualTo(String value) {
            addCriterion("symbol >=", value, "symbol");
            return (Criteria) this;
        }

        public Criteria andSymbolLessThan(String value) {
            addCriterion("symbol <", value, "symbol");
            return (Criteria) this;
        }

        public Criteria andSymbolLessThanOrEqualTo(String value) {
            addCriterion("symbol <=", value, "symbol");
            return (Criteria) this;
        }

        public Criteria andSymbolLike(String value) {
            addCriterion("symbol like", value, "symbol");
            return (Criteria) this;
        }

        public Criteria andSymbolNotLike(String value) {
            addCriterion("symbol not like", value, "symbol");
            return (Criteria) this;
        }

        public Criteria andSymbolIn(List<String> values) {
            addCriterion("symbol in", values, "symbol");
            return (Criteria) this;
        }

        public Criteria andSymbolNotIn(List<String> values) {
            addCriterion("symbol not in", values, "symbol");
            return (Criteria) this;
        }

        public Criteria andSymbolBetween(String value1, String value2) {
            addCriterion("symbol between", value1, value2, "symbol");
            return (Criteria) this;
        }

        public Criteria andSymbolNotBetween(String value1, String value2) {
            addCriterion("symbol not between", value1, value2, "symbol");
            return (Criteria) this;
        }

        public Criteria andUnitIsNull() {
            addCriterion("unit is null");
            return (Criteria) this;
        }

        public Criteria andUnitIsNotNull() {
            addCriterion("unit is not null");
            return (Criteria) this;
        }

        public Criteria andUnitEqualTo(String value) {
            addCriterion("unit =", value, "unit");
            return (Criteria) this;
        }

        public Criteria andUnitNotEqualTo(String value) {
            addCriterion("unit <>", value, "unit");
            return (Criteria) this;
        }

        public Criteria andUnitGreaterThan(String value) {
            addCriterion("unit >", value, "unit");
            return (Criteria) this;
        }

        public Criteria andUnitGreaterThanOrEqualTo(String value) {
            addCriterion("unit >=", value, "unit");
            return (Criteria) this;
        }

        public Criteria andUnitLessThan(String value) {
            addCriterion("unit <", value, "unit");
            return (Criteria) this;
        }

        public Criteria andUnitLessThanOrEqualTo(String value) {
            addCriterion("unit <=", value, "unit");
            return (Criteria) this;
        }

        public Criteria andUnitLike(String value) {
            addCriterion("unit like", value, "unit");
            return (Criteria) this;
        }

        public Criteria andUnitNotLike(String value) {
            addCriterion("unit not like", value, "unit");
            return (Criteria) this;
        }

        public Criteria andUnitIn(List<String> values) {
            addCriterion("unit in", values, "unit");
            return (Criteria) this;
        }

        public Criteria andUnitNotIn(List<String> values) {
            addCriterion("unit not in", values, "unit");
            return (Criteria) this;
        }

        public Criteria andUnitBetween(String value1, String value2) {
            addCriterion("unit between", value1, value2, "unit");
            return (Criteria) this;
        }

        public Criteria andUnitNotBetween(String value1, String value2) {
            addCriterion("unit not between", value1, value2, "unit");
            return (Criteria) this;
        }

        public Criteria andStatusIsNull() {
            addCriterion("status is null");
            return (Criteria) this;
        }

        public Criteria andStatusIsNotNull() {
            addCriterion("status is not null");
            return (Criteria) this;
        }

        public Criteria andStatusEqualTo(String value) {
            addCriterion("status =", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualTo(String value) {
            addCriterion("status <>", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThan(String value) {
            addCriterion("status >", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(String value) {
            addCriterion("status >=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThan(String value) {
            addCriterion("status <", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualTo(String value) {
            addCriterion("status <=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLike(String value) {
            addCriterion("status like", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotLike(String value) {
            addCriterion("status not like", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusIn(List<String> values) {
            addCriterion("status in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotIn(List<String> values) {
            addCriterion("status not in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusBetween(String value1, String value2) {
            addCriterion("status between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotBetween(String value1, String value2) {
            addCriterion("status not between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andAllNameIsNull() {
            addCriterion("all_name is null");
            return (Criteria) this;
        }

        public Criteria andAllNameIsNotNull() {
            addCriterion("all_name is not null");
            return (Criteria) this;
        }

        public Criteria andAllNameEqualTo(String value) {
            addCriterion("all_name =", value, "allName");
            return (Criteria) this;
        }

        public Criteria andAllNameNotEqualTo(String value) {
            addCriterion("all_name <>", value, "allName");
            return (Criteria) this;
        }

        public Criteria andAllNameGreaterThan(String value) {
            addCriterion("all_name >", value, "allName");
            return (Criteria) this;
        }

        public Criteria andAllNameGreaterThanOrEqualTo(String value) {
            addCriterion("all_name >=", value, "allName");
            return (Criteria) this;
        }

        public Criteria andAllNameLessThan(String value) {
            addCriterion("all_name <", value, "allName");
            return (Criteria) this;
        }

        public Criteria andAllNameLessThanOrEqualTo(String value) {
            addCriterion("all_name <=", value, "allName");
            return (Criteria) this;
        }

        public Criteria andAllNameLike(String value) {
            addCriterion("all_name like", value, "allName");
            return (Criteria) this;
        }

        public Criteria andAllNameNotLike(String value) {
            addCriterion("all_name not like", value, "allName");
            return (Criteria) this;
        }

        public Criteria andAllNameIn(List<String> values) {
            addCriterion("all_name in", values, "allName");
            return (Criteria) this;
        }

        public Criteria andAllNameNotIn(List<String> values) {
            addCriterion("all_name not in", values, "allName");
            return (Criteria) this;
        }

        public Criteria andAllNameBetween(String value1, String value2) {
            addCriterion("all_name between", value1, value2, "allName");
            return (Criteria) this;
        }

        public Criteria andAllNameNotBetween(String value1, String value2) {
            addCriterion("all_name not between", value1, value2, "allName");
            return (Criteria) this;
        }

        public Criteria andIsNewIsNull() {
            addCriterion("is_new is null");
            return (Criteria) this;
        }

        public Criteria andIsNewIsNotNull() {
            addCriterion("is_new is not null");
            return (Criteria) this;
        }

        public Criteria andIsNewEqualTo(Boolean value) {
            addCriterion("is_new =", value, "isNew");
            return (Criteria) this;
        }

        public Criteria andIsNewNotEqualTo(Boolean value) {
            addCriterion("is_new <>", value, "isNew");
            return (Criteria) this;
        }

        public Criteria andIsNewGreaterThan(Boolean value) {
            addCriterion("is_new >", value, "isNew");
            return (Criteria) this;
        }

        public Criteria andIsNewGreaterThanOrEqualTo(Boolean value) {
            addCriterion("is_new >=", value, "isNew");
            return (Criteria) this;
        }

        public Criteria andIsNewLessThan(Boolean value) {
            addCriterion("is_new <", value, "isNew");
            return (Criteria) this;
        }

        public Criteria andIsNewLessThanOrEqualTo(Boolean value) {
            addCriterion("is_new <=", value, "isNew");
            return (Criteria) this;
        }

        public Criteria andIsNewIn(List<Boolean> values) {
            addCriterion("is_new in", values, "isNew");
            return (Criteria) this;
        }

        public Criteria andIsNewNotIn(List<Boolean> values) {
            addCriterion("is_new not in", values, "isNew");
            return (Criteria) this;
        }

        public Criteria andIsNewBetween(Boolean value1, Boolean value2) {
            addCriterion("is_new between", value1, value2, "isNew");
            return (Criteria) this;
        }

        public Criteria andIsNewNotBetween(Boolean value1, Boolean value2) {
            addCriterion("is_new not between", value1, value2, "isNew");
            return (Criteria) this;
        }

        public Criteria andKlineFromIsNull() {
            addCriterion("kline_from is null");
            return (Criteria) this;
        }

        public Criteria andKlineFromIsNotNull() {
            addCriterion("kline_from is not null");
            return (Criteria) this;
        }

        public Criteria andKlineFromEqualTo(String value) {
            addCriterion("kline_from =", value, "klineFrom");
            return (Criteria) this;
        }

        public Criteria andKlineFromNotEqualTo(String value) {
            addCriterion("kline_from <>", value, "klineFrom");
            return (Criteria) this;
        }

        public Criteria andKlineFromGreaterThan(String value) {
            addCriterion("kline_from >", value, "klineFrom");
            return (Criteria) this;
        }

        public Criteria andKlineFromGreaterThanOrEqualTo(String value) {
            addCriterion("kline_from >=", value, "klineFrom");
            return (Criteria) this;
        }

        public Criteria andKlineFromLessThan(String value) {
            addCriterion("kline_from <", value, "klineFrom");
            return (Criteria) this;
        }

        public Criteria andKlineFromLessThanOrEqualTo(String value) {
            addCriterion("kline_from <=", value, "klineFrom");
            return (Criteria) this;
        }

        public Criteria andKlineFromLike(String value) {
            addCriterion("kline_from like", value, "klineFrom");
            return (Criteria) this;
        }

        public Criteria andKlineFromNotLike(String value) {
            addCriterion("kline_from not like", value, "klineFrom");
            return (Criteria) this;
        }

        public Criteria andKlineFromIn(List<String> values) {
            addCriterion("kline_from in", values, "klineFrom");
            return (Criteria) this;
        }

        public Criteria andKlineFromNotIn(List<String> values) {
            addCriterion("kline_from not in", values, "klineFrom");
            return (Criteria) this;
        }

        public Criteria andKlineFromBetween(String value1, String value2) {
            addCriterion("kline_from between", value1, value2, "klineFrom");
            return (Criteria) this;
        }

        public Criteria andKlineFromNotBetween(String value1, String value2) {
            addCriterion("kline_from not between", value1, value2, "klineFrom");
            return (Criteria) this;
        }

        public Criteria andMKlineConfIsNull() {
            addCriterion("m_kline_conf is null");
            return (Criteria) this;
        }

        public Criteria andMKlineConfIsNotNull() {
            addCriterion("m_kline_conf is not null");
            return (Criteria) this;
        }

        public Criteria andMKlineConfEqualTo(Integer value) {
            addCriterion("m_kline_conf =", value, "mKlineConf");
            return (Criteria) this;
        }

        public Criteria andMKlineConfNotEqualTo(Integer value) {
            addCriterion("m_kline_conf <>", value, "mKlineConf");
            return (Criteria) this;
        }

        public Criteria andMKlineConfGreaterThan(Integer value) {
            addCriterion("m_kline_conf >", value, "mKlineConf");
            return (Criteria) this;
        }

        public Criteria andMKlineConfGreaterThanOrEqualTo(Integer value) {
            addCriterion("m_kline_conf >=", value, "mKlineConf");
            return (Criteria) this;
        }

        public Criteria andMKlineConfLessThan(Integer value) {
            addCriterion("m_kline_conf <", value, "mKlineConf");
            return (Criteria) this;
        }

        public Criteria andMKlineConfLessThanOrEqualTo(Integer value) {
            addCriterion("m_kline_conf <=", value, "mKlineConf");
            return (Criteria) this;
        }

        public Criteria andMKlineConfIn(List<Integer> values) {
            addCriterion("m_kline_conf in", values, "mKlineConf");
            return (Criteria) this;
        }

        public Criteria andMKlineConfNotIn(List<Integer> values) {
            addCriterion("m_kline_conf not in", values, "mKlineConf");
            return (Criteria) this;
        }

        public Criteria andMKlineConfBetween(Integer value1, Integer value2) {
            addCriterion("m_kline_conf between", value1, value2, "mKlineConf");
            return (Criteria) this;
        }

        public Criteria andMKlineConfNotBetween(Integer value1, Integer value2) {
            addCriterion("m_kline_conf not between", value1, value2, "mKlineConf");
            return (Criteria) this;
        }

        public Criteria andDKlineConfIsNull() {
            addCriterion("d_kline_conf is null");
            return (Criteria) this;
        }

        public Criteria andDKlineConfIsNotNull() {
            addCriterion("d_kline_conf is not null");
            return (Criteria) this;
        }

        public Criteria andDKlineConfEqualTo(Integer value) {
            addCriterion("d_kline_conf =", value, "dKlineConf");
            return (Criteria) this;
        }

        public Criteria andDKlineConfNotEqualTo(Integer value) {
            addCriterion("d_kline_conf <>", value, "dKlineConf");
            return (Criteria) this;
        }

        public Criteria andDKlineConfGreaterThan(Integer value) {
            addCriterion("d_kline_conf >", value, "dKlineConf");
            return (Criteria) this;
        }

        public Criteria andDKlineConfGreaterThanOrEqualTo(Integer value) {
            addCriterion("d_kline_conf >=", value, "dKlineConf");
            return (Criteria) this;
        }

        public Criteria andDKlineConfLessThan(Integer value) {
            addCriterion("d_kline_conf <", value, "dKlineConf");
            return (Criteria) this;
        }

        public Criteria andDKlineConfLessThanOrEqualTo(Integer value) {
            addCriterion("d_kline_conf <=", value, "dKlineConf");
            return (Criteria) this;
        }

        public Criteria andDKlineConfIn(List<Integer> values) {
            addCriterion("d_kline_conf in", values, "dKlineConf");
            return (Criteria) this;
        }

        public Criteria andDKlineConfNotIn(List<Integer> values) {
            addCriterion("d_kline_conf not in", values, "dKlineConf");
            return (Criteria) this;
        }

        public Criteria andDKlineConfBetween(Integer value1, Integer value2) {
            addCriterion("d_kline_conf between", value1, value2, "dKlineConf");
            return (Criteria) this;
        }

        public Criteria andDKlineConfNotBetween(Integer value1, Integer value2) {
            addCriterion("d_kline_conf not between", value1, value2, "dKlineConf");
            return (Criteria) this;
        }

        public Criteria andPriceConfIsNull() {
            addCriterion("price_conf is null");
            return (Criteria) this;
        }

        public Criteria andPriceConfIsNotNull() {
            addCriterion("price_conf is not null");
            return (Criteria) this;
        }

        public Criteria andPriceConfEqualTo(Integer value) {
            addCriterion("price_conf =", value, "priceConf");
            return (Criteria) this;
        }

        public Criteria andPriceConfNotEqualTo(Integer value) {
            addCriterion("price_conf <>", value, "priceConf");
            return (Criteria) this;
        }

        public Criteria andPriceConfGreaterThan(Integer value) {
            addCriterion("price_conf >", value, "priceConf");
            return (Criteria) this;
        }

        public Criteria andPriceConfGreaterThanOrEqualTo(Integer value) {
            addCriterion("price_conf >=", value, "priceConf");
            return (Criteria) this;
        }

        public Criteria andPriceConfLessThan(Integer value) {
            addCriterion("price_conf <", value, "priceConf");
            return (Criteria) this;
        }

        public Criteria andPriceConfLessThanOrEqualTo(Integer value) {
            addCriterion("price_conf <=", value, "priceConf");
            return (Criteria) this;
        }

        public Criteria andPriceConfIn(List<Integer> values) {
            addCriterion("price_conf in", values, "priceConf");
            return (Criteria) this;
        }

        public Criteria andPriceConfNotIn(List<Integer> values) {
            addCriterion("price_conf not in", values, "priceConf");
            return (Criteria) this;
        }

        public Criteria andPriceConfBetween(Integer value1, Integer value2) {
            addCriterion("price_conf between", value1, value2, "priceConf");
            return (Criteria) this;
        }

        public Criteria andPriceConfNotBetween(Integer value1, Integer value2) {
            addCriterion("price_conf not between", value1, value2, "priceConf");
            return (Criteria) this;
        }

        public Criteria andHighLowConfIsNull() {
            addCriterion("high_low_conf is null");
            return (Criteria) this;
        }

        public Criteria andHighLowConfIsNotNull() {
            addCriterion("high_low_conf is not null");
            return (Criteria) this;
        }

        public Criteria andHighLowConfEqualTo(Integer value) {
            addCriterion("high_low_conf =", value, "highLowConf");
            return (Criteria) this;
        }

        public Criteria andHighLowConfNotEqualTo(Integer value) {
            addCriterion("high_low_conf <>", value, "highLowConf");
            return (Criteria) this;
        }

        public Criteria andHighLowConfGreaterThan(Integer value) {
            addCriterion("high_low_conf >", value, "highLowConf");
            return (Criteria) this;
        }

        public Criteria andHighLowConfGreaterThanOrEqualTo(Integer value) {
            addCriterion("high_low_conf >=", value, "highLowConf");
            return (Criteria) this;
        }

        public Criteria andHighLowConfLessThan(Integer value) {
            addCriterion("high_low_conf <", value, "highLowConf");
            return (Criteria) this;
        }

        public Criteria andHighLowConfLessThanOrEqualTo(Integer value) {
            addCriterion("high_low_conf <=", value, "highLowConf");
            return (Criteria) this;
        }

        public Criteria andHighLowConfIn(List<Integer> values) {
            addCriterion("high_low_conf in", values, "highLowConf");
            return (Criteria) this;
        }

        public Criteria andHighLowConfNotIn(List<Integer> values) {
            addCriterion("high_low_conf not in", values, "highLowConf");
            return (Criteria) this;
        }

        public Criteria andHighLowConfBetween(Integer value1, Integer value2) {
            addCriterion("high_low_conf between", value1, value2, "highLowConf");
            return (Criteria) this;
        }

        public Criteria andHighLowConfNotBetween(Integer value1, Integer value2) {
            addCriterion("high_low_conf not between", value1, value2, "highLowConf");
            return (Criteria) this;
        }

        public Criteria andVolConfIsNull() {
            addCriterion("vol_conf is null");
            return (Criteria) this;
        }

        public Criteria andVolConfIsNotNull() {
            addCriterion("vol_conf is not null");
            return (Criteria) this;
        }

        public Criteria andVolConfEqualTo(Integer value) {
            addCriterion("vol_conf =", value, "volConf");
            return (Criteria) this;
        }

        public Criteria andVolConfNotEqualTo(Integer value) {
            addCriterion("vol_conf <>", value, "volConf");
            return (Criteria) this;
        }

        public Criteria andVolConfGreaterThan(Integer value) {
            addCriterion("vol_conf >", value, "volConf");
            return (Criteria) this;
        }

        public Criteria andVolConfGreaterThanOrEqualTo(Integer value) {
            addCriterion("vol_conf >=", value, "volConf");
            return (Criteria) this;
        }

        public Criteria andVolConfLessThan(Integer value) {
            addCriterion("vol_conf <", value, "volConf");
            return (Criteria) this;
        }

        public Criteria andVolConfLessThanOrEqualTo(Integer value) {
            addCriterion("vol_conf <=", value, "volConf");
            return (Criteria) this;
        }

        public Criteria andVolConfIn(List<Integer> values) {
            addCriterion("vol_conf in", values, "volConf");
            return (Criteria) this;
        }

        public Criteria andVolConfNotIn(List<Integer> values) {
            addCriterion("vol_conf not in", values, "volConf");
            return (Criteria) this;
        }

        public Criteria andVolConfBetween(Integer value1, Integer value2) {
            addCriterion("vol_conf between", value1, value2, "volConf");
            return (Criteria) this;
        }

        public Criteria andVolConfNotBetween(Integer value1, Integer value2) {
            addCriterion("vol_conf not between", value1, value2, "volConf");
            return (Criteria) this;
        }

        public Criteria andUtimeIsNull() {
            addCriterion("utime is null");
            return (Criteria) this;
        }

        public Criteria andUtimeIsNotNull() {
            addCriterion("utime is not null");
            return (Criteria) this;
        }

        public Criteria andUtimeEqualTo(Date value) {
            addCriterion("utime =", value, "utime");
            return (Criteria) this;
        }

        public Criteria andUtimeNotEqualTo(Date value) {
            addCriterion("utime <>", value, "utime");
            return (Criteria) this;
        }

        public Criteria andUtimeGreaterThan(Date value) {
            addCriterion("utime >", value, "utime");
            return (Criteria) this;
        }

        public Criteria andUtimeGreaterThanOrEqualTo(Date value) {
            addCriterion("utime >=", value, "utime");
            return (Criteria) this;
        }

        public Criteria andUtimeLessThan(Date value) {
            addCriterion("utime <", value, "utime");
            return (Criteria) this;
        }

        public Criteria andUtimeLessThanOrEqualTo(Date value) {
            addCriterion("utime <=", value, "utime");
            return (Criteria) this;
        }

        public Criteria andUtimeIn(List<Date> values) {
            addCriterion("utime in", values, "utime");
            return (Criteria) this;
        }

        public Criteria andUtimeNotIn(List<Date> values) {
            addCriterion("utime not in", values, "utime");
            return (Criteria) this;
        }

        public Criteria andUtimeBetween(Date value1, Date value2) {
            addCriterion("utime between", value1, value2, "utime");
            return (Criteria) this;
        }

        public Criteria andUtimeNotBetween(Date value1, Date value2) {
            addCriterion("utime not between", value1, value2, "utime");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}