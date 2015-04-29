package com.yeaya.basex;

public class LangHelper {

	public static void long2Bytes(long value, byte[] bytes, int offset) {
		int count = 8;
		if (bytes.length - offset < 8) {
			count = bytes.length - offset;
		}
        for (int i = 0; i < count; i++) {
            bytes[offset + i] = (byte) ((value >> 8 * i) & 0xFF);
        }
    }
	
	public static long bytes2Long(byte[] bytes, int offset) {
		int count = 8;
		if (bytes.length - offset < 8) {
			count = bytes.length - offset;
		}
		
	    long num = 0;
	    byte high = bytes[offset + count - 1];
		if (high < 0) {
			num = -1;
		}
	    for (int i = count - 1; i >= 0; --i) {  
	        num <<= 8;  
	        num |= (bytes[i + offset] & 0xFF);  
	    }
	    return num;
	}

	public static boolean bytesEquals(byte[] b1, byte[] b2) {
		if (b1.length != b2.length) {
			return false;
		}
		for (int i = 0; i < b1.length; i++) {
			if (b1[i] != b2[i]) {
				return false;
			}
		}
		return true;
	}
}
