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
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.logging.Logger;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/*
 * Graphical User Interface for selecting Kindle device.
 */
public class Graphical extends JPanel implements ActionListener {
	private static final Logger logger = Logger.getLogger("com.yaikhom.kdx");

	private static final long serialVersionUID = 1L;
	private JButton openButton, saveButton, saveAsButton;
	private JTextArea message;
	private JTree collTree;
	private JScrollPane scrollPane;
	private Manager kdxm;
	private String kdxRootPath;

	public Graphical() {
		super(new BorderLayout());
		message = new JTextArea(5, 20);
		message.setText("Empty collection...");
		scrollPane = new JScrollPane(message);

		// Open Kindle root directory.
		openButton = new JButton("Open Kindle");
		openButton.addActionListener(this);

		// Save as Kindle collection file.
		saveButton = new JButton("Save to Kindle");
		saveButton.setEnabled(false);
		saveButton.addActionListener(this);

		// Save elsewhere (prompts user).
		saveAsButton = new JButton("Save as file");
		saveAsButton.setEnabled(false);
		saveAsButton.addActionListener(this);

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(openButton);
		buttonPanel.add(saveButton);
		buttonPanel.add(saveAsButton);

		add(buttonPanel, BorderLayout.PAGE_START);
		add(scrollPane, BorderLayout.CENTER);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == openButton) {
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
							scrollPane.remove(message);
							scrollPane.add(collTree);
							scrollPane.setViewportView(collTree);
							saveButton.setEnabled(true);
							saveAsButton.setEnabled(true);
						} else {
							if (collTree != null) {
								scrollPane.remove(collTree);
								scrollPane.add(message);
								scrollPane.setViewportView(message);
								saveButton.setEnabled(false);
								saveAsButton.setEnabled(false);
							}
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
		if (e.getSource() == saveButton) {
			String collPath = kdxRootPath + "/system/collections.json";
			File oldColl = new File(collPath);
			if (oldColl.exists()) {
				File bakColl = new File(collPath + "." + (new Date()).getTime());
				if (!oldColl.renameTo(bakColl)) {
					logger.warning("Failed to save existing collection.");
				}
			}
			try {
				kdxm.save(collPath);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			logger.info("Collection successfully saved to Kindle.");
		}
		if (e.getSource() == saveAsButton) {
			JFileChooser fc = new JFileChooser();
			int returnVal = fc.showSaveDialog(Graphical.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				try {
					kdxm.save(file.getAbsolutePath());
				} catch (IOException ex) {
					ex.printStackTrace();
				}
				logger.info("Collection saved successfully to file "
						+ file.getName());
			} else {
				logger.info("Save command cancelled by user.");
			}
		}
	}

	/**
	 * Creates the GUI for selecting the KDX device.
	 */
	private static void selectKindleDevice() {
		JFrame frame = new JFrame("KDX Collection Generator");
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
//Created 07 January 2011, 5:45pm