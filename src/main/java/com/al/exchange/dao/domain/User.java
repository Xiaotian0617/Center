package com.al.exchange.dao.domain;

import java.math.BigDecimal;
import java.util.Date;

public class User {
    private Long id;

    private String nickName;

    private String password;

    private String email;

    private String phone;

    private String avatar;

    private String imToken;

    private Integer bigv;

    private BigDecimal subscribeCharge;

    private Date createTime;

    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName == null ? null : nickName.trim();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password == null ? null : password.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone == null ? null : phone.trim();
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar == null ? null : avatar.trim();
    }

    public String getImToken() {
        return imToken;
    }

    public void setImToken(String imToken) {
        this.imToken = imToken == null ? null : imToken.trim();
    }

    public Integer getBigv() {
        return bigv;
    }

    public void setBigv(Integer bigv) {
        this.bigv = bigv;
    }

    public BigDecimal getSubscribeCharge() {
        return subscribeCharge;
    }

    public void setSubscribeCharge(BigDecimal subscribeCharge) {
        this.subscribeCharge = subscribeCharge;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}