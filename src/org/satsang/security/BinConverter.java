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

/**
 * Some helper routines for data conversion, all data is treated in network byte
 * order.
 */
public class BinConverter {
	/**
	 * Gets bytes from an array into an integer.
	 * 
	 * @param buf
	 *            where to get the bytes
	 * @param ofs
	 *            index from where to read the data
	 * @return the 32bit integer
	 */
	public final static int byteArrayToInt(byte[] buf, int ofs) {
		return (buf[ofs] << 24) | ((buf[ofs + 1] & 0x0ff) << 16) | ((buf[ofs + 2] & 0x0ff) << 8)
				| (buf[ofs + 3] & 0x0ff);
	}

	// /////////////////////////////////////////////////////////////////////////

	/**
	 * Converts an integer to bytes, which are put into an array.
	 * 
	 * @param value
	 *            the 32bit integer to convert
	 * @param buf
	 *            the target buf
	 * @param ofs
	 *            where to place the bytes in the buf
	 */
	public final static void intToByteArray(int value, byte[] buf, int ofs) {
		buf[ofs] = (byte) ((value >>> 24) & 0x0ff);
		buf[ofs + 1] = (byte) ((value >>> 16) & 0x0ff);
		buf[ofs + 2] = (byte) ((value >>> 8) & 0x0ff);
		buf[ofs + 3] = (byte) value;
	}

	// /////////////////////////////////////////////////////////////////////////

	/**
	 * Gets bytes from an array into a long.
	 * 
	 * @param buf
	 *            where to get the bytes
	 * @param ofs
	 *            index from where to read the data
	 * @return the 64bit integer
	 */
	public final static long byteArrayToLong(byte[] buf, int ofs) {
		// Looks more complex - but it is faster (at least on 32bit platforms).

		return ((long) ((buf[ofs] << 24) | ((buf[ofs + 1] & 0x0ff) << 16) | ((buf[ofs + 2] & 0x0ff) << 8) | (buf[ofs + 3] & 0x0ff)) << 32)
				| ((long) ((buf[ofs + 4] << 24) | ((buf[ofs + 5] & 0x0ff) << 16) | ((buf[ofs + 6] & 0x0ff) << 8) | (buf[ofs + 7] & 0x0ff)) & 0x0ffffffffL);
	}

	// /////////////////////////////////////////////////////////////////////////

	/**
	 * Converts a long to bytes, which are put into an array.
	 * 
	 * @param value
	 *            the 64bit integer to convert
	 * @param buf
	 *            the target buf
	 * @param ofs
	 *            where to place the bytes in the buf
	 */
	public final static void longToByteArray(long value, byte[] buf, int ofs) {
		int tmp = (int) (value >>> 32);

		buf[ofs] = (byte) (tmp >>> 24);
		buf[ofs + 1] = (byte) ((tmp >>> 16) & 0x0ff);
		buf[ofs + 2] = (byte) ((tmp >>> 8) & 0x0ff);
		buf[ofs + 3] = (byte) tmp;

		tmp = (int) value;

		buf[ofs + 4] = (byte) (tmp >>> 24);
		buf[ofs + 5] = (byte) ((tmp >>> 16) & 0x0ff);
		buf[ofs + 6] = (byte) ((tmp >>> 8) & 0x0ff);
		buf[ofs + 7] = (byte) tmp;
	}

	// /////////////////////////////////////////////////////////////////////////

	/**
	 * Converts values from an integer array to a long.
	 * 
	 * @param buf
	 *            where to get the bytes
	 * @param ofs
	 *            index from where to read the data
	 * @return the 64bit integer
	 */
	public final static long intArrayToLong(int[] buf, int ofs) {
		return (((long) buf[ofs]) << 32) | (((long) buf[ofs + 1]) & 0x0ffffffffL);
	}

	// /////////////////////////////////////////////////////////////////////////

	/**
	 * Converts a long to integers which are put into an array.
	 * 
	 * @param value
	 *            the 64bit integer to convert
	 * @param buf
	 *            the target buf
	 * @param ofs
	 *            where to place the bytes in the buf
	 */
	public final static void longToIntArray(long value, int[] buf, int ofs) {
		buf[ofs] = (int) (value >>> 32);
		buf[ofs + 1] = (int) value;
	}

	// /////////////////////////////////////////////////////////////////////////

	/**
	 * Makes a long from two integers (treated unsigned).
	 * 
	 * @param lo
	 *            lower 32bits
	 * @param hi
	 *            higher 32bits
	 * @return the built long
	 */
	public final static long makeLong(int lo, int hi) {
		return (((long) hi << 32) | ((long) lo & 0x00000000ffffffffL));
	}

