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

public class IchVariantsCountDFE extends FeatureExtractorResource_ImplBase implements DocumentFeatureExtractor {
	public static final String FN_ICH_VARIANT_PREFIX = "LetterRatio_";
	public final String[] ICH_VARIANTS_VALUES = { "i", "ig", "ich", "ik", "ih", "ek", "isch", "ish" };
	public final Set<String> ICH_VARIANTS = new HashSet<String>(Arrays.asList(ICH_VARIANTS_VALUES));

	public List<Feature> extract(JCas jcas) throws TextClassificationException {
		List<String> tokens = JCasUtil.toText(JCasUtil.select(jcas, Token.class));

		HashMap<String, Double> variantCount = new HashMap<String, Double>();
		for (String v : ICH_VARIANTS)
			variantCount.put(v, 0d);

		for (String token : tokens)
			if (ICH_VARIANTS.contains(token.toLowerCase()))
				variantCount.put(token.toLowerCase(), variantCount.get(token.toLowerCase()) + 1);

		List<Feature> featList = new ArrayList<Feature>();

		for (String variant : ICH_VARIANTS) {
			double count = variantCount.get(variant) * 1000 / tokens.size();
			featList.add(new Feature(FN_ICH_VARIANT_PREFIX + variant, count));
			// System.out.println("[ICH-VAR] [" + variant + "] " + count);
		}
		return featList;
	}
}
