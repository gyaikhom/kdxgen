/*
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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;
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
	private String documentsDirectory; // Path to the documents
	private Checksum checksum; // For KDX checksum calculations
	private int maxlengthCollectionName;

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
		Set<String> set = collections.keySet();
		Iterator<String> i = set.iterator();
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
	 * @return the item key for indexing the document within the collection.
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchAlgorithmException
	 */
	private String processPDF(File file, String currentDir)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		StringBuffer temp = new StringBuffer();
		temp.append(currentDir);
		temp.append("/");
		temp.append(file.getName());
		String itemKey = checksum.getKDXFilenameHash(temp.toString());
		if (itemKey == null)
			return null;
		else
			return "*" + itemKey; // KDX format requires '*' prefixing.
	}

	/**
	 * This processes a AZW file for inclusion in the KDX collection.
	 * 
	 * @param file
	 *            the file to process.
	 * @return the item key for indexing the document within the collection.
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchAlgorithmException
	 */
	private String processAZW(File file) throws NoSuchAlgorithmException,
			UnsupportedEncodingException {
		String itemKey = null;
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
						itemKey = asin + "^" + type;
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
		if (itemKey == null)
			return null;
		else
			return "#" + itemKey; // KDX format requires '#' prefixing.
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
		String itemKey = null;
		if (isPDF(file)) {
			itemKey = processPDF(file, currentDir);
		} else {
			if (isAZW(file)) {
				itemKey = processAZW(file);
			}
		}
		if (itemKey != null) {
			Collection currentCollection = collections.get(currentDir);
			if (currentCollection == null) {
				currentCollection = new Collection();
				currentCollection.setName(currentDir);
			}
			currentCollection.addItem(itemKey);
			collections.put(currentDir, currentCollection);
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
			buf.append('/');
			buf.append(file.getName());
			String cname = buf.toString();
			if (cname.length() > maxlengthCollectionName) {
				cname = cname.substring(0, maxlengthCollectionName - 3);
				cname += "...";
				logger.info("Collection name too long. Shortening to '" + cname
						+ "' ...");
			}
			String[] children = file.list();
			if (children != null) {
				for (int i = 0; i < children.length; i++) {
					processFileTree(new File(file, children[i]), cname);
				}
			}
		} else {
			processFile(file, basepath);
		}
	}

	/**
	 * This processes the directory supplied by the user.
	 * 
	 * @return a JSON string in KDX collections format.
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public String process() throws NoSuchAlgorithmException, IOException {
		File documents = new File(documentsDirectory);
		String root = documents.getName();
		if (!"documents".equals(root)) {
			logger.warning("Supplied path may not work on KDX, since "
					+ "KDX requires documents to be stored in a "
					+ "directory named 'documents'. On KDX the filepath "
					+ "hash is calculated relative to this root.");
		}
		processFileTree(documents, "");
		return toString();
	}

	/**
	 * Initialises a KDX collection manager for the supplied directory.
	 * 
	 * @param path
	 *            the path to the documents.
	 * @throws IOException
	 * @throws SecurityException
	 */
	public Manager(String path) throws SecurityException, IOException {
		collections = new HashMap<String, Collection>();
		checksum = new Checksum();
		documentsDirectory = path;
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
	 * @throws IOException
	 * @throws SecurityException
	 */
	public Manager(String path, int maxlen) throws SecurityException,
			IOException {
		collections = new HashMap<String, Collection>();
		checksum = new Checksum();
		documentsDirectory = path;
		maxlengthCollectionName = maxlen;
		if (maxlen > maxKDXDisplayLen) {
			logger.info("Collection name too long; "
					+ "may not display properly on KDX.");
		}
	}
}
// Created 24 October 2010, 7:50pm