package de.tudarmstadt.awesome.erclaerung.feature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;

/**
 * This engine dynamically extracts a list of prefixes from the given tokens.
 * 
 * @author manuel
 */
public class PrefixDistributionHeuristicDFE extends FeatureExtractorResource_ImplBase implements
                DocumentFeatureExtractor {
	public static final String FN_PREFIX_VARIANT_PREFIX_HEU = "PrefixHeuRatio_";
	private File input = new File("target/precomputation/prefixHeuristic.txt");
	private static int MAX_PREFIX_LENGTH = 3;
	private static int MIN_PREFIX_LENGTH = 2;

	public List<Feature> extract(JCas jcas) throws TextClassificationException {
		List<String> tokens = JCasUtil.toText(JCasUtil.select(jcas, Token.class));
		Map<String, Integer> seenPrefixes = new HashMap<String, Integer>((tokens.size() * MAX_PREFIX_LENGTH));
		// Add the precomputed prefixes.

		List<String> prePrefixes = null;
		try {
			prePrefixes = Files.readLines(input, Charsets.UTF_8);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		for (String string : prePrefixes) {
			seenPrefixes.put(string, 0);
		}
		List<String> seenStrings = new ArrayList<String>(tokens.size());
		String[] tokensArray = tokens.toArray(new String[tokens.size()]);
		for (int tokenIndex = 0; tokenIndex < tokensArray.length; tokenIndex++) {
			String token = tokensArray[tokenIndex];
			if (!seenStrings.contains(token)) {
				seenStrings.add(token);
				// If token is longer than permitted maxSize set it to maxSize else to a letter less than the token
				// is long.
				int letterMax = MAX_PREFIX_LENGTH != -1 && token.length() - 1 > MAX_PREFIX_LENGTH ? MAX_PREFIX_LENGTH
				                : token.length() - 2;
				for (int letterIndex = letterMax - 1; letterIndex >= MIN_PREFIX_LENGTH - 1; letterIndex--) {
					String prefix = token.substring(0, letterIndex + 1);
					for (int comTokenIndex = tokenIndex + 1; comTokenIndex < tokensArray.length; comTokenIndex++) {
						if (tokensArray[comTokenIndex].startsWith(prefix)) {
							if (seenPrefixes.containsKey(prefix)) {
								int occurences = seenPrefixes.get(prefix) + 1;
								seenPrefixes.put(prefix, occurences);
							}
						}
					}
				}
			}
		}

		// Read the precomputed prefixes

		List<Feature> featList = new ArrayList<Feature>();
		for (String prefix : seenPrefixes.keySet()) {
			double count = seenPrefixes.get(prefix) * 1000 / tokens.size();
			featList.add(new Feature(FN_PREFIX_VARIANT_PREFIX_HEU + prefix, count));
		}
		return featList;

	}
}
