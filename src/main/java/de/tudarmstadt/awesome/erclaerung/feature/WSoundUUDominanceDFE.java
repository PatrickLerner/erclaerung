package de.tudarmstadt.awesome.erclaerung.feature;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;

public class WSoundUUDominanceDFE extends FeatureExtractorResource_ImplBase implements DocumentFeatureExtractor {
	public static final String FN_UU_DOMINANCE = "UUDominance";

	public List<Feature> extract(JCas jcas) throws TextClassificationException {
		String documentText = jcas.getDocumentText().toLowerCase();

		double w_sound_as_w = StringUtils.countMatches(documentText, "w");
		double w_sound_as_uu = StringUtils.countMatches(documentText, "uu");

		List<Feature> featList = new ArrayList<Feature>();
		featList.add(new Feature(FN_UU_DOMINANCE, w_sound_as_uu >= w_sound_as_w));
		return featList;
	}
}
