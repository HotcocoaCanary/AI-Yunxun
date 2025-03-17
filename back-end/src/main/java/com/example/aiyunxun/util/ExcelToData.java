package com.example.aiyunxun.util;

import com.example.aiyunxun.entity.Node;
import com.example.aiyunxun.entity.Relationship;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.DateUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ExcelToData{
    private final Map<String, List<Node>> nodeMap = new HashMap<>();
    private final Map<String, List<Relationship>> relationshipMap = new HashMap<>();

    private static final String NODE_SHEET_PREFIX = "Node_";
    private static final String RELATION_SHEET_PREFIX = "Relationship_";
    private static final String ID_PROPERTY = "id";
    private static final int HEADER_ROW_INDEX = 0;

    public ExcelToData(String excelPath) {
        try (Workbook workbook = WorkbookFactory.create(new File(excelPath))) {
            // 第一阶段：处理所有节点工作表
            processSheetsByType(workbook, NODE_SHEET_PREFIX, this::processNodeSheet);
            // 第二阶段：处理所有关系工作表
            processSheetsByType(workbook, RELATION_SHEET_PREFIX, this::processRelationshipSheet);
        } catch (IOException e) {
            throw new RuntimeException("Excel处理失败: " + e.getMessage(), e);
        }
    }

    private void processSheetsByType(Workbook workbook,
                                   String sheetPrefix,
                                   Consumer<Sheet> processor) {
        workbook.sheetIterator()
            .forEachRemaining(sheet -> {
                String sheetName = sheet.getSheetName();
                if (sheetName.startsWith(sheetPrefix)) {
                    processor.accept(sheet);
                }
            });
    }

    private void processNodeSheet(Sheet sheet) {
        String[] parts = sheet.getSheetName().split("_", 2);
        if (parts.length < 2) return;

        String label = parts[1];
        nodeMap.put(label, createNodeList(label, sheet));
    }

    private void processRelationshipSheet(Sheet sheet) {
        String[] parts = sheet.getSheetName().split("_", 2);
        if (parts.length < 2) return;

        String type = parts[1];
        relationshipMap.put(type, createRelationList(type, sheet));
    }

    private List<Node> createNodeList(String label, Sheet sheet) {
        List<Row> rows = getDataRows(sheet);
        if (rows.isEmpty()) return Collections.emptyList();

        List<String> attributes = getHeaderValues(sheet.getRow(HEADER_ROW_INDEX));
        return rows.stream()
                .map(row -> createNode(label, attributes, row))
                .collect(Collectors.toList());
    }

    private Node createNode(String label, List<String> attributes, Row row) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ID_PROPERTY, generateRowBasedId(row));

        for (int i = 0; i < attributes.size(); i++) {
            Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            properties.put(attributes.get(i), getCellValue(cell));
        }

        return new Node(label, properties);
    }

    private List<Relationship> createRelationList(String type, Sheet sheet) {
        List<Row> rows = getDataRows(sheet);
        if (rows.isEmpty()) return Collections.emptyList();

        List<String> headers = getHeaderValues(sheet.getRow(HEADER_ROW_INDEX));
        List<Integer> markers = findRelationMarkers(headers, type);
        validateMarkers(markers);

        return rows.stream()
                .map(row -> processRelationRow(type, headers, markers, row))
                .collect(Collectors.toList());
    }

    private Relationship processRelationRow(String type, List<String> headers,
                                            List<Integer> markers, Row row) {
        try {
            return markers.size() == 1 ?
                    createSingleMarkerRelationship(type, headers, markers.get(0), row) :
                    createDoubleMarkerRelationship(type, headers, markers, row);
        } catch (Exception e) {
            throw new RelationProcessingException(row.getRowNum() + 1, e);
        }
    }

    private Relationship createSingleMarkerRelationship(String type, List<String> headers,
                                                        int markerIndex, Row row) {
        Node startNode = parseNode(headers, row, 0, 1, markerIndex - 1);
        Node endNode = parseNode(headers, row, markerIndex + 1, markerIndex + 2, headers.size() - 1);

        return new Relationship(type, startNode, endNode, Collections.emptyMap());
    }

    private Relationship createDoubleMarkerRelationship(String type, List<String> headers,
                                                        List<Integer> markers, Row row) {
        int firstMarker = markers.get(0);
        int secondMarker = markers.get(1);

        Node startNode = parseNode(headers, row, 0, 1, firstMarker - 1);
        Map<String, Object> relProps = extractProperties(row, firstMarker + 1, secondMarker - 1, headers);
        relProps.put(ID_PROPERTY, generateRowBasedId(row));

        Node endNode = parseNode(headers, row, secondMarker + 1, secondMarker + 2, headers.size() - 1);

        return new Relationship(type, startNode, endNode, relProps);
    }

    private Node parseNode(List<String> headers, Row row, int labelIndex,
                           int propStart, int propEnd) {
        String label = headers.get(labelIndex);
        Map<String, Object> props = extractProperties(row, propStart, propEnd, headers);
        return findNodeByProperties(label, props, row.getRowNum() + 1);
    }

    // Helper methods
    private List<Row> getDataRows(Sheet sheet) {
        List<Row> rows = new ArrayList<>();
        Iterator<Row> iterator = sheet.iterator();
        if (iterator.hasNext()) iterator.next(); // Skip header

        iterator.forEachRemaining(rows::add);
        return rows;
    }

    private List<String> getHeaderValues(Row headerRow) {
        return getCellValuesAsString(headerRow);
    }

    private String generateRowBasedId(Row row) {
        return String.valueOf(row.getRowNum() - 1);
    }

    // Existing helper methods with minor optimizations
    private List<Integer> findRelationMarkers(List<String> headers, String relationType) {
        return IntStream.range(0, headers.size())
                .filter(i -> relationType.equals(headers.get(i)))
                .boxed()
                .collect(Collectors.toList());
    }

    private Node findNodeByProperties(String label, Map<String, Object> matchProps, int rowNum) {
        List<Node> nodes = Optional.ofNullable(nodeMap.get(label))
                .orElseThrow(() -> new NodeNotFoundException(rowNum, label));

        return nodes.stream()
                .filter(node -> node.getProperties().entrySet()
                        .containsAll(matchProps.entrySet()))
                .findFirst()
                .orElseThrow(() -> new NodeNotFoundException(rowNum, label, matchProps));
    }

    // Custom exceptions
    private static class ExcelProcessingException extends RuntimeException {
        ExcelProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static class RelationProcessingException extends RuntimeException {
        RelationProcessingException(int rowNumber, Throwable cause) {
            super("Error processing row " + rowNumber, cause);
        }
    }

    private static class NodeNotFoundException extends RuntimeException {
        NodeNotFoundException(int rowNumber, String label) {
            super(String.format("Row %d: Node type [%s] not found", rowNumber, label));
        }

        NodeNotFoundException(int rowNumber, String label, Map<String, Object> props) {
            super(String.format("Row %d: No matching %s node with properties: %s",
                    rowNumber, label, props));
        }
    }

    // Remaining helper methods (getCellValue, evaluateFormulaCell, etc.)保持原样
    // Getters保持原样
    // 辅助方法：获取单元格值
    private Object getCellValue(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> DateUtil.isCellDateFormatted(cell) ?
                    cell.getDateCellValue() :
                    cell.getNumericCellValue();
            case BOOLEAN -> cell.getBooleanCellValue();
            case FORMULA -> evaluateFormulaCell(cell);
            default -> null;
        };
    }

    // 辅助方法：处理公式单元格
    private Object evaluateFormulaCell(Cell cell) {
        try {
            return switch (cell.getCachedFormulaResultType()) {
                case STRING -> cell.getStringCellValue();
                case NUMERIC -> DateUtil.isCellDateFormatted(cell) ?
                        cell.getDateCellValue() :
                        cell.getNumericCellValue();
                case BOOLEAN -> cell.getBooleanCellValue();
                default -> null;
            };
        } catch (Exception e) {
            return null;
        }
    }

    // 验证标记列数量
    private void validateMarkers(List<Integer> markers) {
        if (markers.size() != 1 && markers.size() != 2) {
            throw new IllegalArgumentException("关系类型标记列数量错误，应为1或2个，实际找到：" + markers.size());
        }
    }

    // 通用属性提取
    private Map<String, Object> extractProperties(Row row, int startCol, int endCol, List<String> headers) {
        Map<String, Object> props = new HashMap<>();
        for (int i = startCol; i <= endCol; i++) {
            Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            props.put(headers.get(i), getCellValue(cell));
        }
        return props;
    }

    // 辅助方法：获取整行字符串值
    private List<String> getCellValuesAsString(Row row) {
        List<String> values = new ArrayList<>();
        for (Cell cell : row) {
            values.add(getCellStringValue(cell));
        }
        return values;
    }

    // 辅助方法：获取单元格字符串值
    private String getCellStringValue(Cell cell) {
        Object value = getCellValue(cell);
        return value != null ? value.toString() : "";
    }

    public Map<String, List<Node>> getNodeMap() {
        return Collections.unmodifiableMap(nodeMap);
    }
    public Map<String, List<Relationship>> getRelationshipMap() {
        return Collections.unmodifiableMap(relationshipMap);
    }
}