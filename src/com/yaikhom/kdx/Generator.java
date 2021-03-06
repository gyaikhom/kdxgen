/*
 * This file is part of the kdxgen project (http://kdxgen.sourceforge.net)
 * 
 * Copyright (c) 2010, 2011 Gagarine Yaikhom
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
import java.security.NoSuchAlgorithmException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

/**
 * Encapsulates a collection generator.
 * 
 * @author gyaikhom
 */
public class Generator {
	private static final Logger logger = Logger.getLogger("com.yaikhom.kdx");

	private void showHelp() {
		StringBuffer footer = new StringBuffer();
		footer.append("\nCopyright (c) 2010 ");
		footer.append("Gagarine Yaikhom\n");
		footer.append("\nThe program kdxgen (The KDX Collection Generator) "
				+ "is a command line tool for generating Kindle book "
				+ "collections from a directory tree containing e-books. "
				+ "The collection names reflect multi-level organisation, "
				+ "as they are generated according to the directory tree. "
				+ "This program has only been tested using GNU/Linux. "
				+ "Please read README for further help. For project details "
				+ "visit http://kdxgen.sourceforge.net.\n");
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(80, "java -jar kdxgen.jar", null, options, footer
				.toString(), true);
	}

	/**
	 * Initialise the collection generator.
	 * 
	 * @throws IOException
	 * @throws SecurityException
	 */
	public Generator() throws SecurityException, IOException {
		FileHandler logFileHandler = new FileHandler("%t/kdxgen.log", false);
		SimpleFormatter logFileFormatter = new SimpleFormatter();
		logFileHandler.setFormatter(logFileFormatter);
		logger.addHandler(logFileHandler);
		logger.setLevel(Level.ALL);
	}

	private static final String REQ_KDX_ROOT = "d";
	private static final String OPT_CLI = "c";
	private static final String OPT_OUTPUT_FILE = "o";
	private static final String OPT_MAXLEN = "l";
	private static final String OPT_VERBOSE = "v";
	private static Options options = null;
	static {
		options = new Options();
		options.addOption(REQ_KDX_ROOT, true, "Path to Kindle device root. "
				+ "This must point to the root directory "
				+ "of the Kindle device.");
		options.addOption(OPT_CLI, false, "By default, graphical user "
				+ "interface (GUI) is use. Use this to turn off GUI.");
		options.addOption(OPT_OUTPUT_FILE, true, "Send result to output "
				+ "file. If unspecified, result will be sent to "
				+ "standard output (stdout).");
		options.addOption(OPT_MAXLEN, true, "The maximum number of characters "
				+ "allowed as collection names. If the generated collection "
				+ "name is longer than the permitted, it will be shortened "
				+ "to fit within the specified length. By default, this value "
				+ "is set to " + Manager.maxKDXDisplayLen + " characters.");
		options.addOption(OPT_VERBOSE, false, "Display log information on "
				+ "console. By default, log information is directed to "
				+ "'/tmp/kdxgen.log' file only.");
	}

	private CommandLine cmd = null;
	private static String kdxRootPath = null;
	private static String outputFile = null;
	private static int maxlen = -1;
	private static boolean cli = false;

	/**
	 * Parse command line, and set argument after validation. Print usage
	 * information if no executable.
	 * 
	 * @param args
	 *            the arguments list.
	 */
	private void parseArgs(String[] args) {
		CommandLineParser parser = new PosixParser();
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			logger.severe("Error parsing commandline arguments...Exiting");
			e.printStackTrace();
			System.exit(1);
		}
		if (cmd.hasOption(OPT_CLI)) {
			cli = true;

			/*
			 * The following values are needed for running the application
			 * without GUI. With GUI, the required values will be obtained from
			 * a GUI form.
			 */
			if (!cmd.hasOption(REQ_KDX_ROOT)) {
				showHelp();
				System.exit(1);
			} else {
				kdxRootPath = cmd.getOptionValue(REQ_KDX_ROOT);
				if (kdxRootPath.length() < 1) {
					logger.severe("Invalid Kindle root directory...Exiting");
					showHelp();
					System.exit(1);
				}
			}
			if (cmd.hasOption(OPT_OUTPUT_FILE)) {
				outputFile = cmd.getOptionValue(OPT_OUTPUT_FILE);
			}
			if (cmd.hasOption(OPT_MAXLEN)) {
				maxlen = Integer.parseInt(cmd.getOptionValue(OPT_MAXLEN));
			}
			if (cmd.hasOption(OPT_VERBOSE)) {
				logger.setUseParentHandlers(true);
			} else {
				logger.setUseParentHandlers(false);
			}
		}
	}

	/**
	 * Setup the arguments for the collection generator. These value could come
	 * from CLI arguments, or the GUI form.
	 * 
	 * @param args
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	private void setup(String[] args) throws NoSuchAlgorithmException,
			IOException {
		parseArgs(args);
		if (!cli) {
			Graphical gui = new Graphical();
			gui.start();
		} else {
			Manager kdxm = (maxlen == -1) ? new Manager(kdxRootPath, true)
					: new Manager(kdxRootPath, maxlen, true);
			kdxm.process();
			kdxm.save(outputFile);
		}
	}

	/**
	 * Entry to the KDX collection generator.
	 * 
	 * <p>
	 * <b>Usage:</b>{@code java -jar kdxgen.jar [options]}
	 * 
	 * <p>
	 * {@code -c} Use comman line interface instead of the graphical user
	 * interface (GUI). By default, GUI is assumed.
	 * 
	 * <p>
	 * {@code -d <arg>} Path to Kindle device root. This must point to the root
	 * directory of the Kindle device.
	 * 
	 * <p>
	 * {@code -l <arg>} The maximum number of characters allowed as collection
	 * names. If the generated collection name is longer than the permitted, it
	 * will be shortened to fit within the specified length. By default, this
	 * value is set to 48 characters.
	 * 
	 * <p>
	 * {@code -o <arg>} Send result to output file. If unspecified, result will
	 * be sent to standard output (stdout).
	 * 
	 * <p>
	 * {@code -v} Display log information on console. By default, log
	 * information is directed to '{@code /tmp/kdxgen.log}' file only.
	 * 
	 * @param args
	 *            the command line arguments.
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static void main(String[] args) throws NoSuchAlgorithmException,
			IOException {
		Generator kdxgen = new Generator();
		kdxgen.setup(args);
	}
}

// Created 24 October 2010, 7:45pm
