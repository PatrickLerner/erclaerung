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

public class ZZVsSSDominance extends FeatureExtractorResource_ImplBase implements DocumentFeatureExtractor {
	public static final String FN_ZZ_SS_PREFIX = "Zz_Ss_";

	public List<Feature> extract(JCas jcas) throws TextClassificationException {
		float ss = 0;
		float zz = 0;
		float s = 0;
		List<Feature> featList = new ArrayList<Feature>();
		List<String> tokens = JCasUtil.toText(JCasUtil.select(jcas, Token.class));
		for (String token : tokens) {
			if (token.toLowerCase().contains("zz")) {
				zz++;
			}
			if (token.toLowerCase().contains("ss")) {
				ss++;
			}
			else if (token.toLowerCase().contains("s")) {
				s++;
			}
		}
		featList.add(new Feature(FN_ZZ_SS_PREFIX + "_ss", ss / tokens.size()));
		featList.add(new Feature(FN_ZZ_SS_PREFIX + "_z", zz / tokens.size()));
		featList.add(new Feature(FN_ZZ_SS_PREFIX + "_s", s / tokens.size()));
		System.out.println("zz:" + (zz / tokens.size()) + " ss:" + (ss / tokens.size()) + "s:" + (s / tokens.size()));
		return featList;
	}
}
