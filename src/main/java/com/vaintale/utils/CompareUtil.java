package com.vaintale.utils;

import com.vaintale.annotation.Compare;
import com.vaintale.base.CompareNode;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 实体类比较工具类
 * 比较两个实体类的属性值是否一致，不一致则返回不一致的属性值
 * 支持属性值为对象的比较
 * 支持属性值为集合的比较
 * 支持属性值为数组的比较
 * 支持属性值为Map的比较
 * 支持属性值为基本类型的比较
 * 支持属性值为String的比较
 * 支持属性值为Date的比较
 * 支持属性值为BigDecimal的比较
 * 支持属性值为Boolean的比较
 * 支持属性值为Long的比较
 * 支持属性值为Integer的比较
 * 支持属性值为Double的比较
 * 支持属性值为Float的比较
 * 支持属性值为Short的比较
 * 支持属性值为Byte的比较
 * 支持属性值为Character的比较
 * 支持属性值为Enum的比较
 * 支持属性值为自定义类型的比较
 * 支持属性值为null的比较
 * 支持属性值为自定义类型的比较
 * 支持属性值为自定义类型的比较
 * 支持属性值为自定义类型的比较
 *
 * @author vaintale
 * @date 2025/2/22
 */
public class CompareUtil<T> {

    private static final String COMMA = "，";

    /**
     * 属性比较
     *
     * @param source 源数据对象
     * @param target 目标数据对象
     * @return 对应属性值的比较变化
     */
    public String compare(T source, T target) {
        return compare(source, target, null);
    }


    /**
     * 属性比较，返回属性值的比较变化
     *
     * @param source              源数据对象
     * @param target              目标数据对象
     * @param ignoreCompareFields 忽略比较的字段
     * @return 对应属性值的比较变化
     */
    public String compare(T source, T target, List<String> ignoreCompareFields) {
        if (Objects.isNull(source) && Objects.isNull(target)) {
            return "";
        }
        Map<String, CompareNode> sourceMap = this.getFiledValueMap(source);
        Map<String, CompareNode> targetMap = this.getFiledValueMap(target);
        if (sourceMap.isEmpty() && targetMap.isEmpty()) {
            return "";
        }
        // 如果源数据为空，则只显示目标数据，不显示属性变化情况
        if (sourceMap.isEmpty()) {
            return doEmpty(targetMap, ignoreCompareFields);
        }
        // 如果源数据为空，则显示属性变化情况
        String s = doCompare(sourceMap, targetMap, ignoreCompareFields);
        if (!s.endsWith(COMMA)) {
            return s;
        }
        return s.substring(0, s.length() - 1);
    }

    private String doEmpty(Map<String, CompareNode> targetMap, List<String> ignoreCompareFields) {
        StringBuilder sb = new StringBuilder();
        Collection<CompareNode> values = targetMap.values();
        int size = values.size();
        int current = 0;
        for (CompareNode node : values) {
            current++;
            Object o = Optional.ofNullable(node.getFieldValue()).orElse("");
            if (Objects.nonNull(ignoreCompareFields) && ignoreCompareFields.contains(node.getFieldKey())) {
                continue;
            }
            if (o.toString().length() > 0) {
                sb.append("[" + node.getFieldName() + "：" + o + "]");
                if (current < size) {
                    sb.append(COMMA);
                }
            }
        }
        return sb.toString();
    }

    private String doCompare(Map<String, CompareNode> sourceMap, Map<String, CompareNode> targetMap, List<String> ignoreCompareFields) {
        StringBuilder sb = new StringBuilder();
        Set<String> keys = sourceMap.keySet();
        int size = keys.size();
        int current = 0;
        for (String key : keys) {
            current++;
            CompareNode sn = sourceMap.get(key);
            CompareNode tn = targetMap.get(key);
            if (Objects.nonNull(ignoreCompareFields) && ignoreCompareFields.contains(sn.getFieldKey())) {
                continue;
            }
            String sv = Optional.ofNullable(sn.getFieldValue()).orElse("").toString();
            String tv = Optional.ofNullable(tn.getFieldValue()).orElse("").toString();
            // 只有两者属性值不一致时, 才显示变化情况
            if (!sv.equals(tv)) {
                sb.append(String.format("[%s：%s -> %s]", sn.getFieldName(), sv, tv));
                if (current < size) {
                    sb.append(COMMA);
                }
            }
        }
        return sb.toString();
    }

    private Map<String, CompareNode> getFiledValueMap(T t) {
        if (Objects.isNull(t)) {
            return Collections.emptyMap();
        }
        Field[] fields = t.getClass().getDeclaredFields();
        if (Objects.isNull(fields) || fields.length == 0) {
            return Collections.emptyMap();
        }
        Map<String, CompareNode> map = new LinkedHashMap();
        for (Field field : fields) {
            Compare compareAnnotation = field.getAnnotation(Compare.class);
            if (Objects.isNull(compareAnnotation)) {
                continue;
            }
            field.setAccessible(true);
            try {
                String fieldKey = field.getName();
                CompareNode node = new CompareNode();
                node.setFieldKey(fieldKey);
                node.setFieldValue(field.get(t));
                node.setFieldName(compareAnnotation.value());
                map.put(field.getName(), node);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    /**
     * 比较属性值，返回注解中的属性名称
     *
     * @param source
     * @param target
     * @param ignoreCompareFields
     * @return {@link String }
     */
    public List<HashMap<String, String>> compareFiled(T source, T target, List<String> ignoreCompareFields) {
        ArrayList<HashMap<String, String>> listStr = new ArrayList<>(20);
        if (Objects.isNull(source) && Objects.isNull(target)) {
            return listStr;
        }
        Map<String, CompareNode> sourceMap = this.getFiledValueMap(source);
        Map<String, CompareNode> targetMap = this.getFiledValueMap(target);
        if (sourceMap.isEmpty() && targetMap.isEmpty()) {
            return listStr;
        }

        // 如果源数据为空，则显示属性变化情况
        return doCompareFiled(sourceMap, targetMap, ignoreCompareFields);
    }

    private List<HashMap<String, String>> doCompareFiled(Map<String, CompareNode> sourceMap, Map<String, CompareNode> targetMap, List<String> ignoreCompareFields) {
        HashMap<String, String> hashMap = new HashMap<>();
        ArrayList<HashMap<String, String>> listStr = new ArrayList<>(20);

        Set<String> keys = sourceMap.keySet();

        for (String key : keys) {
            CompareNode sn = sourceMap.get(key);
            CompareNode tn = targetMap.get(key);
            if (Objects.nonNull(ignoreCompareFields) && ignoreCompareFields.contains(sn.getFieldKey())) {
                continue;
            }
            String sv = Optional.ofNullable(sn.getFieldValue()).orElse("").toString();
            String tv = Optional.ofNullable(tn.getFieldValue()).orElse("").toString();
            // 只有两者属性值不一致时, 才显示变化情况
            if (!sv.equals(tv)) {
                hashMap.put(sn.getFieldKey(), sn.getFieldName());
            }
            listStr.add(hashMap);
        }
        return listStr;
    }
}
