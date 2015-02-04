package de.tudarmstadt.awesome.erclaerung.reports;

import java.awt.Point;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import de.tudarmstadt.ukp.dkpro.lab.reporting.BatchReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService;
import de.tudarmstadt.ukp.dkpro.lab.task.TaskContextMetadata;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.ExtractFeaturesAndPredictTask;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.uima.ExtractFeaturesAndPredictConnector;

public class EvaluationReport extends BatchReportBase implements Constants {
	private HashMap<String, Point> coords;

	private double getDistance(String a, String b) {
		Point ap = coords.get(a);
		Point bp = coords.get(b);
		return Math.sqrt(Math.pow(ap.getX() - bp.getX(), 2) + Math.pow(ap.getY() - bp.getY(), 2));
	}

	public void execute() throws Exception {
		if (this.coords == null) {
			coords = new HashMap<String, Point>();

			/*
			 * Longitude and Latitude of where the dialect is uses must be defined here
			 */
			coords.put("mbair", new Point(48, 11));
			coords.put("schwaeb", new Point(47, 10));
			coords.put("ofr", new Point(49, 11));
			coords.put("obs", new Point(51, 13));
			coords.put("rip", new Point(50, 6));
			coords.put("ohchal", new Point(47, 9));
			coords.put("oschwaeb", new Point(48, 10));
			coords.put("els", new Point(48, 7));
			coords.put("hess", new Point(50, 8));
			coords.put("thuer", new Point(50, 11));
		}

		StorageService store = getContext().getStorageService();
		for (TaskContextMetadata subcontext : getSubtasks()) {
			if (subcontext.getType().startsWith(ExtractFeaturesAndPredictTask.class.getName())) {
				// deserialize file
				FileInputStream f = new FileInputStream(store.getStorageFolder(subcontext.getId(),
				                ExtractFeaturesAndPredictConnector.PREDICTION_MAP_FILE_NAME));
				ObjectInputStream s = new ObjectInputStream(f);
				@SuppressWarnings("unchecked")
				Map<String, List<String>> resultMap = (Map<String, List<String>>) s.readObject();
				s.close();

				double sum = 0;
				double count = 0;
				System.out.println("\n\nEVALUATION REPORT:\n");
				System.out.println("The smaller the number the better the prediction is.\n");
				for (String id : resultMap.keySet()) {
					String fileName = id.substring(0, id.indexOf('.'));
					String pred = StringUtils.join(resultMap.get(id), ",");
					double res = this.getDistance(fileName, pred);
					count += 1;
					sum += res;
					System.out.println(StringUtils.leftPad(id, 25) + ": " + StringUtils.center(pred, 7) + " " + res);
				}
				System.out.println("");
				System.out.println(StringUtils.leftPad("Average Result: ", 25) + (sum / count));
				System.out.println("\nEVALUATION REPORT END\n\n");
			}
		}
	}
}
