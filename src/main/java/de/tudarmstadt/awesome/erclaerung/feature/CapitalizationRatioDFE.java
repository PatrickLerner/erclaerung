package de.tudarmstadt.awesome.erclaerung.feature;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;

public class CapitalizationRatioDFE extends FeatureExtractorResource_ImplBase implements DocumentFeatureExtractor {
	public static final String FN_CAPITAL_RATIO = "CapitalizationRatio";

	public List<Feature> extract(JCas jcas) throws TextClassificationException {
		String documentText = jcas.getDocumentText().toLowerCase();
		double capitalLetters = 0;

		for (char letter : documentText.toCharArray()) {
			if (new String(new char[] { letter }).toUpperCase().toCharArray()[0] == letter)
				capitalLetters += 1;
		}

		double ratio = capitalLetters / documentText.length();

		// System.out.println("[CAPITAL] " + jcas.getDocumentLanguage() + " " + ratio);

		List<Feature> featList = new ArrayList<Feature>();
		featList.add(new Feature(FN_CAPITAL_RATIO, ratio));
		return featList;
	}
}