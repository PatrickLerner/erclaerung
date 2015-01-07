package de.tudarmstadt.awesome.erclaerung.feature;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;

public class WSoundUUDominanceDFE extends FeatureExtractorResource_ImplBase implements DocumentFeatureExtractor {
	public static final String FN_UU_DOMINANCE = "UUDominance";

	public List<Feature> extract(JCas jcas) throws TextClassificationException {
		String documentText = jcas.getDocumentText().toLowerCase();

		double count_uu = StringUtils.countMatches(documentText, "uu");
		double count_tokens = JCasUtil.select(jcas, Token.class).size();

		double res = 1000 * count_uu / count_tokens;

		// System.out.println("[UU] " + res);

		List<Feature> featList = new ArrayList<Feature>();
		featList.add(new Feature(FN_UU_DOMINANCE, res));
		return featList;
	}
}
