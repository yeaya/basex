package com.yeaya.basex;

import java.util.Random;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import com.yeaya.basex.LangHelper;

import static org.junit.Assert.*;

public class testBaseX {
	
	@Test
	public void testCustomCharTable() {
		String sct = "当程序发生不可控的错误时，通常做法是通知用户并中止程序的执行。与异常不同的是Error及其子类的对象不应被抛出。";
		String charTable = BaseX.extractCharTable(sct);
		System.out.println("sct:" + sct);
		System.out.println("charTable:" + charTable);
		BaseX baseX = BaseX.create(charTable);
    	Random r = new Random();

		for (int i = 0; i < 1000; i++) {
			long l = r.nextLong();
			String s = baseX.encode(l);
			long ll = baseX.decode(s);
			if (l != ll) {
				 s = baseX.encode(l);
				 ll = baseX.decode(s);
				 s = baseX.encode(l);
				 ll = baseX.decode(s);
				assertEquals(l, ll);
			}
		}

	}
	@Test
	public void testCompare57() {
		for (int i = 0; i < 5; i++) {
			checkBaseX57(true);
			checkBaseX57(false);
		}
	}
	
	public void checkBaseX57(boolean isBase57) {
		BaseX baseX = BaseX.create();
		
		long begin = System.currentTimeMillis();

    	Random r = new Random();
    	byte[] data = new byte[1024];
    	for (int p = 0; p < 55; p++) {
	    	for (int i = 0; i < data.length; i++) {
	    		data[i] = (byte)r.nextInt();
	    	}
	
	    //	data[0] = (byte)-1;
	    	for (int i = 0; i < data.length; i++) {
	    		byte[] d2 = new byte[i];
	    		for (int j = 0; j < i; j++) {
	    			d2[j] = data[j];
	    		}
	    		String base57;
	    		byte[] d3;
	    		if (isBase57) {
	    			base57 = Base57.encodeBytes(d2);
	    			d3 = Base57.decodeBytes(base57);
	    		} else {
	    			base57 = baseX.encodeBytes(d2);
	    			d3 = baseX.decodeBytes(base57);
	    		}
	    		if (d3 == null || !LangHelper.bytesEquals(d2, d3)) {
	        		base57 = baseX.encodeBytes(d2);
	
	        		d3 = baseX.decodeBytes(base57);
	
	                System.out.println("BaseX testPerformance error");
	    			assertFalse(true);
	    		}
	    	}
    	}
    	long cost = System.currentTimeMillis() - begin;
        System.out.println("baseX isBase57=" + isBase57 + " checkBaseX57 end cost: " + cost);
	}
	
	@Test
	public void testBase57AndBaseX57() {
    	Random r = new Random();
    	BaseX baseX = BaseX.create();
    	BaseX baseX2 = BaseX.create(57);
    	for (int i = 0; i < 10000; i++) {
    		long l = r.nextLong();
    		String s1 = Base57.encode(l);
    		String s2 = baseX.encode(l);
    		String s3 = baseX2.encode(l);
    		assertEquals(s1, s2);
    		assertEquals(s1, s3);
    		long l1 = Base57.decode(s1);
    		long l2 = baseX.decode(s2);
    		long l3 = baseX2.decode(s3);
    		assertEquals(l1, l2);
    		assertEquals(l1, l3);
    	}
	}
	
	@Test
	public void testMax() {
		BaseX baseX2 = BaseX.create(57);

		BaseX baseX = BaseX.create(BaseX.BASE57_CHAR_TABLE);

		for (int i = 2; i < 94; i++) {
			checkPositive(i);
			checkNegative(i);
		}
	}
	
	public void checkPositive(int basex) {
		BaseX baseX = BaseX.create(basex);
		
		long l = 0x7f;
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 10; j++)
				check(l - j, baseX);
			l = (l << 8) + 0xFF;
		}
	}
	
	public void checkNegative(int basex) {
		BaseX baseX = BaseX.create(basex);
		
		long l = 0xff;
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 10; j++)
				check(l + j, baseX);
			l = (l << 8) + 0xFF;
		}
	}
	
	public void check(long l, BaseX baseX) {
		String s = baseX.encode(l);
		long l2 = baseX.decode(s);
		if (l != l2) {
			s = baseX.encode(l);
			l2 = baseX.decode(s);
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
		BaseX baseX = BaseX.create(basex);

		long begin = System.currentTimeMillis();

		for (int i = 0; i < 10000; i++) {
			UUID uuid = UUID.randomUUID();
			String s = baseX.encodeUuid(uuid);
			UUID uuid2 = baseX.decodeUuid(s);
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
		BaseX baseX = BaseX.create(basex);

		long begin = System.currentTimeMillis();
    	Random r = new Random();

        for (int i = 0; i < 10000; i++) {
    	//	uuid = UUID.randomUUID();
        	long ll = r.nextLong();
        	String s = baseX.encode(ll);
        	long ll2 = baseX.decode(s);
            assertEquals(ll, ll2);

        	if (ll != ll2) {
        		s = baseX.encode(ll);
            	ll2 = baseX.decode(s);

            	s = baseX.encode(ll);
            	ll2 = baseX.decode(s);
            	
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
		BaseX baseX = BaseX.create(basex);

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
    		String encode = baseX.encodeBytes(d2);
    		byte[] d3 = baseX.decodeBytes(encode);
    		if (d3 == null || !LangHelper.bytesEquals(d2, d3)) {
        		encode = baseX.encodeBytes(d2);

        		d3 = baseX.decodeBytes(encode);

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
		for (int i = 94; i >= 0; i--) {
			checkPerformance(i);
		}
	}

	public void checkPerformance(int basex) {
		BaseX baseX = BaseX.create(basex);
		
		long begin = System.currentTimeMillis();

    	Random r = new Random();
    	for (int p = 0; p < 10; p++) {
        	byte[] data = new byte[128 * p + 128];

	    	for (int i = 0; i < data.length; i++) {
	    		data[i] = (byte)r.nextInt();
	    	}
	
	    	if (p % 2 == 0) {
	    		data[0] = (byte)(0 - data[0]);
	    	}
	    	for (int i = 0; i < data.length; i++) {
	    		byte[] d2 = new byte[i];
	    		for (int j = 0; j < i; j++) {
	    			d2[j] = data[j];
	    		}
	    		String base57;
	    		byte[] d3;
	    		if (basex == 0) {
	    			base57 = Base64.encodeBase64String(d2);
	    			d3 = Base64.decodeBase64(base57);
	    		} else if (basex == 1) {
	    			base57 = Base57.encodeBytes(d2);
	    			d3 = Base57.decodeBytes(base57);
	    		} else {
	    			base57 = baseX.encodeBytes(d2);
	    			d3 = baseX.decodeBytes(base57);
	    		}
	    		if (d3 == null || !LangHelper.bytesEquals(d2, d3)) {
	        		base57 = baseX.encodeBytes(d2);
	
	        		d3 = baseX.decodeBytes(base57);
	
	                System.out.println("BaseX testPerformance error");
	    			assertFalse(true);
	    		}
	    	}
    	}
    	long cost = System.currentTimeMillis() - begin;
        System.out.println("baseX X=" + basex + " testPerformance end cost: " + cost);
	}
}
