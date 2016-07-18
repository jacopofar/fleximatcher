package test.com.github.jacopofar.fleximatcher;

import com.github.jacopofar.fleximatcher.FlexiMatcher;
import com.github.jacopofar.fleximatcher.annotations.MatchingResults;
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
        fm.addTagRule("letter", "a", "letter_1");
        //fm.addTagRule("letter", "a", "letter_1","{letter:#0#}");
        fm.addTagRule("letter", "b", "letter_2","{letter:#0#}");
        fm.addTagRule("letter", "c", "letter_3","{letter:#0#}");
        fm.addTagRule("letter", "d", "letter_4","{letter:#0#}");

        //fm.addTagRule("letters", "[tag:letter]", "letters_1","{'letters':[#0.letter#]}");
        //fm.addTagRule("letters", "[tag:letter],[tag:letters]", "letters_2","{'letters':[#0.letter#, #2.letters#]}");

        fm.addTagRule("letters", "[tag:letter]", "letters_1");
        fm.addTagRule("letters", "[tag:letter][tag:letters]","letters_2");


        String multiple = "aaa";
        //not passing?
        //String multiple = "aaaa";
        //not passing when adding a , to the pattern definition
        //String multiple = "a,a,a";

        MatchingResults res = fm.matches(multiple, "[tag:letters]", FlexiMatcher.getDefaultAnnotator(), true, true, true);
        if(res.isMatching()){
            System.out.println("is matching!!");
        }
        else{
            System.out.println("is NOT matching :(");

        }
        res.getAnnotations().ifPresent(ints -> {
            System.out.println(" there are " + ints.size() + " interpretations");
            ints.forEach(al -> {
                        System.out.println(" -- ");
                        al.forEach(ann -> System.out.println("     " + ann.toString()));
                    }
            );
        });
        res.getFlatAnnotations().forEach(a -> {
            System.out.println("      " + a.getJSON().toString());
        });
        //TODO define a complete test batch for annotations, but first decide how to define them consistently
    }
}
