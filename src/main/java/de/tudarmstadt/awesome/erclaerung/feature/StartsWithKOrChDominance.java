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

public class StartsWithKOrChDominance extends FeatureExtractorResource_ImplBase implements DocumentFeatureExtractor {

	public static final String FN_K_VS_CH_PREFIX = "KvsCh_";
	public final String[] PREFIXES = { "ch", "k" };
	public final Set<String> PREFIX_VARIANTS = new HashSet<String>(Arrays.asList(PREFIXES));

	public List<Feature> extract(JCas jcas) throws TextClassificationException {
		List<String> tokens = JCasUtil.toText(JCasUtil.select(jcas, Token.class));
		int k = 0;
		int ch = 0;
		// initialize the count for all variants with zero

		for (String token : tokens) {
			if (token.toLowerCase().startsWith("k"))
				k++;
			else if (token.toLowerCase().startsWith("ch"))
				ch++;
		}

		// generate a feature list
		List<Feature> featList = new ArrayList<Feature>();
		featList.add(new Feature(FN_K_VS_CH_PREFIX, k / k + ch));
		return featList;
	}

}
