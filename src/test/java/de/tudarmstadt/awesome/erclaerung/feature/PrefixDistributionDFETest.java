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

import de.tudarmstadt.awesome.erclaerung.feature.PrefixDistributionDFE;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.core.util.FeatureTestUtil;

public class PrefixDistributionDFETest {
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
		PrefixDistributionDFE extractor = new PrefixDistributionDFE();
		List<Feature> features = extractor.extract(this.jcas1);

		assertEquals(11, features.size());

		for (Feature feature : features) {
			if (feature.getName().equals("PrefixRatio_be"))
				FeatureTestUtil.assertFeature("PrefixRatio_be", 2d * 1000d / 199d, feature, 0.0001);
			if (feature.getName().equals("PrefixRatio_er"))
				FeatureTestUtil.assertFeature("PrefixRatio_er", 3d * 1000d / 199d, feature, 0.0001);
			if (feature.getName().equals("PrefixRatio_ver"))
				FeatureTestUtil.assertFeature("PrefixRatio_ver", 3d * 1000d / 199d, feature, 0.0001);
			if (feature.getName().equals("PrefixRatio_in"))
				FeatureTestUtil.assertFeature("PrefixRatio_in", 4d * 1000d / 199d, feature, 0.0001);
		}
	}
}
