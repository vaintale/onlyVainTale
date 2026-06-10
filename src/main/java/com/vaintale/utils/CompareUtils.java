package com.vaintale.utils;

import com.vaintale.annotation.Compare;
import com.vaintale.base.CompareNode;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实体类比较工具类
 *
 * @author vaintale
 * @date 2025/2/22
 */
public class CompareUtils<T> {

    private static final String COMMA = "，";
    private static final String PATH_SEPARATOR = ".";
    private static final int MAX_DEPTH = 5; // 最大比较深度限制

    // 用于缓存字段信息，提升性能
    private static final Map<Class<?>, List<FieldInfo>> FIELD_CACHE = new ConcurrentHashMap<>();

    /**
     * 属性比较（使用默认配置）
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
     * @param source 源数据对象
     * @param target 目标数据对象
     * @param ignoreCompareFields 忽略比较的字段（支持路径匹配，如 "address.city"）
     * @return 对应属性值的比较变化
     */
    public String compare(T source, T target, List<String> ignoreCompareFields) {
        if (Objects.isNull(source) && Objects.isNull(target)) {
            return "";
        }

        List<FieldChange> changes = new ArrayList<>();
        Map<String, CompareNode> sourceMap = this.getFieldValueMapDeep(source, "", 0);
        Map<String, CompareNode> targetMap = this.getFieldValueMapDeep(target, "", 0);

        if (sourceMap.isEmpty() && targetMap.isEmpty()) {
            return "";
        }

        // 收集所有字段路径
        Set<String> allPaths = new LinkedHashSet<>();
        allPaths.addAll(sourceMap.keySet());
        allPaths.addAll(targetMap.keySet());

        for (String path : allPaths) {
            // 检查是否忽略该字段
            if (shouldIgnoreField(path, ignoreCompareFields)) {
                continue;
            }

            CompareNode sn = sourceMap.get(path);
            CompareNode tn = targetMap.get(path);

            String sv = Optional.ofNullable(sn).map(CompareNode::getFieldValue).orElse("").toString();
            String tv = Optional.ofNullable(tn).map(CompareNode::getFieldValue).orElse("").toString();
            String fieldName = Optional.ofNullable(sn).map(CompareNode::getFieldName)
                    .orElse(Optional.ofNullable(tn).map(CompareNode::getFieldName).orElse(path));

            // 处理新增字段
            if (sn == null && tn != null && hasValue(tv)) {
                changes.add(new FieldChange("ADD", fieldName, null, tv));
            }
            // 处理删除字段
            else if (sn != null && tn == null && hasValue(sv)) {
                changes.add(new FieldChange("DELETE", fieldName, sv, null));
            }
            // 处理修改字段
            else if (sn != null && tn != null && !sv.equals(tv)) {
                changes.add(new FieldChange("UPDATE", fieldName, sv, tv));
            }
        }

        return formatChanges(changes);
    }

    /**
     * 比较字段变化，返回结构化的字段变化列表
     *
     * @param source 源数据对象
     * @param target 目标数据对象
     * @param ignoreCompareFields 忽略比较的字段
     * @return 字段变化列表，每个元素包含变化详情
     */
    public List<Map<String, Object>> compareFields(T source, T target, List<String> ignoreCompareFields) {
        List<Map<String, Object>> resultList = new ArrayList<>();

        if (Objects.isNull(source) && Objects.isNull(target)) {
            return resultList;
        }

        Map<String, CompareNode> sourceMap = this.getFieldValueMapDeep(source, "", 0);
        Map<String, CompareNode> targetMap = this.getFieldValueMapDeep(target, "", 0);

        if (sourceMap.isEmpty() && targetMap.isEmpty()) {
            return resultList;
        }

        Set<String> allPaths = new LinkedHashSet<>();
        allPaths.addAll(sourceMap.keySet());
        allPaths.addAll(targetMap.keySet());

        for (String path : allPaths) {
            if (shouldIgnoreField(path, ignoreCompareFields)) {
                continue;
            }

            CompareNode sn = sourceMap.get(path);
            CompareNode tn = targetMap.get(path);

            String sv = Optional.ofNullable(sn).map(CompareNode::getFieldValue).orElse("").toString();
            String tv = Optional.ofNullable(tn).map(CompareNode::getFieldValue).orElse("").toString();
            String fieldName = Optional.ofNullable(sn).map(CompareNode::getFieldName)
                    .orElse(Optional.ofNullable(tn).map(CompareNode::getFieldName).orElse(path));

            Map<String, Object> changeInfo = new HashMap<>();

            // 处理新增字段
            if (sn == null && tn != null && hasValue(tv)) {
                changeInfo.put("changeType", "ADD");
                changeInfo.put("fieldPath", path);
                changeInfo.put("fieldName", fieldName);
                changeInfo.put("newValue", tv);
                resultList.add(changeInfo);
            }
            // 处理删除字段
            else if (sn != null && tn == null && hasValue(sv)) {
                changeInfo.put("changeType", "DELETE");
                changeInfo.put("fieldPath", path);
                changeInfo.put("fieldName", fieldName);
                changeInfo.put("oldValue", sv);
                resultList.add(changeInfo);
            }
            // 处理修改字段
            else if (sn != null && tn != null && !sv.equals(tv)) {
                changeInfo.put("changeType", "UPDATE");
                changeInfo.put("fieldPath", path);
                changeInfo.put("fieldName", fieldName);
                changeInfo.put("oldValue", sv);
                changeInfo.put("newValue", tv);
                resultList.add(changeInfo);
            }
        }

        return resultList;
    }

