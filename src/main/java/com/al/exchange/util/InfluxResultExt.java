package com.al.exchange.util;

import com.al.exchange.dao.domain.MarketCapExt;
import com.al.exchange.service.DataProvideService;
import lombok.extern.slf4j.Slf4j;
import org.influxdb.InfluxDBMapperException;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;
import org.influxdb.dto.QueryResult;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * NOTE:
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author mr.wang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018/6/30 9:22
 */
@Slf4j
@Service
public class InfluxResultExt {


    /**
     * Data structure used to cache classes used as measurements.
     */
    private static final
    ConcurrentMap<String, ConcurrentMap<String, Field>> CLASS_FIELD_CACHE = new ConcurrentHashMap<>();

    private static final int FRACTION_MIN_WIDTH = 0;
    private static final int FRACTION_MAX_WIDTH = 9;
    private static final boolean ADD_DECIMAL_POINT = true;

    public static ConcurrentMap<String, ConcurrentMap<String, Field>> getClassFieldCache() {
        return CLASS_FIELD_CACHE;
    }

    /**
     * When a query is executed without {@link TimeUnit}, InfluxDB returns the <tt>time</tt>
     * column as an ISO8601 date.
     */
    private static final DateTimeFormatter ISO8601_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
            .appendFraction(ChronoField.NANO_OF_SECOND, FRACTION_MIN_WIDTH, FRACTION_MAX_WIDTH, ADD_DECIMAL_POINT)
            .appendPattern("X")
            .toFormatter();

    /**
     * <p>
     * Process a {@link QueryResult} object returned by the InfluxDB client inspecting the internal
     * data structure and creating the respective object instances based on the Class passed as
     * parameter.
     * </p>
     *
     * @param queryResult the InfluxDB result object
     * @param clazz       the Class that will be used to hold your measurement data
     * @return a {@link List} of objects from the same Class passed as parameter and sorted on the
     * same order as received from InfluxDB.
     * @throws InfluxDBMapperException If {@link QueryResult} parameter contain errors,
     *                                 <tt>clazz</tt> parameter is not annotated with &#64;Measurement or it was not
     *                                 possible to define the values of your POJO (e.g. due to an unsupported field type).
     */
    public <T> List<T> toPOJO(final QueryResult queryResult, Class clazz, String measurementName) throws InfluxDBMapperException {
        Objects.requireNonNull(queryResult, "queryResult");
        Objects.requireNonNull(clazz, "clazz");

        if (!StringUtils.hasText(measurementName)) {
            new RuntimeException("query influxdb error,measurementName is null");
        }

        long time = System.currentTimeMillis();
        String radomMeasurementName = measurementName + time;

        //throwExceptionIfMissingAnnotation(clazz);
        throwExceptionIfResultWithError(queryResult);
        cacheMeasurementClass(radomMeasurementName, clazz);

        List<T> result = new LinkedList<T>();
        queryResult.getResults().stream()
                .filter(internalResult -> Objects.nonNull(internalResult) && Objects.nonNull(internalResult.getSeries()))
                .forEach(internalResult -> {
                    internalResult.getSeries().stream()
                            .filter(series -> series.getName().equals(measurementName))
                            .forEachOrdered(series -> {
                                parseSeriesAs(series, clazz, result, measurementName,time);
                            });
                });
        removeCacheMeasurementClass(measurementName,time);
        return result;
    }

    private void removeCacheMeasurementClass(String measurementName, long num) {
        CLASS_FIELD_CACHE.remove(measurementName+num);
    }

