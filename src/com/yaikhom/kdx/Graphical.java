/*
 * This file is part of the kdxgen project (http://kdxgen.sourceforge.net)
 * 
 * Copyright (c) 2010 Gagarine Yaikhom
 * 
 * 
 * All programs in this directory and subdirectories are published under the GNU
 * General Public License as described below.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Further information about the GNU GPL is available at:
 * http://www.gnu.org/copyleft/gpl.ja.html
 */

package com.yaikhom.kdx;

import java.io.*;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.logging.Logger;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

/*
 * Graphical User Interface for selecting Kindle device.
 */
public class Graphical extends JPanel implements ActionListener {
	private static final Logger logger = Logger.getLogger("com.yaikhom.kdx");

	private static final long serialVersionUID = 1L;
	private JFrame frame;
	private JButton openButton, saveButton, saveAsButton, exitButton;
	private JEditorPane help;
	private JTree collTree;
	private JScrollPane scrollPane;
	private Manager kdxm;
	private String kdxRootPath;

	public Graphical() {
		super(new BorderLayout());

		// Help message and introduction.
		help = new JEditorPane();
		help.setEditable(false);
		HTMLEditorKit kit = new HTMLEditorKit();
		help.setEditorKit(kit);
		StyleSheet style = kit.getStyleSheet();
		style.addRule("body{color:#000;font-family:arial;margin:10px;}");
		style.addRule("h1{color:blue;font-size:16pt;margin-bottom:0px;}");
		style.addRule("ol{list-style-position:inside;margin-left:20px;}");
		style.addRule("ul{list-style-position:inside;margin-left:20px;}");
		style.addRule("p{padding-bottom:10px;}");
		style.addRule("tt{color:#610B0B;font-family:courier;font-weight:bold;}");

		URL helpURL = Graphical.class.getResource("resources/intro.html");
		if (helpURL == null) {
			logger.warning("Couldn't find introduction file" + helpURL);
		} else {
			try {
				help.setPage(helpURL);
			} catch (IOException e) {
				logger.warning("Invalid introduction file URL");
			}
		}
		scrollPane = new JScrollPane(help);
		scrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setPreferredSize(new Dimension(600, 400));
		scrollPane.setMinimumSize(new Dimension(200, 100));

		// Open Kindle root directory.
		openButton = createButton("Open Kindle", "open", true);

		// Save as Kindle collection file.
		saveButton = createButton("Save to Kindle", "save", false);

		// Save elsewhere (prompts user).
		saveAsButton = createButton("Save as file", "saveas", false);

		// Exit application (prompts user).
		exitButton = createButton("Exit", "exit", true);

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(openButton);
		buttonPanel.add(saveButton);
		buttonPanel.add(saveAsButton);
		buttonPanel.add(exitButton);

		add(buttonPanel, BorderLayout.PAGE_START);
		add(scrollPane, BorderLayout.CENTER);
	}

	/**
	 * Get image icon from resources.
	 * 
	 * @param name
	 *            name of the action
	 * @return icon image which corresponds to the image file
	 */
	private ImageIcon getIcon(String name) {
		URL url = Graphical.class.getResource("resources/" + name + ".png");
		if (url == null) {
			logger.warning("Couldn't find icon" + url);
			return null;
		} else {
			return new ImageIcon(url);
		}
	}

	/**
	 * Create a button using icon and text.
	 * 
	 * @param label
	 *            label to display on button
	 * @param action
	 *            icon to display on button
	 *            @param isEnabled
	 *            true if the button should be enables; false otherwise
	 * @return a button with icon and text
	 */
	private JButton createButton(String label, String action, boolean isEnabled) {
		JButton b = new JButton(label);
		ImageIcon i = getIcon(action);
		if (i != null)
			b.setIcon(i);
		b.setEnabled(isEnabled);
		b.addActionListener(this);
		return b;
	}

	/**
	 * Invoked when the 'Open Kindle' button is pressed.
	 */
	private void actionOpen() {
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = fc.showOpenDialog(Graphical.this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			kdxRootPath = file.getAbsolutePath();
			logger.info("Opening Kindle directory " + kdxRootPath);
			try {
				kdxm = new Manager(kdxRootPath, false);
				try {
					if (kdxm.process()) {
						collTree = kdxm.getCollectionTree();
						scrollPane.remove(help);
						scrollPane.add(collTree);
						scrollPane.setViewportView(collTree);
						saveButton.setEnabled(true);
						saveAsButton.setEnabled(true);
					} else {
						if (collTree != null) {
							scrollPane.remove(collTree);
							scrollPane.add(help);
							scrollPane.setViewportView(help);
							saveButton.setEnabled(false);
							saveAsButton.setEnabled(false);
						}
						JOptionPane.showMessageDialog(frame,
								"Supplied path is not a Kindle "
										+ "device root directory.",
								"Invalid Kindle device",
								JOptionPane.ERROR_MESSAGE);
					}
				} catch (NoSuchAlgorithmException ex) {
					ex.printStackTrace();
				}
			} catch (SecurityException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		} else {
			logger.info("Open command cancelled by user.");
		}
	}

	/**
	 * Invoked when the 'Save to Kindle' button is pressed.
	 */
	private void actionSave() {
		String collPath = kdxRootPath + "/system/collections.json";
		File oldColl = new File(collPath);
		if (oldColl.exists()) {
			File bakColl = new File(collPath + "." + (new Date()).getTime());
			if (!oldColl.renameTo(bakColl)) {
				logger.warning("Failed to save existing collection.");
				int response = JOptionPane
						.showConfirmDialog(frame,
								"Failed to save existing collection. "
										+ "Do you wish to continue?",
								"Saving to Kindle device",
								JOptionPane.OK_CANCEL_OPTION);
				if (response == JOptionPane.CANCEL_OPTION) {
					logger.warning("Save to Kindle device cancelled.");
					return;
				}
			}
		}
		try {
			kdxm.save(collPath);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		JOptionPane.showMessageDialog(frame,
				"Collection successfully saved to Kindle device.",
				"Saving to Kindle device", JOptionPane.INFORMATION_MESSAGE);
		logger.info("Collection successfully saved to Kindle device.");
	}

	/**
	 * Invoked when the 'Save as file' button is pressed.
	 */
	private void actionSaveAs() {
		JFileChooser fc = new JFileChooser();
		int returnVal = fc.showSaveDialog(Graphical.this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			try {
				kdxm.save(file.getAbsolutePath());
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			JOptionPane.showMessageDialog(frame,
					"Collection successfully saved to file" + file.getName()
							+ ".", "Saving to Kindle device",
					JOptionPane.INFORMATION_MESSAGE);
			logger.info("Collection successfully saved to file "
					+ file.getName());
		} else {
			logger.info("Save command cancelled by user.");
		}
	}

	/**
	 * This is called when application event is triggered.
	 * 
	 * @param e
	 *            event object.
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == openButton) {
			actionOpen();
			return;
		}
		if (e.getSource() == saveButton) {
			actionSave();
			return;
		}
		if (e.getSource() == saveAsButton) {
			actionSaveAs();
		}
		if (e.getSource() == exitButton) {
			logger.info("Exiting application...");
			System.exit(0);
		}
	}

	/**
	 * Creates the GUI for selecting the KDX device.
	 */
	private void selectKindleDevice() {
		frame = new JFrame("KDX Collection Generator");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new Graphical());
		frame.pack();
		frame.setVisible(true);
	}

	public void start() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				UIManager.put("swing.boldMetal", Boolean.FALSE);
				selectKindleDevice();
			}
		});
	}
}
// Created 07 January 2011, 5:45pm