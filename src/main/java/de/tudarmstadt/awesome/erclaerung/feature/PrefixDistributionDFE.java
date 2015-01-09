package de.tudarmstadt.awesome.erclaerung.feature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
 * This engine analyses how common various ways to say the word modern German word "ich" exist in any source text. Some
 * variants "ik" are more common in northern German texts and softer variants such as "ich" are more common in central
 * and southern dialects.
 * 
 * @author patrick
 */
public class PrefixDistributionDFE extends FeatureExtractorResource_ImplBase implements DocumentFeatureExtractor {
	public static final String FN_PREFIX_VARIANT_PREFIX = "PrefixRatio_";
	public final String[] PREFIXES = { "be", "ent", "er", "ver", "zer", "abe", "ane", "uf", "umbe", "uz", "in" };
	public final Set<String> PREFIX_VARIANTS = new HashSet<String>(Arrays.asList(PREFIXES));

	public List<Feature> extract(JCas jcas) throws TextClassificationException {
		List<String> tokens = JCasUtil.toText(JCasUtil.select(jcas, Token.class));

		// initialize the count for all variants with zero
		HashMap<String, Double> variantCount = new HashMap<String, Double>();
		for (String v : PREFIX_VARIANTS)
			variantCount.put(v, 0d);

		// go through all of a text's tokens and count the various variants
		for (String token : tokens) {
			for (String prefix : PREFIX_VARIANTS)
				if (token.length() >= prefix.length() && token.substring(0, prefix.length()).equals(prefix))
					variantCount.put(prefix, variantCount.get(prefix) + 1);
		}

		// generate a feature list
		List<Feature> featList = new ArrayList<Feature>();
		for (String variant : PREFIX_VARIANTS) {
			// for each variant add a feature, but normalize it over text length to make it valid
			// for comparisons
			double count = variantCount.get(variant) * 1000 / tokens.size();
			featList.add(new Feature(FN_PREFIX_VARIANT_PREFIX + variant, count));
			// System.out.println("[PREFIX-VAR] [" + variant + "] " + count);
		}
		return featList;
	}
}
