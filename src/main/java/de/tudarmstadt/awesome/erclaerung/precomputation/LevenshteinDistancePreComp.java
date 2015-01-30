package de.tudarmstadt.awesome.erclaerung.precomputation;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionException;
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

public class LevenshteinDistancePreComp {
	private static int minDistance = 1;
	private static int maxDistance = 1;
	private static int maxWordLength = -1;
	private static int minWordLength = 7;
	// Filters the last letters from levenshtein computation
	private static int suffixRepression = 1;
	private File output = new File("src/main/resources/precomputation/levenshtein.txt");
	PrintWriter writer = new PrintWriter(output, "UTF-8");

	// from dkpro.similarity.algorithms.api
	// seems not to be in Maven -> copy/paste
	private static int minimum(int a, int b, int c) {
		return Math.min(Math.min(a, b), c);
	}

	private int computeLevenshteinDistance(CharSequence str1, CharSequence str2) {
		int[][] distance = new int[str1.length() + 1][str2.length() + 1];

		for (int i = 0; i <= str1.length(); i++) {
			distance[i][0] = i;
		}
		for (int j = 0; j <= str2.length(); j++) {
			distance[0][j] = j;
		}

		for (int i = 1; i <= str1.length(); i++) {
			for (int j = 1; j <= str2.length(); j++) {
				distance[i][j] = minimum(distance[i - 1][j] + 1, distance[i][j - 1] + 1, distance[i - 1][j - 1]
				                + ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0 : 1));
			}
		}

		return distance[str1.length()][str2.length()];
	}

	public LevenshteinDistancePreComp() throws CollectionException, IOException, ResourceInitializationException,
	                AnalysisEngineProcessException {
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(BonnerXMLReader.class,
		                BonnerXMLReader.PARAM_SOURCE_LOCATION, "src/main/resources/bonner_korpora_train/*.xml");
		AnalysisEngineDescription segmenter = AnalysisEngineFactory
		                .createEngineDescription(BreakIteratorSegmenter.class);

		// Collect all jcas
		System.out.println("Collecting documents for Levenshtein Comparison.");
		ArrayList<List<String>> tokens = new ArrayList<List<String>>();
		List<String> xmlNames = new ArrayList<String>();
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
								int levDist = computeLevenshteinDistance(token1, token2);
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
											writer.println(s);
										}
									}
								}
							}
						}
					}
				}
				System.out.println("[LEV-PRE] Found " + similarWordCount + " similar words.");
				writer.flush();
			}
		}
		writer.close();
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

}