    void throwExceptionIfMissingAnnotation(final Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Measurement.class)) {
            throw new IllegalArgumentException(
                    "Class " + clazz.getName() + " is not annotated with @" + Measurement.class.getSimpleName());
        }
    }

    void throwExceptionIfResultWithError(final QueryResult queryResult) {
        if (queryResult.getError() != null) {
            throw new InfluxDBMapperException("InfluxDB returned an error: " + queryResult.getError());
        }

        queryResult.getResults().forEach(seriesResult -> {
            if (seriesResult.getError() != null) {
                throw new InfluxDBMapperException("InfluxDB returned an error with Series: " + seriesResult.getError());
            }
        });
    }

    void cacheMeasurementClass(String measurementName,final Class<?>... classVarAgrs) {
        for (Class<?> clazz : classVarAgrs) {
            if (CLASS_FIELD_CACHE.containsKey(measurementName)) {
                continue;
            }
            ConcurrentMap<String, Field> initialMap = new ConcurrentHashMap<>();
            ConcurrentMap<String, Field> influxColumnAndFieldMap = CLASS_FIELD_CACHE.putIfAbsent(measurementName, initialMap);
            if (influxColumnAndFieldMap == null) {
                influxColumnAndFieldMap = initialMap;
            }

            for (Field field : clazz.getDeclaredFields()) {
                Column colAnnotation = field.getAnnotation(Column.class);
                if (colAnnotation != null) {
                    influxColumnAndFieldMap.put(colAnnotation.name(), field);
                }
            }
        }
    }

    String getMeasurementName(final Class<?> clazz) {
        return ((Measurement) clazz.getAnnotation(Measurement.class)).name();
    }

    <T> List<T> parseSeriesAs(final QueryResult.Series series, final Class<T> clazz, final List<T> result, String measurementName,Long time) {
        int columnSize = series.getColumns().size();
        ConcurrentMap<String, Field> colNameAndFieldMap = CLASS_FIELD_CACHE.get(measurementName+time);
        return parseSeriesAs(series,clazz,result,measurementName,colNameAndFieldMap);
    }

    <T> List<T> parseSeriesAs(final QueryResult.Series series, final Class<T> clazz, final List<T> result, String measurementName,ConcurrentMap<String, Field> colNameAndFieldMap) {
        int columnSize = series.getColumns().size();
        try {
            T object = null;
            for (List<Object> row : series.getValues()) {
                for (int i = 0; i < columnSize; i++) {
                    Field correspondingField = colNameAndFieldMap.get(series.getColumns().get(i)/*InfluxDB columnName*/);
                    if (correspondingField != null) {
                        if (object == null) {
                            if (clazz.getName().contains("KlineOthers")) {
                                object = (T) new DataProvideService.KlineOthers(measurementName);
                            }else if (clazz.getName().contains("MarketCapExt")){
                                object = (T) new MarketCapExt(measurementName);
                            }else {
                                object = clazz.newInstance();
                            }
                        }
                        setFieldValue(object, correspondingField, row.get(i));
                    }
                }
                // When the "GROUP BY" clause is used, "tags" are returned as Map<String,String> and
                // accordingly with InfluxDB documentation
                // https://docs.influxdata.com/influxdb/v1.2/concepts/glossary/#tag-value
                // "tag" values are always String.
                if (series.getTags() != null && !series.getTags().isEmpty()) {
                    for (Map.Entry<String, String> entry : series.getTags().entrySet()) {
                        Field correspondingField = colNameAndFieldMap.get(entry.getKey()/*InfluxDB columnName*/);
                        if (correspondingField != null) {
                            // I don't think it is possible to reach here without a valid "object"
                            setFieldValue(object, correspondingField, entry.getValue());
                        }
                    }
                }
                if (object != null) {
                    result.add(object);
                    object = null;
                }
            }
        } catch (IllegalAccessException e) {
            throw new InfluxDBMapperException(e);
        } catch (InstantiationException e) {
            e.printStackTrace();
        }finally {
            return result;
        }
    }

    /**
     * InfluxDB client returns any number as Double.
     * See https://github.com/influxdata/influxdb-java/issues/153#issuecomment-259681987
     * for more information.
     *
     * @param object
     * @param field
     * @param value
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    <T> void setFieldValue(final T object, final Field field, final Object value)
            throws IllegalArgumentException, IllegalAccessException {
        if (value == null) {
            return;
        }
        Class<?> fieldType = field.getType();
        boolean oldAccessibleState = field.isAccessible();
        try {
            field.setAccessible(true);
            if (fieldValueModified(fieldType, field, object, value)
                    || fieldValueForPrimitivesModified(fieldType, field, object, value)
                    || fieldValueForPrimitiveWrappersModified(fieldType, field, object, value)) {
                return;
            }
            String msg = "Class '%s' field '%s' is from an unsupported type '%s'.";
            throw new InfluxDBMapperException(
                    String.format(msg, object.getClass().getName(), field.getName(), field.getType()));
        }catch (Throwable e){
            String msg = "Class '%s' field '%s' was defined with a different field type and caused a ClassCastException. "
                    + "The correct type is '%s' (current field value: '%s').";
            throw new InfluxDBMapperException(
                    String.format(msg, object.getClass().getName(), field.getName(), value.getClass().getName(), value));
        } finally {
            field.setAccessible(oldAccessibleState);
        }
    }

    <T> boolean fieldValueModified(final Class<?> fieldType, final Field field, final T object, final Object value)
            throws IllegalArgumentException, IllegalAccessException {
        if (Instant.class.isAssignableFrom(fieldType)) {
            Instant instant;
            if (value instanceof String) {
                instant = Instant.from(ISO8601_FORMATTER.parse(String.valueOf(value)));
            } else if (value instanceof Long) {
                instant = Instant.ofEpochMilli((Long) value);
            } else if (value instanceof Double) {
                instant = Instant.ofEpochMilli(((Double) value).longValue());
            } else {
                throw new InfluxDBMapperException("Unsupported type " + field.getClass() + " for field " + field.getName());
            }
            field.set(object, instant);
            return true;
        }
        return false;
    }

    <T> boolean fieldValueForPrimitivesModified(final Class<?> fieldType, final Field field, final T object,
                                                final Object value) throws IllegalArgumentException, IllegalAccessException {
        if (double.class.isAssignableFrom(fieldType)) {
            field.setDouble(object, ((Double) value).doubleValue());
            return true;
        }
        if (long.class.isAssignableFrom(fieldType)) {
            field.setLong(object, ((Double) value).longValue());
            return true;
        }
        if (int.class.isAssignableFrom(fieldType)) {
            field.setInt(object, ((Double) value).intValue());
            return true;
        }
        if (boolean.class.isAssignableFrom(fieldType)) {
            field.setBoolean(object, Boolean.valueOf(String.valueOf(value)).booleanValue());
            return true;
        }
        return false;
    }

    <T> boolean fieldValueForPrimitiveWrappersModified(final Class<?> fieldType, final Field field, final T object,
                                                       final Object value) throws IllegalArgumentException, IllegalAccessException {
        if (BigDecimal.class.isAssignableFrom(fieldType)) {
            field.set(object, new BigDecimal(value.toString()));
            return true;
        }
        if (String.class.isAssignableFrom(fieldType)) {
            field.set(object, String.valueOf(value));
            return true;
        }
        if (Double.class.isAssignableFrom(fieldType)) {
            field.set(object, value);
            return true;
        }
        if (Long.class.isAssignableFrom(fieldType)) {
            field.set(object, Long.valueOf(((Double) value).longValue()));
            return true;
        }
        if (Integer.class.isAssignableFrom(fieldType)) {
            field.set(object, Integer.valueOf(((Double) value).intValue()));
            return true;
        }
        if (Boolean.class.isAssignableFrom(fieldType)) {
            field.set(object, Boolean.valueOf(String.valueOf(value)));
            return true;
        }
        return false;
    }


}
