package com.al.exchange.dao.domain;

import lombok.Data;

import java.util.Date;

@Data
public class OnlykeyBean {

    private Integer id;

    private String onlykey;

    private String description;

    private Boolean disable;

    private Date utime;

}