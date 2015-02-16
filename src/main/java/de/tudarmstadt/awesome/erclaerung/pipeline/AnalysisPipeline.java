package de.tudarmstadt.awesome.erclaerung.pipeline;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

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
import de.tudarmstadt.awesome.erclaerung.feature.EiSoundAiDominanceDFE;
import de.tudarmstadt.awesome.erclaerung.feature.IchVariantsCountDFE;
import de.tudarmstadt.awesome.erclaerung.feature.LetterDistributionDFE;
import de.tudarmstadt.awesome.erclaerung.feature.LetterPositionDistributionDFE;
import de.tudarmstadt.awesome.erclaerung.feature.ManualWordListDFE;
import de.tudarmstadt.awesome.erclaerung.feature.PrefixDistributionDFE;
import de.tudarmstadt.awesome.erclaerung.feature.ReverseLetterPositionDistributionDFE;
import de.tudarmstadt.awesome.erclaerung.feature.SsSoundZzDominanceDFE;
import de.tudarmstadt.awesome.erclaerung.feature.StartsWithKOrCDominanceDFE;
import de.tudarmstadt.awesome.erclaerung.feature.TsSoundCzDominanceDFE;
import de.tudarmstadt.awesome.erclaerung.feature.UnSoundVnDominanceDFE;
import de.tudarmstadt.awesome.erclaerung.feature.WSoundUUDominanceDFE;
import de.tudarmstadt.awesome.erclaerung.readers.BonnerXMLReader;
import de.tudarmstadt.awesome.erclaerung.readers.UnlabeledTextReader;
import de.tudarmstadt.awesome.erclaerung.reports.BaselineReport;
import de.tudarmstadt.awesome.erclaerung.reports.EvaluationReportNeighbors;
import de.tudarmstadt.awesome.erclaerung.reports.HTMLReportDetailed;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.lab.Lab;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfTokensPerSentenceDFE;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.LuceneNGramDFE;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.base.FrequencyDistributionNGramFeatureExtractorBase;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchCrossValidationReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchRuntimeReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskCrossValidation;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskPrediction;
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.WekaDataWriter;

/**
 * This the main pipeline which is called when an analysis is supposed to take place
 * 
 * @author Patrick Lerner
 */
public class AnalysisPipeline implements Constants {
	private String inputDirectory;
	private String tempDirectory;

	@Argument
	public void setInputDirectory(String inputDirectory) {
		this.inputDirectory = inputDirectory;
	}

	@Option(name = "-t", usage = "Sets the path to the temporary directory")
	public void setTempDirectory(String path) {
		this.tempDirectory = path;
	}

	@Option(name = "-v", usage = "run cross validation with 'leave one out'")
	private boolean runCrossValidation;

	/**
	 * Sets up DKPro Home where DKProTC likes to store it's data. If the user doesn't specift it, just use the OS's temp
	 * directory.
	 */
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

		// Precomputation
		// PrefixDistributionHeuristicPre prePre = new PrefixDistributionHeuristicPre();
		// prePre.computeList();

		ParameterSpace pSpace = getParameterSpace();
		runPrediction(pSpace);
	}

	@SuppressWarnings("unchecked")
	public ParameterSpace getParameterSpace() {

		// training data to learn a model
		Map<String, Object> dimReaders = new HashMap<String, Object>();
		dimReaders.put(DIM_READER_TRAIN, BonnerXMLReader.class);
		dimReaders.put(DIM_READER_TRAIN_PARAMS,
		                Arrays.asList(new Object[] { BonnerXMLReader.PARAM_SOURCE_LOCATION,
		                                "src/main/resources/bonner_korpora_train/*.xml" }));
		// unlabeled data which will be classified using the trained model

		String testDirectory = "src/main/resources/test_data/*_*.txt";
		if (this.inputDirectory != null)
			testDirectory = this.inputDirectory + "/*.txt";

		dimReaders.put(DIM_READER_TEST, UnlabeledTextReader.class);
		dimReaders.put(DIM_READER_TEST_PARAMS,
		                Arrays.asList(new Object[] { UnlabeledTextReader.PARAM_SOURCE_LOCATION, testDirectory }));

		Dimension<List<String>> dimClassificationArgs = Dimension.create(DIM_CLASSIFICATION_ARGS,
		                Arrays.asList(new String[] { NaiveBayes.class.getName() }));
		Dimension<List<String>> dimFeatureSets = Dimension.create(
		                DIM_FEATURE_SET,
		                Arrays.asList(new String[] { NrOfTokensPerSentenceDFE.class.getName(),
		                                LuceneNGramDFE.class.getName(), CapitalizationRatioDFE.class.getName(),
		                                EiSoundAiDominanceDFE.class.getName(), IchVariantsCountDFE.class.getName(),
		                                LetterDistributionDFE.class.getName(),
		                                LetterPositionDistributionDFE.class.getName(),
		                                PrefixDistributionDFE.class.getName(), ManualWordListDFE.class.getName(),
		                                ReverseLetterPositionDistributionDFE.class.getName(),
		                                SsSoundZzDominanceDFE.class.getName(),
		                                StartsWithKOrCDominanceDFE.class.getName(),
		                                TsSoundCzDominanceDFE.class.getName(), UnSoundVnDominanceDFE.class.getName(),
		                                WSoundUUDominanceDFE.class.getName() }));

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
		BatchTask batch;
		if (runCrossValidation)
			batch = new BatchTaskCrossValidation("Dialect", getPreprocessing(), 40);
		else
			batch = new BatchTaskPrediction("DialectPrediction", getPreprocessing());
		batch.setParameterSpace(pSpace);
		batch.addReport(HTMLReportDetailed.class);
		if (this.inputDirectory == null || runCrossValidation)
			batch.addReport(EvaluationReportNeighbors.class);
		if (runCrossValidation)
			batch.addReport(BatchCrossValidationReport.class);
		batch.addReport(BaselineReport.class);
		batch.addReport(BatchRuntimeReport.class);
		// Run
		Lab.getInstance().run(batch);
	}

	protected AnalysisEngineDescription getPreprocessing() throws ResourceInitializationException {
		return createEngineDescription(BreakIteratorSegmenter.class);
	}
}
