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
 * SsSoundZzDominanceDFE calculates a value for the dominance of "ss" in words in a text compared to "zz" in words in a
 * text.
 * 
 * @author Manuel
 */
public class SsSoundZzDominanceDFE extends FeatureExtractorResource_ImplBase implements DocumentFeatureExtractor {
	public static final String FN_ZZ_SS_PREFIX = "Zz_Ss_";

	public List<Feature> extract(JCas jcas) throws TextClassificationException {
		float ss = 0;
		float zz = 0;
		List<Feature> featList = new ArrayList<Feature>();
		List<String> tokens = JCasUtil.toText(JCasUtil.select(jcas, Token.class));
		for (String token : tokens) {
			if (token.toLowerCase().contains("zz")) {
				zz++;
			}
			if (token.toLowerCase().contains("ss")) {
				ss++;
			}
		}
		if (ss + zz == 0)
			featList.add(new Feature(FN_ZZ_SS_PREFIX, new Float(0.5)));
		else
			featList.add(new Feature(FN_ZZ_SS_PREFIX, new Float(new Float(ss * 1000) / new Float(ss + zz))));
		return featList;
	}
}
