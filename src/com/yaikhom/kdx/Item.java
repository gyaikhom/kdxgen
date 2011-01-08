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

/**
 * Encapsulates a collection item.
 * 
 * @author gyaikhom
 */
public class Item {
	public static final int UNKNOWN_TYPE = 0;
	public static final int PDF_FILE = 1;
	public static final int AZW_FILE = 2;
	public static final int AZW1_FILE = 3;
	public static final String[] typeName = {"Unknown", "pdf", "azw", "azw1"};
	
	// Item properties
	private String name = null;
	private String path = null;
	private int type = UNKNOWN_TYPE;
	private String key = null; // SHA1 checksum
	
	public int getFileType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	
	@Override
	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append("Name: " + name);
		s.append(", Path: " + path);
		s.append(", Type: " + typeName[type]);
		s.append(", Key: " + key);
		return s.toString();
	}
}
//Created 07 January 2011, 7:05pm