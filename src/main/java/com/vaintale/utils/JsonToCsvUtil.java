package com.vaintale.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * json:{"id":1, "name":"Alice", "age":20} or [{"id":1, "name":"Alice", "age":20}, {"id":2, "name":"Bob", "age":25}]
 * outPath:输出路径
 *
 * @author vaintale
 * @date 2025/6/28
 */
public class JsonToCsvUtil {
    public static void jsonToCsv(String json, String outPath) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(json);


            // 1. 统一为List<JsonNode>处理
            List<JsonNode> nodes = new ArrayList<>();
            if (root.isArray()) {
                root.forEach(nodes::add);
            } else if (root.isObject()) {
                nodes.add(root);
            } else {
                throw new IllegalArgumentException("输入的JSON必须为对象或对象数组！");
            }

            // 2. 提取所有字段名，按出现顺序
            Set<String> fieldSet = new LinkedHashSet<>();
            for (JsonNode node : nodes) {
                node.fieldNames().forEachRemaining(fieldSet::add);
            }
            String[] headers = fieldSet.toArray(new String[0]);

            // 3. 创建CSV格式，推荐Builder写法
            CSVFormat format = CSVFormat.Builder.create()
                    .setHeader(headers)
                    .setSkipHeaderRecord(false)
                    .build();

            // 4. 写CSV
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outPath), StandardCharsets.UTF_8));
                 CSVPrinter printer = new CSVPrinter(writer, format)) {
                for (JsonNode node : nodes) {
                    List<String> row = new ArrayList<>();
                    for (String header : headers) {
                        row.add(node.has(header) ? node.get(header).asText() : "");
                    }
                    printer.printRecord(row);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("转换失败: " + e.getMessage());

        }
    }
}
