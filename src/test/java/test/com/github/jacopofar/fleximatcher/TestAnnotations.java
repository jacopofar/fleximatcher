package test.com.github.jacopofar.fleximatcher;

import com.github.jacopofar.fleximatcher.FlexiMatcher;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

public class TestAnnotations {

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	private FlexiMatcher fm;

	@Before
	public void setUp() throws Exception {
		fm = new FlexiMatcher();		
	}


	@Test
	public void testTag() {

		fm.addTagRule("fruit", "apple", "id_apple","{fruit:apple'}");
        fm.addTagRule("fruit", "an [tag:fruit]", "id_an_fruit","{'internal_fruit':#1.fruit#,'article':'an'}");
		fm.addTagRule("fruit", "pear", "id_pear","{'fruit':'pear'}");
		fm.addTagRule("fruit", "a [tag:fruit]", "id_a_fruit","{'internal_fruit':#1.fruit#,'article':#0#}");


        String multiple = "eggs, water,flour, sugar and magic dust";
        //TODO define a complete test batch for annotations, but first decide how to define them consistently
	}
}
