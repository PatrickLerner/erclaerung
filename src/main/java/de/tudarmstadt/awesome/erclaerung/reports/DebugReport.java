package de.tudarmstadt.awesome.erclaerung.reports;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import de.tudarmstadt.ukp.dkpro.lab.reporting.BatchReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService;
import de.tudarmstadt.ukp.dkpro.lab.task.TaskContextMetadata;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskCrossValidation;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.ExtractFeaturesAndPredictTask;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.uima.ExtractFeaturesAndPredictConnector;

/**
 * Only prints out the result on the console for quick and dirty debugging.
 * 
 * @author Patrick Lerner
 * @deprecated
 */
public class DebugReport extends BatchReportBase implements Constants {
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
			else if (subcontext.getType().startsWith(BatchTaskCrossValidation.class.getName())) {
				FileReader fileReader = new FileReader(store.getStorageFolder(subcontext.getId(), "id2outcome.txt"));
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					if (line.contains("=")) {
						String id = line.substring(0, line.indexOf('='));
						// String real = line.substring(line.indexOf('=') + 1, line.indexOf(';'));
						String pred = line.substring(line.indexOf(';') + 1);
						System.out.println(StringUtils.leftPad(id, 25) + ": " + pred);
					}
				}
				fileReader.close();
			}
		}
	}
}
