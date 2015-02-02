package de.tudarmstadt.awesome.erclaerung.precomputation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.awesome.erclaerung.readers.BonnerXMLReader;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

/**
 * @author Manuel
 *
 */
public class LevenshteinDistancePreComp {

	private static int minDistance = 1;
	private static int maxDistance = 1;
	private static int maxWordLength = -1;
	private static int minWordLength = 7;
	private static int suffixRepression = 1;
	private File output = new File("src/main/resources/precomputation/levenshtein.txt");
	PrintWriter writer;

	public LevenshteinDistancePreComp() throws FileNotFoundException, UnsupportedEncodingException {
		writer = new PrintWriter(output, "UTF-8");
	}

	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException,
	                AnalysisEngineProcessException, ResourceInitializationException {
		LevenshteinDistancePreComp lev = new LevenshteinDistancePreComp();
		lev.computeList();
		// System.out.println(getTransformationStepsPretty("parfum", "pxyzarfm"));
		// System.out.println(levenshteinDistance("parfum", "pxyzarfm"));
		// System.out.println(getTransformationStepsPretty("test", "ts"));
		// System.out.println(getTransformationStepsPretty("verboten", "vorboten"));
		// System.out.println(lev.computeLevenshteinDistance("parfum", "xxxxx"));
		// System.out.println(lev.computeLevenshteinDistance("parfum", "hallo"));
		// System.out.println(lev.computeLevenshteinDistance("parfum", "putzm"));
	}

	private static int minimum(int a, int b, int c) {
		return Math.min(Math.min(a, b), c);
	}

	public static LevenshteinTransformation getTransformation(String string1, String string2) {
		List<LevenshteinStep> steps = LevenshteinDistancePreComp.getSimplestTransformationPath(string1, string2);
		return new LevenshteinTransformation(string1, string2, steps);
	}

	private static String getTransformationStepsPretty(String string1, String string2) {
		return LevenshteinDistancePreComp.getTransformation(string1, string2).toStringWithTransformation(false);
	}

	/**
	 * Returns one of the simplest paths to convert one string into another in <Strong>correct order of indexes</Strong>
	 * of source string.
	 * 
	 * @param str1
	 *            Source String
	 * @param str2
	 *            Destination String
	 * @return A List of {@link LevenshteinStep}s representing the steps from one string to another.
	 */
	public static List<LevenshteinStep> getSimplestTransformationPath(String str1, String str2) {
		int[][] levenshteinMatrix = getLevenshteinMatrix(str1, str2);
		List<LevenshteinStep> reverseSteps = new ArrayList<LevenshteinStep>();
		int i = levenshteinMatrix.length - 1;
		int j = levenshteinMatrix[0].length - 1;
		while (i != 0 || j != 0) {
			int current = levenshteinMatrix[i][j];

			if (i == 0) {
				reverseSteps.add(new LevenshteinStep(i, LevenshteinStep.Operation.INSERT, str2.charAt(j - 1)));
				j--;
			}
			else if (j == 0) {
				reverseSteps.add(new LevenshteinStep(i - 1, LevenshteinStep.Operation.DELETE, str1.charAt(i - 1)));
				i--;
			}
			else {
				int up = levenshteinMatrix[i - 1][j];
				int left = levenshteinMatrix[i][j - 1];
				int upLeft = levenshteinMatrix[i - 1][j - 1];
				int min = minimum(up, left, upLeft);
				if (min == current || min == upLeft) {
					if (upLeft == current)
						reverseSteps.add(new LevenshteinStep(i - 1, LevenshteinStep.Operation.NONOP, str1.charAt(i - 1)));
					else {
						reverseSteps.add(new LevenshteinStep(i - 1, LevenshteinStep.Operation.SUBSTITUTION, str2
						                .charAt(j - 1)));
					}
					j--;
					i--;

				}
				else if (min == left) {
					reverseSteps.add(new LevenshteinStep(i, LevenshteinStep.Operation.INSERT, str2.charAt(j - i)));
					j--;
				}
				else if (min == up) {
					reverseSteps.add(new LevenshteinStep(i - 1, LevenshteinStep.Operation.DELETE, str1.charAt(i - 1)));
					i--;
				}
			}
		}
		List<LevenshteinStep> steps = new ArrayList<LevenshteinStep>();
		for (LevenshteinStep levenshteinStep : reverseSteps) {
			steps.add(0, levenshteinStep);
		}
		// LevenshteinTransformation trans = new LevenshteinTransformation(str1, str2, steps);
		// System.out.println(trans.toStringWithTransformation());
		return steps;
	}

