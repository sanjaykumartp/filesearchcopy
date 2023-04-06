package com.assessment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;

import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

public class FilesSearchController {

	public static String searchDirectory(File directory, String searchString, ExcelWriter excelWriter) throws IOException {
		StringBuilder resultBuilder = new StringBuilder();
		boolean searchStringFound = false; // added variable to check if search string is found in any file

		// check if directory exists
		if (!directory.exists()) {
			resultBuilder.append("Invalid directory path: " + directory.getPath());
			throw new IllegalArgumentException(resultBuilder.toString());
		}
		// check if directory is empty
		File[] files = directory.listFiles();
		if (files == null || files.length == 0) {
			resultBuilder.append("No text, doc, docx,rtf or pdf files found in : " + directory.getPath());
			throw new IllegalArgumentException(resultBuilder.toString());
		}
		// check if search string is empty
		if (searchString.trim().isEmpty() || searchString.trim().matches("[+|]+")) {
			resultBuilder.append("Please enter a valid search string.");
			throw new IllegalArgumentException(resultBuilder.toString());
		}

		for (File file : files) {
			if (file.isDirectory()) {
				resultBuilder.append(searchDirectory(file, searchString, excelWriter));
			} else {
				boolean foundInFile = searchFile(file, searchString, excelWriter); // added variable to check if search string is found in current file
				if (foundInFile) {
					searchStringFound = true;
				}
			}
		}
		if (!searchStringFound) {
		    if (searchString.matches("\\+?[0-9]+")) {
		        resultBuilder.append("Entered mobile number " + searchString + " is not present in any file.");
		    } else if (searchString.matches("[\\w.]+@[\\w.]+")) {
		        resultBuilder.append("Entered email id " + searchString + " is not present in any file.");
		    }else if (searchString.matches("[^{]*\\{[^{]*\\}[^}]*")) {
		        boolean isStringPresentInFiles = searchFile(directory, searchString, excelWriter);
		        if (isStringPresentInFiles) {
		            resultBuilder.append("Entered search string is present in all files.");
		            throw new IllegalArgumentException(resultBuilder.toString());
		        } else {
		            throw new IllegalArgumentException("Entered search string " + searchString + " is not present in any file.");
		        }
		    }
		    else {
		        resultBuilder.append("Search Keyword is not present in any file.");
		        throw new IllegalArgumentException(resultBuilder.toString());
		    }
		}
		resultBuilder.append("Search Completed ");
		return resultBuilder.toString();
	}
	private static boolean searchFile(File file, String searchString, ExcelWriter excelWriter) throws IOException {
		String fileExtension = FilenameUtils.getExtension(file.getName());
		boolean fileContainsKeywords = false;
		switch (fileExtension) {
		case "txt":
			try (BufferedReader br = new BufferedReader(new FileReader(file))) {
				StringBuilder fileContent = new StringBuilder(); // store the file content
				String firstLine = br.readLine(); // read the first line of the file
				String name = ""; // initialize the name variable
				if (firstLine != null) {
					String[] words = firstLine.trim().split("\\s+"); // split the first line by spaces
					if (words.length > 0) {
						name = words[0]; // set the name to the first word of the first line
					}
				}
				while ((firstLine = br.readLine()) != null) {
					fileContent.append(firstLine).append("\n");
				}
				String searchCriteria = "";
				boolean containsOrKeywords = false;
				
				if (searchString.equalsIgnoreCase("All")) {
					containsOrKeywords = true;
				}else {
					  String[] orKeywords = searchString.split("\\|\\|");
	                    for (String orKeyword : orKeywords) {
	                        orKeyword = orKeyword.trim().toLowerCase();
	                        String[] andKeywords = orKeyword.split("\\+");
	                        boolean containsAndKeywords = true;
	                        for (String andKeyword : andKeywords) {
	                            andKeyword = andKeyword.trim().toLowerCase();
	                            if (andKeyword.startsWith("{") && andKeyword.endsWith("}")) {
	                                String keywordList  = andKeyword.substring(1, andKeyword.length() - 1);
	                                String[] keywords = keywordList.split(",");
	                                for (String keyword : keywords) {
	                                    keyword = keyword.trim().toLowerCase();
	                                if (fileContent.toString().toLowerCase().contains(keyword)) {
	                                    // The file contains the keyword in braces, so skip it
	                                    containsAndKeywords = false;
	                                    break;
	                                }else if(!fileContent.toString().toLowerCase().contains(keyword)) {
	                                	 containsAndKeywords = false;
	                                     break;
	                                }
	                                }
	                            } else {
	                                if (!fileContent.toString().toLowerCase().contains(andKeyword)) {
	                                    containsAndKeywords = false;
	                                    break;
	                                }
	                                if (fileContent.toString().toLowerCase().contains(andKeyword)) {
	                                    searchCriteria += andKeyword + " ";
	                                }
	                            }
	                        }
	                        if (containsAndKeywords) {
	                            containsOrKeywords = true;
	                            break;
	                        }
	                    }
	                }
				searchCriteria = searchCriteria.trim();

				if (containsOrKeywords) {
					SearchResult result = new SearchResult(file.getName(), "", "", "", searchCriteria, getResumeCreatedDate(file),getResumeModifiedDate(file));
					result.setName(name); // set the name in the search result

					// Extract emails and mobile numbers from the file content
					Pattern emailPattern = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b");
					Pattern mobilePattern = Pattern.compile("\\b\\d{10}\\b");

					Matcher emailMatcher = emailPattern.matcher(fileContent.toString());
					while (emailMatcher.find()) {
						result.setEmail(emailMatcher.group());
					}

					Matcher mobileMatcher = mobilePattern.matcher(fileContent.toString());
					while (mobileMatcher.find()) {
						result.setMobileNumber(mobileMatcher.group());
					}

					result.setFileName(file.getName());
					result.setSearch_criteria(searchCriteria);
					result.setResumeCreatedDate(getResumeCreatedDate(file));
					result.setResumeModifiedDate(getResumeModifiedDate(file));
					excelWriter.addResult(result);
					fileContainsKeywords = true;
				}
			}
			break;
		case "doc":
        case "docx":
            try (FileInputStream fis = new FileInputStream(file);XWPFDocument document = new XWPFDocument(fis)) {
                XWPFWordExtractor extractor = new XWPFWordExtractor(document);
                String text = extractor.getText();
                String searchCriteria = "";
                boolean containsOrKeywords = false;
                if (searchString.equalsIgnoreCase("All")) {
                    containsOrKeywords = true;
                } else {
                    String[] orKeywords = searchString.split("\\|\\|");
                    for (String orKeyword : orKeywords) {
                        orKeyword = orKeyword.trim().toLowerCase();
                        String[] andKeywords = orKeyword.split("\\+");
                        boolean containsAndKeywords = true;
                        for (String andKeyword : andKeywords) {
                            andKeyword = andKeyword.trim().toLowerCase();
                            if (andKeyword.startsWith("{") && andKeyword.endsWith("}")) {
                                String keywordList  = andKeyword.substring(1, andKeyword.length() - 1);
                                String[] keywords = keywordList.split(",");
                                for (String keyword : keywords) {
                                    keyword = keyword.trim().toLowerCase();
                                if (text.toLowerCase().contains(keyword)) {
                                    // The file contains the keyword in braces, so skip it
                                    containsAndKeywords = false;
                                    break;
                                }else if(!text.toLowerCase().contains(keyword)) {
                                	 containsAndKeywords = false;
                                     break;
                                }
                                }
                            } else {
                                if (!text.toLowerCase().contains(andKeyword)) {
                                    containsAndKeywords = false;
                                    break;
                                }
                                if (text.toLowerCase().contains(andKeyword)) {
                                    searchCriteria += andKeyword + " ";
                                }
                            }
                        }
                        if (containsAndKeywords) {
                            containsOrKeywords = true;
                            break;
                        }
                    }
                }
                searchCriteria = searchCriteria.trim();
                if (containsOrKeywords) {
                    SearchResult result = new SearchResult(file.getName(), "", "", "", searchCriteria, getResumeCreatedDate(file), getResumeModifiedDate(file));
                    result.setFileName(file.getName());
                    result.setSearch_criteria(searchCriteria);
                    result.setResumeCreatedDate(getResumeCreatedDate(file));
                    result.setResumeModifiedDate(getResumeModifiedDate(file));
                    extractNameEmailMobile(text, result);
                    excelWriter.addResult(result);
                    fileContainsKeywords = true;
                }
            }
            break;
		case "pdf":
			try (PDDocument document = PDDocument.load(file)) {
				PDFTextStripper stripper = new PDFTextStripper();
				String text = stripper.getText(document);
				String searchCriteria = "";
				boolean containsOrKeywords = false;
				if (searchString.equalsIgnoreCase("All")) {
					containsOrKeywords = true;
				}else {
					  String[] orKeywords = searchString.split("\\|\\|");
	                    for (String orKeyword : orKeywords) {
	                        orKeyword = orKeyword.trim().toLowerCase();
	                        String[] andKeywords = orKeyword.split("\\+");
	                        boolean containsAndKeywords = true;
	                        for (String andKeyword : andKeywords) {
	                            andKeyword = andKeyword.trim().toLowerCase();
	                            if (andKeyword.startsWith("{") && andKeyword.endsWith("}")) {
	                                String keywordList  = andKeyword.substring(1, andKeyword.length() - 1);
	                                String[] keywords = keywordList.split(",");
	                                for (String keyword : keywords) {
	                                    keyword = keyword.trim().toLowerCase();
	                                if (text.toLowerCase().contains(keyword)) {
	                                    // The file contains the keyword in braces, so skip it
	                                    containsAndKeywords = false;
	                                    break;
	                                }else if(!text.toLowerCase().contains(keyword)) {
	                                	 containsAndKeywords = false;
	                                     break;
	                                }
	                                }
	                            } else {
	                                if (!text.toLowerCase().contains(andKeyword)) {
	                                    containsAndKeywords = false;
	                                    break;
	                                }
	                                if (text.toLowerCase().contains(andKeyword)) {
	                                    searchCriteria += andKeyword + " ";
	                                }
	                            }
	                        }
	                        if (containsAndKeywords) {
	                            containsOrKeywords = true;
	                            break;
	                        }
	                    }
	                }
				searchCriteria = searchCriteria.trim();
				if (containsOrKeywords) {
					SearchResult result = new SearchResult(file.getName(), "", "", "", searchCriteria, getResumeCreatedDate(file),getResumeModifiedDate(file));
					result.setFileName(file.getName());
					result.setSearch_criteria(searchCriteria);
					result.setResumeCreatedDate(getResumeCreatedDate(file));
					result.setResumeModifiedDate(getResumeModifiedDate(file));
					extractNameEmailMobile(text, result);
					excelWriter.addResult(result);
					fileContainsKeywords = true;
				}
			}
			break;
		case "rtf":
			try (InputStream is = new FileInputStream(file)) {
				RTFEditorKit rtfEditorKit = new RTFEditorKit();
				Document document = rtfEditorKit.createDefaultDocument();
				rtfEditorKit.read(is, document, 0);
				String text = document.getText(0, document.getLength());
				String searchCriteria = "";
				boolean containsOrKeywords = false;
				if (searchString.equalsIgnoreCase("All")) {
					containsOrKeywords = true;
				}else {
					  String[] orKeywords = searchString.split("\\|\\|");
	                    for (String orKeyword : orKeywords) {
	                        orKeyword = orKeyword.trim().toLowerCase();
	                        String[] andKeywords = orKeyword.split("\\+");
	                        boolean containsAndKeywords = true;
	                        for (String andKeyword : andKeywords) {
	                            andKeyword = andKeyword.trim().toLowerCase();
	                            if (andKeyword.startsWith("{") && andKeyword.endsWith("}")) {
	                                String keywordList  = andKeyword.substring(1, andKeyword.length() - 1);
	                                String[] keywords = keywordList.split(",");
	                                for (String keyword : keywords) {
	                                    keyword = keyword.trim().toLowerCase();
	                                if (text.toLowerCase().contains(keyword)) {
	                                    // The file contains the keyword in braces, so skip it
	                                    containsAndKeywords = false;
	                                    break;
	                                }else if(!text.toLowerCase().contains(keyword)) {
	                                	 containsAndKeywords = false;
	                                     break;
	                                }
	                                }
	                            } else {
	                                if (!text.toLowerCase().contains(andKeyword)) {
	                                    containsAndKeywords = false;
	                                    break;
	                                }
	                                if (text.toLowerCase().contains(andKeyword)) {
	                                    searchCriteria += andKeyword + " ";
	                                }
	                            }
	                        }
	                        if (containsAndKeywords) {
	                            containsOrKeywords = true;
	                            break;
	                        }
	                    }
	                }
				searchCriteria = searchCriteria.trim();
				if (containsOrKeywords) {
					SearchResult result = new SearchResult(file.getName(), "", "", "", searchCriteria, getResumeCreatedDate(file), getResumeModifiedDate(file));
					result.setFileName(file.getName());
					result.setSearch_criteria(searchCriteria);
					result.setResumeCreatedDate(getResumeCreatedDate(file));
					result.setResumeModifiedDate(getResumeModifiedDate(file));
					extractNameEmailMobile(text, result);
					excelWriter.addResult(result);
					fileContainsKeywords = true;
				}
			} catch (BadLocationException | IOException e) {
				e.printStackTrace();
			}
		
			break;
//		case "odt":
//            try (FileInputStream fis = new FileInputStream(file);XWPFDocument document = new XWPFDocument(fis)) {
//                ODFTextExtractor extractor = new ODFTextExtractor(document);
//                String text = extractor.getText();
//                String searchCriteria = "";
//                String[] orKeywords = searchString.split("\\|\\|");
//                boolean containsOrKeywords = false;
//                for (String orKeyword : orKeywords) {
//                    orKeyword = orKeyword.trim().toLowerCase();
//                    String[] andKeywords = orKeyword.split("\\+");
//                    boolean containsAndKeywords = true;
//                    for (String andKeyword : andKeywords) {
//                        andKeyword = andKeyword.trim().toLowerCase();
//                        if (!text.toLowerCase().contains(andKeyword)) {
//                            containsAndKeywords = false;
//                            break;
//                        }
//                        if (text.toLowerCase().contains(andKeyword)) {
//                            searchCriteria += andKeyword + " ";
//                        }
//                    }
//                    if (containsAndKeywords) {
//                        containsOrKeywords = true;
//                        break;
//                    }
//                }
//                searchCriteria = searchCriteria.trim();
//                if (containsOrKeywords) {
//                    SearchResult result = new SearchResult(file.getName(), "", "", "", searchCriteria, getResumeCreatedDate(file),getResumeModifiedDate(file));
//                    result.setFileName(file.getName());
//                    result.setSearch_criteria(searchCriteria);
//                    result.setResumeCreatedDate(getResumeCreatedDate(file));
//                    result.setResumeModifiedDate(getResumeModifiedDate(file));
//                    extractNameEmailMobile(text, result);
//                    excelWriter.addResult(result);
//                    fileContainsKeywords = true;
//                }
//            }
//            break;
		default:
			System.out.println("Unsupported file type: " + fileExtension);
			break;
		}
		return fileContainsKeywords;
	}
	/*
	 * Modified Resume date
	 */
	private static String getResumeModifiedDate(File file) throws IOException {
		Path filePath = file.toPath();
		FileTime fileTime = Files.getLastModifiedTime(filePath);
		LocalDateTime localDateTime = LocalDateTime.ofInstant(fileTime.toInstant(), ZoneId.systemDefault());
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		return localDateTime.format(formatter);
	}
	/*
	 * Created Resume date
	 */
	private static String getResumeCreatedDate(File file) throws IOException {
		Path filePath = file.toPath();
		BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
		LocalDateTime localDateTime = LocalDateTime.ofInstant(attrs.creationTime().toInstant(), ZoneId.systemDefault());
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		return localDateTime.format(formatter);
	}

