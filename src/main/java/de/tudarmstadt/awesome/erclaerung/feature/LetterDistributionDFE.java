package de.tudarmstadt.awesome.erclaerung.feature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;

/**
 * The following engine analyses how often specific letters are used in a text.
 * 
 * @author patrick
 */
public class LetterDistributionDFE extends FeatureExtractorResource_ImplBase implements DocumentFeatureExtractor {
	public static final String FN_LETTER_RATIO_PREFIX = "LetterRatio_";

	/**
	 * The letters we are actually looking at, discounting any 'special characters' or other unknown ones
	 */
	public static final String LETTERS = "abcdefghijklmnopqrstuvwxyzßäöü";

	public List<Feature> extract(JCas jcas) throws TextClassificationException {
		String documentText = jcas.getDocumentText().toLowerCase();
		HashMap<Character, Double> letterCount = new HashMap<Character, Double>();

		// initialize hashmap
		for (char l : LETTERS.toCharArray())
			letterCount.put(l, 0d);

		// go through all letters to see what we actually have
		double totalLetterCount = 0;
		for (char l : documentText.toCharArray()) {
			if (letterCount.containsKey(l)) {
				letterCount.put(l, letterCount.get(l) + 1);
				totalLetterCount += 1;
			}
		}

		// create one feature for each letter, normalizing over total count
		List<Feature> featList = new ArrayList<Feature>();
		for (char letter : letterCount.keySet()) {
			String letter_str = new String(new char[] { letter });
			double countLetter = letterCount.get(letter);
			featList.add(new Feature(FN_LETTER_RATIO_PREFIX + letter_str.toUpperCase(), countLetter / totalLetterCount));
		}
		return featList;
	}
}
