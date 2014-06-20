/*
 * Copyright 1997-2006 Markus Hahn 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.satsang.security;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Random;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;

/**
 * Support class for easy string encryption with the Blowfish algorithm. Works
 * in CBC mode with a SHA-1 key setup and correct padding - the purpose of this
 * class is mainly to show a possible implementation with Blowfish.
 */
public class BlowfishEasy {

	BlowfishCBC bfc;
	static SecureRandom _srnd;
	private final String DEFAULT_KEY = "ShreeGurudev";

	static {
		// (this approach still needs to be proven; it is good if lots of
		// instances are created and the generator is created internally all the
		// time, but it can be less efficient if SecureRandom is just a wrapper
		// for one central source, with thread protection as an overhead)

		_srnd = new SecureRandom();
	}

	/**
	 * Constructor to use string data as the key.
	 * 
	 * @param passw
	 *            the password, usually gained by String.toCharArray()
	 */
	public BlowfishEasy(String passwc) {
		int i, c;
		SHA1 sh = null;
		byte[] hash;

		char[] passw;
		// hash down the password to a 160bit key, using SHA-1

		sh = new SHA1();
		if ((passwc == null) || passwc.equals("")) {
			passw = DEFAULT_KEY.toCharArray();
		} else {
			passw = passwc.toCharArray();
		}
		for (i = 0, c = passw.length; i < c; i++) {
			sh.update((byte) ((passw[i] >> 8) & 0x0ff));
			sh.update((byte) (passw[i] & 0x0ff));
		}

		sh.finalize();

		// setup the encryptor (using a dummy IV for now)

		hash = new byte[SHA1.DIGEST_SIZE];
		sh.getDigest(hash, 0);

		this.bfc = new BlowfishCBC(hash, 0, hash.length, 0);

	}

	/**
	 * Encrypts a string (treated in Unicode) using the internal random
	 * generator.
	 * 
	 * @param plaintext
	 *            string to encrypt
	 * @return encrypted string in binhex format
	 */
	public String encryptString(String plaintext) {
		long cbciv;

		synchronized (_srnd) {
			cbciv = _srnd.nextLong();
		}

		return encStr(plaintext, cbciv);
	}

	/**
	 * Encrypts a string (in Unicode).
	 * 
	 * @param plaintext
	 *            string to encrypt
	 * @param rndgen
	 *            random generator (usually a java.security.SecureRandom
	 *            instance)
	 * @return encrypted string in binhex format
	 */
	public String encryptString(String plaintext, Random rndgen) {
		return encStr(plaintext, rndgen.nextLong());
	}

	// internal routine for string encryption
	private String encStr(String plaintext, long new_cbciv) {
		int i, pos, strlen;
		char achar;
		byte padval;
		byte[] buf;
		byte[] new_cbciv_buf;

		strlen = plaintext.length();
		buf = new byte[((strlen << 1) & ~7) + 8];

		pos = 0;
		for (i = 0; i < strlen; i++) {
			achar = plaintext.charAt(i);
			buf[pos++] = (byte) ((achar >> 8) & 0x0ff);
			buf[pos++] = (byte) (achar & 0x0ff);
		}

		padval = (byte) (buf.length - (strlen << 1));
		while (pos < buf.length) {
			buf[pos++] = padval;
		}

		this.bfc.setCBCIV(new_cbciv);

		this.bfc.encrypt(buf, 0, buf, 0, buf.length);

		new_cbciv_buf = new byte[BlowfishCBC.BLOCKSIZE];

		BinConverter.longToByteArray(new_cbciv, new_cbciv_buf, 0);

		return BinConverter.bytesToHexStr(new_cbciv_buf, 0, BlowfishCBC.BLOCKSIZE)
				+ BinConverter.bytesToHexStr(buf, 0, buf.length);
	}

	/**
	 * Decrypts a hexbin string (handling is case sensitive).
	 * 
	 * @param ciphertext
	 *            hexbin string to decrypt
	 * @return decrypted string (null equals an error)
	 */
	public String decryptString(String ciphertext) {
		int num_of_bytes, padbyte, len;
		byte[] buf;
		byte[] cbciv;

		len = (ciphertext.length() >> 1) & ~7;

		if (BlowfishECB.BLOCKSIZE > len) {
			return null;
		}

		cbciv = new byte[BlowfishCBC.BLOCKSIZE];

		num_of_bytes = BinConverter.hexStrToBytes(ciphertext, cbciv, 0, 0, BlowfishCBC.BLOCKSIZE);

		if (num_of_bytes < BlowfishCBC.BLOCKSIZE) {
			return null;
		}

		this.bfc.setCBCIV(cbciv, 0);

		len -= BlowfishCBC.BLOCKSIZE;
		if (len == 0) {
			return "";
		}

		buf = new byte[len];

		num_of_bytes = BinConverter.hexStrToBytes(ciphertext, buf, BlowfishCBC.BLOCKSIZE << 1, 0, len);

		if (num_of_bytes < len) {
			return null;
		}

		this.bfc.decrypt(buf, 0, buf, 0, buf.length);

		padbyte = buf[buf.length - 1] & 0x0ff;

		// (try to get everything, even if the padding seem to be wrong)
		if (BlowfishCBC.BLOCKSIZE < padbyte) {
			padbyte = 0;
		}

		num_of_bytes -= padbyte;

		if (num_of_bytes < 0) {
			return "";
		}

		return BinConverter.byteArrayToStr(buf, 0, num_of_bytes);
	}

