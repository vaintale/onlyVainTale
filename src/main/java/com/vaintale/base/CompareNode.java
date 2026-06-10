package com.vaintale.base;

/**
 * 实体类比较节点
 * @author vaintale
 * @date 2025/2/22
 */
public class CompareNode {

    /**
     * 字段key（属性名）
     */
    private String fieldKey;

    /**
     * 字段值
     */
    private Object fieldValue;

    /**
     * 字段显示名称
     */
    private String fieldName;

    /**
     * 字段路径（用于嵌套比较）
     */
    private String fieldPath;

    public String getFieldKey() {
        return fieldKey;
    }

    public void setFieldKey(String fieldKey) {
        this.fieldKey = fieldKey;
    }

    public Object getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(Object fieldValue) {
        this.fieldValue = fieldValue;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldPath() {
        return fieldPath;
    }

    public void setFieldPath(String fieldPath) {
        this.fieldPath = fieldPath;
    }
}
