package com.al.exchange.util;

import com.al.exchange.web.WebResult;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

@Slf4j
public final class PropertiesUtils {

    private static Properties prop;

    public static Properties getProperties() {
        if (prop==null){
            prop  = new Properties();
            try(InputStream in = WebResult.class.getClassLoader().getResourceAsStream("webResult.properties")) {
                prop.load(in);
            } catch (IOException e) {
                log.error("返回信息时，获取配置文件出错", e.getMessage());
                e.printStackTrace();
            }
        }
        return prop;
    }

    public static Map<String, String> getMapForProperties() {
        Properties properties = getProperties();
        return PropertiesToMap(properties);
    }

    public static Map<String, String> PropertiesToMap(Properties properties) {
        //Map<String, String> map = new HashMap<String, String>((Map) properties);
        return (Map) getProperties();
    }

    public static void main(String[] args) {
        Map<String, String> map = getMapForProperties();
        System.out.println(map.get("E202[title]"));
    }


}
