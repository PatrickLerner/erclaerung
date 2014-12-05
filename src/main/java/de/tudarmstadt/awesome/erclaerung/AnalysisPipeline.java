package de.tudarmstadt.awesome.erclaerung;

import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.factory.ExternalResourceFactory.createExternalResourceDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.CasDumpWriter;
import org.kohsuke.args4j.Argument;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.task.uima.ExtractFeaturesConnector;
import de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfSentencesDFE;
import de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfTokensDFE;
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.WekaDataWriter;

/**
 * This the main pipeline which is called when an analysis is supposed to take place
 * 
 * @author Patrick Lerner
 *
 */
public class AnalysisPipeline {
	private File input;

	@Argument
	public void setInputFile(File input) {
		this.input = input;
	}

	/**
	 * This method is called once the pipeline has been properly set up to start the process
	 */
	public void run() throws Exception {
		if (this.input == null)
			throw new RuntimeException("Input of pipeline is empty.");

		// Initialize the bonner korpora
		CollectionReaderDescription cr = createReaderDescription(BonnerXMLReader.class,
		                BonnerXMLReader.PARAM_SOURCE_LOCATION, "src/main/resources/bonner_korpora/*.xml");

		AnalysisEngineDescription seg = createEngineDescription(BreakIteratorSegmenter.class);

		Object[] featureExtractors = new Object[] { createExternalResourceDescription(NrOfTokensDFE.class),
		                createExternalResourceDescription(NrOfSentencesDFE.class) };

		AnalysisEngineDescription fea = createEngineDescription(ExtractFeaturesConnector.class,
		// output directory
		                ExtractFeaturesConnector.PARAM_OUTPUT_DIRECTORY, "target/temp_feature_output",
		                // writer
		                ExtractFeaturesConnector.PARAM_DATA_WRITER_CLASS, WekaDataWriter.class,
		                // learning mode (single label, i.e. only based on dialect for now)
		                ExtractFeaturesConnector.PARAM_LEARNING_MODE, Constants.LM_SINGLE_LABEL,
		                // whole document extraction
		                ExtractFeaturesConnector.PARAM_FEATURE_MODE, Constants.FM_DOCUMENT,
		                // probably good for something (copied from example)
		                ExtractFeaturesConnector.PARAM_ADD_INSTANCE_ID, true,
		                // which extractos are to be used
		                ExtractFeaturesConnector.PARAM_FEATURE_EXTRACTORS, asList(featureExtractors));

		AnalysisEngineDescription cc = createEngineDescription(CasDumpWriter.class, CasDumpWriter.PARAM_OUTPUT_FILE,
		                "target/output.txt");

		runPipeline(cr, seg, fea, cc);
	}
}
