package test.com.github.jacopofar.fleximatcher;

import com.github.jacopofar.fleximatcher.FlexiMatcher;
import com.github.jacopofar.fleximatcher.annotations.AnnotationHandler;
import com.github.jacopofar.fleximatcher.annotations.DefaultAnnotationHandler;
import com.github.jacopofar.fleximatcher.annotations.MatchingResults;
import com.github.jacopofar.fleximatcher.annotations.ResultPrintingAnnotationHandler;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;

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
		//check that a deeper match works, too
		assertFalse(fm.matches("AbC", "A[i:b][r:k]", FlexiMatcher.getDefaultAnnotator(), true, false, true).isMatching());
		assertTrue(fm.matches("AbC", "A[i:b][r:[cC]]", FlexiMatcher.getDefaultAnnotator(), true, false, true).isMatching());
		assertTrue(fm.matches("eeAbC", "A[i:b][r:[cC]]", FlexiMatcher.getDefaultAnnotator(), true, false, true).isMatching());

		//regex refuse an empty pattern
		assertThatThrownBy(() -> {
			fm.matches("eeAbC", "A[i:b][r]", FlexiMatcher.getDefaultAnnotator(), true, false, true).isMatching();
		}).hasMessageContaining("Cannot create a regex annotator with an empty pattern");

		assertThatThrownBy(() -> {
			fm.matches("eeAbC", "A[i:b][r]", FlexiMatcher.getDefaultAnnotator(), true, false, true).isMatching();
		}).hasMessageContaining("Cannot create a regex annotator with an empty pattern");

		fm.matches("eeAbC", "A[i:b][r:k?]", FlexiMatcher.getDefaultAnnotator(), true, false, true).isMatching();
	}
	@Test
	public void testExpressionExceptions() {
		//generate proper exceptions for these ones
		assertThatThrownBy(() -> {
			fm.matches("AbC", "[r:Ab");
		}).hasMessageContaining("brackets are unbalanced in pattern");

		assertThatThrownBy(() -> {
			fm.matches("AbC", "[r:");
		}).hasMessageContaining("brackets are unbalanced in pattern");
		assertThatThrownBy(() -> {
			fm.matches("yeeee", "[i:   [");
		}).hasMessageContaining("brackets are unbalanced in pattern");
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
		/*
		Expected annotations:

		position 0:
		   A:[0..1)
		position 1:
          [r:[bD]+]:[1..2)
          [i:B]:[1..2)
          b:[1..2)
          [multi:b]:[1..2)
          [multi:[r:[bD]+][i:B][multi:b]]:[1..2)
		position 2:
		   C:[2..3)
*/
		assertEquals("number of annotations:",7.0,ah.getAnnotationsCount(),0.0);
        assertEquals("A[multi:[r:[bD]+][i:B]]C", fm.generateSample("A[multi:[r:[bD]+][i:B]]C"));
	}

	@Test
	public void testChar() {
		assertTrue("identity",fm.matches("AbC", "AbC"));
		assertTrue(fm.matches("AbC", "A[char:98]C"));
        assertTrue(fm.matches("AbC", "A[char:0x62]C"));

        assertTrue(fm.matches("aaèr", "aa[char:0xe8]r"));
        assertTrue(fm.matches("aaèr", "aa[char:0xE8]r"));

        assertFalse(fm.matches("aaér", "aa[char:0xe8]r"));
        assertFalse(fm.matches("aaér", "aa[char:0xE8]r"));
        //U+1f617 is an emoji, and in UTF-16 becomes a surrogate pair
        assertTrue(fm.matches("the emoji " + new String(Character.toChars(Integer.parseInt("1f617",16))) + " works, too", "the emoji [char:0x1f617] works, too"));

    }

	@Test
	public void testTag() {
		assertTrue("identity",fm.matches("AbC", "AbC"));
		assertFalse( "the rule doesn't appear before being inserted",fm.getTagRule("fruit","id_apple").isPresent());
		fm.addTagRule("fruit", "apple", "id_apple");
		assertTrue("the rule appear after being inserted", fm.getTagRule("fruit","id_apple").isPresent());
		assertTrue("simple tag",fm.matches("apple", "[tag:fruit]"));
		fm.addTagRule("fruit", "an [tag:fruit]", "id_an_fruit");
		assertTrue("nested tag",fm.matches("an apple", "[tag:fruit]"));
		fm.addTagRule("fruit", "pear", "id_pear");
		fm.addTagRule("fruit", "a [tag:fruit]", "id_a_fruit");
		assertTrue("nested tag",fm.matches("an apple", "[tag:fruit]"));
		assertTrue("nested tag",fm.matches("a pear", "[tag:fruit]"));
		assertFalse("nested tag",fm.matches("a", "[tag:fruit]"));
		assertFalse("nested tag",fm.matches("an", "[tag:fruit]"));

        //check that recursion works
        assertTrue(fm.matches("apple","[tag:fruit]"));
        assertTrue(fm.matches("an apple","[tag:fruit]"));
        assertTrue(fm.matches("an an apple","[tag:fruit]"));
        assertTrue(fm.matches("an a an apple","[tag:fruit]"));
        assertTrue(fm.matches("an an an an an an a an apple","[tag:fruit]"));

        //check that it works even after removing and adding back the terminal
        fm.removeTagRule("fruit","id_apple");
        fm.addTagRule("fruit", "apple", "id_apple");

        assertTrue(fm.matches("apple","[tag:fruit]"));
        assertTrue(fm.matches("an apple","[tag:fruit]"));
        assertTrue(fm.matches("an an apple","[tag:fruit]"));


        //now check it's working on rules calling in turn
        fm.addTagRule("clockwise", "clockwise", "id_c");
        fm.addTagRule("anticlockwise", "anticlockwise", "id_ac");
        fm.addTagRule("clockwise", "anti[tag:anticlockwise]", "id_cr");
        fm.addTagRule("anticlockwise", "anti[tag:clockwise]", "id_acr");

        assertTrue(fm.matches("clockwise","[tag:clockwise]"));
        assertTrue(fm.matches("antianticlockwise","[tag:clockwise]"));
        assertFalse(fm.matches("anticlockwise","[tag:clockwise]"));
        assertTrue(fm.matches("antiantiantianticlockwise","[tag:clockwise]"));
        assertTrue(fm.matches("antiantiantiantiantianticlockwise","[tag:clockwise]"));
        assertFalse(fm.matches("antiantiantiantianticlockwise","[tag:clockwise]"));

        fm.removeTagRule("clockwise","id_cr");
        fm.addTagRule("clockwise", "anti[tag:anticlockwise]", "id_cr");

        assertTrue(fm.matches("clockwise","[tag:clockwise]"));
        assertTrue(fm.matches("antianticlockwise","[tag:clockwise]"));
        assertFalse(fm.matches("anticlockwise","[tag:clockwise]"));
        assertTrue(fm.matches("antiantiantianticlockwise","[tag:clockwise]"));
        assertTrue(fm.matches("antiantiantiantiantianticlockwise","[tag:clockwise]"));
        assertFalse(fm.matches("antiantiantiantianticlockwise","[tag:clockwise]"));

        assertTrue(fm.matches("an an apple","[tag:fruit]"));
        assertTrue(fm.matches("an an apple","[tag:fruit]"));


        //generated expressions effectively match the pattern
        for(int i=1; i<20; i++){
            String sample = fm.generateSample("[tag:clockwise]");
            assertTrue(fm.matches(sample,"[tag:clockwise]"));

            sample = fm.generateSample("try a [tag:fruit]");
            assertTrue(fm.matches(sample,"try a [tag:fruit]"));
        }


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
		assertFalse(fm.removeTagRule("fruit","id that doesn't exist"));
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

		fm.addTagRule("orphantag", "hello", "id1");
		assertTrue("future orphan tag rule is present", fm.getTagRule("orphantag","id1").isPresent());
		assertTrue("future orphan tag name is present",fm.getTagNames().anyMatch(p -> p.equals("orphantag")));
		fm.removeTagRule("orphantag","id1");
		assertFalse("orphan tag name is not present anymore",fm.getTagNames().anyMatch(p -> p.equals("orphantag")));

	}
}
