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
	 * <ol>
	 * <li>tag name (for example "fruit")</li>
	 * <li>tag pattern (for example "[i:M]elon")</li>
	 * <li>(optional) tag annotation template(for example "{'fruit':'melon'}</li>
	 * <li>(optional) tag identifier, if not present the datasetname followed by "_" and the line number will be used instead</li>
	 * </ol>
	 * @return the number of new tag rules inserted, not counting the ones replacing existing ones and comments
	 * */
	public static int readTagsFromTSVReader(Reader r,FlexiMatcher fm,String datasetName){

		Iterator<String> it = new BufferedReader(r).lines().iterator();
		int inserted=0;
		int lineNum=0;
		while(it.hasNext()){
			lineNum++;
			String[] line=it.next().split("\t");
			if(line[0].startsWith("#"))
				continue;
			switch(line.length){
			case 2:
				inserted+=fm.addTagRule(line[0], line[1], datasetName+"_"+lineNum)?0:1;
				break;
			case 3:
				inserted+=fm.addTagRule(line[0], line[1], datasetName+"_"+lineNum,line[2])?0:1;
				break;
			case 4:
				inserted+=fm.addTagRule(line[0], line[1], line[2],line[3])?0:1;
				break;
			default:
				throw new RuntimeException("Line "+lineNum +" with an invalid length of "+line.length+ " at "+datasetName);

			}
			if(line.length==2)
				inserted+=fm.addTagRule(line[0], line[2], line[1])?0:1;
			else
				if(line.length==4)
					inserted+=fm.addTagRule(line[0], line[2], line[1],line[3])?0:1;
		}
		return inserted;
	}

	/**
	 * Load tag rules from a TSV file.
	 * Lines starting with a # are ignored, columns are, in order:
	 * <ol>
	 * <li>tag name (for example "fruit")</li>
	 * <li>tag pattern (for example "[i:M]elon")</li>
	 * <li>(optional) tag identifier, if not present the datasetname followed by "_" and the line number will be used instead</li>
	 * <li>(optional) tag annotation template(for example "{'fruit':'melon'}</li>
	 * </ol>
	 * @return the number of new tag rules inserted, not counting the ones replacing existing ones and comments
	 * */
	public static int readTagsFromTSV(String path,FlexiMatcher fm) throws FileNotFoundException{
		return readTagsFromTSVReader(new FileReader(new File(path)),fm,path);
	}

}
