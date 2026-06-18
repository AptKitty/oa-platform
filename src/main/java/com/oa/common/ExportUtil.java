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

    public static void exportToPdf(JTable table, String title) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File(title + ".pdf"));
        if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) return;
        try {
            com.itextpdf.text.Document doc = new com.itextpdf.text.Document();
            com.itextpdf.text.pdf.PdfWriter.getInstance(doc, new FileOutputStream(chooser.getSelectedFile()));
            doc.open();
            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 16, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font cellFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10);

            doc.add(new com.itextpdf.text.Paragraph(title, titleFont));
            doc.add(new com.itextpdf.text.Paragraph(" "));

            DefaultTableModel model = (DefaultTableModel) table.getModel();
            int cols = model.getColumnCount();
            com.itextpdf.text.pdf.PdfPTable pdfTable = new com.itextpdf.text.pdf.PdfPTable(cols);
            pdfTable.setWidthPercentage(100);

            for (int c = 0; c < cols; c++)
                pdfTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(model.getColumnName(c), headerFont)));
            for (int r = 0; r < model.getRowCount(); r++)
                for (int c = 0; c < cols; c++) {
                    Object val = model.getValueAt(r, c);
                    pdfTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(val != null ? val.toString() : "", cellFont)));
                }

            doc.add(pdfTable);
            doc.close();
            JOptionPane.showMessageDialog(null, "PDF\u5bfc\u51fa\u6210\u529f");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "PDF\u5bfc\u51fa\u5931\u8d25: " + e.getMessage());
        }
    }
}