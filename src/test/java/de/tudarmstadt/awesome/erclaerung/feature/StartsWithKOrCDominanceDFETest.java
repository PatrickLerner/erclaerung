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
public class StartsWithKOrCDominanceDFETest {

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
		jcas.setDocumentText("kristen klaine crefft claider krefften gossen groß cleiner pezze muss los clostern posten gwachsen");
		engine.process(jcas);
		StartsWithKOrCDominanceDFE extractor = new StartsWithKOrCDominanceDFE();
		List<Feature> features = extractor.extract(this.jcas);

		assertEquals(1, features.size());
		String[] words = jcas.getDocumentText().split(" ");
		float c = 0;
		float k = 0;
		for (String string : words) {
			if (string.startsWith("c"))
				c++;
			if (string.startsWith("k"))
				k++;
		}
		for (Feature feature : features) {
			FeatureTestUtil.assertFeature(StartsWithKOrCDominanceDFE.FN_K_VS_CH_PREFIX, new Float(428.57144), feature);
			assertEquals(feature.getValue(), new Float((k * 1000) / (k + c)));
		}
	}

}
