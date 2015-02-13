package de.tudarmstadt.awesome.erclaerung.feature;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;

/**
 * EiSoundAiDominanceDFE calculates a value for the dominance of "ai" in words in a text compared to "ei" in words in a
 * text.
 * 
 * @author Manuel
 *
 * 
 */
public class EiSoundAiDominanceDFE extends FeatureExtractorResource_ImplBase implements DocumentFeatureExtractor {
	public static final String FN_EI_AI_PREFIX = "Ai_Ei_";

	public List<Feature> extract(JCas jcas) throws TextClassificationException {
		int ei = 0;
		int ai = 0;
		List<Feature> featList = new ArrayList<Feature>();
		List<String> tokens = JCasUtil.toText(JCasUtil.select(jcas, Token.class));
		for (String token : tokens) {
			if (token.toLowerCase().contains("ai")) {
				ai++;
			}
			if (token.toLowerCase().contains("ei")) {
				ei++;
			}
		}
		if (ai + ei == 0) {
			featList.add(new Feature(FN_EI_AI_PREFIX, new Float(0.5)));
		}
		else {
			featList.add(new Feature(FN_EI_AI_PREFIX, new Float(new Float(ei * 1000) / new Float(ei + ai))));
		}

		return featList;
	}
}
