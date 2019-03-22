package de.codecentric.fpl.datatypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.list.FplList;

public class UnWrapperTest {

	@Test
	public void testInstanciateUnWrapper() {
		new UnWrapper(); // cover no-op constructor
	}
	
	@Test
	public void testUnwrapFplWrapper() {
		FplWrapper w = new FplWrapper(Integer.valueOf(42));
		Object u = UnWrapper.unwrap(w);
		assertEquals(Integer.valueOf(42), u);
	}

	@Test
	public void testWrapList() throws EvaluationException {
		List<Integer> list = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			list.add(i);
		}
		FplList wrapped = (FplList) UnWrapper.wrap(list);
		assertEquals(10, wrapped.size());
		for (int i = 0; i < 10; i++) {
			assertEquals(i, ((FplInteger)wrapped.get(i)).getValue());
		}
	}

	@Test
	public void testWrapUnknownClass() throws EvaluationException {
		FplWrapper obj = (FplWrapper) UnWrapper.wrap(this);
		assertTrue(this == obj.getInstance());
	}
	

	@Test
	public void testWrapObject() throws EvaluationException {
		Map<Object, Object> map = new HashMap<>();
		map.put("key", "value");
		FplObject obj = (FplObject) UnWrapper.wrap(map);
		assertEquals("value", ((FplString)obj.get("key")).getContent());
	}
	
	@Test
	public void testWrapObjectWithEmptyKey() throws EvaluationException {
		Map<Object, Object> map = new HashMap<>();
		map.put("", "value");
		try {
			UnWrapper.wrap(map);
			fail("exception missing");
		} catch (EvaluationException e) {
			assertEquals("nil or \"\" is not a valid name", e.getLocalizedMessage());
		}
	}
	
	@Test
	public void testWrapObjectWithNonStringKey() throws EvaluationException {
		Map<Object, Object> map = new HashMap<>();
		map.put(Integer.valueOf(1), "one");
		FplWrapper wrapper = (FplWrapper) UnWrapper.wrap(map);
		assertTrue(wrapper.getInstance() instanceof Map);
		assertTrue(map == wrapper.getInstance());
	}
	
	@Test
	public void testWrapArray() throws EvaluationException {
		Integer[] a = new Integer[3];
		for (int i = 0; i < a.length; i++) {
			a[i] = i;
		}
		FplList list = (FplList) UnWrapper.wrap(a);
		assertEquals(3, list.size());
		for (int i = 0; i < a.length; i++) {
			assertEquals(a[i].intValue(), ((FplInteger)list.get(i)).getValue());
		}
	}
	
	@Test
	public void testUnwrapList() throws EvaluationException {
		FplValue[] values = new FplValue[4];
		for (int i = 0; i < values.length; i++) {
			values[i] = FplInteger.valueOf(i);
		}
		List<?> list = (List<?>) UnWrapper.unwrap(new FplList(values));
		assertEquals(values.length, list.size());
		for (int i = 0; i < values.length; i++) {
			assertEquals(Long.valueOf(i), list.get(i));
		}
	}
	
	@Test
	public void testUnwrapObject() throws Exception {
		FplObject obj = new FplObject();
		obj.put("foo", new FplString("bar"));
		Map<?, ?> map = (Map<?, ?>) UnWrapper.unwrap(obj);
		assertEquals("bar", map.get("foo"));
	}
}
