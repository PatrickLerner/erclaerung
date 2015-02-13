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
 * @author Patrick Lerner
 */
public class WSoundUUDominanceDFETest {
	private JCas jcas1;

	@Before
	public void setUp() throws ResourceInitializationException, AnalysisEngineProcessException {
		AnalysisEngineDescription desc = createEngineDescription(BreakIteratorSegmenter.class);
		AnalysisEngine engine = createEngine(desc);

		jcas1 = engine.newJCas();
		jcas1.setDocumentLanguage("en");
		jcas1.setDocumentText("Uudern holgeu stetten, sô ist das die fürnemst, von hoiden und von cristen wirt sy"
		                + " besuocht mit ernst; kain mensch sich dô anthalten mag von andâcht muosz er woinen mit"
		                + " freid ein sieszi clag.");
		engine.process(jcas1);
	}

	@Test
	public void extractTest1() throws Exception {
		WSoundUUDominanceDFE extractor = new WSoundUUDominanceDFE();
		List<Feature> features = extractor.extract(this.jcas1);

		assertEquals(1, features.size());

		for (Feature feature : features) {
			FeatureTestUtil.assertFeature("UUDominance", 1d * 1000.0d / 38.0d, feature, 0.0001);
		}
	}
}
