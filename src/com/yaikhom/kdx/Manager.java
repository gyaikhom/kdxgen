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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import com.yaikhom.kdx.Collection;

/**
 * Encapsulates a KDX collection manager.
 * 
 * <p>
 * <b>TODO:</b> The current document meta-data retrieval process, which uses the
 * filename instead of the document headers, is unreliable. In future updates,
 * this will be modified so that the information is retrieved directly from the
 * meta-data file header. Further information available at:
 * 
 * <p>
 * {@code http://wiki.mobileread.com/wiki/E-book_formats}
 * 
 * 
 * @author gyaikhom
 */
public class Manager {
	private static final Logger logger = Logger.getLogger("com.yaikhom.kdx");

	/*
	 * This HashMap contains all of the collections. They will be used when
	 * generating collections.json
	 */
	private HashMap<String, Collection> collections;
	private String kdxRootPath; // Path to the Kindle device root directory
	private Checksum checksum; // For KDX checksum calculations
	private int maxlengthCollectionName;
	private boolean cli; // true of command line; false if GUI
	SortedSet<String> sortedCollection;

	/**
	 * Default maximum number of characters allowed in collection names.
	 */
	public static final int maxKDXDisplayLen = 48; // Fits nicely within KDX

	/**
	 * This returns all of the collections, as required by the KDX
	 * collections.json file. This file should be copied (or replace)
	 * collections.json file in the {@code system/} directory of KDX mount.
	 * 
	 * @return a JSON string.
	 * @see Collection
	 * @see Checksum
	 */
	@Override
	public String toString() {
		Iterator<String> i = sortedCollection.iterator();
		StringBuffer buf = new StringBuffer("{");
		if (i.hasNext()) {
			Collection c = collections.get(i.next());
			buf.append(c.toString());
			while (i.hasNext()) {
				c = collections.get(i.next());
				buf.append("," + c.toString());
			}
		}
		buf.append("}");
		return buf.toString();
	}

	/**
	 * Get collection names.
	 * 
	 * @return Names of collections
	 */
	public String getCollectionNames() {
		Iterator<String> i = sortedCollection.iterator();
		StringBuffer buf = new StringBuffer();
		if (i.hasNext()) {
			Collection c = collections.get(i.next());
			buf.append(c.getName());
			while (i.hasNext()) {
				c = collections.get(i.next());
				buf.append("\n" + c.getName());
			}
		}
		return buf.toString();
	}

	/**
	 * Get collection tree (collections and their files).
	 * 
	 * @return A two-level collection tree
	 */
	public JTree getCollectionTree() {
		Iterator<String> i = sortedCollection.iterator();
		DefaultMutableTreeNode kdxc = new DefaultMutableTreeNode(
				"The Kindle Collection");
		if (i.hasNext()) {
			Collection c = collections.get(i.next());
			kdxc.add(c.getCollectionNode());
			while (i.hasNext()) {
				c = collections.get(i.next());
				kdxc.add(c.getCollectionNode());
			}
		}
		JTree tree = new JTree(kdxc);
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		return tree;
	}

	/**
	 * This checks if the name of the file has the supplied extension. We assume
	 * that the extension is the text after the last dot '.', if it exists. If
	 * there is no '.', this method returns false.
	 * 
	 * @param file
	 *            the name of the file.
	 * @param extGroup
	 *            a list of strings that form an extension group.
	 * @return true if the filename has the supplied extension; otherwise,
	 *         false.
	 */
	private boolean hasExtension(File file, ArrayList<String> extGroup) {
		boolean is = false;
		if (extGroup != null && extGroup.size() > 0) {
			String fname = file.getName();
			int i = fname.lastIndexOf('.');
			if (i != -1) {
				String e = fname.substring(i + 1);
				Iterator<String> j = extGroup.iterator();
				while (j.hasNext()) {
					if (is = e.equals(j.next()))
						break;
				}
			}
		}
		return is;
	}

