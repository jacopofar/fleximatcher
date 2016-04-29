[![Build Status](https://travis-ci.org/jacopofar/fleximatcher.svg?branch=master)](https://travis-ci.org/jacopofar/fleximatcher)
What's this?
============

Fleximatcher is a Java 8 library to match text with arbitrary patterns, focused on natural language text.

Given a pattern string and a text string, Fleximatcher can:

* test whether the text matches the pattern
* test whether the text contains a substring which matches the pattern
* determine which parts of the string match the elements of the pattern, and specifically
  * in case of many possible interpretations, return all of them
  * in case the pattern elements have additional annotations defined, return them
* apply recursive patterns (that is, generative grammars)
Fleximatcher is made to be used as a library and embedded in applications, but a separate applications called fleximatcher web interface is available to expose a JSON interface and a web page to quickly check how patterns match with texts and which annotations they produce.

By itself, Fleximatcher is language agnostic and has no NLP capabilities out of the box, but you can map other libraries with pattern elements in a few lines to use them.
A mapping with Italian NLP components (PoS tagging, tokenizer and verb conjugations) is available

 [//]: # (TODO add and describe english rules) 

Fleximatcher patterns are made of rules and plain text. Rules are called using square brackets and possibly passing a string parameter. For example:

>_The [en-isa:animal] is my favourite animal_

will call the rule mapped with the name "_en-isa_" with the parameter "_animal_", letting the rule decide which parts of the string, if any, matches with it. The rule can also add JSON strings to the annotations, and can recursively call Fleximatcher to match the text against other patterns and retrieve their annotations.

Text outside squared brackets will be matched exactly against the string.

Some rules are already defined: i,r,multi and tag:

__i__ matches case insensitive text, using the Locale of the machine to do the comparison

[i:hello world] matches "hello world", "HELLO WORLD", "HelLo WorLD"

__r__ matches against Java regular expressions
_[r:[AX][1-9][0-9]*]_ matches "A12", "X299", "A2" but not "A01" or "E38"

__multi__ matches many rules with the same substring

_[multi:[[r:a[0-9]+[bB]+][i:a3b]]]_ will match "a3b", "a3B" but not "A3b" (it does not match the first rule) nor "a4b" (does not match the first rule). Given the text "a3BbB" it will match the first 3 characters, because the first rule match the string in many ways but only the first three characters are matched by the second one.

"a3bbBba3B" will be matched by the multi rule two times, at the first and the last two characters.

Note that in this example the result of multi is the same of [r:[aA]3[bB]], it is more useful when used to combine custom rules.

__tag__ is a rule used to match substrings against previously defined tags.
For example, if we can define the following mappings:

animals=>cats
animals=>cats

then match the string "I like [tag:animals]" to match "I like cats". Tags can be recursive, so we can define also:

animals=>the [tag:animals]

and match "I like the dogs". Tag mappings are rules of a generative grammar, and there's no need to normalize them. Fleximatcher will use a maximum depth of recursion (by default 5, can be changed at runtime using setMaximumTagNesting method) to avoid infinite loops, that means that if you have to use very complex tags rules you'll have to increase the maximum allowed depth or rewrite them, otherwise the library could stop the parsing.

 [//]: # (TODO lanciare eccezione per massima profondità raggiunta?) 

How to use it
=============

The main class necessary to use Fleximatcher is called Fleximatcher. The usage is as follow:

1. Create a Fleximatcher instance
2. Bind rules names with rules generator instances, if needed
3. Bind patterns with tag rules
4. Use the Fleximatcher instance to match text

the library is thread safe and can parse many strings in parallel, tags and rules mapping can be changed in any moment.

Define new tags
--------------

You can define a new tag by calling the addTag methods of FlexiMatcher.

For example:

	fm.addTagRule("fruit", "pear", "id_pear","{'fruit_name':'pear'}")
	fm.addTagRule("fruit", "a [tag:fruit]", "id_a_fruit","{'fruit_name':#1.fruit_name#,'article':#0#}")

using these rules, the pattern "[tag:fruit]" will annotate "pear" with {'fruit_name':'pear'} and "a pear" with {'fruit_name':'pear','article':'a '} (note the space after the article, the software doesn't give whitespaces a special meaning).
	
The first one allows the tag "fruit" to be called and match the string "pear", annotating it with the JSON string {'fruit':'pear'}
the second one, too, will add a new "fruit" tag (so you can use "[tag:fruit]" to refer to them), matching the pattern "a [tag:fruit]" and returning an annotation containing both the article "a " and the value of the "fruit_name" key associated with the second part of the pattern.

Note that you don't have to define tags in dependency order, so you can insert the second one first as well, the patterns will be evaluated onyl when actually matching a text.

The identifiers (like "id_a_fruit") are used to delete or overwrite the tag rule later. You can add, remove or replace tags in any moment, for example using the library inside a chatbot you could add and remove tags to match nicknames when users join or part.

The annotation expression is a JSON string containing placeholders: #0# is replaced with the text of the first element of the pattern sequence, #1# with the second one, and so on. #1.fruit_name# will be replaced with the key 'fruit_name' of the second pattern component. Nested keys (like #0.father.name#) are not allowed.

This format is made to easily import rules from a TSV, using this utility method:

	FileTagLoader.readTagsFromTSV("rule_list.tsv", fm);

this will load the tag described in the tsv file inside the given fleximatcher instance. Look at the JavaDoc for further details.

If you need something more complex than an annotation expression, you can define your own annotation rules by calling the other addTagRule method:

	public boolean addTagRule(String tag, String identifier, RuleDefinition annotationRule)

the annotationRule object, as described in the JavaDoc, will contain the pattern to match and a method to generate the annotation from the matched ones.


Define new rules
---------------
Let's assume you want to create a rule to match German part of speech. You want to match expressions like "ich bin [de-pos:A]" to match adjectives (adopting the [EAGLES](http://www.ilc.cnr.it/EAGLES96/home.html ) standard)
A rule is defined as an instance of a class inheriting from the RuleGenerator class, which is bound to a rule name at runtime. Rules do not know the name they are mapped to. In this case we'll create the class GermanPoSRuleFactory:

	public class GermanPoSRuleFactory implements RuleFactory {
		private GermanModel deModel;
		public GermanPoSRuleFactory(){
			deModel=new SomeSortOfGermanModel();
			//insert here the code necessary to load models, like OpenNLP ones, or connect to databases.
		}
		
		public MatchingRule getRule(String parameter) {
		//this will return a rule instantiated with the parameters (in this example, the "A" string in "[de-pos:A]")
			return new GermanPosRule(deModel,parameter);
		}

	}

and it will be bound to "de-pos" rule name this way:

	public class GermanPosRule extends MatchingRule {

		private SomeSortOfGermanModel deModel;
		private HashSet<String> acceptedTags=new HashSet<>(5);

		public GermanPosRule(SomeSortOfGermanModel deModel, String tag) {
			this.deModel=deModel;
			if(tag.isEmpty())
				throw new RuntimeException("tag not valid, mustn't be empty");
			//check here that the tag is a known one here...
			//[...]
			//you may want to accept many tags (for example, a tag adjective may correspond to different kind of more specific tags)
			acceptedTags.add(tag);
		}

		@Override
		public boolean annotate(String text,AnnotationHandler ah) {
		//retrieve the tags using some library, and iterate over them
			Span[] tags = deModel.getPosTags(text);
			for(Span t:tags){
			//in this example we could accept many different tags
				if(acceptedTags.contains(t.getType()))
					try {
					//use the annotation handler to mark the requested tag, and use an annotation
					//note that nowhere in the rule code we use the name of the rule (de-pos), that will be bound at runtime
					//JSON annotation is optional, use null to not insert it
						ah.addAnnotation(t,new JSONObject("{'tag':'"+t.getType()+"'}"));
					} catch (JSONException e) {
						e.printStackTrace();
					}
			}
			return false;
		}

		@Override
		public String toString() {
			return "matches the german POS tags "+acceptedTags.toString();
		}
	}

note that the rule does not contain its own name, that's mapped with it when preparing the fleximatcher instance.

A rule has to implement the getRule method, which given the parameter (in the previous example, the "A" of "[de-pos:A]", an empty string if no parameter was specified) will return an object which class inherits from Rule, which is an annotator conceptually similar to UIMA or GATES ones. Rules can write annotations to text spans and see annotations already assigned. Annotations are stored in an annotation handler, which contains the status of an annotation process

A Rule has the method annotate which receives the text to annotate and an AnnotationHandler instance, and will use the annotation handler addAnnotation method to notify any match with the string, possibly with the corresponding annotations as JSON strings.

Rules can also check the text against other rules. Let's assume we want to create a rule which matches adjectives related to the word Krapfen in German.
In this rule we'll look for an adjective using the de-pos rule followed by the word "Krapfen".
The annotate method of this rule will be:

	public boolean annotate(String text,AnnotationHandler ah) {
		//[...]
		//create a sub-annotation handler with an arbitrary pattern our rule wants to match
		AnnotationHandler sa = ah.getSubHandler("de-pos");
		Optional<Set<LinkedList<TextAnnotation>>> subMatches=fleximatcher_instance.matches(text, "[de-pos:A] Krapfen", sa, false, false,true);
		//use the results as you need
		if(subMatches.isPresent()){
				for(LinkedList<TextAnnotation> matchSequence:subMatches.get()){
					ah.addAnnotation(new Span(matchSequence.getFirst().getSpan().getStart(),matchSequence.getLast().getSpan().getEnd())
					,new JSONObject("{'adjective_for_a_Krapfen':'"+matchSequence.getFirst().getSpan().getCoveredText(text)+"'}"));
				}
		}
		
	}
	
Calling fleximatcher on the text "Ich bin eine süße Krapfen" and the pattern "[de-krapfen-adjective]", assuming it was bound to the corresponding rule factory, it will match "süße Krapfen" (assuming the PoS tagging library worked correctly) and give the corresponding annotation containing the adjective süße. You have to pass an instance of Fleximatcher which already knows the de-pos rule, it can be the same instance that is running this rule or another one, usually you want to pass the fleximatcher instance to the RuleFactory when binding the rule.

You may create a new Fleximatcher instance and use it to check any pattern you need, but using getSubHandler you avoid to match the same rule again and again: fleximatcher will annotate the text only once and give the already stored annotations later.

__NOTE__: the sub-annotation handler will not throw exceptions when matching a text different than the one used for the rule, in fact the default handler does not store the text it's working over. You should never call the matches method with a text different from text, or you will get strange results (or errors in case you add spans outside the string)

A rule can have no parameters, for example you may want to define "de-token" to match German tokens. In that case the rule will be called with "[de-token]" and will receive an empty string as the parameter.

The default annotation handler will cache rules annotations for a string, rules should not make assumptions on the fact that library will call them and in which order, in other words a rule should be a pure function.

Rules must be thread safe, or strange, nasty, errors may happen when parsing a text.

Retrieve the results
---------------------
The most flexile method used to match patterns against a text is this:


		/**
			 * Matches the text against the given pattern, using the given annotator
			 * @param text the string to match (e.g.: "the dog")
			 * @param pattern the pattern to search for (e.g. "the [r:[a-z]+]")
			 * @param ah the AnnotationHandler which will be used to store annotations
			 * @param fullyAnnotate if true, will give the annotation handler any annotation found, if false will stop as soon as is sure that the string doesn't match
			 * @param matchWhole if true, will match the pattern against the whole text, if false will search for a substring matching it.
			 * @param populateResult if true, will populate the results, if false it will only check whether there is a match or not.
			 * The difference between fullyAnnotate and populateResult is that the former can stop the annotation process, the latter stops the generation of the results but let the annotation handler receive the annotations found.
			 * @return a MatchingResults reporting whether there was a match or not and, when requested, the annotations and 
			 * */
			public MatchingResults matches(String text,String pattern, AnnotationHandler ah, boolean fullyAnnotate,boolean matchWhole,boolean populateResult){

The JavaDoc description should be clear, the difference between _fullyAnnotate_ and _populateResult_ is that the first one is applied during the annotation process, and will not annotate completely the text with the annotation handler in case it surely doesn't match. The second one is applied when populating results, and will annotate the text without returning the results. The difference is important when you want to use a custom annotation handler and retrieve all the annotations or some statistics about them.

Two simpler helpers are provided for common matching needs:

	matches(String text,String pattern)
this will return true if the pattern matches the whole text exactly, using the default annotation handler and stopping the process as soon as it's sure that there's no match. For example, calling it with "[de-pos:A] Krapfen" on a text not containing the string "Krapfen" will stop and return false before looking for German PoS tags.

	contains(String text,String pattern)
will return true if a substring of text matches the pattern. It works exactly like matches(String text,String pattern) but will match strings containing a match.

__NOTE__: if given a pattern P and a string X, the fact that matches(X,P) is true doesn't necessarily mean that contains(S+X+E,P) with arbitrary strings S and E will be true. Rules can decide to annotate differently the same span based on the surrounding text; for example, PoS taggers can assign a different tag to a token based on the surrounding words.
