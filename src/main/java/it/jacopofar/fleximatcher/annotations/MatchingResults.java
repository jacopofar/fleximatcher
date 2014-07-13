package it.jacopofar.fleximatcher.annotations;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Class representing the results of a match. There are three cases:
 * 1. no match
 * 2. empty pattern (so may or may not match, but no matching sequence)
 * 3. non-empty pattern match
 * In all cases the methods isMatching() and isEmptyMatch() will return the status
 * In the third case (isMatching()==true and isEmptyMatch()==false) the getAnnotations() method may return an Optional containing the set of the list of annotations
 * */
public class MatchingResults {
	private final boolean isMatching,isEmptyMatch;
	private final Set<LinkedList<TextAnnotation>> annotationsSequences;
	private MatchingResults(boolean matches, boolean emptyMatch) {
		this.isMatching=matches;
		this.isEmptyMatch=emptyMatch;
		annotationsSequences=null;
	}
	private MatchingResults(boolean matches, boolean emptyMatch,Set<LinkedList<TextAnnotation>> results) {
		this.isMatching=matches;
		this.isEmptyMatch=emptyMatch;
		this.annotationsSequences=results;
	}

	public static MatchingResults emptyMacth() {
		return new MatchingResults(true,true);
	}

	public static MatchingResults noMatch() {
		return new MatchingResults(false,false);
	}
	public boolean isMatching(){
		return isMatching;
	}
	public boolean isEmptyMatch(){
		return isEmptyMatch;
	}

	public static MatchingResults matchWithAnnotations(Set<LinkedList<TextAnnotation>> results) {
		return new MatchingResults(true,true,results);
	}
	public Optional<Set<LinkedList<TextAnnotation>>> getAnnotations(){
		return Optional.ofNullable(this.annotationsSequences);
	}
	/**
	 * Returns an array of the substring matching with the sequences.
	 * This doesn't higlight the structure of the matches
	 * */
	public String[] getMatchingStrings(String string) {
		if(annotationsSequences==null)
			return new String[0];
		String[] res=new String[annotationsSequences.size()];
		int i=0;
		for(LinkedList<TextAnnotation> seq:annotationsSequences){
			res[i++]=string.substring(seq.getFirst().getSpan().getStart(),seq.getLast().getSpan().getEnd());
		}
		return  res;
	}
	
	/**
	 * Shows how each match was obtained and with which annotations, in a human readable form
	 * @param the text to annotate (usually the one you used to match the pattern)
	 * */
	public String[] getHighlightedStrings(String string) {
		if(annotationsSequences==null)
			return new String[0];
		String[] res=new String[annotationsSequences.size()];
		int i=0;
		for(LinkedList<TextAnnotation> seq:annotationsSequences){
			String highl="";
			for(TextAnnotation ta:seq){
				highl+=ta.getType()+":"+string.substring(ta.getSpan().getStart(),ta.getSpan().getEnd())+" ("+ta.getJSON().flatMap(j->Optional.of(j.toString())).orElse("no annotation")+")";
			}
			res[i++]=highl;
		}
		return  res;
	}
	/**
	 * Return a stream of all of the annotations in this
	 * */
	public Stream<TextAnnotation> getFlatAnnotations() {
		return annotationsSequences.stream().flatMap(k->k.stream());
		
	}


}
