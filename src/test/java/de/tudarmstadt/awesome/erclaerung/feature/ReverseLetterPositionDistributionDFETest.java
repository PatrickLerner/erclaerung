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

public class ReverseLetterPositionDistributionDFETest {

	private JCas jcas1;

	@Before
	public void setUp() throws ResourceInitializationException, AnalysisEngineProcessException {
		AnalysisEngineDescription desc = createEngineDescription(BreakIteratorSegmenter.class);
		AnalysisEngine engine = createEngine(desc);

		jcas1 = engine.newJCas();
		jcas1.setDocumentLanguage("en");
		jcas1.setDocumentText("Ir schawen waz auch so hoch und durchgoßen mit gnaden, daz sie dick lag ein gantzen tag"
		                + " zwen oder drey, daz sie kein verstantnüß hete der außern sinne. Da sie zu eim mal lage in"
		                + " sölcher genad, da kom ein edle fraw zu dem kloster, dy wolt nicht gelawben, daz sie von"
		                + " genaden dar zu kumen wer, daz sie sich nit verstünde; und ging dar und stecket ir ein"
		                + " nadel in die versen piß an daz öre, daz sie sein nie enpfand von prynender minn. Da enpfing"
		                + " sie auch und ward ir ein gedrücket von got daz zeichen, daz ym ging durch sein seyten, also"
		                + " daz ein offene wunden erschein an irem hertzen. Oft so sie kom in einen ernst oder so sie"
		                + " in dem chor stund und sang, so außwyl von der wunden frisch plut piß auf dy erden. Daz ward"
		                + " denn von den swestern auf gehebt an neẅe tücher, die ez von andaht langzeit behilten"
		                + " rosenvarb und wolsmecket. Ir hertz ward auch gesehen von wolgelerten predigern an irem"
		                + " tode; die sprachen, daz ez ein bewert zeichen were.");
		engine.process(jcas1);
	}

	@Test
	public void extractTest1() throws Exception {
		ReverseLetterPositionDistributionDFE extractor = new ReverseLetterPositionDistributionDFE();
		List<Feature> features = extractor.extract(this.jcas1);
		String prefix = ReverseLetterPositionDistributionDFE.FN_REVERSE_LETTER_POSITION_PREFIX;
		assertEquals(ReverseLetterPositionDistributionDFE.LETTERS.length()
		                * ReverseLetterPositionDistributionDFE.POSITIONS.length, features.size());
		for (Feature feature : features) {
			if (feature.getName().equals(prefix + "n0"))
				FeatureTestUtil.assertFeature(prefix + "n0", new Float(145.65826330532212), feature);
			else if (feature.getName().equals(prefix + "n1"))
				FeatureTestUtil.assertFeature(prefix + "n1", new Float(44.817927170868344), feature);
			else if (feature.getName().equals(prefix + "g0"))
				FeatureTestUtil.assertFeature(prefix + "g0", new Float(16.80672268907563), feature);
			else if (feature.getName().equals(prefix + "f0"))
				FeatureTestUtil.assertFeature(prefix + "f0", new Float(5.602240896358543), feature);
			else if (feature.getName().equals(prefix + "j0"))
				FeatureTestUtil.assertFeature(prefix + "j0", new Float(0.0), feature);
		}

	}
}