	/**
	 * Returns a matrix consisting of the possible steps to convert one string into another. See
	 * http://stackoverflow.com/a/10641240 for an excellent explanation.
	 * 
	 * @param str1
	 *            source string
	 * @param str2
	 *            destination string
	 * @return A matrix containing all possible ways to go from one string to another.
	 */
	private static int[][] getLevenshteinMatrix(CharSequence str1, CharSequence str2) {
		int[][] distance = new int[str1.length() + 1][str2.length() + 1];

		for (int i = 0; i <= str1.length(); i++) {
			distance[i][0] = i;
		}
		for (int j = 0; j <= str2.length(); j++) {
			distance[0][j] = j;
		}

		for (int i = 1; i <= str1.length(); i++) {
			for (int j = 1; j <= str2.length(); j++) {
				int delete = distance[i - 1][j] + 1;
				int insert = distance[i][j - 1] + 1;
				int replace = distance[i - 1][j - 1] + ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0 : 1);
				distance[i][j] = minimum(insert, delete, replace);
				/*
				 * if (insert <= delete && insert <= replace) System.out.println("insert " + distance[i][j]); if (delete
				 * <= insert && delete <= replace) System.out.println("delete " + distance[i][j]); if (replace <= insert
				 * && replace <= delete) System.out.println("replace " + distance[i][j]);
				 */
				// System.out.println(Arrays.deepToString(distance).replace("],", "\n") + "\n");
			}
		}
		// System.out.println(Arrays.deepToString(distance).replace("],", "\n") + "\n");
		return distance;
		// return distance[str1.length()][str2.length()];
	}

	// from http://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance#Java
	/**
	 * Returns the levenshtein distance between two strings which is a measure for the steps needed to convert one
	 * string into another.
	 * 
	 * @param s0
	 *            string 1.
	 * @param s1
	 *            string 2.
	 * @return The levenshtein distance. see: http://en.wikipedia.org/wiki/Levenshtein_distance.
	 */
	public static int levenshteinDistance(String s0, String s1) {
		int len0 = s0.length() + 1;
		int len1 = s1.length() + 1;

		// the array of distances
		int[] cost = new int[len0];
		int[] newcost = new int[len0];

		// initial cost of skipping prefix in String s0
		for (int i = 0; i < len0; i++)
			cost[i] = i;

		// dynamically computing the array of distances

		// transformation cost for each letter in s1
		for (int j = 1; j < len1; j++) {
			// initial cost of skipping prefix in String s1
			newcost[0] = j;

			// transformation cost for each letter in s0
			for (int i = 1; i < len0; i++) {
				// matching current letters in both strings
				int match = (s0.charAt(i - 1) == s1.charAt(j - 1)) ? 0 : 1;

				// computing cost for each transformation
				int cost_replace = cost[i - 1] + match;
				int cost_insert = cost[i] + 1;
				int cost_delete = newcost[i - 1] + 1;

				// keep minimum cost
				newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);

			}

			// swap cost/newcost arrays
			int[] swap = cost;
			cost = newcost;
			newcost = swap;
		}

		// the distance is the cost for transforming all letters in both strings
		return cost[len0 - 1];
	}

	public void computeList() throws ResourceInitializationException, AnalysisEngineProcessException {
		long startTime = System.nanoTime();
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(BonnerXMLReader.class,
		                BonnerXMLReader.PARAM_SOURCE_LOCATION, "src/main/resources/bonner_korpora_train/*.xml");
		AnalysisEngineDescription segmenter = AnalysisEngineFactory
		                .createEngineDescription(BreakIteratorSegmenter.class);

		// Collect all jcas
		System.out.println("Collecting documents for Levenshtein Comparison.");
		ArrayList<List<String>> tokens = new ArrayList<List<String>>();
		List<String> xmlNames = new ArrayList<String>();
		List<LevenshteinStep> collectedSteps = new ArrayList<LevenshteinStep>();
		for (JCas jcas : new JCasIterable(reader)) {
			SimplePipeline.runPipeline(jcas, segmenter);
			tokens.add(JCasUtil.toText(JCasUtil.select(jcas, Token.class)));
			xmlNames.add(getDocumentName(jcas));
		}
		List<String[]> closeTuples = new ArrayList<String[]>();
		List<String> closeToplesS = new ArrayList<String>();
		// List A
		for (int i = 0; i < tokens.size(); i++) {
			List<String> tokensI = tokens.get(i);
			// List B
			for (int j = i + 1; j < tokens.size(); j++) {
				int similarWordCount = 0;
				System.out.println("[LEV-PRE] " + xmlNames.get(i) + "->" + xmlNames.get(j) + "(" + (j - i) + "/"
				                + (tokens.size() - i - 1) + ")");
				writer.println("\n\n" + xmlNames.get(i) + "->" + xmlNames.get(j) + "\n");
				writer.flush();
				List<String> tokensJ = tokens.get(j);
				int iter = 0;
				// Token A
				for (int k = 0; k < tokensI.size(); k++) {
					if (++iter % 500 == 0)
						System.out.println("[LEV-PRE] Tokens: " + iter + "/" + tokensI.size());

					String token1 = tokensI.get(k).toLowerCase();
					int len1 = token1.length();
					if ((minWordLength == -1 || len1 >= minWordLength)
					                && (maxWordLength == -1 || len1 <= maxWordLength)) {
						for (int l = k + 1; l < tokensJ.size(); l++) {
							// Token B
							String token2 = tokensJ.get(l).toLowerCase();
							int len2 = token2.length();
							if ((minWordLength == -1 || len2 >= minWordLength)
							                && (maxWordLength == -1 || len2 <= maxWordLength)) {
								int levDist = levenshteinDistance(token1, token2);
								if (levDist <= maxDistance && levDist >= minDistance) {
									String[] tuple = new String[] { token1, token2 };
									String s = token1 + "_" + token2;
									if (!closeToplesS.contains(s)) {
										if (manualFilter(token1, token2) && !tokensJ.contains(token1)
										                && !tokensI.contains(token2)) {
											closeTuples.add(tuple);
											closeToplesS.add(s);
											// System.out.println("Lev: " + levDist + " String1: " + token1 +
											// " String2:"
											// + token2);
											similarWordCount++;

											collectedSteps.addAll(LevenshteinDistancePreComp.getTransformation(token1,
											                token2).getIndexReversedLevenshteinSteps(false));
											writer.println("\n"
											                + LevenshteinDistancePreComp.getTransformationStepsPretty(
											                                token1, token2));
										}
									}
								}
							}
						}
					}
				}
				HashMap<LevenshteinStep, Integer> stepSet = new HashMap<LevenshteinStep, Integer>();
				List<LevenshteinStep> seen = new ArrayList<LevenshteinStep>();
				for (LevenshteinStep levenshteinStep : collectedSteps) {
					if (!seen.contains(levenshteinStep)) {
						seen.add(levenshteinStep);
						stepSet.put(levenshteinStep, Collections.frequency(collectedSteps, levenshteinStep));
					}
				}
				Map<LevenshteinStep, Integer> sortedStepSet = sortByComparator(stepSet, false);
				System.out.println(sortedStepSet.toString().replace(",", "\n"));
				System.out.println("[LEV-PRE] Found " + similarWordCount + " similar words.");
				writer.flush();
			}
		}

		writer.close();
		long endTime = System.nanoTime();
		System.out.println("Took " + (endTime - startTime) / 1000000 + " milliseconds.");
	}

	private boolean manualFilter(String token1, String token2) {
		// strings are same except for last letter.
		if (suffixRepression > 0) {
			String token1Shave = token1.substring(0, token1.length() - suffixRepression);
			String token2Shave = token2.substring(0, token2.length() - suffixRepression);
			if (token1Shave.equals(token2) || token1Shave.equals(token2Shave) || token2Shave.equals(token1))
				return false;
		}
		return true;
	}

	private String getDocumentName(final JCas jcas) {
		DocumentMetaData aMetaData = DocumentMetaData.get(jcas);
		return aMetaData.getDocumentId();
	}

	private static Map<LevenshteinStep, Integer> sortByComparator(Map<LevenshteinStep, Integer> unsortMap,
	                final boolean ascending) {

		List<Entry<LevenshteinStep, Integer>> list = new LinkedList<Entry<LevenshteinStep, Integer>>(
		                unsortMap.entrySet());

		// Sorting the list based on values
		Collections.sort(list, new Comparator<Entry<LevenshteinStep, Integer>>() {
			public int compare(Entry<LevenshteinStep, Integer> o1, Entry<LevenshteinStep, Integer> o2) {
				if (ascending) {
					return o1.getValue().compareTo(o2.getValue());
				}
				else {
					return o2.getValue().compareTo(o1.getValue());

				}
			}
		});

		// Maintaining insertion order with the help of LinkedList
		Map<LevenshteinStep, Integer> sortedMap = new LinkedHashMap<LevenshteinStep, Integer>();
		for (Entry<LevenshteinStep, Integer> entry : list) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		return sortedMap;
	}

}
