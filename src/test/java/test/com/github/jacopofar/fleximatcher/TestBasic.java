package test.com.github.jacopofar.fleximatcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import com.github.jacopofar.fleximatcher.FlexiMatcher;
import com.github.jacopofar.fleximatcher.annotations.AnnotationHandler;
import com.github.jacopofar.fleximatcher.annotations.DefaultAnnotationHandler;
import com.github.jacopofar.fleximatcher.annotations.MatchingResults;
import com.github.jacopofar.fleximatcher.annotations.ResultPrintingAnnotationHandler;

import java.util.Arrays;
import java.util.List;

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
//		new TextAnnotation(new Span(3,8), "[tag:fruit]", new JSONObject("{"fruit":"apple"}"));
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
		assertEquals("number of annotations:",8.0,ah.getAnnotationsCount(),0.0);

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
		
		assertTrue(fm.removeTagRule("fruit","id_a_fruit"));
		assertFalse(fm.removeTagRule("fruit","id_a_fruit"));
		assertFalse(fm.removeTagRule("fruit","id thatdoesn't exist"));
		ah = new DefaultAnnotationHandler();
		res = fm.matches(multiple, "[tag:fruit]",ah, true,false,true);
		assertEquals("number of annotations at top level",3,res.getAnnotations().get().size(),0.0);
		matched = Arrays.asList(res.getMatchingStrings(multiple));
		assertTrue("contains 'apple'",matched.contains("apple"));
		assertTrue("contains 'pear'",matched.contains("pear"));
		assertTrue("contains 'an apple'",matched.contains("an apple"));
		
		assertTrue(fm.addTagRule("fruit", "apple", "id_apple","{'fruit':'apple'}"));
		assertTrue(fm.addTagRule("fruit", "an [tag:fruit]", "id_an_fruit","{'internal_fruit':#1.fruit#,'article':'an'}"));
		assertTrue(fm.addTagRule("fruit", "pear", "id_pear","{'fruit':'pear'}"));
		assertFalse(fm.addTagRule("fruit", "a [tag:fruit]", "id_a_fruit","{'internal_fruit':#1.fruit#,'article':#0#}"));
		
		ah = new DefaultAnnotationHandler();
		res = fm.matches(multiple, "[tag:fruit]",ah, true,false,true);
		matched = Arrays.asList(res.getMatchingStrings(multiple));
		assertEquals("number of annotations at top level",4,res.getAnnotations().get().size(),0.0);
		assertTrue("contains 'apple'",matched.contains("apple"));
		assertTrue("contains 'pear'",matched.contains("pear"));
		assertTrue("contains 'an apple'",matched.contains("an apple"));
		
		assertTrue(res.getFlatAnnotations().anyMatch(
				p->p.getJSON()==null?false:p.getJSON().get().optString("internal_fruit").equals("apple")));
		assertTrue(res.getFlatAnnotations().anyMatch(
				p->p.getJSON()==null?false:p.getJSON().get().optString("internal_fruit").equals("pear")));
		
	}
}
