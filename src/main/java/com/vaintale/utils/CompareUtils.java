package com.vaintale.utils;

import com.vaintale.annotation.Compare;
import com.vaintale.base.CompareNode;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 实体类比较工具类
 *
 * @author vaintale
 * @date 2025/2/22
 */
public class CompareUtils<T> {

    private static final String COMMA = "，";

    /**
     * 属性比较
     * 输出格式：[value：source -> source1]，[value：1.4 -> 1.7]
     *
     * @param source 源数据对象
     * @param target 目标数据对象
     * @return 对应属性值的比较变化
     */
    public String compare(T source, T target) {
        return compare(source, target, null);
    }

    /**
     * 属性比较
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
        // 如果目标数据为空，只显示源数据（删除场景）
        if (targetMap.isEmpty()) {
            return doDelete(sourceMap, ignoreCompareFields);
        }
        // 正常比较
        String s = doCompare(sourceMap, targetMap, ignoreCompareFields);
        if (!s.endsWith(COMMA)) {
            return s;
        }
        return s.substring(0, s.length() - 1);
    }

    /**
     * 处理新增场景（源对象为空）
     */
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
            if (!o.toString().isEmpty()) {
                sb.append("新增[").append(node.getFieldName()).append("：").append(o).append("]");
                if (current < size) {
                    sb.append(COMMA);
                }
            }
        }
        return sb.toString();
    }

    /**
     * 处理删除场景（目标对象为空）
     */
    private String doDelete(Map<String, CompareNode> sourceMap, List<String> ignoreCompareFields) {
        StringBuilder sb = new StringBuilder();
        Collection<CompareNode> values = sourceMap.values();
        int size = values.size();
        int current = 0;
        for (CompareNode node : values) {
            current++;
            Object o = Optional.ofNullable(node.getFieldValue()).orElse("");
            if (Objects.nonNull(ignoreCompareFields) && ignoreCompareFields.contains(node.getFieldKey())) {
                continue;
            }
            if (!o.toString().isEmpty()) {
                sb.append("删除[").append(node.getFieldName()).append("：").append(o).append("]");
                if (current < size) {
                    sb.append(COMMA);
                }
            }
        }
        return sb.toString();
    }

    private String doCompare(Map<String, CompareNode> sourceMap, Map<String, CompareNode> targetMap, List<String> ignoreCompareFields) {
        StringBuilder sb = new StringBuilder();
        // 获取所有字段的并集，处理新增和删除的情况
        Set<String> allKeys = new LinkedHashSet<>();
        allKeys.addAll(sourceMap.keySet());
        allKeys.addAll(targetMap.keySet());

        int size = allKeys.size();
        int current = 0;

        for (String key : allKeys) {
            current++;
            CompareNode sn = sourceMap.get(key);
            CompareNode tn = targetMap.get(key);

            // 检查是否忽略该字段
            if (Objects.nonNull(ignoreCompareFields) && ignoreCompareFields.contains(key)) {
                continue;
            }

            String sv = Optional.ofNullable(sn).map(CompareNode::getFieldValue).orElse("").toString();
            String tv = Optional.ofNullable(tn).map(CompareNode::getFieldValue).orElse("").toString();
            String fieldName = Optional.ofNullable(sn).map(CompareNode::getFieldName).orElse(
                    Optional.ofNullable(tn).map(CompareNode::getFieldName).orElse(key));

            // 处理新增字段（源为空，目标不为空）
            if (sn == null && tn != null && !tv.isEmpty()) {
                sb.append("新增[").append(fieldName).append("：").append(tv).append("]");
                if (current < size) {
                    sb.append(COMMA);
                }
            }
            // 处理删除字段（源不为空，目标为空）
            else if (sn != null && tn == null && !sv.isEmpty()) {
                sb.append("删除[").append(fieldName).append("：").append(sv).append("]");
                if (current < size) {
                    sb.append(COMMA);
                }
            }
            // 处理修改字段
            else if (sn != null && tn != null && !sv.equals(tv)) {
                sb.append(String.format("[%s：%s -> %s]", fieldName, sv, tv));
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
        if (fields.length == 0) {
            return Collections.emptyMap();
        }
        Map<String, CompareNode> map = new LinkedHashMap<>(fields.length);
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
                throw new RuntimeException(e);
            }
        }
        return map;
    }

    /**
     * 比较字段变化，返回结构化的字段变化列表
     * {newValue=A, fieldName=注解value, fieldKey=字段名称, changeType=变化类型, oldValue=B}
     *
     * @param source              源数据对象
     * @param target              目标数据对象
     * @param ignoreCompareFields 忽略比较的字段
     * @return 字段变化列表，每个元素包含变化详情
     */
    public List<Map<String, Object>> compareFiled(T source, T target, List<String> ignoreCompareFields) {
        List<Map<String, Object>> resultList = new ArrayList<>();

        if (Objects.isNull(source) && Objects.isNull(target)) {
            return resultList;
        }

        Map<String, CompareNode> sourceMap = this.getFiledValueMap(source);
        Map<String, CompareNode> targetMap = this.getFiledValueMap(target);

        if (sourceMap.isEmpty() && targetMap.isEmpty()) {
            return resultList;
        }

        // 获取所有字段的并集
        Set<String> allKeys = new LinkedHashSet<>();
        allKeys.addAll(sourceMap.keySet());
        allKeys.addAll(targetMap.keySet());

        for (String key : allKeys) {
            CompareNode sn = sourceMap.get(key);
            CompareNode tn = targetMap.get(key);

            // 检查是否忽略该字段
            if (Objects.nonNull(ignoreCompareFields) && ignoreCompareFields.contains(key)) {
                continue;
            }

            String sv = Optional.ofNullable(sn).map(CompareNode::getFieldValue).orElse("").toString();
            String tv = Optional.ofNullable(tn).map(CompareNode::getFieldValue).orElse("").toString();
            String fieldName = Optional.ofNullable(sn).map(CompareNode::getFieldName).orElse(
                    Optional.ofNullable(tn).map(CompareNode::getFieldName).orElse(key));

            Map<String, Object> changeInfo = new HashMap<>();

            // 处理新增字段
            if (sn == null && tn != null && !tv.isEmpty()) {
                changeInfo.put("changeType", "ADD");
                changeInfo.put("fieldKey", key);
                changeInfo.put("fieldName", fieldName);
                changeInfo.put("newValue", tv);
                resultList.add(changeInfo);
            }
            // 处理删除字段
            else if (sn != null && tn == null && !sv.isEmpty()) {
                changeInfo.put("changeType", "DELETE");
                changeInfo.put("fieldKey", key);
                changeInfo.put("fieldName", fieldName);
                changeInfo.put("oldValue", sv);
                resultList.add(changeInfo);
            }
            // 处理修改字段
            else if (sn != null && tn != null && !sv.equals(tv)) {
                changeInfo.put("changeType", "UPDATE");
                changeInfo.put("fieldKey", key);
                changeInfo.put("fieldName", fieldName);
                changeInfo.put("oldValue", sv);
                changeInfo.put("newValue", tv);
                resultList.add(changeInfo);
            }
        }

        return resultList;
    }

    /**
     * 兼容旧版本的简单字段列表返回（仅返回变化的字段名和中文名，不包含变化类型和值）
     *
     * @deprecated 建议使用 {@link #compareFiled(T, T, List)} 获取更完整的信息
     */
    @Deprecated
    public List<HashMap<String, String>> compareFiledSimple(T source, T target, List<String> ignoreCompareFields) {
        List<HashMap<String, String>> listStr = new ArrayList<>();

        if (Objects.isNull(source) && Objects.isNull(target)) {
            return listStr;
        }

        Map<String, CompareNode> sourceMap = this.getFiledValueMap(source);
        Map<String, CompareNode> targetMap = this.getFiledValueMap(target);

        if (sourceMap.isEmpty() && targetMap.isEmpty()) {
            return listStr;
        }

        Set<String> allKeys = new LinkedHashSet<>();
        allKeys.addAll(sourceMap.keySet());
        allKeys.addAll(targetMap.keySet());

        for (String key : allKeys) {
            CompareNode sn = sourceMap.get(key);
            CompareNode tn = targetMap.get(key);

            if (Objects.nonNull(ignoreCompareFields) && ignoreCompareFields.contains(key)) {
                continue;
            }

            String sv = Optional.ofNullable(sn).map(CompareNode::getFieldValue).orElse("").toString();
            String tv = Optional.ofNullable(tn).map(CompareNode::getFieldValue).orElse("").toString();
            String fieldName = Optional.ofNullable(sn).map(CompareNode::getFieldName).orElse(
                    Optional.ofNullable(tn).map(CompareNode::getFieldName).orElse(key));

            // 只有在值发生变化时才记录（新增和删除也算变化）
            boolean hasChange = sn == null && tn != null || sn != null && tn == null || sn != null && !sv.equals(tv);

            if (hasChange) {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put(key, fieldName);
                listStr.add(hashMap);
            }
        }

        return listStr;
    }
}