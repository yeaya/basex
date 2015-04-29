package com.yeaya.basex;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class BaseX {
	public static final char[] NUMBER_CHAR_TABLE = {
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
	};
	public static final char BASE57_CHAR_TABLE[] = {
		'5', '6', '7', '8', '9',
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
		'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
		'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
	};
	public static final char[] ALPHA_CHAR_TABLE = {
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
		'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
		'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
	};
	
	public static final char[] NUMBER_ALPHA_CHAR_TABLE = {
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
		'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
		'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
	};
	
	protected int baseX;
	protected char charTable[];
	protected HashMap<Integer, Integer> valueMap;
	protected String maxPositiveTable[];
	protected int decodeLengthTable[];
	
	public static BaseX create(int baseX) {
		if (baseX <= 1) {
			return null;
		} else if (baseX <= 10) {
			return create(NUMBER_CHAR_TABLE, baseX);
		} else if (baseX <= 52) {
			return create(ALPHA_CHAR_TABLE, baseX);
		} else if (baseX <= 62) {
			if (baseX == 57) {
				return new BaseX(BASE57_CHAR_TABLE);
			} else {
				return create(NUMBER_ALPHA_CHAR_TABLE, baseX);
			}
		} else if (baseX <= 94) {
			char table[] = new char[baseX];
			for (int i = 0; i < baseX; i++) {
				table[i] = (char)('!' + i);
			}
			return create(table, baseX);
		}
		return null;
	}
	
	public static BaseX create() {
		return new BaseX(BASE57_CHAR_TABLE);
	}

	public static BaseX create(String charTable) {
		return new BaseX(charTable.toCharArray());
	}

	public static BaseX create(char[] charTable) {
		return new BaseX(extractCharTable(charTable, charTable.length));
	}

	public static BaseX create(char[] charTable, int maxCount) {
		return new BaseX(extractCharTable(charTable, maxCount));
	}
	
	protected BaseX() {

	}

	protected BaseX(char[] charTable) {
		this.charTable = charTable;
		init();
	}

	protected void init() {
		baseX = this.charTable.length;
		valueMap = new HashMap<Integer, Integer>();
		for (int i = 0; i < this.charTable.length; i++) {
			char c = this.charTable[i];
			valueMap.put(new Integer(c), new Integer(i));
		}
		
		maxPositiveTable = new String[1 + 8];
		long l = 0x7f;
		for (int i = 1; i < 9; i++) {
			String maxPositive = calcMaxEncodePositive(l);
			maxPositiveTable[i] = maxPositive;
			l = (l << 8) + 0xFF;
		}
		decodeLengthTable = new int[1 + maxPositiveTable[8].length()];
		decodeLengthTable[0] = 0;

		for (int i = 1; i < decodeLengthTable.length; i++) {
			decodeLengthTable[i] = -1;
		}
		for (int i = 1; i < 9; i++) {
			String maxPositive = maxPositiveTable[i];
			decodeLengthTable[maxPositive.length()] = i;
		}
	}

	public static String extractCharTable(String charTable) {
		return extractCharTable(charTable, charTable.length());
	}

	public static String extractCharTable(String charTable, int maxCount) {
		return new String(extractCharTable(charTable.toCharArray(), maxCount));
	}
	
	public static char[] extractCharTable(char[] charTable) {
		return extractCharTable(charTable, charTable.length);
	}
	
	public static char[] extractCharTable(char[] charTable, int maxCount) {
		HashSet<Integer> set = new HashSet<Integer>();
		StringBuilder sb = new StringBuilder(charTable.length);
		for (int i = 0; i < charTable.length; i++) {
			char c = charTable[i];
			Integer ci = new Integer(c);
			if (c != 0 && !set.contains(ci)) {
				sb.append(c);
				set.add(ci);
			}
		}
		char[] array = sb.toString().toCharArray();
		Arrays.sort(array);
		if (array.length > maxCount) {
			return Arrays.copyOf(array, maxCount);
		}
		return array;
	}
	
	public String encode(long l) {
		return encodeWithMaxEncodedLength(l, maxPositiveTable[8].length());
	}

	public String encodeBytes(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		int remain = bytes.length % 8;
		int n = bytes.length / 8;
		int offset = 0;
		for (int i = 0; i < n; i++) {
			long l = LangHelper.bytes2Long(bytes, offset);
			String encodedString = encodeWithMaxEncodedLength(l, maxPositiveTable[8].length());
			encodedString = align(encodedString, maxPositiveTable[8].length());
			sb.append(encodedString);
			offset += 8;
		}
		if (remain != 0) {
			int maxLengthForRemain = maxPositiveTable[remain].length();
			long l = LangHelper.bytes2Long(bytes, offset);
			String encodedString = encodeWithMaxEncodedLength(l, maxLengthForRemain);
			encodedString = align(encodedString, maxLengthForRemain);

			sb.append(encodedString);
		}
		return sb.toString();
	}
	
	public String encodeUuid(UUID uuid) {
		int maxPositiveLength = maxPositiveTable[8].length();
        String least = encodeWithMaxEncodedLength(uuid.getLeastSignificantBits(), maxPositiveLength);
        String most = encodeWithMaxEncodedLength(uuid.getMostSignificantBits(), maxPositiveLength);
        return align(most, maxPositiveLength) + align(least, maxPositiveLength);
	}

	public long decode(String s) {
		return decodeWithMaxPositiveValue(s, maxPositiveTable[8]);
	}

	public byte[] decodeBytes(String s) {
		int readOffset = 0;
		int writeOffset = 0;
		int n = s.length() / maxPositiveTable[8].length();
		int remain = s.length() % maxPositiveTable[8].length();
		int remainDecodedLength = decodeLengthTable[remain];
		if (remainDecodedLength < 0) {
			return null;
		}
		int bufLen = n * 8 + remainDecodedLength;

		byte[] bytes = new byte[bufLen];
		for (int i = 0; i < n; i++) {
			String sub = s.substring(readOffset, readOffset + maxPositiveTable[8].length());
			readOffset += maxPositiveTable[8].length();
			long l = decodeWithMaxPositiveValue(sub, maxPositiveTable[8]);
			LangHelper.long2Bytes(l, bytes, writeOffset);
			writeOffset += 8;
		}
		if (remain != 0) {
			String sub = s.substring(readOffset);
			long l = decodeWithMaxPositiveValue(sub, maxPositiveTable[remainDecodedLength]);
			LangHelper.long2Bytes(l, bytes, writeOffset);
		}
		return bytes;
	}

	public UUID decodeUuid(String s) {
		int maxPositiveLength = maxPositiveTable[8].length();
		if (s == null || s.length() != (maxPositiveLength * 2)) {
			throw new IllegalArgumentException("Input string length must be "
					+ (maxPositiveLength * 2) + ", not " + s.length());
		}
		String leastString = s.substring(maxPositiveLength);
		String mostString = s.substring(0, maxPositiveLength);
		long least = decode(leastString);
		long most = decode(mostString);
        return new UUID(most, least);
	}
	
	protected String calcMaxEncodePositive(long ll) {
		StringBuilder sb = new StringBuilder();

		long pl = ll;
		while (pl >= baseX) {
			int m = (int)(pl % baseX);
			sb.append(charTable[(int)m]);
			pl = (pl - m) / baseX;
		}
		if (pl != 0) {
			sb.append(charTable[(int)pl]);
		}

		String positive = sb.reverse().toString();

		long nl = ll;
		sb = new StringBuilder();
		int[] array = new int[positive.length()];
		int index = 0;
		while (nl >= baseX) {
			int m = (int)(nl % baseX);
			array[index++] = m;
			nl = (nl - m) / baseX;
		}
		if (nl != 0) {
			array[index] = (int)nl;
		}
		
		for (int j = 0; j < array.length; j++) {
			array[j] = baseX - 1 - array[j];
		}
		for (int j = 0; j < array.length; j++) {
			if (array[j] < baseX - 1) {
				array[j]++;
				break;
			} else {
				array[j] = 0;
			}
		}
		for (int j = 0; j < array.length; j++) {
			sb.append(charTable[array[j]]);
		}
		
		String negative = sb.reverse().toString();

		if (positive.compareTo(negative) >= 0) {
			positive = charTable[0] + positive;
		}

		return positive;
	}
	
	protected String encodeWithMaxEncodedLength(long l, int maxEncodedLength) {
		StringBuilder sb = new StringBuilder();
		if (l >= 0) {
			while (l >= baseX) {
				int m = (int)(l % baseX);
				sb.append(charTable[(int)m]);
				l = l / baseX;
			}
			if (l != 0) {
				sb.append(charTable[(int)l]);
			}
		} else {
			l = -l;
			int[] array = new int[maxEncodedLength];
			int index = 0;
			while (l >= baseX) {
				int m = (int)(l % baseX);
				array[index++] = m;
				l = l / baseX;
			}
			if (l != 0) {
				array[index] = (int)l;
			}
			
			for (int j = 0; j < array.length; j++) {
				array[j] = baseX - 1 - array[j];
			}
			for (int j = 0; j < array.length; j++) {
				if (array[j] < baseX - 1) {
					array[j]++;
					break;
				} else {
					array[j] = 0;
				}
			}
			for (int j = 0; j < array.length; j++) {
				sb.append(charTable[array[j]]);
			}
		}
		return sb.reverse().toString();
	}
	
	protected long decodeWithMaxPositiveValue(String s, String maxPositiveValue) {
		char[] ca = s.toCharArray();
		long l = 0;
		if (s.length() == maxPositiveValue.length() && s.compareTo(maxPositiveValue) > 0) {
			for (int i = 0; i < ca.length; i++) {
				char c = ca[i];
				Integer value = valueMap.get(new Integer(c));
				if (value == null) {
					throw new IllegalArgumentException("Input string contains a char(" + ca[i] + ") that is not exist in char table");
				}
				l = l * baseX + (baseX - 1 - value.intValue());
			}
			l++;
			l = -l;
		} else {
			for (int i = 0; i < ca.length; i++) {
				char c = ca[i];
				Integer value = valueMap.get(new Integer(c));
				if (value == null) {
					throw new IllegalArgumentException("Input string contains a char(" + ca[i] + ") that is not exist in char table");
				}
				l = l * baseX + value;
			}
		}
		return l;
	}

	protected String align(String s, int length) {
		int strLength = s.length();
		if (strLength < length) {
			char[] array = new char[length];
			int count = length - strLength;
			char zeroChar = charTable[0];
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
}
