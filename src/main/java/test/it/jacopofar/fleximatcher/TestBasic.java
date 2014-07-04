package test.it.jacopofar.fleximatcher;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import it.jacopofar.fleximatcher.FlexiMatcher;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

public class TestBasic {

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	private FlexiMatcher fm;

	@Before
	public void setUp() throws Exception {
		fm = new FlexiMatcher();		
	}

	@Test
	public void testInsensitive() {
		assertTrue(fm.matches("AbC", "AbC"));
		assertTrue(fm.matches("AbC", "A[i:B]C"));
		assertFalse(fm.matches("AbC", "ABC"));
		assertTrue(fm.matches("AbC", "A[i:B]C"));
	}
	@Test
	public void testRegex() {
		assertTrue(fm.matches("AbC", "AbC"));
		assertFalse(fm.matches("AbC", "A[r:[BD]+]C"));
		assertTrue(fm.matches("AbC", "A[r:[bD]+]C"));
		assertFalse(fm.matches("AbC", "ABC"));
		assertTrue(fm.matches("AbC", "[r:[KAb]{2}]C"));
		assertTrue(fm.matches("AbC", "[r:[AbD]{2}]C"));
	}
	@Test
	public void testMulti() {
		assertTrue("identity",fm.matches("AbC", "AbC"));
		assertTrue(fm.matches("AbC", "A[r:[bD]+]C"));
		assertTrue(fm.matches("AbC", "A[multi:[r:[bD]+][i:B]]C"));
		assertFalse(fm.matches("AbC", "A[r:[BD]+]C"));
		assertFalse(fm.matches("AbC", "ABC"));
	}

}
