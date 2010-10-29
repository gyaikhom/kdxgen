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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Encapsulates a KDX collection.
 * 
 * @author gyaikhom
 */
public class Collection {
	private static final Logger logger = Logger.getLogger("com.yaikhom.kdx");
	private String name;
	private Long lastAccess;
	private List<String> items;

	public Collection() throws SecurityException, IOException {
		name = new String();
		lastAccess = (new Date()).getTime();
		items = new ArrayList<String>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getLastAccess() {
		return lastAccess;
	}

	public void setLastAccess(Long lastAccess) {
		this.lastAccess = lastAccess;
	}

	public List<String> getItems() {
		return items;
	}

	public void setItems(List<String> items) {
		this.items = items;
	}

	public int addItem(String item) {
		items.add(item);
		return items.size();
	}

	public int removeItem(String item) {
		items.remove(item);
		return items.size();
	}

	/**
	 * This returns the collection as a JSON string, in a format required by the
	 * KDX collections.json file. E.g.,
	 * 
	 * <p>
	 * {@code "Hello World@en-US": "items":["Item1", "Item2", ...],
	 * "lastAccess":1234567890123" }}
	 * 
	 * <p>
	 * Here, Item1, Item2, etc. are either the ASIN number of the .azw document
	 * (as parsed from the filename), or the SHA1 checksum of the .pdf document,
	 * the file name relative to
	 * 
	 * <p>
	 * {@code /mnt/us/}
	 * 
	 * @return a JSON string.
	 * @see Manager#processFile
	 */
	@Override
	public String toString() {
		logger.info("Printing collection '" + name + "' ...");
		StringBuffer buf = new StringBuffer();
		if (items.size() > 0) {
			buf.append("\"" + name.replace("/", "\\/")
					+ "@en-US\":{\"items\":[");
			Iterator<String> i = items.iterator();
			if (i.hasNext()) {
				buf.append("\"" + i.next() + "\"");
				while (i.hasNext()) {
					buf.append(",\"" + i.next() + "\"");
				}
				buf.append("],\"lastAccess\":" + lastAccess + "}");
			}
		} else {
			logger.info("Skipping empty collection '" + name + "' ...");
		}
		return buf.toString();
	}
}
// Created 24 October 2010, 7:37pm