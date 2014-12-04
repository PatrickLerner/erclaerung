package de.tudarmstadt.awesome.erclaerung;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.CasDumpWriter;
import org.kohsuke.args4j.Argument;

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
		                BonnerXMLReader.PARAM_SOURCE_LOCATION, "src/main/resources/bonner_korpora/*.xml",
		                BonnerXMLReader.PARAM_LANGUAGE, "en");

		AnalysisEngineDescription cc = createEngineDescription(CasDumpWriter.class, CasDumpWriter.PARAM_OUTPUT_FILE,
		                "target/output.txt");

		runPipeline(cr, cc);
	}
}
