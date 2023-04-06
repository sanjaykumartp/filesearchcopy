package com.assessment;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.swing.JOptionPane;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelWriter {
	private Workbook workbook;
	private Sheet sheet;
	private int rowNum;
	HttpServletResponse response;
	public ExcelWriter() {
		workbook = new XSSFWorkbook();
		sheet = workbook.createSheet();
		Row headerRow = sheet.createRow(0);
		headerRow.createCell(0).setCellValue("Sl_no");
		headerRow.createCell(1).setCellValue("File Name");
		headerRow.createCell(2).setCellValue("Name");
		headerRow.createCell(3).setCellValue("Email");
		headerRow.createCell(4).setCellValue("Mobile Number");
		headerRow.createCell(5).setCellValue("Search Criteria");
		headerRow.createCell(6).setCellValue("Resume CreatedDate");
		headerRow.createCell(7).setCellValue("Resume ModifiedDate");
		rowNum = 1;
		SearchResult.setCounter(0);
	}

	public void addResult(SearchResult result) {
		Row row = sheet.createRow(rowNum++);
		row.createCell(0).setCellValue(result.getSl_no());
		row.createCell(1).setCellValue(result.getFileName());
		row.createCell(2).setCellValue(result.getName());
		row.createCell(3).setCellValue(result.getEmail());
		row.createCell(4).setCellValue(result.getMobileNumber());
		row.createCell(5).setCellValue(result.getSearch_criteria());
		row.createCell(6).setCellValue(result.getResumeCreatedDate());
		row.createCell(7).setCellValue(result.getResumeModifiedDate());
	}

	public void save() throws IOException {
		String downloadDir = System.getProperty("user.home") + "/Downloads/";
		String fileName = "Result_" + System.currentTimeMillis() + ".xlsx";
		String filePath = downloadDir + fileName;
		FileOutputStream outputStream = new FileOutputStream(filePath);
		workbook.write(outputStream);
		// Show message box
		int option = JOptionPane.showConfirmDialog(null, "Excel file saved to: " + filePath + ". Do you want to open it?", "File Saved", JOptionPane.YES_NO_OPTION);
		if (option == JOptionPane.YES_OPTION) {
			// Open the file
			File file = new File(filePath);
			try {
				Desktop.getDesktop().open(file);
			} catch (IOException e) {
				System.err.println("Error opening file: " + e.getMessage());
			}
		}
		workbook.close();
		System.out.println("Excel file saved to: " + filePath);
	}
}