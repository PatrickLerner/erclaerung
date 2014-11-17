package de.tudarmstadt.awesome.erclaerung;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.*;
import static org.apache.uima.fit.factory.CollectionReaderFactory.*;
import static org.apache.uima.fit.pipeline.SimplePipeline.*;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.CasDumpWriter;

import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class Program {
	public static void main(String[] args) throws Exception {
		CollectionReaderDescription cr = createReaderDescription(
				TextReader.class, TextReader.PARAM_PATH,
				"src/test/resources/*.txt", TextReader.PARAM_LANGUAGE, "en");

		AnalysisEngineDescription seg = createEngineDescription(BreakIteratorSegmenter.class);

		AnalysisEngineDescription tagger = createEngineDescription(OpenNlpPosTagger.class);

		AnalysisEngineDescription cc = createEngineDescription(
				CasDumpWriter.class, CasDumpWriter.PARAM_OUTPUT_FILE,
				"target/output.txt");

		runPipeline(cr, seg, tagger, cc);
	}
}