package com.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.assessment.ExcelWriter;
import com.assessment.FilesSearchController;

public class View {

	private JFrame frame;
	private JPanel panel;
	private JTextField directoryPathTextField;
	private JTextField searchStringTextField;
	private JTextArea resultsTextArea;

	public View() {
		frame = new JFrame("Search Tab");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(600, 400);

		panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder("Search Tab"));
		panel.setLayout(null);

		JLabel directoryPathLabel = new JLabel("Directory Path:");
		directoryPathLabel.setBounds(20, 20, 100, 20);
		panel.add(directoryPathLabel);

		directoryPathTextField = new JTextField();
		directoryPathTextField.setBounds(130, 20, 400, 20);
		panel.add(directoryPathTextField);

		JLabel searchStringLabel = new JLabel("Search Keyword:");
		searchStringLabel.setBounds(20, 50, 100, 20);
		panel.add(searchStringLabel);

		searchStringTextField = new JTextField();
		searchStringTextField.setBounds(130, 50, 400, 20);
		panel.add(searchStringTextField);

		JButton searchButton = new JButton("Search");
		searchButton.setBounds(20, 80, 100, 20);
		panel.add(searchButton);

		resultsTextArea = new JTextArea();
		JScrollPane scrollPane = new JScrollPane(resultsTextArea);
		scrollPane.setBounds(20, 110, 1000, 500);
		panel.add(scrollPane);

		frame.add(panel);
		frame.setVisible(true);
		// Add action listener to search button
		searchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String directoryPath = directoryPathTextField.getText();
				String searchString = searchStringTextField.getText();
				
				
				try {
					ExcelWriter excelWriter = new ExcelWriter();
					String results = FilesSearchController.searchDirectory(new File(directoryPath), searchString, excelWriter);
					excelWriter.save();

					resultsTextArea.setText(results.toString());
				} catch (IllegalArgumentException ex) {
					resultsTextArea.setText("Error: " + ex.getMessage());
				} catch (IOException ex) {
					resultsTextArea.setText("Error: " + ex.getMessage());
				} catch (Exception ex) {
					resultsTextArea.setText("Error: " + ex.getMessage());
				}
			}
		});
	}

	public static void main(String[] args) {
		new View();
	}
}