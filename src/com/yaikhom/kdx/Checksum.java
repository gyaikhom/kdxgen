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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Encapsulates checksum calculation, in accordance with KDX collections
 * requirement.
 * 
 * @author gyaikhom
 */
public class Checksum {
	/**
	 * This is the root KDX mount path. It is used when generating the SHA1
	 * checksum, which is then used in maintaining a collection.
	 * 
	 * @see Manager
	 */
	private static final String kdxMount = "/mnt/us/documents/";
	private boolean uppercaseHexadecimal = false;
	private char startChar = 'a';

	/**
	 * Does the hexadecimal string use uppercase characters?
	 * 
	 * @return {@code true} if uppercase characters; other wise {@code false}.
	 */
	public boolean isUppercaseHexadecimal() {
		return uppercaseHexadecimal;
	}

	/**
	 * Should the hexadecimal string generated using uppercase characters?
	 * 
	 * @param uppercaseHexadecimal
	 *            {@code true} if uppercase characters; other wise {@code false}
	 *            .
	 */
	public void setUppercaseHexadecimal(boolean uppercaseHexadecimal) {
		this.uppercaseHexadecimal = uppercaseHexadecimal;
		startChar = uppercaseHexadecimal ? 'A' : 'a';
	}

	/**
	 * Initialises a checksum calculator, which uses lowercase characters.
	 */
	public Checksum() {
	}

	/**
	 * Initialises a checksum calculator, with option to specify character
	 * choice for the hexadecimal characters.
	 * 
	 * @param flag
	 *            {@code true} if uppercase characters must be used; other wise
	 *            {@code false}.
	 */
	public Checksum(boolean flag) {
		uppercaseHexadecimal = flag;
		startChar = uppercaseHexadecimal ? 'A' : 'a';
	}

	/**
	 * Returns the hex character of a half byte.
	 * 
	 * @param hb
	 *            the half-byte, where (0xFFF0 & hb) is 0x0000.
	 * @return the hexadecimal character, one of ('0'..'9', or 'a', 'b', 'c',
	 *         'd', 'e', 'f').
	 */
	private char getHex(int hb) {
		return (char) (hb > 9 ? startChar + (hb - 10) : '0' + hb);
	}

	/**
	 * Converts a byte sequence to a hexadecimal string.
	 * 
	 * @param data
	 *            the array of bytes to be converted.
	 * @return the string of hexadecimal characters.
	 */
	private String convertToHex(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			int lhb, rhb; // left and right half bytes
			lhb = (data[i] >>> 4) & 0x000F;
			rhb = data[i] & 0x000F;
			buf.append(getHex(lhb));
			buf.append(getHex(rhb));
		}
		return buf.toString();
	}

	/**
	 * Calculates the SHA1 checksum of a string, and returns the value as a
	 * hexadecimal string.
	 * 
	 * @param text
	 *            the string of characters to process.
	 * @return the SHA1 checksum as a hexadecimal string, or null on failure.
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	public String getSHA1(String text) throws NoSuchAlgorithmException,
			UnsupportedEncodingException {
		String hexhash = null;
		if (text != null && text.length() > 0) {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			byte[] sha1hash = new byte[40];
			md.update(text.getBytes("iso-8859-1"), 0, text.length());
			sha1hash = md.digest();
			hexhash = convertToHex(sha1hash);
		}
		return hexhash;
	}

	/**
	 * Calculates the SHA1 checksum of the file path. The supplied filename is
	 * first packaged into a KDX filename, relative to the KDX mount point, and
	 * its SHA1 checksum is calculated. The resulting hash is used for indexing
	 * files inside a collection.
	 * 
	 * @param fname
	 *            the filename relative to the documents root.
	 * @return the SHA1 checksum as a hexadecimal string.
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchAlgorithmException
	 * @see Collection
	 * @see Manager#processFile
	 */
	public String getKDXFilenameHash(String fname)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		StringBuffer buf = new StringBuffer();
		buf.append(kdxMount); // KDX hashing assumes this virtual mount point
		buf.append(fname);
		return getSHA1(buf.toString());
	}
}
// Created 24 October 2010, 7:40pm