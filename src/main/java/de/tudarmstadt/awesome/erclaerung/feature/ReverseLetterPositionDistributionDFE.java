package de.tudarmstadt.awesome.erclaerung.feature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;

public class ReverseLetterPositionDistributionDFE extends FeatureExtractorResource_ImplBase {
	public static final String FN_REVERSE_LETTER_POSITION_PREFIX = "Letter_Position";
	public static final String LETTERS = "abcdefghijklmnopqrstuvwxyzßäöü";
	public static final int[] POSITIONS = { 0, 1 };

	private boolean positionsContains(int i) {
		for (int j : POSITIONS) {
			if (i == j)
				return true;
		}
		return false;
	}

	private boolean lettersContains(Character c) {
		for (char d : LETTERS.toCharArray()) {
			if (c == d)
				return true;
		}
		return false;
	}

	public List<Feature> extract(JCas jcas) {
		// Initialize
		HashMap<String, Double> lettersAndPositions = new HashMap<String, Double>();
		for (Character c : LETTERS.toCharArray()) {
			for (int i : POSITIONS) {
				lettersAndPositions.put(c.toString() + i, 0.0);
			}
		}
		// Get Tokens
		List<String> tokens = JCasUtil.toText(JCasUtil.select(jcas, Token.class));
		int count = 0;
		for (String token : tokens) {
			Character[] letterArray = ArrayUtils.toObject(token.toCharArray());
			for (int i = 0; i < letterArray.length; i++) {
				if (positionsContains(i) && lettersContains(letterArray[letterArray.length - 1 - i]))
					count++;
				lettersAndPositions.put(letterArray[i].toString() + i,
				                lettersAndPositions.get(letterArray[letterArray.length - 1 - i].toString() + i));
			}
		}
		List<Feature> featList = new ArrayList<Feature>();
		for (String key : lettersAndPositions.keySet()) {
			featList.add(new Feature(FN_REVERSE_LETTER_POSITION_PREFIX + key, lettersAndPositions.get(key) / count));
		}
		return featList;
	}
}
