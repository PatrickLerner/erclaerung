package de.tudarmstadt.awesome.erclaerung.feature;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.core.util.FeatureTestUtil;

/**
 * @author Manuel
 *
 */
public class SsSoundZzDominanceDFETest {

	private JCas jcas;
	AnalysisEngine engine;

	@Before
	public void setUp() throws ResourceInitializationException, AnalysisEngineProcessException {
		AnalysisEngineDescription desc = createEngineDescription(BreakIteratorSegmenter.class);
		engine = createEngine(desc);

		jcas = engine.newJCas();
		jcas.setDocumentLanguage("en");
	}

	@Test
	public void extractTest1() throws Exception {
		jcas.setDocumentText("Essen sezzen messen besser lesen gossen groß Tasse pezze muss los paste posten gwachsen");
		engine.process(jcas);
		SsSoundZzDominanceDFE extractor = new SsSoundZzDominanceDFE();
		List<Feature> features = extractor.extract(this.jcas);

		assertEquals(1, features.size());
		String[] words = jcas.getDocumentText().split(" ");
		int ss = 0;
		int zz = 0;
		for (String string : words) {
			if (string.contains("ss"))
				ss++;
			if (string.contains("zz"))
				zz++;
		}
		for (Feature feature : features) {
			FeatureTestUtil.assertFeature(SsSoundZzDominanceDFE.FN_ZZ_SS_PREFIX, new Float(750), feature);
			assertEquals(feature.getValue(), new Float((ss * 1000) / (ss + zz)));
		}
	}

	@Test
	public void extractTest2() throws Exception {
		jcas.setDocumentText("grozze grosse slozzen slossen suezzer suesser sluzzel slussel");
		engine.process(jcas);
		SsSoundZzDominanceDFE extractor = new SsSoundZzDominanceDFE();
		List<Feature> features = extractor.extract(this.jcas);

		assertEquals(1, features.size());
		String[] words = jcas.getDocumentText().split(" ");
		int ss = 0;
		int zz = 0;
		for (String string : words) {
			if (string.contains("ss"))
				ss++;
			if (string.contains("zz"))
				zz++;
		}
		for (Feature feature : features) {
			FeatureTestUtil.assertFeature(SsSoundZzDominanceDFE.FN_ZZ_SS_PREFIX, new Float(500), feature);
			assertEquals(feature.getValue(), new Float((ss * 1000) / (ss + zz)));
		}
	}
}
