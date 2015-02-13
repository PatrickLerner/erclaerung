package de.tudarmstadt.awesome.erclaerung.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;

/**
 * Calculates a reference value to compare the pipeline to.
 * 
 * @author Manuel
 *
 */
@Deprecated
public class ReferenceValueDFE extends FeatureExtractorResource_ImplBase implements DocumentFeatureExtractor {
	private Random rnd = new Random();
	private int id;

	public ReferenceValueDFE() {
		id = rnd.nextInt();
	}

	public List<Feature> extract(JCas view) throws TextClassificationException {
		List<Feature> features = new ArrayList<Feature>();
		features.add(new Feature("reference" + id, rnd.nextDouble()));
		return features;
	}

}
