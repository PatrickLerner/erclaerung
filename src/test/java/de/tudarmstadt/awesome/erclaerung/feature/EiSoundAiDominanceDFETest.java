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
public class EiSoundAiDominanceDFETest {

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
		jcas.setDocumentText("haizzet heizzet waschen maisterschefte weschen haisset groß cleiner meisterschefte muss kainen clostern posten meinung");
		engine.process(jcas);
		EiSoundAiDominanceDFE extractor = new EiSoundAiDominanceDFE();
		List<Feature> features = extractor.extract(this.jcas);

		assertEquals(1, features.size());
		String[] words = jcas.getDocumentText().split(" ");
		float ai = 0;
		float ei = 0;
		for (String string : words) {
			if (string.toLowerCase().contains("ai")) {
				ai++;
			}
			if (string.toLowerCase().contains("ei")) {
				ei++;
			}
		}
		for (Feature feature : features) {
			FeatureTestUtil.assertFeature(EiSoundAiDominanceDFE.FN_EI_AI_PREFIX, new Float(500), feature);
			assertEquals(feature.getValue(), new Float(new Float(ei * 1000) / new Float(ai + ei)));
		}
	}

}