	private static void extractNameEmailMobile(String text, SearchResult result) {
		String[] lines = text.split("\\r?\\n");//split using regular expression "This is the first line.", "This is the second line."
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			if (line.toLowerCase().contains("@") && line.toLowerCase().contains(".com")) {
				String[] parts = line.split("\\s+");
				for (String part : parts) {
					if (part.contains("@") && part.contains(".com")) {
						String email = part.replaceAll("[^a-zA-Z0-9@.]+", "");
						if (email.toLowerCase().startsWith("email:-")) {
							email = email.substring(7);
						}
						result.setEmail(email);
					}
				}
			}else {
				Pattern pattern = Pattern.compile("(\\d[\\s-]?){10}");
				Matcher matcher = pattern.matcher(line);
				while (matcher.find()) {
					String mobileNumber = matcher.group(0).replaceAll("\\s|-", "");
					result.setMobileNumber(mobileNumber);
				}
			}
			if (i == 0) {
				String firstLine = line.trim();
				if (firstLine.matches("(?i)^\\w+\\s*Name\\s*:(.*)$")) {
					String[] parts = firstLine.split(":\\s*")[1].trim().split("\\s+");
					StringBuilder fullName = new StringBuilder();
					for (String part : parts) {
						if (part.matches("[a-zA-Z]+")) {
							fullName.append(part).append(" ");
						}
					}
					result.setName(fullName.toString().trim());
				} else {
					String[] parts = firstLine.split("\\s+");
					StringBuilder fullName = new StringBuilder();
					for (String part : parts) {
						if (part.matches("[a-zA-Z]+")) {
							fullName.append(part).append(" ");
						}
					}
					result.setName(fullName.toString().trim());
				}
			}
		}
	}
}