package com.al.exchange.web;

import com.al.exchange.util.PropertiesUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Map;

@Data
@Slf4j
public class WebResult implements Serializable {

    private int code;
    private ResultDialogEntity dialog;
    private Object data;

    //成功操作 操作码默认为0   无参
    public static WebResult okResult() {
        return okResult(getOkCode(), "");
    }

    //成功操作 操作码默认为0   只有提示码
    public static WebResult okResult(int code) {
        return okResult(getOkCode(), String.valueOf(code), "");
    }

    //成功操作 操作码默认为0   只有返回数据
    public static WebResult okResult(Object data) {
        return okResult(getOkCode(), data);
    }

    //成功操作 操作码默认为0   只有提示码 和 数据
    public static WebResult okResult(int code, Object data) {
        return okResult(getOkCode(), String.valueOf(code), data);
    }

    //内部使用
    private static WebResult okResult(int code, String windowCode, Object data) {
        Map<String, String> propertiesmap = PropertiesUtils.getMapForProperties();
        WebResult wr = new WebResult();
        wr.setCode(code);
        wr.setData(data);
        wr.setDialog(setResultDialog(propertiesmap, windowCode, null));
        return wr;
    }

    private static Integer getOkCode() {
        Map<String, String> propertiesmap = PropertiesUtils.getMapForProperties();
        return Integer.valueOf(propertiesmap.get("0[code]"));
    }

    /**
     * Note:新的成功弹窗提示
     * <p></p>
     *
     * @param code 错误码
     * @param data 返回对象
     * @return WebResult    返回类型
     * @author WangKai
     */
    public static WebResult okResult(int code, Object data, String params[]) {
        return backResult(getOkCode(), String.valueOf(code), data, params);
    }

    //##### 失败 返回 ######
    //只传入一个失败码
    public static WebResult failResult(int code) {
        return failResult(code, "");
    }

    public static WebResult failResult(int code, Object object) {
        Map<String, String> propertiesmap = PropertiesUtils.getMapForProperties();
        WebResult wr = new WebResult();
        wr.setCode(code);
        wr.setData(object);
        wr.setDialog(setResultDialog(propertiesmap, String.valueOf(code), null));
        return wr;
    }

    public static WebResult failResult(int code, Object data, String params[]) {
        return failResult(code, String.valueOf(code), data, params);
    }

    public static WebResult failException() {
        return failResult(9999);
    }

    public static WebResult failException(String eMsg) {
        Map<String, String> map = PropertiesUtils.getMapForProperties();
        WebResult wr = new WebResult();
        wr.setCode(9999);
        wr.setData("");
        ResultDialogEntity rde = new ResultDialogEntity();
        rde.setTitle(map.get("9999[title]"));
        rde.setCode("9999");
        rde.setType(map.get("9999[type]"));
        rde.setContent(eMsg);
        wr.setDialog(rde);
        return wr;
    }

    /**
     * Note:新的失败弹窗提示
     * <p></p>
     *
     * @param code       错误码
     * @param windowCode 弹窗码 用于确认后跳转位置
     * @param data       返回对象
     * @return WebResult    返回类型
     * @author WangKai
     */
    private static WebResult failResult(int code, String windowCode, Object data, String params[]) {
        return backResult(code, windowCode, data, params);
    }

    /**
     * Note: 拼装好返回的属性
     * <p></p>
     *
     * @param map
     * @param code
     * @param @return
     * @return ResultDialogEntity    返回类型
     * @author WangKai
     */
    private static ResultDialogEntity setResultDialog(Map<String, String> map, String code, String... params) {
        ResultDialogEntity rde = new ResultDialogEntity();
        rde.setTitle(map.get(code + "[title]"));
        rde.setCancelBtn(map.get(code + "[cancel]"));
        rde.setConfirmBtn(map.get(code + "[confirm]"));
        rde.setCode(code);
        rde.setType(map.get(code + "[type]"));
        rde.setTime(map.get(code + "[time]"));
        if (params != null && params.length > 0) {
            String content = MessageFormat.format(map.get(code + "[content]"), params);
            rde.setContent(content);
        } else {
            rde.setContent(map.get(code + "[content]"));
        }
        return rde;
    }

    private static WebResult backResult(int code, String windowCode, Object data, String params[]) {
        Map<String, String> map = PropertiesUtils.getMapForProperties();
        WebResult wr = new WebResult();
        wr.setCode(code);
        wr.setData(data);
        wr.setDialog(setResultDialog(map, windowCode, params));
        return wr;
    }

}
