package de.codecentric.fpl.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.codecentric.fpl.EvaluationException;
import de.codecentric.fpl.datatypes.EvaluatesToThisValue;
import de.codecentric.fpl.datatypes.FplString;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.fpl.datatypes.Named;

public class PipelineScopeTest {
	private PipelineScope scope;

	@BeforeEach
	public void before() {
		scope = new PipelineScope("$", null);
	}

	@AfterEach
	public void after() {
		scope = null;
	}

	@Test
	public void nullSymbolFails() throws ScopeException {
		assertThrows(IllegalArgumentException.class, () -> {
			new PipelineScope(null, null);
		});
	}

	@Test
	public void emptySymbolFails() throws ScopeException {
		assertThrows(IllegalArgumentException.class, () -> {
			new PipelineScope("", null);
		});
	}

	@Test
	public void emptyScope() {
		assertNull(scope.get("foo"));
		assertNull(scope.get("$"));
	}

	@Test
	public void set() {
		scope.set(new FplString("foo"));
		assertEquals(new FplString("foo"), scope.get("$"));
	}

	@Test
	public void put() throws ScopeException {
		scope.put("foo", new FplString("bar"));
		assertEquals(new FplString("bar"), scope.get("foo"));
		assertNull(scope.get("$"));
	}

	@Test
	public void putPipeSymbol() throws ScopeException {
		assertThrows(ScopeException.class, () -> {
			scope.put("$", new FplString("foo"));
		});
	}

	@Test
	public void replace() throws ScopeException {
		scope.put("foo", new FplString("baz"));
		scope.replace("foo", new FplString("bar"));
		assertEquals(new FplString("bar"), scope.get("foo"));
		assertNull(scope.get("$"));
	}

	@Test
	public void replacePipeSymbol() throws ScopeException {
		assertThrows(ScopeException.class, () -> {
			scope.replace("$", new FplString("foo"));
		});
	}
	
	@Test
	public void define() throws ScopeException {
		scope.define("foo", new FplString("bar"));
		assertEquals(new FplString("bar"), scope.get("foo"));
		assertNull(scope.get("$"));
	}

	@Test
	public void definePipeSymbol() throws ScopeException {
		assertThrows(ScopeException.class, () -> {
			scope.define("$", new FplString("foo"));
		});
	}

	@Test
	public void defineNamed() throws ScopeException {
		scope.define(new TestNamed("foo"));
		assertEquals(new TestNamed("foo"), scope.get("foo"));
		assertNull(scope.get("$"));
	}

	@Test
	public void defineNamedPipeSymbol() throws ScopeException {
		assertThrows(ScopeException.class, () -> {
			scope.define(new TestNamed("$"));
		});
	}

	@Test
	public void testToString() throws ScopeException {
		assertEquals("PipelineScope<pipeline-$>", scope.toString());
	}

	@Test
	public void iterator() throws ScopeException {
		scope.define("foo", new FplString("bar"));
		scope.set(new FplString("dollar"));
		int count = 0;
		Iterator<Entry<String, FplValue>> iter = scope.iterator();
		while (iter.hasNext()) {
			count++;
			Entry<String, FplValue> next = iter.next();
			assertTrue(next.getKey().equals("foo") || next.getKey().equals("$"));
		}
		assertEquals(2, count);
	}
	
	@Test
	public void keySet() throws ScopeException {
		scope.define("foo", new FplString("bar"));
		scope.set(new FplString("dollar"));
		Set<String> keySet = scope.keySet();
		assertEquals(2, keySet.size());
		assertTrue(keySet.contains("$"));
		assertTrue(keySet.contains("foo"));
	}
	
	@Test
	public void values() throws ScopeException {
		scope.define("foo", new FplString("bar"));
		scope.set(new FplString("dollar"));
		Collection<FplValue> values = scope.values();
		assertEquals(2, values.size());
		assertTrue(values.contains(new FplString("bar")));
		assertTrue(values.contains(new FplString("dollar")));
	}
	
	@Test
	public void entrySet() throws ScopeException {
		scope.define("foo", new FplString("bar"));
		scope.set(new FplString("dollar"));
		Set<Entry<String, FplValue>> entrieSet = scope.entrieSet();
		assertEquals(2, entrieSet.size());
	}
	
	private static class TestNamed implements Named, EvaluatesToThisValue {
		private final String name;
		
		public TestNamed(String name) {
			this.name = name;
		}
		
		@Override
		public FplValue evaluate(Scope scope) throws EvaluationException {
			return this;
		}

		@Override
		public String typeName() {
			return "TestNamed";
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TestNamed other = (TestNamed) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}
	}
}
