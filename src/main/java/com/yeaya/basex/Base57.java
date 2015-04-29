package com.yeaya.basex;

import java.util.UUID;

public class Base57 {
	protected static final int BASEX = 57;

	protected static final char CHAR_TABLE[] = {
		'5', '6', '7', '8', '9',
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
		'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
		'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
	};

	protected static final byte VALUE_TABLE [] = {
	    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, //   0 -   9
	    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, //  10 -  19
	    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, //  20 -  29
	    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, //  30 -  39
	    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, //  40 -  49
	    -1, -1, -1,  0,  1,  2,  3,  4, -1, -1, //  50 -  59
	    -1, -1, -1, -1, -1,  5,  6,  7,  8,  9, //  60 -  69
	    10, 11, 12, 13, 14, 15, 16, 17, 18, 19, //  70 -  79
	    20, 21, 22, 23, 24, 25, 26, 27, 28, 29, //  80 -  89
	    30, -1, -1, -1, -1, -1, -1, 31, 32, 33, //  90 -  99
	    34, 35, 36, 37, 38, 39, 40, 41, 42, 43, // 100 - 109
	    44, 45, 46, 47, 48, 49, 50, 51, 52, 53, // 110 - 119
	    54, 55, 56, -1, -1, -1, -1, -1          // 120 - 127
	};

	protected static final int MAX_ENCODED_LENGTH_TABLE[] = {
		0,		// 0
		2,		// 1
		3,		// 2
		5,		// 3
		6,		// 4
		7,		// 5
		9,		// 6
		10,		// 7
		11		// 8
	};
	
	protected static final int MAX_ENCODED_LENGTH_8_BYTE = 11;

	protected static final int MAX_DECODED_LENGTH_TABLE[] = {
		0,		// 0
		-1,		// 1
		1,		// 2
		2,		// 3
		-1,		// 4
		3,		// 5
		4,		// 6
		5,		// 7
		-1,		// 8
		6,		// 9
		7,		// 10
		8		// 11
	};

	protected static final String MAX_POSITIVE_VALUE_TABLE[] = {
		null,			// 0
		"7I",			// 1
		"F9s",			// 2
		"5oLua",		// 3
		"8bTvbj",		// 4
		"L6hwBxC",		// 5
		"6JzarcHDv",	// 6
		"AhO68GEdB6",	// 7
		"UWESlPEBFAy"	// 8
	};

	public static char getZeroChar() {
		return CHAR_TABLE[0];
	}

	public static final char[] getCharTable() {
		return CHAR_TABLE;
	}

	public static String encode(long l) {
		return encodeWithMaxEncodedLength(l, MAX_ENCODED_LENGTH_8_BYTE);
	}

	public static String encodeBytes(byte[] bytes) {
		int remain = bytes.length % 8;
		int n = bytes.length / 8;
		int offset = 0;
		StringBuilder sb = new StringBuilder(n * MAX_ENCODED_LENGTH_8_BYTE + MAX_ENCODED_LENGTH_8_BYTE);
		for (int i = 0; i < n; i++) {
			long l = LangHelper.bytes2Long(bytes, offset);
			String encodedString = encodeWithMaxEncodedLength(l, MAX_ENCODED_LENGTH_8_BYTE);
			encodedString = align(encodedString, MAX_ENCODED_LENGTH_8_BYTE);
			sb.append(encodedString);
			offset += 8;
		}
		if (remain != 0) {
			int maxLengthForRemain = MAX_ENCODED_LENGTH_TABLE[remain];
			long l = LangHelper.bytes2Long(bytes, offset);
			String encodedString = encodeWithMaxEncodedLength(l, maxLengthForRemain);
			encodedString = align(encodedString, maxLengthForRemain);

			sb.append(encodedString);
		}
		return sb.toString();
	}

	public static String encodeUuid(UUID uuid) {
        String least = encodeWithMaxEncodedLength(uuid.getLeastSignificantBits(), MAX_ENCODED_LENGTH_8_BYTE);
        String most = encodeWithMaxEncodedLength(uuid.getMostSignificantBits(), MAX_ENCODED_LENGTH_8_BYTE);
        return align(most, MAX_ENCODED_LENGTH_8_BYTE) + align(least, MAX_ENCODED_LENGTH_8_BYTE);
	}
	
	public static long decode(String s) {
		return decodeWithMaxPositiveValue(s, MAX_POSITIVE_VALUE_TABLE[8]);
	}

