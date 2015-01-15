package de.tudarmstadt.awesome.erclaerung.feature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;

public class PrefixDistributionHeuristic extends FeatureExtractorResource_ImplBase implements DocumentFeatureExtractor {
	public static final String FN_PREFIX_VARIANT_PREFIX_HEU = "PrefixHeuRatio_";
	public static final int minPrefixSize = 2;

	public List<Feature> extract(JCas jcas) throws TextClassificationException {
		List<String> tokens = JCasUtil.toText(JCasUtil.select(jcas, Token.class));
		HashMap<String, Integer> occurences = new HashMap<String, Integer>();
		String[] tokensArray = tokens.toArray(new String[tokens.size()]);
		for (int tokenIndex = 0; tokenIndex < tokensArray.length; tokenIndex++) {
			String token = tokensArray[tokenIndex];
			for (int letterIndex = tokensArray[tokenIndex].length() - 2; letterIndex >= minPrefixSize; letterIndex--) {
				String prefix = token.substring(0, letterIndex + 1);
				for (int comTokenIndex = tokenIndex + 1; comTokenIndex < tokensArray.length; comTokenIndex++) {
					if (tokensArray[comTokenIndex].startsWith(prefix)) {
						if (occurences.containsKey(prefix)) {
							int t = occurences.get(prefix) + 1;
							occurences.put(prefix, t);
						}

						else {
							occurences.put(prefix, 1);
							System.out.println("[PREFIX-VAR] [" + prefix + "] " + 1);
						}
					}
				}
			}
		}
		// generate a feature list
		List<Feature> featList = new ArrayList<Feature>();
		for (String prefix : occurences.keySet()) {
			double count = occurences.get(prefix) * 1000 / tokens.size();
			featList.add(new Feature(FN_PREFIX_VARIANT_PREFIX_HEU + prefix, count));
		}
		return featList;
	}
}
