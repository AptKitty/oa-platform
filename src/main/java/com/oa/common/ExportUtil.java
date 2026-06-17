package com.oa.common;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.util.List;
import java.util.function.Function;

public class ExportUtil {

    public static void exportToExcel(JTable table, String title) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File(title + ".xlsx"));
        if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) return;
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet(title);
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            Row header = sheet.createRow(0);
            for (int c = 0; c < model.getColumnCount(); c++)
                header.createCell(c).setCellValue(model.getColumnName(c));
            for (int r = 0; r < model.getRowCount(); r++) {
                Row row = sheet.createRow(r + 1);
                for (int c = 0; c < model.getColumnCount(); c++) {
                    Object val = model.getValueAt(r, c);
                    if (val != null) row.createCell(c).setCellValue(val.toString());
                }
            }
            try (FileOutputStream fos = new FileOutputStream(chooser.getSelectedFile())) {
                wb.write(fos);
            }
            JOptionPane.showMessageDialog(null, "导出成功");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "导出失败: " + e.getMessage());
        }
    }

    public static <T> void importFromExcel(File file, Function<String[], T> rowMapper, List<T> resultList) {
        try (FileInputStream fis = new FileInputStream(file);
             Workbook wb = file.getName().endsWith(".xlsx") ? new XSSFWorkbook(fis) : new HSSFWorkbook(fis)) {
            Sheet sheet = wb.getSheetAt(0);
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                String[] cells = new String[row.getLastCellNum()];
                boolean empty = true;
                for (int c = 0; c < cells.length; c++) {
                    Cell cell = row.getCell(c);
                    cells[c] = cell != null ? cell.toString().trim() : "";
                    if (!cells[c].isEmpty()) empty = false;
                }
                if (!empty) resultList.add(rowMapper.apply(cells));
            }
        } catch (Exception e) {
            throw new RuntimeException("导入失败: " + e.getMessage(), e);
        }
    }
}