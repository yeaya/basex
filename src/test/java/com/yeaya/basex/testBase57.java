package com.yeaya.basex;

import java.util.Random;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import com.yeaya.basex.LangHelper;

import static org.junit.Assert.*;

public class testBase57 {
	@Test
	public void testMax() {
		for (int i = 2; i < 94; i++) {
			checkPositive(i);
			checkNegative(i);
		}
	}
	
	public void checkPositive(int basex) {		
		long l = 0x7f;
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 10; j++)
				check(l - j);
			l = (l << 8) + 0xFF;
		}
	}
	
	public void checkNegative(int basex) {		
		long l = 0xff;
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 10; j++)
				check(l + j);
			l = (l << 8) + 0xFF;
		}
	}
	
	public void check(long l) {
		String s = Base57.encode(l);
		long l2 = Base57.decode(s);
		if (l != l2) {
			s = Base57.encode(l);
			l2 = Base57.decode(s);
			assertEquals(l, l2);
		}
	}
	
	@Test
	public void testEncodeUUID() {
		for (int i = 2; i < 94; i++) {
			checkEncodeUUID(i);
		}
	}

	public void checkEncodeUUID(int basex) {
		long begin = System.currentTimeMillis();

		for (int i = 0; i < 10000; i++) {
			UUID uuid = UUID.randomUUID();
			String s = Base57.encodeUuid(uuid);
			UUID uuid2 = Base57.decodeUuid(s);
            assertEquals(uuid.toString(), uuid2.toString());
        }
        long cost = System.currentTimeMillis() - begin;
        System.out.println("BaseX X=" + basex + " testEncodeUUID end cost: " + cost);
	}

	@Test
	public void testEncode() {
		for (int i = 2; i < 94; i++) {
			checkRandom(i);
		}
	}
	
	public void checkRandom(int basex) {
		BaseX Base57 = BaseX.create(basex);

		long begin = System.currentTimeMillis();
    	Random r = new Random();

        for (int i = 0; i < 10000; i++) {
    	//	uuid = UUID.randomUUID();
        	long ll = r.nextLong();
        	String s = Base57.encode(ll);
        	long ll2 = Base57.decode(s);
            assertEquals(ll, ll2);

        	if (ll != ll2) {
        		s = Base57.encode(ll);
            	ll2 = Base57.decode(s);

            	s = Base57.encode(ll);
            	ll2 = Base57.decode(s);
            	
        		System.out.println("error i=" + i + " ll=" + ll + " ll2=" + ll2 + " s=" + s);
        		break;
        	}
        }
        long cost = System.currentTimeMillis() - begin;
        System.out.println("BaseX X=" + basex + " testEncode end cost: " + cost);

	}

	@Test
	public void testEncodeBytes() {
		for (int i = 2; i < 94; i++) {
			checkEncodeBytes(i);
		}
	}
	
	public void checkEncodeBytes(int basex) {
		BaseX Base57 = BaseX.create(basex);

    	Random r = new Random();
    	byte[] data = new byte[128];
    	for (int i = 0; i < data.length; i++) {
    		data[i] = (byte)r.nextInt();
    	}

    	data[0] = (byte)-1;
    	for (int i = 0; i < data.length; i++) {
    		byte[] d2 = new byte[i];
    		for (int j = 0; j < i; j++) {
    			d2[j] = data[j];
    		}
    		String encode = Base57.encodeBytes(d2);
    		byte[] d3 = Base57.decodeBytes(encode);
    		if (d3 == null || !LangHelper.bytesEquals(d2, d3)) {
        		encode = Base57.encodeBytes(d2);

        		d3 = Base57.decodeBytes(encode);

                System.out.println("Base57 testEncodeBytes 57 error");
    		}
    	//	String encoded = Base64.encodeBase64URLSafeString(d2);
    	//	byte[] d5 = Base64.decodeBase64(encoded);
         //   System.out.println("Base57 testEncodeBytes 57 length=" + i + " " + base57.length() + " " + base57);
          //  System.out.println("Base57 testEncodeBytes 64 length=" + i + " " + encoded.length() + " " + encoded);
    	}
	}
	
	@Test
	public void testPerformance() {
		for (int i = 2; i < 94; i++) {
			checkPerformance(i);
		}
	}

	public void checkPerformance(int basex) {		
		long begin = System.currentTimeMillis();

    	Random r = new Random();
    	byte[] data = new byte[1024];
    	for (int p = 0; p < 5; p++) {
	    	for (int i = 0; i < data.length; i++) {
	    		data[i] = (byte)r.nextInt();
	    	}
	
	    	data[0] = (byte)-1;
	    	for (int i = 0; i < data.length; i++) {
	    		byte[] d2 = new byte[i];
	    		for (int j = 0; j < i; j++) {
	    			d2[j] = data[j];
	    		}
	    		String base57 = Base57.encodeBytes(d2);
	    		byte[] d3 = Base57.decodeBytes(base57);
	    		if (d3 == null || !LangHelper.bytesEquals(d2, d3)) {
	        		base57 = Base57.encodeBytes(d2);
	
	        		d3 = Base57.decodeBytes(base57);
	
	                System.out.println("Base57 testPerformance error");
	    			assertFalse(true);
	    		}
	    	}
    	}
    	long cost = System.currentTimeMillis() - begin;
        System.out.println("Base57 testPerformance end cost: " + cost);
	}
}
