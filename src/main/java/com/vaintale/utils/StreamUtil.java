package com.vaintale.utils;

import cn.hutool.core.util.NumberUtil;
import com.google.common.base.Splitter;
import com.vaintale.base.vo.TreeVO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author vaintale
 * @date 2024/12/23
 */
public class StreamUtil {
    /**
     * 把list集合数据根据id和parentId组成树结构
     *
     * @param data
     * @return
     */
    public static List<TreeVO> dataToTree(List<TreeVO> data) {
        return data.stream().filter(item -> "0".equals(item.getParentId())).peek(
                tmp -> tmp.setChildren(getChildrens(tmp, data))
        ).collect(Collectors.toList());
    }


    /**
     * 获取当前数据的children数据
     *
     * @param vo
     * @param data
     * @return
     */
    private static List<TreeVO> getChildrens(TreeVO vo, List<TreeVO> data) {
        return data.stream().filter(item -> item.getParentId().equals(vo.getId())).peek(tmp -> tmp.setChildren(getChildrens(tmp, data))).collect(Collectors.toList());
    }

    /**
     * 字符串切割转list
     *
     * @param str
     * @return {@link List }<{@link Long }>
     * @author vaintale
     * @date 2023/06/05
     */
    public static List<Long> strToList(String str) {
        List<Long> list = Splitter.on(",").trimResults().splitToList(str)
                .stream().filter(NumberUtil::isInteger)
                .map(Long::parseLong).collect(Collectors.toList());
        return list;
    }


}
