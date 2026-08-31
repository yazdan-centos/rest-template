package org.mapnaom.resttemplate.service;

import org.apache.poi.ss.usermodel.*;

import java.util.Locale;

public final class ExcelSupport {
    private ExcelSupport() {}

    public static String text(Cell cell) {
        if (cell == null) return "";
        DataFormatter formatter = new DataFormatter(Locale.ROOT);
        return formatter.formatCellValue(cell).trim();
    }

    public static String normalized(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT)
                .replace(" ", "").replace("_", "").replace("-", "");
    }

    public static int column(Row header, String... names) {
        for (Cell cell : header) {
            String actual = normalized(text(cell));
            for (String name : names) {
                if (actual.equals(normalized(name))) return cell.getColumnIndex();
            }
        }
        return -1;
    }

    public static String required(Row row, int index, String label) {
        String value = text(index < 0 ? null : row.getCell(index));
        if (value.isBlank()) throw new IllegalArgumentException("Missing required Excel value: " + label);
        return value;
    }
}
