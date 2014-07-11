package it.jacopofar.fleximatcher.importer;

import it.jacopofar.fleximatcher.FlexiMatcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.Iterator;

public class FileTagLoader {

	public static void main(String[] args) {

	}
	
	/**
	 * Load tag rules from a TSV reader.
	 * Lines starting with a # are ignored, columns are, in order:
	 * tag identifier
	 * tag name
	 * tag pattern
	 * (optional) tag annotation template
	 * @return the number of new tag rules inserted, not counting the ones replacing existing ones and comments
	 * */
	public static int readTagsFromTSVReader(Reader r,FlexiMatcher fm){

		Iterator<String> it = new BufferedReader(r).lines().iterator();
		int inserted=0;
		while(it.hasNext()){
			
			String[] line=it.next().split("\t");
			if(line[0].startsWith("#"))
				continue;
			if(line.length==3)
				inserted+=fm.addTagRule(line[0], line[2], line[1])?0:1;
			if(line.length==4)
				inserted+=fm.addTagRule(line[0], line[2], line[1],line[3])?0:1;
		}
		return inserted;
	}
	
	/**
	 * Load tag rules from a TSV file.
	 * Lines starting with a # are ignored, columns are, in order:
	 * tag identifier
	 * tag name
	 * tag pattern
	 * (optional) tag annotation template
	 * @return the number of new tag rules inserted, not counting the ones replacing existing ones and comments
	 * */
	public static int readTagsFromTSV(String path,FlexiMatcher fm) throws FileNotFoundException{
		return readTagsFromTSVReader(new FileReader(new File(path)),fm);
	}

}