	public static byte[] decodeBytes(String s) {
		int readOffset = 0;
		int writeOffset = 0;
		int n = s.length() / MAX_ENCODED_LENGTH_8_BYTE;
		int remain = s.length() % MAX_ENCODED_LENGTH_8_BYTE;
		int remainDecodedLength = MAX_DECODED_LENGTH_TABLE[remain];
		if (remainDecodedLength < 0) {
			return null;
		}
		int bufLen = n * 8 + remainDecodedLength;

		byte[] bytes = new byte[bufLen];
		for (int i = 0; i < n; i++) {
			String sub = s.substring(readOffset, readOffset + MAX_ENCODED_LENGTH_8_BYTE);
			readOffset += MAX_ENCODED_LENGTH_8_BYTE;
			long l = decodeWithMaxPositiveValue(sub, MAX_POSITIVE_VALUE_TABLE[8]);
			LangHelper.long2Bytes(l, bytes, writeOffset);
			writeOffset += 8;
		}
		if (remain != 0) {
			String sub = s.substring(readOffset);
			long l = decodeWithMaxPositiveValue(sub, MAX_POSITIVE_VALUE_TABLE[remainDecodedLength]);
			LangHelper.long2Bytes(l, bytes, writeOffset);
		}
		return bytes;
	}

	public static UUID decodeUuid(String s) {
		if (s.length() != 22) { // 22:(MAX_ENCODED_LENGTH_TABLE[8] * 2)
			throw new IllegalArgumentException("Input string length must be 22, not " + s.length());
		}
		String leastString = s.substring(MAX_ENCODED_LENGTH_8_BYTE);
		String mostString = s.substring(0, MAX_ENCODED_LENGTH_8_BYTE);
		long least = decode(leastString);
		long most = decode(mostString);
        return new UUID(most, least);
	}
/*
	protected static String align(String s, int length) {
		while (s.length() < length) {
			s = CHAR_TABLE[0] + s;
		}
		return s;
	}*/
	protected static String align(String s, int length) {
		int strLength = s.length();
		if (strLength < length) {
			char[] array = new char[length];
			int count = length - strLength;
			char zeroChar = CHAR_TABLE[0];
			for (int i = 0; i < count; i++) {
				array[i] = zeroChar;
			}
			char[] arrayS = s.toCharArray();
			int index = 0;
			for (int i = count; i < length; i++) {
				array[i] = arrayS[index++];
			}
			return new String(array);
		}
		return s;
	}

	protected static String encodeWithMaxEncodedLength(long l, int maxEncodedLength) {
		StringBuilder sb = new StringBuilder(MAX_ENCODED_LENGTH_8_BYTE);
		if (l >= 0) {
			while (l >= BASEX) {
				int m = (int)(l % BASEX);
				sb.append(CHAR_TABLE[m]);
				l = l / BASEX;
			}
			if (l != 0) {
				sb.append(CHAR_TABLE[(int)l]);
			}
		} else {
			l = -l;
			int[] array = new int[maxEncodedLength];
			int index = 0;
			while (l >= BASEX) {
				int m = (int)(l % BASEX);
				array[index++] = m;
				l = (l - m) / BASEX;
			}
			if (l != 0) {
				array[index] = (int)l;
			}
			
			for (int j = 0; j < array.length; j++) {
				array[j] = BASEX - 1 - array[j];
			}
			for (int j = 0; j < array.length; j++) {
				if (array[j] < BASEX - 1) {
					array[j]++;
					break;
				} else {
					array[j] = 0;
				}
			}
			for (int j = 0; j < array.length; j++) {
				sb.append(CHAR_TABLE[array[j]]);
			}
		}
		return sb.reverse().toString();
	}

	protected static long decodeWithMaxPositiveValue(String s, String maxPositiveValue) {
		char[] ca = s.toCharArray();
		long l = 0;
		if (s.length() == maxPositiveValue.length() && s.compareTo(maxPositiveValue) > 0) {
			for (int i = 0; i < ca.length; i++) {
				int c = ca[i] % 128;
				int value = VALUE_TABLE[c];
				if (value < 0) {
					throw new IllegalArgumentException("Input string contains a char(" + ca[i] + ") that is not exist in char table");
				}
				value = BASEX - 1 - value;
				l = l * BASEX + value;
			}
			l++;
			l = -l;
		} else {
			for (int i = 0; i < ca.length; i++) {
				int c = ca[i] % 128;
				int value = VALUE_TABLE[c];
				if (value < 0) {
					throw new IllegalArgumentException("Input string contains a char(" + ca[i] + ") that is not exist in char table");
				}
				l = l * BASEX + value;
			}
		}
		return l;
	}
}
