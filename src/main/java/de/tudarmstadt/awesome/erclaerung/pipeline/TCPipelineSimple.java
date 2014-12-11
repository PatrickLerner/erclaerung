package de.tudarmstadt.awesome.erclaerung.pipeline;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;

import weka.classifiers.bayes.NaiveBayes;
import de.tudarmstadt.awesome.erclaerung.readers.BonnerXMLReader;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.lab.Lab;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.lab.task.impl.BatchTask.ExecutionPolicy;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.LuceneNGramDFE;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.base.FrequencyDistributionNGramFeatureExtractorBase;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchCrossValidationReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchRuntimeReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.ClassificationReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskCrossValidation;
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.WekaDataWriter;

public class TCPipelineSimple implements Constants {
	public void run() throws Exception {
		System.setProperty("DKPRO_HOME", "/DKPro/");
		this.runCrossValidation(getParameterSpace());
	}

	protected void runCrossValidation(ParameterSpace pSpace) throws Exception {
		BatchTaskCrossValidation batch = new BatchTaskCrossValidation("SimplePipe", getPreprocessing(), 2);
		batch.addInnerReport(ClassificationReport.class);
		batch.setParameterSpace(pSpace);
		batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
		batch.addReport(BatchCrossValidationReport.class);
		batch.addReport(BatchRuntimeReport.class);

		Lab.getInstance().run(batch);
	}

	protected AnalysisEngineDescription getPreprocessing() throws ResourceInitializationException {
		return createEngineDescription(BreakIteratorSegmenter.class);
	}

	@SuppressWarnings("unchecked")
	protected ParameterSpace getParameterSpace() {
		Map<String, Object> dimReaders = new HashMap<String, Object>();
		dimReaders.put(DIM_READER_TRAIN, BonnerXMLReader.class);
		dimReaders.put(DIM_READER_TRAIN_PARAMS,
		                Arrays.asList(new Object[] { BonnerXMLReader.PARAM_SOURCE_LOCATION,
		                                "src/main/resources/bonner_korpora_train/*.xml" }));

		Dimension<List<String>> dimClassificationArgs = Dimension.create(DIM_CLASSIFICATION_ARGS,
		                Arrays.asList(NaiveBayes.class.getName()));

		Dimension<List<String>> dimFeatureSets = Dimension.create(DIM_FEATURE_SET,
		                Arrays.asList(new String[] { LuceneNGramDFE.class.getName() }));

		Dimension<List<Object>> dimPipelineParameters = Dimension.create(
		                DIM_PIPELINE_PARAMS,
		                Arrays.asList(new Object[] {
		                                FrequencyDistributionNGramFeatureExtractorBase.PARAM_NGRAM_USE_TOP_K, "100",
		                                FrequencyDistributionNGramFeatureExtractorBase.PARAM_NGRAM_MIN_N, 1,
		                                FrequencyDistributionNGramFeatureExtractorBase.PARAM_NGRAM_MAX_N, 3 }));

		ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders), Dimension.create(
		                DIM_DATA_WRITER, WekaDataWriter.class.getName()), Dimension.create(DIM_LEARNING_MODE,
		                LM_SINGLE_LABEL), Dimension.create(DIM_FEATURE_MODE, FM_DOCUMENT), dimPipelineParameters,
		                dimFeatureSets, dimClassificationArgs);
		return pSpace;

	}
}
