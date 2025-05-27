package com.vaintale.base.vo;

/**
 * @author vaintale
 * @date 2024/12/23
 */


import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 树形结构VO
 *
 * @author VainTale
 */
public class TreeVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 5345100859882702740L;
    private String id;
    private String parentId;
    private String name;
    private List<TreeVO> children;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TreeVO> getChildren() {
        return children;
    }

    public void setChildren(List<TreeVO> children) {
        this.children = children;
    }

    public TreeVO(String id, String parentId, String name, List<TreeVO> children) {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        this.children = children;
    }

    public TreeVO() {
    }
}

