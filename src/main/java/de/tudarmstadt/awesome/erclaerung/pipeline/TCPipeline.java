package de.tudarmstadt.awesome.erclaerung.pipeline;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;

import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.trees.RandomForest;
import de.tudarmstadt.awesome.erclaerung.readers.BonnerXMLReader;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.lab.Lab;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfTokensDFE;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchCrossValidationReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.report.BatchTrainTestReport;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskCrossValidation;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskTrainTest;
import de.tudarmstadt.ukp.dkpro.tc.weka.writer.WekaDataWriter;

public class TCPipeline implements Constants {

	public void run() throws Exception {
		System.setProperty("DKPRO_HOME", "/DKPro");
		ParameterSpace pSpace = getParameterSpace();
		this.runCrossValidation(pSpace);
		// this.runTrainTest(pSpace);
	}

	protected void runCrossValidation(ParameterSpace pSpace) throws Exception {
		BatchTaskCrossValidation batch = new BatchTaskCrossValidation("SomeCV", getPreprocessing(), 2);
		batch.setParameterSpace(pSpace);
		batch.addReport(BatchCrossValidationReport.class);

		Lab.getInstance().run(batch);

	}

	protected void runTrainTest(ParameterSpace pSpace) throws Exception {
		BatchTaskTrainTest batch = new BatchTaskTrainTest("SomeTT", getPreprocessing());
		batch.setParameterSpace(pSpace);
		batch.addReport(BatchTrainTestReport.class);

		Lab.getInstance().run(batch);

	}

	protected AnalysisEngineDescription getPreprocessing() throws ResourceInitializationException {
		return createEngineDescription(BreakIteratorSegmenter.class);
	}

	@SuppressWarnings("unchecked")
	private static ParameterSpace getParameterSpace() {
		Map<String, Object> dimReaders = new HashMap<String, Object>();
		dimReaders.put(Constants.DIM_READER_TRAIN, BonnerXMLReader.class);
		dimReaders.put(Constants.DIM_READER_TRAIN_PARAMS,
		                Arrays.asList(new Object[] { BonnerXMLReader.PARAM_SOURCE_LOCATION,
		                                "src/main/resources/bonner_korpora_train/*.xml" }));
		dimReaders.put(Constants.DIM_READER_TRAIN, BonnerXMLReader.class);
		dimReaders.put(Constants.DIM_READER_TEST_PARAMS,
		                Arrays.asList(new Object[] { BonnerXMLReader.PARAM_SOURCE_LOCATION,
		                                "src/main/resources/bonner_korpora_test/*.xml" }));
		Dimension<List<String>> dimClassificationArgs = Dimension.create(Constants.DIM_CLASSIFICATION_ARGS,
		                Arrays.asList(new String[] { NaiveBayes.class.getName() }),
		                Arrays.asList(new String[] { RandomForest.class.getName() }));
		Dimension<List<String>> dimFeatureSets = Dimension.create(Constants.DIM_FEATURE_SET,
		                Arrays.asList(new String[] { NrOfTokensDFE.class.getName() }));
		// , LuceneNGramDFE.class.getName() }));
		ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders), Dimension.create(
		                DIM_DATA_WRITER, WekaDataWriter.class.getName()), Dimension.create(DIM_LEARNING_MODE,
		                LM_SINGLE_LABEL), Dimension.create(DIM_FEATURE_MODE, FM_DOCUMENT), dimFeatureSets,
		                dimClassificationArgs);
		return pSpace;
	}
}
