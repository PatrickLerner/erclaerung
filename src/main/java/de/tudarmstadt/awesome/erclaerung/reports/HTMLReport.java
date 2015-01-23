package de.tudarmstadt.awesome.erclaerung.reports;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import de.tudarmstadt.ukp.dkpro.lab.reporting.BatchReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService;
import de.tudarmstadt.ukp.dkpro.lab.task.TaskContextMetadata;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.ExtractFeaturesAndPredictTask;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.uima.ExtractFeaturesAndPredictConnector;

public class HTMLReport extends BatchReportBase implements Constants {
	private static final String predicted_value = "Prediction";
	private String dataPlaceHolder = "$data";
	private String titlePlaceHolder = "$title";
	private File template = new File("src/main/resources/html/template.html");
	private File output = new File("src/main/resources/html/test.html");

	public void execute() throws Exception {
		StorageService store = getContext().getStorageService();
		String htmlString = FileUtils.readFileToString(template);
		for (TaskContextMetadata subcontext : getSubtasks()) {
			if (subcontext.getType().startsWith(ExtractFeaturesAndPredictTask.class.getName())) {
				// deserialize file

				FileInputStream f = new FileInputStream(store.getStorageFolder(subcontext.getId(),
				                ExtractFeaturesAndPredictConnector.PREDICTION_MAP_FILE_NAME));
				ObjectInputStream s = new ObjectInputStream(f);
				@SuppressWarnings("unchecked")
				Map<String, List<String>> resultMap = (Map<String, List<String>>) s.readObject();
				s.close();

				// simple replacement of the placeholders in the template file.
				htmlString = htmlString.replace(titlePlaceHolder, subcontext.getLabel());
				for (String id : resultMap.keySet()) {
					Map<String, String> row = new HashMap<String, String>();
					row.put(predicted_value, StringUtils.join(resultMap.get(id), ","));
					htmlString = htmlString.replace(dataPlaceHolder, "<p>" + id + "<br />\n" + row + "</p>\n"
					                + dataPlaceHolder);
				}
			}
		}
		htmlString = htmlString.replace(dataPlaceHolder, "");

		FileUtils.writeStringToFile(output, htmlString);
		System.out.print("HTML report created at: " + output.getAbsolutePath());
	}

}