	// /////////////////////////////////////////////////////////////////////////

	/**
	 * Gets the lower 32 bits of a long.
	 * 
	 * @param val
	 *            the long integer
	 * @return lower 32 bits
	 */
	public final static int longLo32(long val) {
		return (int) val;
	}

	// /////////////////////////////////////////////////////////////////////////

	/**
	 * Gets the higher 32 bits of a long.
	 * 
	 * @param val
	 *            the long integer
	 * @return higher 32 bits
	 */
	public final static int longHi32(long val) {
		return (int) (val >>> 32);
	}

	// /////////////////////////////////////////////////////////////////////////

	// our table for hex conversion
	final static char[] HEXTAB = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	/**
	 * Converts a byte array to a hex string.
	 * 
	 * @param data
	 *            the byte array
	 * @return the hex string
	 */
	public final static String bytesToHexStr(byte[] data) {
		return bytesToHexStr(data, 0, data.length);
	}

	// /////////////////////////////////////////////////////////////////////////

	/**
	 * Converts a byte array to a hex string.
	 * 
	 * @param data
	 *            the byte array
	 * @param ofs
	 *            start index where to get the bytes
	 * @param len
	 *            number of bytes to convert
	 * @return the hex string
	 */
	public final static String bytesToHexStr(byte[] data, int ofs, int len) {
		int pos, c;
		StringBuffer sbuf;

		sbuf = new StringBuffer();
		sbuf.setLength(len << 1);

		pos = 0;
		c = ofs + len;

		while (ofs < c) {
			sbuf.setCharAt(pos++, HEXTAB[(data[ofs] >> 4) & 0x0f]);
			sbuf.setCharAt(pos++, HEXTAB[data[ofs++] & 0x0f]);
		}
		return sbuf.toString();
	}

	// /////////////////////////////////////////////////////////////////////////

	/**
	 * Converts a hex string back into a byte array (invalid codes will be
	 * skipped).
	 * 
	 * @param hex
	 *            hex string
	 * @param data
	 *            the target array
	 * @param srcofs
	 *            from which character in the string the conversion should
	 *            begin, remember that (nSrcPos modulo 2) should equals 0
	 *            normally
	 * @param dstofs
	 *            to store the bytes from which position in the array
	 * @param len
	 *            number of bytes to extract
	 * @return number of extracted bytes
	 */
	public final static int hexStrToBytes(String hex, byte[] data, int srcofs, int dstofs, int len) {
		int i, j, strlen, avail_bytes, dstofs_bak;
		byte abyte;
		boolean convertOK;

		// check for correct ranges
		strlen = hex.length();

		avail_bytes = (strlen - srcofs) >> 1;
		if (avail_bytes < len) {
			len = avail_bytes;
		}

		int nOutputCapacity = data.length - dstofs;
		if (len > nOutputCapacity) {
			len = nOutputCapacity;
		}

		// convert now

		dstofs_bak = dstofs;

		for (i = 0; i < len; i++) {
			abyte = 0;
			convertOK = true;

			for (j = 0; j < 2; j++) {
				abyte <<= 4;
				char cActChar = hex.charAt(srcofs++);

				if ((cActChar >= 'a') && (cActChar <= 'f')) {
					abyte |= (byte) (cActChar - 'a') + 10;
				} else {
					if ((cActChar >= '0') && (cActChar <= '9')) {
						abyte |= (byte) (cActChar - '0');
					} else {
						convertOK = false;
					}
				}
			}
			if (convertOK) {
				data[dstofs++] = abyte;
			}
		}

		return (dstofs - dstofs_bak);
	}

	// /////////////////////////////////////////////////////////////////////////

	/**
	 * Converts a byte array into a Unicode string.
	 * 
	 * @param data
	 *            the byte array
	 * @param ofs
	 *            where to begin the conversion
	 * @param len
	 *            number of bytes to handle
	 * @return the string
	 */
	public final static String byteArrayToStr(byte[] data, int ofs, int len) {
		int avail_capacity, sbuf_pos;
		StringBuffer sbuf;

		// we need two bytes for every character
		len &= ~1;

		// enough bytes in the buf?
		avail_capacity = data.length - ofs;

		if (avail_capacity < len) {
			len = avail_capacity;
		}

		sbuf = new StringBuffer();
		sbuf.setLength(len >> 1);

		sbuf_pos = 0;

		while (0 < len) {
			sbuf.setCharAt(sbuf_pos++, (char) ((data[ofs] << 8) | (data[ofs + 1] & 0x0ff)));
			ofs += 2;
			len -= 2;
		}

		return sbuf.toString();
	}
}
