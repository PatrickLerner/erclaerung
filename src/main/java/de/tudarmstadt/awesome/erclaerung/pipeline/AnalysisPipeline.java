package de.tudarmstadt.awesome.erclaerung.pipeline;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import weka.classifiers.bayes.NaiveBayes;
import de.tudarmstadt.awesome.erclaerung.feature.CapitalizationRatioDFE;
import de.tudarmstadt.awesome.erclaerung.feature.IchVariantsCountDFE;
import de.tudarmstadt.awesome.erclaerung.feature.LetterDistributionDFE;
import de.tudarmstadt.awesome.erclaerung.feature.PrefixDistributionDFE;
import de.tudarmstadt.awesome.erclaerung.feature.UnSoundVnDominanceDFE;
import de.tudarmstadt.awesome.erclaerung.feature.WSoundUUDominanceDFE;
import de.tudarmstadt.awesome.erclaerung.readers.BonnerXMLReader;
import de.tudarmstadt.awesome.erclaerung.readers.UnlabeledTextReader;
import de.tudarmstadt.awesome.erclaerung.reports.DebugReport;
import de.tudarmstadt.awesome.erclaerung.reports.HTMLReport;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.lab.Lab;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfTokensPerSentenceDFE;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.LuceneNGramDFE;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.base.FrequencyDistributionNGramFeatureExtractorBase;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskPrediction;
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.WekaDataWriter;

/**
 * This the main pipeline which is called when an analysis is supposed to take place
 * 
 * @author Patrick Lerner
 *
 */
public class AnalysisPipeline implements Constants {
	private File input;
	private String tempDirectory;

	@Argument
	public void setInputFile(File input) {
		this.input = input;
	}

	@Option(name = "-t", usage = "Sets the path to the temporary directory")
	public void setTempDirectory(String path) {
		this.tempDirectory = path;
	}

	private void setDkproHome() {
		if (this.tempDirectory == null)
			this.tempDirectory = System.getProperty("java.io.tmpdir");
		System.setProperty("DKPRO_HOME", this.tempDirectory);
		System.out.println("DKPRO_HOME is \"" + this.tempDirectory + "\"");
	}

	/**
	 * This method is called once the pipeline has been properly set up to start the process
	 */
	public void run() throws Exception {
		this.setDkproHome();

		if (this.input == null)
			throw new RuntimeException("Input of pipeline is empty.");

		ParameterSpace pSpace = getParameterSpace();
		runPrediction(pSpace);
	}

	@SuppressWarnings("unchecked")
	public static ParameterSpace getParameterSpace() {

		// training data to learn a model
		Map<String, Object> dimReaders = new HashMap<String, Object>();
		dimReaders.put(DIM_READER_TRAIN, BonnerXMLReader.class);
		dimReaders.put(DIM_READER_TRAIN_PARAMS,
		                Arrays.asList(new Object[] { BonnerXMLReader.PARAM_SOURCE_LOCATION,
		                                "src/main/resources/bonner_korpora_train/*.xml" }));
		// unlabeled data which will be classified using the trained model
		dimReaders.put(DIM_READER_TEST, UnlabeledTextReader.class);
		dimReaders.put(DIM_READER_TEST_PARAMS,
		                Arrays.asList(new Object[] { UnlabeledTextReader.PARAM_SOURCE_LOCATION,
		                                "src/main/resources/wiki_test/*.txt" }));

		Dimension<List<String>> dimClassificationArgs = Dimension.create(DIM_CLASSIFICATION_ARGS,
		                Arrays.asList(new String[] { NaiveBayes.class.getName() }));

		Dimension<List<String>> dimFeatureSets = Dimension.create(
		                DIM_FEATURE_SET,
		                Arrays.asList(new String[] { NrOfTokensPerSentenceDFE.class.getName(),
		                                IchVariantsCountDFE.class.getName(), LuceneNGramDFE.class.getName(),
		                                LetterDistributionDFE.class.getName(), UnSoundVnDominanceDFE.class.getName(),
		                                WSoundUUDominanceDFE.class.getName(), PrefixDistributionDFE.class.getName(),
		                                CapitalizationRatioDFE.class.getName() }));

		Dimension<List<Object>> dimPipelineParameters = Dimension.create(
		                DIM_PIPELINE_PARAMS,
		                Arrays.asList(new Object[] {
		                                FrequencyDistributionNGramFeatureExtractorBase.PARAM_NGRAM_USE_TOP_K, "1000",
		                                FrequencyDistributionNGramFeatureExtractorBase.PARAM_NGRAM_MIN_N, 1,
		                                FrequencyDistributionNGramFeatureExtractorBase.PARAM_NGRAM_MAX_N, 5 }));

		ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders), Dimension.create(
		                DIM_DATA_WRITER, WekaDataWriter.class.getName()), Dimension.create(DIM_LEARNING_MODE,
		                LM_SINGLE_LABEL), Dimension.create(DIM_FEATURE_MODE, FM_DOCUMENT), dimPipelineParameters,
		                dimFeatureSets, dimClassificationArgs);

		return pSpace;
	}

	protected void runPrediction(ParameterSpace pSpace) throws Exception {
		BatchTaskPrediction batch = new BatchTaskPrediction("DialectPrediction", getPreprocessing());
		// BatchTaskCrossValidation batch = new BatchTaskCrossValidation("Dialect", getPreprocessing(), 40);
		batch.setParameterSpace(pSpace);

		batch.addReport(DebugReport.class);
		batch.addReport(HTMLReport.class);
		// batch.addReport(WekaBatchCrossValidationReport.class);
		// batch.addReport(WekaBatchRuntimeReport.class);
		// Run
		Lab.getInstance().run(batch);
	}

	protected AnalysisEngineDescription getPreprocessing() throws ResourceInitializationException {
		return createEngineDescription(BreakIteratorSegmenter.class);
	}
}
