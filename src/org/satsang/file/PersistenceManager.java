package org.satsang.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.android.activities.MantraActivity;
import org.satsang.live.config.ConfigurationLive;
import org.satsang.security.BlowfishEasy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class used to read and write from file system. It takes List of String as
 * parameter and file names as parameters and stores list parameters in file. It
 * also reads content of the file and returns the list. Based on \n carriage
 * return it identifies the list
 */
public class PersistenceManager {
	static private final Logger Log = LoggerFactory.getLogger(PersistenceManager.class);

	/*
	 * This method reads list of Satsang files from .dat files (ex
	 * pravachan.dat, satsang.dat, audit.dat) and returns the list Caller need
	 * to provide absolute path of the file
	 */
	public List<String> read(String fileName) throws FileNotFoundException, Exception {
		Log.info("Entering:PersistenceManager.read(fileName)");
		List<String> lines = new ArrayList<String>();
		FileInputStream fis = null;
		BufferedReader reader = null;
		boolean isEncryptionEnabled = isEncryptionEnabled();
		if (fileName != null && (!"".equalsIgnoreCase(fileName))) {
			File datFile = new File(fileName);
			if (datFile != null && (!datFile.exists()) && (!datFile.isDirectory())) {
				throw new FileNotFoundException("File: " + fileName + " not found");
			} else {
				// file exists and not null
				try {
					// logger.info("Reading from existing file");
					fis = new FileInputStream(datFile);
					reader = new BufferedReader(new InputStreamReader(fis));
					String strLine;
					while ((strLine = reader.readLine()) != null) {
						if (isEncryptionEnabled) {
							// System.out.println("Encryption is enabled");
							BlowfishEasy dec = new BlowfishEasy("");
							String decLine = dec.decryptString(strLine);
							lines.add(decLine);
						} else {
							if (strLine.trim().length() > 0) {
//								Log.trace("Adding item line:" + strLine);
								lines.add(strLine.trim());
							}

						}
					}
					// logger.debug("Number of records: " + lines.size());
				} catch (IOException ioe) {
					Log.error("Exception:" + ioe.getMessage(), ioe);
				} catch (Exception e) {
					Log.error("Exception:" + e.getMessage(), e);
				} finally {
					try {
						if (reader != null)
							reader.close();
						if (fis != null)
							fis.close();
					} catch (Exception e) {
						Log.error("Exception:finally:PersistenceManager.getListFromFile(fileName):" + e.getMessage(),e);
					}
				}
			}
			// fileName is null block
		} else {
			// logger.error("Error reading file name. Check file name");
			throw new Exception("Invalid fileName");
		}
		// logger.info("Exiting:PersistenceManager.read(fileName)");
		return lines;
	}

	/*
	 * Method used to store schedule/list in local file. This method will
	 * overwrite content if file exists or will create new file and store
	 * records. This method to be used for storing updated satsang schedule,
	 * pravachan schedule. It is assumed that list will be synchronized earlier
	 * before writing to file
	 */
	public void store(List<String> lines, String fileName) throws Exception {
		Log.info("Entering:PersistenceManager.store(lines, fileName)");
		FileWriter writer = null;
		PrintWriter out = null;
		boolean isEncryptionEnabled=isEncryptionEnabled();
		String lineSeparator = System.getProperty("line.separator");
		Log.debug("Line separtor:" + lineSeparator);
		if (fileName != null && (!"".equalsIgnoreCase(fileName))) {
			try {
				// Log.d("D","Writing to file:" + fileName);
				writer = new FileWriter(fileName);
				out = new PrintWriter(new BufferedWriter(writer));
				// cannot check if list is null let program throw exception
				for (int i = 0; i < lines.size(); i++) {
					// logger.debug("Writing schedule: " + lines.get(i));
					if (isEncryptionEnabled) {
						BlowfishEasy enc = new BlowfishEasy("");
						String encryptedLine = enc.encryptString(lines.get(i));
						out.write(encryptedLine + lineSeparator);
					} else {
						out.write(lines.get(i) + lineSeparator);
					}

				}
				writer.flush();
				out.flush();
			} catch (IOException ioe) {
				Log.error("Exception:" + ioe.getMessage(), ioe);
			} catch (Exception e) {
				Log.error("Exception:" + e.getMessage(), e);
			} finally {
				try {
					if (writer != null)
						writer.close();
					if (out != null)
						out.close();
				} catch (Exception e) {
					 Log.error("Finally:"+e.getMessage(),e);
				}
			}// end file name not null
		} else {
			// fileName is null block
			// logger.error("Error in storing file. Check file Name");
			throw new Exception("Error storing file");
		}

		// logger.info("Exiting:PersistenceManager.store(lines, fileName)");
	}

	/**
	 * This method will append the contents to existing file. For ex. audit
	 * records will be appended to existing file. If file does not exist then it
	 * will create new file
	 */
	public void append(List<String> lines, String fileName) throws FileNotFoundException, Exception {
		Log.info("Entering:PersistenceManager.append(lines, fileName)");
		String lineSeparator = System.getProperty("line.separator");
		FileWriter writer = null;
		PrintWriter out = null;
		boolean isEncryptionEnabled=isEncryptionEnabled();
		if (fileName != null && (!"".equalsIgnoreCase(fileName))) {
			File datFile = new File(fileName);
			if ((datFile.isDirectory())) {
				Log.error("FileName : " + fileName + " not found or is a directory");
				throw new FileNotFoundException("Error appending file");
			} else {
				try {

					if (datFile != null && datFile.exists()) {
						// Open file in append mode
						// Log.d("D","Writing to file:" + fileName +
						// " appendMode: true");
						writer = new FileWriter(fileName, true);
					} else {
						// create new file and write
						// Log.d("D","Writing to file:" + fileName +
						// " appendMode: false");
						writer = new FileWriter(fileName);
					}

					out = new PrintWriter(new BufferedWriter(writer));
					// cannot check if lines is null or empty. let it throw
					// exception here
					for (int i = 0; i < lines.size(); i++) {
						// Log.d("D","Writing schedule: " + lines.get(i));
						if (isEncryptionEnabled) {
							BlowfishEasy enc = new BlowfishEasy("");
							String encryptedLine = enc.encryptString(lines.get(i));
							out.write(encryptedLine + lineSeparator);
						} else {
							if (lines.get(i).trim().length() > 0) {
								// logger.debug("Writing to file:"+
								// lines.get(i));
								out.write(lines.get(i) + lineSeparator);
							}
						}

					}
					writer.flush();
					out.flush();
				} catch (IOException ioe) {
					Log.error("Exception:" + ioe.getMessage(), ioe);
				} catch (Exception e) {
					Log.error("Exception:" + e.getMessage(), e);
				} finally {
					try {
						if (writer != null)
							writer.close();
						if (out != null)
							out.close();
					} catch (Exception e) {
						Log.error("Finally:"+e.getMessage(),e);
					}
				}
			}// end fileName not null block
		} else {
			// fileName is null block
			// logger.error("Error appending file. Check file Name");
			throw new Exception("Invalid file name");
		}
		// logger.info("Existing:PersistenceManager.append(lines, fileName)");

	}

	private static boolean isEncryptionEnabled() {
		/*try {
			return new Boolean(ConfigurationLive.getValue("media.schedule.file.encryption.enabled").toString()).booleanValue();
		} catch (Exception e) {
			Log.error("Exception:" + e.getMessage(), e);
		}*/
		Log.info("Exiting:isEncryptionEnabled()");
		return false;
	}
}
