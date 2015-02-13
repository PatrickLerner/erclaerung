package de.tudarmstadt.awesome.erclaerung.reports;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang.StringUtils;

import de.tudarmstadt.ukp.dkpro.lab.reporting.BatchReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.StorageService;
import de.tudarmstadt.ukp.dkpro.lab.task.TaskContextMetadata;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.BatchTaskCrossValidation;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.ExtractFeaturesAndPredictTask;
import de.tudarmstadt.ukp.dkpro.tc.weka.task.uima.ExtractFeaturesAndPredictConnector;

/**
 * @author Patrick Lerner, Manuel Spari
 * @deprecated
 * @see EvaluationReportNeighbor
 */
public class EvaluationReportNeighborsManuel extends BatchReportBase implements Constants {
	private HashMap<String, HashSet<String>> connections;
	private Random rnd = new Random();

	private void addConnection(String a, String b) {
		HashSet<String> neighbors = new HashSet<String>();
		if (this.connections.containsKey(a))
			neighbors = this.connections.get(a);
		neighbors.add(b);
		this.connections.put(a, neighbors);

		neighbors = new HashSet<String>();
		if (this.connections.containsKey(b))
			neighbors = this.connections.get(b);
		neighbors.add(a);
		this.connections.put(b, neighbors);
	}

	private double calculateNeighborScore(String real, String pred) {
		HashSet<String> real_s = connections.get(real);
		// HashSet<String> pred_s = connections.get(pred);
		if (real.equals(pred))
			return 0;
		else if (real_s.contains(pred))
			return 1;
		else
			return 5;
	}

	private String getRandomId() {
		String[] ids = new String[] { "rip", "thuer", "hess", "ofr", "obs", "els", "schwaeb", "ohchal", "oschwaeb",
		                "mbair" };
		return ids[rnd.nextInt(ids.length)];

	}

	public void execute() throws Exception {
		if (this.connections == null) {
			this.connections = new HashMap<String, HashSet<String>>();
			addConnection("rip", "thuer");
			addConnection("rip", "hess");
			addConnection("thuer", "hess");
			addConnection("ofr", "hess");
			addConnection("ofr", "thuer");
			addConnection("obs", "thuer");
			addConnection("obs", "hess");
			addConnection("els", "schwaeb");
			addConnection("hess", "schwaeb");
			addConnection("els", "hess");
			addConnection("ofr", "schwaeb");
			addConnection("ohchal", "els");
			addConnection("ohchal", "schwaeb");
			addConnection("oschwaeb", "schwaeb");
			addConnection("ofr", "oschwaeb");
			addConnection("ohchal", "mbair");
			addConnection("mbair", "oschwaeb");
			addConnection("mbair", "schwaeb");
			addConnection("ohchal", "oschwaeb");
			addConnection("els", "rip");
			addConnection("ofr", "mbair");
		}

		StorageService store = getContext().getStorageService();
		for (TaskContextMetadata subcontext : getSubtasks()) {
			double sum = 0;
			double count = 0;
			double hit = 0;
			double neighbor_hit = 0;
			double finalSum = 0;
			double runs = 10000;
			double finalhits = 0;
			double finalneighborhits = 0;
			if (subcontext.getType().startsWith(ExtractFeaturesAndPredictTask.class.getName())
			                || subcontext.getType().startsWith(BatchTaskCrossValidation.class.getName())) {
				System.out.println("\n\nEVALUATION (NEIGHBORS / MANUEL) REPORT:\n");
				System.out.println("The smaller the number the better the prediction is.\n");
			}
			if (subcontext.getType().startsWith(ExtractFeaturesAndPredictTask.class.getName())) {
				// deserialize file
				FileInputStream f = new FileInputStream(store.getStorageFolder(subcontext.getId(),
				                ExtractFeaturesAndPredictConnector.PREDICTION_MAP_FILE_NAME));
				ObjectInputStream s = new ObjectInputStream(f);
				@SuppressWarnings("unchecked")
				Map<String, List<String>> resultMap = (Map<String, List<String>>) s.readObject();
				s.close();
				// Find a guesswork value

				for (int i = 0; i < runs; i++) {
					double sum_exp = 0;
					double count_exp = 0;
					double hits_exp = 0;
					double neighbor_hits_exp = 0;
					for (String id : resultMap.keySet()) {
						String real = id.substring(0, id.indexOf('_'));
						String pred = this.getRandomId();
						double res = this.calculateNeighborScore(real, pred);
						count_exp += 1;
						sum_exp += res;
						if (res == 0)
							hits_exp += 1;
						if (res == 1)
							neighbor_hits_exp += 1;
					}
					finalSum += sum_exp / count_exp;
					finalhits += hits_exp / count_exp;
					finalneighborhits += neighbor_hits_exp / count_exp;
				}

				// Check the real values
				for (String id : resultMap.keySet()) {
					String real = id.substring(0, id.indexOf('_'));
					String pred = StringUtils.join(resultMap.get(id), ",");

					System.out.print(StringUtils.leftPad(id, 25) + ": ");

					double res = this.calculateNeighborScore(real, pred);
					count += 1;
					sum += res;
					if (res == 0)
						hit += 1;
					if (res == 1)
						neighbor_hit += 1;

					System.out.println(StringUtils.center(pred, 9) + " " + res);
				}
			}
			else if (subcontext.getType().startsWith(BatchTaskCrossValidation.class.getName())) {
				FileReader fileReader = new FileReader(store.getStorageFolder(subcontext.getId(), "id2outcome.txt"));
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					if (line.contains("=")) {
						String id = line.substring(0, line.indexOf('='));
						String pred = line.substring(line.indexOf('=') + 1, line.indexOf(';'));
						String real = line.substring(line.indexOf(';') + 1);

						double res = this.calculateNeighborScore(real, pred);
						count += 1;
						sum += res;
						if (res == 0)
							hit += 1;
						if (res == 1)
							neighbor_hit += 1;

						System.out.println(StringUtils.leftPad(id, 25) + ": " + StringUtils.center(real, 9) + " "
						                + StringUtils.center(pred, 7) + " " + res);
					}
				}
				fileReader.close();
			}
			if (subcontext.getType().startsWith(ExtractFeaturesAndPredictTask.class.getName())
			                || subcontext.getType().startsWith(BatchTaskCrossValidation.class.getName())) {
				System.out.println("");
				System.out.println(StringUtils.leftPad("Average Result: ", 25) + (sum / count));
				System.out.println(StringUtils.leftPad("Correct Hits: ", 25) + (hit / count * 100) + "%");
				System.out.println(StringUtils.leftPad("Correct+Neighbor Hits: ", 25)
				                + ((hit + neighbor_hit) / count * 100) + "%");
				System.out.println(StringUtils.leftPad("Lower borderline value: ", 25) + finalSum / runs);
				System.out.println(StringUtils.leftPad("Lower borderline hits: ", 25) + (finalhits / runs) * 100 + "%");
				System.out.println(StringUtils.leftPad("Lower borderline neighbor hits: ", 25)
				                + (finalneighborhits / runs) * 100 + "%");
				System.out.println("\nEVALUATION (NEIGHBORS / MANUEL) REPORT END\n\n");
			}
		}
	}
}