	/**
	 * Destroys (clears) the encryption engine, after that the instance is not
	 * valid anymore.
	 */
	public void destroy() {
		this.bfc.cleanUp();

	}

	// Method to return request parameters hashmap
	public static HashMap decryptFormParamaters(String encryptedString) {
		BlowfishEasy blowEasy = new BlowfishEasy("");
		// encryptedString="86eeebd875dd8b478e2bf9999c72b470661348ac0fcbe2317e15a0bbd53ea3bf30266bd1578f25ffd7d5e5611d51eee439e198d5f6dbb4aa8eb21bec6be4fa1a9c6b6646ab7dacbdc4645b7f6a6cb70a8ebe891f3ff64cf8fb7ae7264729bf40e175d5344b8323ad2d5564b27e52e6d32a82d7d430dfa431e72c1c7eda006680";
		String decryptString = blowEasy.decryptString(encryptedString);

		StringTokenizer paramTokenizer = new StringTokenizer(decryptString, "&"); //$NON-NLS-1$
		HashMap parameters = new HashMap();
		// split parameters (key=value)
		while (paramTokenizer.hasMoreTokens()) {
			// split key and value ...
			String[] keyValue = StringUtils.split(paramTokenizer.nextToken(), '=');

			// encode name/value to prevent css
			// String escapedKey = StringEscapeUtils.escapeHtml(keyValue[0]);
			// String escapedValue = keyValue.length > 1?
			// StringEscapeUtils.escapeHtml(keyValue[1]):
			// TagConstants.EMPTY_STRING;

			if (!parameters.containsKey(keyValue[0])) {
				// ... and add it to the map
				parameters.put(keyValue[0], keyValue[1]);
			}

		}

		return parameters;
	}

	public static void main(String[] args) {

		try {
			BlowfishEasy bfe = new BlowfishEasy("");
			// Random rndgen= new Random();
			// String test= bfe.encryptString("2222", rndgen);

			// HashMap paramaters = BlowfishEasy.decryptFormParamaters("dd");

			// String
			// result=bfe.decryptString("7fdcd177ba88e7830560e9acf5acc6a23dcccdac653a54e6786007d81b7f0e2256a4c051420a2134b1ddc1f794fac5e3c2f13ed6e57fd45fcbd6d46b50eaeb84e746e86e3108dfb700fc68933666f3217d41e2ece985a6a22fd2f6a352ccf5583ace314526a911fdc5af4d8794774fa8ba6ce0589d263e05a730822883bef17dd649ca6a479b2b9da36ce5b200015ad547c68521e657f348780b23f3769abfa5");
			// String result =
			// bfe.decryptString("b5c1350afa2f0f51e141b10ca7188b85bbed6efa46e019a5f79ae19adaa36358d4d457b5f4156f26c7f12310b8beb971ea87a479f4b27d55206ae7f3c5a59df1b1213b8a876fc28802c26852826ab1a60a684a6201d7dcfbda57e17b5e7ac7d4");

			String result1 = bfe.encryptString("mac=ml00005");
			String result2 = bfe.decryptString(result1);

			// String result =
			// bfe.decryptString("a677feede64fdd95449afd4fce42714a0b2e4f7228458cc2b6c3ce4c454af24823d9ee76cb2dad00e51bd461763bd3ea913f840689c0244df3104cdb57605b853be231ab477eb5db11f700f65d6a4ea6bfd0bfe120136de460992e3336250827355ad9d7044a915898e4ae947a5fdea52c8aeee13a01e23f2441fdc1514b2d48880b9eee567579ecec218ea33a73e62772fa74c059edd3db2c7a2e4e291deaeeee3bf963f7c2b25419f774729b7ea0e8239ea19287ab4c4db06b7a09a58a9a568f6911ce9ee83d763aad23d4551dcb9d03e93c1afa0935c64cbd8235a744594bffdf63bf4f0c5f5acb8da6b53dfc25259");

			System.out.println("result1 :- " + result1);
			System.out.println("result2 :- " + result2);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
