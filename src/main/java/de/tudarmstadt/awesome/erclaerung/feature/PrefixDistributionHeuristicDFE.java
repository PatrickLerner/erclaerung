package de.tudarmstadt.awesome.erclaerung.feature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaDependent;

/**
 * This engine dynamically extracts a list of prefixes from the given tokens.
 * 
 * @author manuel
 */
public class PrefixDistributionHeuristicDFE extends FeatureExtractorResource_ImplBase implements
                DocumentFeatureExtractor, MetaDependent {
	public static final String PARAM_PREFIX_HEU_FD_FILE = "prefixHeuFdFile";

	public static final String FN_PREFIX_VARIANT_PREFIX_HEU = "PrefixHeuRatio_";

	public static final String PARAM_PREFIX_HEU_MIN_SIZE = "PrefixHeuMinSize";
	public static final String PARAM_PREFIX_HEU_MAX_SIZE = "PrefixHeuMaxSize";

	@ConfigurationParameter(name = PARAM_PREFIX_HEU_MIN_SIZE, mandatory = false)
	private int minSize = 2;
	@ConfigurationParameter(name = PARAM_PREFIX_HEU_MAX_SIZE, mandatory = false)
	private int maxSize = -1;
	@ConfigurationParameter(name = PARAM_PREFIX_HEU_FD_FILE, mandatory = true)
	private String fdFile;

	public List<Class<? extends MetaCollector>> getMetaCollectorClasses() {
		List<Class<? extends MetaCollector>> metaCollectorClasses = new ArrayList<Class<? extends MetaCollector>>();
		metaCollectorClasses.add(PrefixDistributionHeuristicMetaCollector.class);
		return metaCollectorClasses;
	}

	public List<Feature> extract(JCas jcas) throws TextClassificationException {

		List<String> tokens = JCasUtil.toText(JCasUtil.select(jcas, Token.class));
		HashMap<String, Integer> occurences = new HashMap<String, Integer>();
		String[] tokensArray = tokens.toArray(new String[tokens.size()]);
		for (int tokenIndex = 0; tokenIndex < tokensArray.length; tokenIndex++) {
			String token = tokensArray[tokenIndex];
			// If token is longer than permitted maxSize set it to maxSize else to a letter less than the token is long.
			int letterMax = maxSize != -1 && token.length() - 1 > maxSize ? maxSize : token.length() - 2;
			for (int letterIndex = letterMax; letterIndex >= minSize; letterIndex--) {
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
