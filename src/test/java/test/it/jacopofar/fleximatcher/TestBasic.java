package test.it.jacopofar.fleximatcher;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import it.jacopofar.fleximatcher.FlexiMatcher;
import it.jacopofar.fleximatcher.annotations.AnnotationHandler;
import it.jacopofar.fleximatcher.annotations.DefaultAnnotationHandler;
import it.jacopofar.fleximatcher.annotations.MatchingResults;
import it.jacopofar.fleximatcher.annotations.ResultPrintingAnnotationHandler;

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
		assertTrue("identity",fm.matches("AbC", "AbC"));
		assertTrue(fm.matches("AbC", "A[i:B]C"));
		assertFalse(fm.matches("AbC", "ABC"));
		assertTrue(fm.matches("AbC", "A[i:B]C"));
	}
	@Test
	public void testRegex() {
		assertTrue("identity",fm.matches("AbC", "AbC"));
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
		ResultPrintingAnnotationHandler ah = new ResultPrintingAnnotationHandler("AbC");
		assertTrue("nested multi",fm.matches("AbC", "A[multi:[r:[bD]+][i:B][multi:b]]C",ah, true,true,true).isMatching());
		assertEquals("number of annotations at top level",3.0,ah.getAnnotationsAtThisLevelStream().count(),0.0);

	}

	@Test
	public void testTag() {
		assertTrue("identity",fm.matches("AbC", "AbC"));
		fm.addTagRule("fruit", "apple", "id_apple");
		assertTrue("simple tag",fm.matches("apple", "[tag:fruit]"));
		fm.addTagRule("fruit", "an [tag:fruit]", "id_an_fruit");
		assertTrue("nested tag",fm.matches("an apple", "[tag:fruit]"));
		fm.addTagRule("fruit", "pear", "id_pear");
		fm.addTagRule("fruit", "a [tag:fruit]", "id_a_fruit");
		assertTrue("nested tag",fm.matches("an apple", "[tag:fruit]"));
		assertTrue("nested tag",fm.matches("a pear", "[tag:fruit]"));
		assertFalse("nested tag",fm.matches("a", "[tag:fruit]"));
		assertFalse("nested tag",fm.matches("an", "[tag:fruit]"));
		AnnotationHandler ah = new DefaultAnnotationHandler();
		//match substrings, it will match both "an apple" and "apple"
		MatchingResults res = fm.matches("an apple", "[tag:fruit]",ah, true,false,true);
		assertTrue("nested multi",res.isMatching());
		assertTrue(res.getAnnotations().isPresent());
		assertEquals("number of annotations at top level",2,res.getAnnotations().get().size(),0.0);
		//match the whole string, it will match only "an apple"
		ah = new DefaultAnnotationHandler();
		res = fm.matches("an apple", "[tag:fruit]",ah, true,true,true);
		assertTrue("nested multi",res.isMatching());
		assertTrue(res.getAnnotations().isPresent());
		assertEquals("number of annotations at top level",1,res.getAnnotations().get().size(),0.0);
		
		//match substrings, it will match both "an apple" and "apple"
		System.out.println("\n\n\n\n\n\n\n\n");
		String multiple="an apple and a pear";
		ah = new DefaultAnnotationHandler();
		res = fm.matches(multiple, "[tag:fruit]",ah, true,false,true);
		assertTrue("nested multi",res.isMatching());
		List<String> matched = Arrays.asList(res.getMatchingStrings(multiple));
		assertTrue("contains 'apple'",matched.contains("apple"));
		assertTrue("contains 'a pear'",matched.contains("a pear"));
		assertTrue("contains 'pear'",matched.contains("pear"));
		assertTrue("contains 'an apple'",matched.contains("an apple"));
		assertTrue(res.getAnnotations().isPresent());
		assertEquals("number of annotations at top level",4,res.getAnnotations().get().size(),0.0);
		
		fm.removeTagRule("fruit","id_a_fruit");
		ah = new DefaultAnnotationHandler();
		res = fm.matches(multiple, "[tag:fruit]",ah, true,false,true);
		assertEquals("number of annotations at top level",3,res.getAnnotations().get().size(),0.0);
		matched = Arrays.asList(res.getMatchingStrings(multiple));
		assertTrue("contains 'apple'",matched.contains("apple"));
		assertTrue("contains 'pear'",matched.contains("pear"));
		assertTrue("contains 'an apple'",matched.contains("an apple"));
		
		//fm = new FlexiMatcher();
		assertTrue(fm.addTagRule("fruit", "apple", "id_apple","{'fruit':'apple'}"));
		assertTrue(fm.addTagRule("fruit", "an [tag:fruit]", "id_an_fruit","{'fruit':'#1#'}"));
		assertTrue(fm.addTagRule("fruit", "pear", "id_pear","{'fruit':'pear'}"));
		assertFalse(fm.addTagRule("fruit", "a [tag:fruit]", "id_a_fruit","{'fruit':'#1#'}"));
		ah = new DefaultAnnotationHandler();
		res = fm.matches(multiple, "[tag:fruit]",ah, true,false,true);
		matched = Arrays.asList(res.getMatchingStrings(multiple));
		assertEquals("number of annotations at top level",4,res.getAnnotations().get().size(),0.0);
		assertTrue("contains 'apple'",matched.contains("apple"));
		assertTrue("contains 'pear'",matched.contains("pear"));
		assertTrue("contains 'an apple'",matched.contains("an apple"));
	}
}