    /**
     * 递归获取所有字段的值（支持嵌套对象）
     *
     * @param obj 对象
     * @param parentPath 父路径
     * @param currentDepth 当前深度
     * @return 字段路径 -> CompareNode 的映射
     */
    private Map<String, CompareNode> getFieldValueMapDeep(Object obj, String parentPath, int currentDepth) {
        if (Objects.isNull(obj)) {
            return Collections.emptyMap();
        }

        // 防止无限递归
        if (currentDepth > MAX_DEPTH) {
            return Collections.emptyMap();
        }

        Map<String, CompareNode> map = new LinkedHashMap<>();
        List<FieldInfo> fieldInfos = getFieldInfos(obj.getClass());

        for (FieldInfo fieldInfo : fieldInfos) {
            Compare compareAnnotation = fieldInfo.getAnnotation();

            try {
                Object fieldValue = fieldInfo.getField().get(obj);
                String currentPath = buildPath(parentPath, fieldInfo.getField().getName());
                String currentDisplayPath = buildPath(parentPath, compareAnnotation.value());

                // 检查是否需要深度比较
                if (compareAnnotation.deep() && currentDepth < compareAnnotation.depth()) {
                    // 深度比较：递归处理嵌套对象
                    if (isNestedObject(fieldValue) && currentDepth < compareAnnotation.depth()) {
                        Map<String, CompareNode> nestedMap = getFieldValueMapDeep(fieldValue, currentDisplayPath, currentDepth + 1);
                        map.putAll(nestedMap);
                    }
                } else {
                    // 非深度比较或达到深度限制：直接添加当前字段
                    CompareNode node = new CompareNode();
                    node.setFieldKey(currentPath);
                    node.setFieldValue(fieldValue);
                    node.setFieldName(currentDisplayPath);
                    node.setFieldPath(currentPath);
                    map.put(currentPath, node);
                }

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return map;
    }

    /**
     * 获取类的字段信息（带缓存）
     */
    private List<FieldInfo> getFieldInfos(Class<?> clazz) {
        return FIELD_CACHE.computeIfAbsent(clazz, c -> {
            List<FieldInfo> fieldInfos = new ArrayList<>();
            Field[] fields = c.getDeclaredFields();

            for (Field field : fields) {
                // 跳过静态字段和transient字段
                if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                    continue;
                }

                Compare compareAnnotation = field.getAnnotation(Compare.class);
                if (Objects.isNull(compareAnnotation)) {
                    continue;
                }

                field.setAccessible(true);
                fieldInfos.add(new FieldInfo(field, compareAnnotation));
            }

            return fieldInfos;
        });
    }

    /**
     * 判断是否为嵌套对象（需要深度比较的对象）
     */
    private boolean isNestedObject(Object obj) {
        if (Objects.isNull(obj)) {
            return false;
        }

        Class<?> clazz = obj.getClass();

        // 基本类型、字符串、包装类、枚举、集合、Map、数组不视为嵌套对象
        return !clazz.isPrimitive() &&
                !clazz.equals(String.class) &&
                !clazz.isEnum() &&
                !Collection.class.isAssignableFrom(clazz) &&
                !Map.class.isAssignableFrom(clazz) &&
                !clazz.isArray() &&
                !isWrapperType(clazz) &&
                !isDateType(clazz);
    }

    /**
     * 判断是否为包装类型
     */
    private boolean isWrapperType(Class<?> clazz) {
        return clazz.equals(Integer.class) ||
                clazz.equals(Long.class) ||
                clazz.equals(Double.class) ||
                clazz.equals(Float.class) ||
                clazz.equals(Boolean.class) ||
                clazz.equals(Character.class) ||
                clazz.equals(Byte.class) ||
                clazz.equals(Short.class);
    }

    /**
     * 判断是否为日期类型
     */
    private boolean isDateType(Class<?> clazz) {
        return clazz.equals(Date.class) ||
                clazz.equals(java.sql.Date.class) ||
                clazz.equals(java.sql.Timestamp.class) ||
                clazz.equals(java.time.LocalDate.class) ||
                clazz.equals(java.time.LocalDateTime.class) ||
                clazz.equals(java.time.LocalTime.class);
    }

    /**
     * 判断值是否有效（非空且非空白字符串）
     */
    private boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * 构建路径
     */
    private String buildPath(String parent, String current) {
        if (parent == null || parent.isEmpty()) {
            return current;
        }
        return parent + PATH_SEPARATOR + current;
    }

    /**
     * 判断字段是否应该被忽略
     */
    private boolean shouldIgnoreField(String fieldPath, List<String> ignoreCompareFields) {
        if (Objects.isNull(ignoreCompareFields) || ignoreCompareFields.isEmpty()) {
            return false;
        }

        // 支持精确匹配和前缀匹配
        for (String ignoreField : ignoreCompareFields) {
            if (fieldPath.equals(ignoreField) || fieldPath.startsWith(ignoreField + PATH_SEPARATOR)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 格式化变更信息
     */
    private String formatChanges(List<FieldChange> changes) {
        if (changes.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < changes.size(); i++) {
            FieldChange change = changes.get(i);
            switch (change.type) {
                case "ADD":
                    sb.append(String.format("新增[%s：%s]", change.fieldName, change.newValue));
                    break;
                case "DELETE":
                    sb.append(String.format("删除[%s：%s]", change.fieldName, change.oldValue));
                    break;
                case "UPDATE":
                    sb.append(String.format("[%s：%s -> %s]", change.fieldName, change.oldValue, change.newValue));
                    break;
                default:
                    break;
            }
            if (i < changes.size() - 1) {
                sb.append(COMMA);
            }
        }
        return sb.toString();
    }

    /**
     * 清除字段缓存（可用于热部署时清理）
     */
    public static void clearCache() {
        FIELD_CACHE.clear();
    }

    /**
     * 字段信息内部类
     */
    private static class FieldInfo {
        private final Field field;
        private final Compare annotation;

        FieldInfo(Field field, Compare annotation) {
            this.field = field;
            this.annotation = annotation;
        }

        Field getField() {
            return field;
        }

        Compare getAnnotation() {
            return annotation;
        }
    }

    /**
     * 字段变更记录
     */
    private static class FieldChange {
        String type;
        String fieldName;
        String oldValue;
        String newValue;

        FieldChange(String type, String fieldName, String oldValue, String newValue) {
            this.type = type;
            this.fieldName = fieldName;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }
    }

    // ========== 兼容旧版本的方法 ==========

    /**
     * @deprecated 请使用 {@link #compareFields(Object, Object, List)}
     */
    @Deprecated
    public List<HashMap<String, String>> compareFiled(T source, T target, List<String> ignoreCompareFields) {
        List<HashMap<String, String>> result = new ArrayList<>();
        List<Map<String, Object>> fields = compareFields(source, target, ignoreCompareFields);

        for (Map<String, Object> field : fields) {
            HashMap<String, String> simple = new HashMap<>();
            simple.put((String) field.get("fieldPath"), (String) field.get("fieldName"));
            result.add(simple);
        }

        return result;
    }
}