package x.commons.util.http;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class SimpleHttpMessageTest {
	
	@Test
	public void test() {
		SimpleHttpMessage sug = new SimpleHttpMessage() {};
		// addHeader
		sug.addHeader("h_h1", "1");
		sug.addHeader("H_h1", "2");
		sug.addHeader("H-H2", "3");
		sug.addHeader("h-H2", "4");
		
		List<String> list = sug.getHeaders("H_H1");
		assert(list.size() == 2);
		assertTrue(list.contains("1"));
		assertTrue(list.contains("2"));
		
		list = sug.getHeaders("h-h2");
		assert(list.size() == 2);
		assertTrue(list.contains("3"));
		assertTrue(list.contains("4"));
		
		// setFirstHeader
		sug.setFirstHeader("H_H1", "5");
		sug.setFirstHeader("H_H3", "6");
		sug.setFirstHeader("H_H3", "7");
		sug.setFirstHeader("H_H3", "8");
		list = sug.getHeaders("H_H1");
		assertTrue(list.size() == 2);
		assertTrue(sug.getFirstHeader("H_H1").equals("5"));
		
		list = sug.getHeaders("H_H3");
		assertTrue(list.size() == 1);
		assertTrue(list.contains("8"));
		
		// setHeaders
		List<String> headers = new ArrayList<String>(Arrays.asList("10", "11", "12"));
		sug.setHeaders("h_H1", headers);
		list = sug.getHeaders("H_H1");
		assertTrue(list.size() == 3);
		assertTrue(list.contains("10"));
		assertTrue(list.contains("11"));
		assertTrue(list.contains("12"));
		
		// containsHeader
		assertTrue(sug.containsHeader("H_h1"));
		assertTrue(!sug.containsHeader("H_h4"));
		
		// removeHeader
		assertTrue(sug.removeHeader("h_h1", "11"));
		list = sug.getHeaders("H_H1");
		assertTrue(list.size() == 2);
		assertTrue(list.contains("10"));
		
		// removeHeaders
		assertTrue(sug.containsHeader("h-h2"));
		assertTrue(!sug.removeHeaders("h_h8"));
		assertTrue(sug.removeHeaders("h-h2"));
		
		// getHeaderNames
		Set<String> set = sug.getHeaderNames();
		assertTrue(set.size() == 2);
		assertTrue(set.contains("h_H1"));
		assertTrue(set.contains("H_H3"));
		
		// getFirsetHeader
		assertTrue(sug.getFirstHeader("xxx") == null);
		assertTrue(sug.getFirstHeader("h_h1").equals("10"));
		assertTrue(sug.getFirstHeader("h_h3").equals("8"));
		
		// getLastHeader
		assertTrue(sug.getLastHeader("yyy") == null);
		assertTrue(sug.getLastHeader("h_h1").equals("12"));
		assertTrue(sug.getLastHeader("h_h3").equals("8"));
	}
	
}