	/**
	 * This processes a PDF file for inclusion in the KDX collection.
	 * 
	 * @param file
	 *            the file to process.
	 * @return the item representing the document within the collection.
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchAlgorithmException
	 */
	private Item processPDF(File file, String currentDir)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		StringBuffer temp = new StringBuffer();
		temp.append(currentDir);
		temp.append(file.getName());
		String key = checksum.getKDXFilenameHash(temp.toString());
		if (key == null)
			return null;
		else {
			Item item = new Item();
			item.setType(Item.PDF_FILE);
			item.setName(file.getName());
			item.setPath(file.getPath());
			item.setKey("*" + key); // KDX format requires '*' prefixing.
			return item;
		}
	}

	/**
	 * This processes a AZW file for inclusion in the KDX collection.
	 * 
	 * @param file
	 *            the file to process.
	 * @return the item representing the document within the collection.
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchAlgorithmException
	 */
	private Item processAZW(File file) throws NoSuchAlgorithmException,
			UnsupportedEncodingException {
		String key = null;
		String fname = file.getName();
		String[] temp1 = fname.split("-asin_");
		if (temp1.length > 1) {
			String[] temp2 = temp1[1].split("-type_");
			if (temp2.length > 1) {
				String asin = temp2[0];
				temp1 = temp2[1].split("-v_");
				if (temp1.length > 1) {
					String type = temp1[0];

					// We only collection proper KDX ebooks.
					// Please note that KDX automatically collects
					// NWPR in the 'periodicals' collection.
					if ("EBOK".equals(type) || "EBSP".equals(type)) {
						// KDX format requires ASIN and TYPE
						key = asin + "^" + type;
					}
				} else {
					logger.info("No version found in '" + fname
							+ "'. Skipping file...");
				}
			} else {
				logger.info("No book type found in '" + fname
						+ "'. Skipping file...");
			}
		} else {
			logger.info("No ASIN found in '" + fname + "'. Skipping file...");
		}
		if (key == null)
			return null;
		else {
			Item item = new Item();
			item.setType(Item.AZW_FILE);
			item.setName(file.getName());
			item.setPath(file.getPath());
			item.setKey("#" + key); // KDX format requires '#' prefixing.
			return item;
		}
	}

	/**
	 * Checks if the file is a PDF file.
	 * 
	 * @param file
	 *            the file to check.
	 * @return true if pdf file; false otherwise.
	 */
	private boolean isPDF(File file) {
		ArrayList<String> extGroup = new ArrayList<String>();
		extGroup.add("pdf");
		extGroup.add("PDF");
		return hasExtension(file, extGroup);
	}

	/**
	 * Checks if the file is an AZW file.
	 * 
	 * @param file
	 *            the file to check.
	 * @return true if azw file; false otherwise.
	 */
	private boolean isAZW(File file) {
		ArrayList<String> extGroup = new ArrayList<String>();
		extGroup.add("azw");
		extGroup.add("azw1");
		extGroup.add("AZW");
		extGroup.add("AZW1");
		return hasExtension(file, extGroup);
	}

	/**
	 * Generates a collection name from the supplied directory.
	 * 
	 * @param currentDir
	 *            the directory path to process.
	 * @return a collection name which corresponds to the directory.
	 */
	private String getCollectionName(String currentDir) {
		if (currentDir != null) {
			int l = currentDir.length();
			if (l > maxlengthCollectionName) {
				currentDir = currentDir.substring(0,
						maxlengthCollectionName - 3);
				currentDir += "...";
				logger.info("Collection name too long. Shortening to '"
						+ currentDir + "' ...");
			} else {
				// remove trailing '/' from collection name
				int i = currentDir.lastIndexOf('/');
				if (i != -1 && i == (l - 1)) {
					currentDir = currentDir.substring(0, i);
				}
			}
		}
		return currentDir;
	}

	/**
	 * This processes a file for inclusion in the KDX collection. The name of
	 * the collection is determined by a path relative to the {@code documents/}
	 * directory at KDX mount point.
	 * 
	 * @param file
	 *            the file to process.
	 * @param currentDir
	 *            the current directory being processed.
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 * @throws SecurityException
	 */
	private int processFile(File file, String currentDir)
			throws NoSuchAlgorithmException, SecurityException, IOException {
		Item item = null;
		if (isPDF(file)) {
			item = processPDF(file, currentDir);
		} else {
			if (isAZW(file)) {
				item = processAZW(file);
			}
		}
		if (item != null) {
			String collectionName = getCollectionName(currentDir);
			if (collectionName != null) {
				Collection currentCollection = collections.get(collectionName);
				if (currentCollection == null) {
					currentCollection = new Collection();
					currentCollection.setName(collectionName);
				}
				currentCollection.addItem(item);
				collections.put(collectionName, currentCollection);
			}
		}
		return 0;
	}

	/**
	 * Process the file, or processes all of the files and directories under the
	 * supplied directory.
	 * 
	 * @param file
	 *            the current file, or directory to process.
	 * @param basepath
	 *            the current path relative to KDX mount point.
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	private void processFileTree(File file, String basepath)
			throws IOException, NoSuchAlgorithmException {
		if (file.isDirectory()) {
			StringBuffer buf = new StringBuffer(basepath);
			buf.append(file.getName());
			buf.append('/');
			String[] children = file.list();
			if (children != null) {
				for (int i = 0; i < children.length; i++) {
					processFileTree(new File(file, children[i]), buf.toString());
				}
			}
		} else {
			processFile(file, basepath);
		}
	}

	/**
	 * Processes the root directory. We treat ebooks at the root as
	 * uncollectible, and hence, are not considered for inclusion in
	 * collections. This keeps the collection name shorter.
	 * 
	 * @param docsRootPath
	 *            the path to documents root directory
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	private void processRoot(String docsRootPath)
			throws NoSuchAlgorithmException, IOException {
		File docsRoot = new File(docsRootPath);
		String[] children = docsRoot.list();
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				File f = new File(docsRoot, children[i]);
				if (f.isDirectory())
					processFileTree(f, "");
			}
		}
	}

	/**
	 * Checks if the supplied directory is a Kindle device file system.
	 * 
	 * @param file
	 *            the Kindle device mount point.
	 * @return returns true if the mount point has Kindle device file system;
	 *         otherwise false.
	 */
	private boolean isKindleFS(File file) {
		byte v = 0x00;
		String[] children = file.list();
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				File f = new File(file, children[i]);
				if (f.isDirectory()) {
					if (((v & 0x01) == 0) && "audible".equals(children[i])) {
						v |= 0x01;
						continue;
					}
					if (((v & 0x02) == 0) && "documents".equals(children[i])) {
						v |= 0x02;
						continue;
					}
					if (((v & 0x04) == 0) && "music".equals(children[i])) {
						v |= 0x04;
						continue;
					}
					if (((v & 0x08) == 0) && "system".equals(children[i])) {
						v |= 0x08;
						continue;
					}
				}
			}
		}
		return (v == 0x0F);
	}

	/**
	 * This processes the Kindle root directory supplied by the user.
	 * 
	 * @return a JSON string in KDX collections format.
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public boolean process() throws NoSuchAlgorithmException, IOException {
		File kdxRoot = new File(kdxRootPath);
		if (!kdxRoot.isDirectory()) {
			logger.severe("Supplied path '" + kdxRoot.getPath()
					+ "' is not a directory.");
			if (cli)
				System.exit(1);
			else
				return false;
		}

		if (isKindleFS(kdxRoot)) {
			logger.info("Kindle device loaded...");
		} else {
			logger.severe("Supplied path is not a Kindle "
					+ "device root directory...");
			if (cli)
				System.exit(1);
			else
				return false;

		}
		processRoot(kdxRootPath + "/documents");
		sortedCollection = new TreeSet<String>(collections.keySet());
		return true;
	}

	/**
	 * Save the collection to a file.
	 * 
	 * @param outputFile
	 *            the output file to write to
	 * @throws IOException
	 */
	public void save(String outputFile) throws IOException {
		if (outputFile != null && outputFile.length() > 0) {
			FileWriter fstream = new FileWriter(outputFile);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(this.toString());
			out.flush();
		} else {
			System.out.print(this.toString());
		}
	}

	/**
	 * Initialises a KDX collection manager for the supplied directory.
	 * 
	 * @param path
	 *            the path to the documents
	 * @param cli
	 *            true if command line; false if GUI
	 * @throws IOException
	 * @throws SecurityException
	 */
	public Manager(String path, boolean cli) throws SecurityException,
			IOException {
		collections = new HashMap<String, Collection>();
		checksum = new Checksum();
		kdxRootPath = path;
		this.cli = cli;
		maxlengthCollectionName = maxKDXDisplayLen;
	}

	/**
	 * Initialises a KDX collection manager for the supplied directory with
	 * collection name length specification. This also specifies the maximum
	 * length (number of characters) that is allowed for collection name.
	 * 
	 * @param path
	 *            the path to the documents.
	 * @param maxlen
	 *            the maximum length of a collection name.
	 * @param cli
	 *            true if command line; false if GUI
	 * @throws IOException
	 * @throws SecurityException
	 */
	public Manager(String path, int maxlen, boolean cli)
			throws SecurityException, IOException {
		collections = new HashMap<String, Collection>();
		checksum = new Checksum();
		kdxRootPath = path;
		this.cli = cli;
		maxlengthCollectionName = maxlen;
		if (maxlen > maxKDXDisplayLen) {
			logger.info("Collection name too long; "
					+ "may not display properly on KDX.");
		}
	}
}
// Created 24 October 2010, 7:50pm