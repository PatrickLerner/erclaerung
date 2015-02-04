package de.tudarmstadt.awesome.erclaerung.reports;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import de.tudarmstadt.ukp.dkpro.lab.reporting.BatchReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService;
import de.tudarmstadt.ukp.dkpro.lab.task.TaskContextMetadata;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.ExtractFeaturesAndPredictTask;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.uima.ExtractFeaturesAndPredictConnector;

public class DebugReport extends BatchReportBase implements Constants {

	private static final String predicted_value = "Prediction";

	@SuppressWarnings("unchecked")
	public void execute() throws Exception {
		StorageService store = getContext().getStorageService();

		for (TaskContextMetadata subcontext : getSubtasks()) {
			if (subcontext.getType().startsWith(ExtractFeaturesAndPredictTask.class.getName())) {
				// deserialize file
				FileInputStream f = new FileInputStream(store.getStorageFolder(subcontext.getId(),
				                ExtractFeaturesAndPredictConnector.PREDICTION_MAP_FILE_NAME));
				ObjectInputStream s = new ObjectInputStream(f);
				Map<String, List<String>> resultMap = (Map<String, List<String>>) s.readObject();
				s.close();

				System.out.println("\n\nDEBUG REPORT:\n");
				// temp output, should be in html somehow later on
				for (String id : resultMap.keySet()) {
					System.out.println(StringUtils.leftPad(id, 25) + ": " + StringUtils.join(resultMap.get(id), ","));
				}
				System.out.println("\nDEBUG REPORT END\n\n");
			}
		}
	}
}
