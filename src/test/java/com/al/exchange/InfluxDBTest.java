package com.al.exchange;


import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest
public class InfluxDBTest {

    private InfluxDB influxDB;
    private final static int UDP_PORT = 8086;
    private final static String DATABASE = "TopCoinDB";


    @Before
    public void setup() throws InterruptedException, IOException {
        this.influxDB = InfluxDBFactory.connect("http://47.97.169.136:" + UDP_PORT, "root", "admin");
        boolean influxDBStarted = false;
        do {
            Pong response;
            try {
                response = influxDB.ping();
                if (!response.getVersion().equalsIgnoreCase("Unknown")) {
                    influxDBStarted = true;
                }
            } catch (Exception e) {
                // TODO
                e.printStackTrace();
            }
        } while (!influxDBStarted);

        this.influxDB.setLogLevel(InfluxDB.LogLevel.NONE);
        this.influxDB.createDatabase(DATABASE);
        System.out.println("################################################################## ");
        System.out.println("#  Version: " + this.influxDB.version() + " #");
        System.out.println("##################################################################");
    }


    @After
    public void cleanup() {
        //this.influxDB.deleteDatabase(DATABASE);
    }

    @Test
    public void testPing() {
        Pong result = this.influxDB.ping();
        System.out.println(result);
        Assert.assertNotNull(result);
        Assert.assertNotEquals(result.getVersion(), "unknown");
    }

    @Test
    public void testVersion() {
        String version = this.influxDB.version();
        Assert.assertNotNull(version);
        Assert.assertFalse(version.contains("unknown"));
    }

    /**
     * 测试数据库是否已存在.
     */
    @Test
    public void testDatabaseExists() {
        String existentdbName = "unittest_1";
        String notExistentdbName = "unittest_2";
        this.influxDB.createDatabase(existentdbName);
        boolean checkDbExistence = this.influxDB.databaseExists(existentdbName);
        Assert.assertTrue("It is expected that databaseExists return true for "
                + existentdbName + " database", checkDbExistence);
        checkDbExistence = this.influxDB.databaseExists(notExistentdbName);


        Assert.assertFalse("It is expected that databaseExists return false for "
                + notExistentdbName + " database", checkDbExistence);
        this.influxDB.deleteDatabase(existentdbName);
    }

    /**
     * Test that describe Databases works.
     */
    @Test
    public void testDescribeDatabases() {
        String dbName = "unittest_" + System.currentTimeMillis();
        this.influxDB.createDatabase(dbName);
        this.influxDB.describeDatabases();
        List<String> result = this.influxDB.describeDatabases();
        Assert.assertNotNull(result);
        Assert.assertTrue(result.size() > 0);
        boolean found = false;
        for (String database : result) {
            if (database.equals(dbName)) {
                found = true;
                break;
            }

        }
        Assert.assertTrue("It is expected that describeDataBases contents the newly create database.", found);
        this.influxDB.deleteDatabase(dbName);
    }


    /**
     * Test that writing to the new lineprotocol.
     */
    @Test
    public void testWrite() {
        String dbName = "TOP_write_unittest_" + System.currentTimeMillis();
        this.influxDB.createDatabase(dbName);
        String rp = getDefaultRetentionPolicy(this.influxDB.version());
        BatchPoints batchPoints = BatchPoints.database(dbName).tag("async", "true").retentionPolicy(rp).build();
        Point point1 = Point
                .measurement("cpu")
                .tag("atag", "test")
                .addField("idle", 90L)
                .addField("usertime", 9L)
                .addField("system", 1L)
                .build();
        Point point2 = Point.measurement("disk").tag("atag", "test")
                .addField("used", 80L)
                .addField("free", 1L)
                .build();
        batchPoints.point(point1);
        batchPoints.point(point2);
        this.influxDB.write(batchPoints);
        Query query = new Query("SELECT * FROM cpu GROUP BY *", dbName);
        QueryResult result = this.influxDB.query(query);
        Query query1 = new Query("SELECT * FROM disk GROUP BY *", dbName);
        QueryResult result1 = this.influxDB.query(query);
        Assert.assertFalse(result.getResults().get(0).getSeries().get(0).getTags().isEmpty());

        // 测试完成后删除数据库
        this.influxDB.deleteDatabase(dbName);
    }


    /**
     * Test for a query.
     */
    @Test
    public void testQuery() {
        QueryResult queryResult = this.influxDB.query(new Query("show databases", "mydb"));
        System.out.println("queryResult:" + queryResult.getError());
        System.out.println("queryResult:" + queryResult.getResults().get(0));
//        Query query = new Query("SELECT * FROM cpu GROUP BY *", "mydb2");
//        QueryResult result = this.influxDB.query(query);
//        QueryResult queryResult1 = this.influxDB.query(new Query("DROP DATABASE mydb2", "mydb"));
//        System.out.println("queryResult1:" + queryResult1.getError());
//        System.out.println("queryResult:" + queryResult.getResults().get(0));

    }


    private String getDefaultRetentionPolicy(String version) {
        if (version.startsWith("0.")) {
            return "default";
        } else {
            return "autogen";
        }
    }

    private String getRandomMeasurement() {
        return "measurement_" + System.nanoTime();
    }


}
