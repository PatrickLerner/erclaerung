package de.tudarmstadt.awesome.erclaerung.feature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;

/**
 * StartsWithKOrCDominanceDFE calculates a value for the dominance of "k" for the beginning of words in a text compared
 * to "c" in the beginning of words in a text.
 * 
 * @author Manuel
 */
public class StartsWithKOrCDominanceDFE extends FeatureExtractorResource_ImplBase implements DocumentFeatureExtractor {

	public static final String FN_K_VS_CH_PREFIX = "KvsCh_";
	public final String[] PREFIXES = { "ch", "k" };
	public final Set<String> PREFIX_VARIANTS = new HashSet<String>(Arrays.asList(PREFIXES));

	public List<Feature> extract(JCas jcas) throws TextClassificationException {
		List<String> tokens = JCasUtil.toText(JCasUtil.select(jcas, Token.class));
		int k = 0;
		int c = 0;
		for (String token : tokens) {
			if (token.toLowerCase().startsWith("k"))
				k++;
			else if (token.toLowerCase().startsWith("c"))
				c++;
		}

		// generate a feature list
		List<Feature> featList = new ArrayList<Feature>();
		if (k + c == 0)
			featList.add(new Feature(FN_K_VS_CH_PREFIX, new Float(0.5)));
		else
			featList.add(new Feature(FN_K_VS_CH_PREFIX, new Float((k * 1000) / (k + c))));
		return featList;
	}

}
