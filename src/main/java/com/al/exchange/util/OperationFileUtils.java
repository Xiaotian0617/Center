package com.al.exchange.util;

import com.al.exchange.dao.domain.MarketVO;
import com.al.exchange.task.SaveMsgToHardDriveTask;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;

/**
 * CalcCenter file:SaveFileUtils
 * <p>
 * 文件操作类
 *
 * @author mr.wang
 * @version 2018年03月08日10:39:53 V1.0
 * @par 版权信息： 2018 copyright 河南艾鹿网络科技有限公司 all rights reserved.
 */
@Slf4j
@Component
public class OperationFileUtils {

    @Value("${file.path}")
    private String exPath;

    ReentrantLock lock = new ReentrantLock();

    private static List<String> marketFiles = new ArrayList<>(1500);

    public String getExPath() {
        return exPath;
    }

    public boolean writeFile(String directoryName, String fileName, String content) {
        Path path = get(getExPath() + directoryName + fileName);
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(content);
        } catch (IOException e) {
            log.error("保存文件失败，错误内容为：", e);
            return false;
        }
        log.debug("保存文件成功，目录为{}", path.toString());
        return true;
    }

    public String readFile(String directoryName, String fileName) {
        Path path = get(getExPath() + directoryName + fileName);
        return readFile(path);
    }

    public String readFile(Path path) {
        try {
            return new String(readAllBytes(path));
            // IO流处理 据说效率会高
            // Files.lines(Paths.get("D:\\jd.txt"),
            // StandardCharsets.UTF_8).forEach(System.out::println);
        } catch (IOException e) {
            log.error("读取文件失败，错误内容为", e);
            return null;
        }
    }

    /**
     * 获取市场行情的文件夹
     *
     * @return
     */
    private String getMarketDirectory() {
        return getExPath() + SaveMsgToHardDriveTask.marketDirectoryName;
    }

    public File getMarketLastFile() {
        getMarketDirectoryFileNum();
        lock.lock();
        try {
            if (marketFiles.size() == 0) {
                return null;
            }
            File file = new File(marketFiles.get(marketFiles.size() - 1));
            return file;
        } catch (Throwable e) {
            log.error("获取行情文件夹中最后一条数据出错，错误原因为：", e);
            return null;
        } finally {
            lock.unlock();
        }
    }

    private void getMarketDirectoryFileNum() {
        String directoryPath = getMarketDirectory();
        lock.lock();
        try {
            marketFiles.clear();
            Files.newDirectoryStream(Paths.get(directoryPath), path -> path.toString().endsWith(".json"))
                    .forEach(path -> marketFiles.add(path.toString()));
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public boolean deleteMarketOldestFile() {
        getMarketDirectoryFileNum();
        lock.lock();
        try {
            if (marketFiles.size() >= 1440) {
                new File(marketFiles.get(0)).delete();
                log.info("已清除最老一个行情文件！文件名称为{}", marketFiles.get(0));
            }
            log.info("目前文件夹中文件数量为{}，暂不需删除", marketFiles.size());
            return true;
        } catch (Throwable e) {
            log.error("删除最老一个行情文件出错！错误原因为:", e);
            return false;
        } finally {
            lock.unlock();
        }
    }

    public Map<String, MarketVO> getLast24HourMarketFromFile() {
        getMarketDirectoryFileNum();
        Map<String, MarketVO> marketVOMap = new HashMap<>();
        try {
            Long yesDayTime = DateUtils.getYesterdayDate().getTime();
            if (getMarketMapByTime(yesDayTime, marketVOMap)) {
                log.info("从文件中初始化24小时行情成功！");
                return marketVOMap;
            }
        } catch (Throwable e) {
            log.error("系统初始化失败24小时行情出错，线上环境涨跌幅可能会受影响", e);
            return null;
        }
        return null;
    }

    private List<String> getAvailableFilesByTime(Long time) {
        String yesTimeFileName = String.format(SaveMsgToHardDriveTask.marketFileName, time);
        lock.lock();
        List<String> availableFiles = marketFiles.stream()
                .filter(fileName -> fileName.replace(".json", "").contains(yesTimeFileName.replace(".json", "")))
                .collect(Collectors.toList());
        lock.unlock();
        return availableFiles;
    }

    public Map<String, MarketVO> getLastZeroTimeMarketFromFile(Long time) {
        getMarketDirectoryFileNum();
        Map<String, MarketVO> marketVOMap = new HashMap<>();
        try {
            if (getMarketMapByTime(time, marketVOMap)) {
                log.info("从文件中初始化当日凌晨行情成功！");
                return marketVOMap;
            }
        } catch (Throwable e) {
            log.error("系统初始化失败当日凌晨行情出错，线上环境涨跌幅可能会受影响", e);
            return null;
        }
        return null;
    }

    private boolean getMarketMapByTime(Long time, Map<String, MarketVO> marketVOMap) {
        List<String> availableFiles = getAvailableFilesByTime(time / 1000000);
        if (availableFiles.size() != 0) {
            // 从筛选出来的文件中挑选中间的一条作为其涨跌幅计算依据 采用进一法 不管怎么都进一 取大概值
            String content = readFile(SaveMsgToHardDriveTask.marketDirectoryName,
                    availableFiles.get((int) Math.ceil(availableFiles.size() / 2))
                            .replace(exPath + SaveMsgToHardDriveTask.marketDirectoryName, ""));
            if (content != null) {
                Map map = JSON.parseObject(content);
                if (map != null && map.size() != 0) {
                    map.keySet().stream().filter(Objects::nonNull).forEach(key -> {
                        marketVOMap.put((String) key, new MarketVO((JSONObject) map.get(key)));
                    });
                    return true;
                }
            }
        }
        return false;
    }
}
