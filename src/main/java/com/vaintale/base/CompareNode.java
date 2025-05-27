package com.vaintale.base;

/**
 * 实体类比较节点
 * @author vaintale
 * @date 2025/2/22
 */
public class CompareNode {

    /**
     * 字段
     */
    private String fieldKey;

    /**
     * 字段值
     */
    private Object fieldValue;

    /**
     * 字段名称
     */
    private String fieldName;

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

}
