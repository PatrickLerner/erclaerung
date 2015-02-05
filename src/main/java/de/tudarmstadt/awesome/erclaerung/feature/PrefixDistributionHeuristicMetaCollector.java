package de.tudarmstadt.awesome.erclaerung.feature;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.FreqDistBasedMetaCollector;

public class PrefixDistributionHeuristicMetaCollector extends FreqDistBasedMetaCollector {
	public static final String PREFIX_HEU_FD_KEY = "prefixHeu.ser";

	@ConfigurationParameter(name = PrefixDistributionHeuristicDFE.PARAM_PREFIX_HEU_MIN_SIZE, mandatory = false)
	private int minSize = 2;
	@ConfigurationParameter(name = PrefixDistributionHeuristicDFE.PARAM_PREFIX_HEU_MAX_SIZE, mandatory = false)
	private int maxSize = -1;
	@ConfigurationParameter(name = PrefixDistributionHeuristicDFE.PARAM_PREFIX_HEU_FD_FILE, mandatory = true)
	private File prefixHeufdFile;

	@Override
	protected File getFreqDistFile() {
		return prefixHeufdFile;
	}

	@Override
	public Map<String, String> getParameterKeyPairs() {
		// What is this even?
		Map<String, String> mapping = new HashMap<String, String>();
		mapping.put(PrefixDistributionHeuristicDFE.PARAM_PREFIX_HEU_FD_FILE, PREFIX_HEU_FD_KEY);
		return mapping;
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		List<String> tokens = JCasUtil.toText(JCasUtil.select(aJCas, Token.class));
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
						}
					}
				}
			}
		}
		// generate a fd
		for (String key : occurences.keySet()) {
			System.out.println("[PRE_META] [" + key + "] " + occurences.get(key));
			fd.addSample(key, occurences.get(key));
		}
	}
}
