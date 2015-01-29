package de.tudarmstadt.awesome.erclaerung.precomputation;

import java.io.IOException;
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
	private static int minWordLength = 5;

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
		for (JCas jcas : new JCasIterable(reader)) {
			SimplePipeline.runPipeline(jcas, segmenter);
			tokens.add(JCasUtil.toText(JCasUtil.select(jcas, Token.class)));
			System.out.println("[LEV-PRE]" + " Added " + getDocumentName(jcas));
		}
		List<String[]> closeTuples = new ArrayList<String[]>();
		List<String> closeToplesS = new ArrayList<String>();

		for (int i = 0; i < tokens.size(); i++) {
			List<String> tokensI = tokens.get(i);
			for (int j = i + 1; j < tokens.size(); j++) {
				List<String> tokensJ = tokens.get(j);
				System.out.println("Checking " + tokensJ.size() + " tokens.");
				int iter = 0;
				for (int k = 0; k < tokensI.size(); k++) {
					if (++iter % 100 == 0)
						System.out.println("Done: " + iter);
					String token1 = tokensI.get(k).toLowerCase();
					if (!tokensJ.contains(token1)) {
						int len1 = token1.length();
						if ((minWordLength == -1 || len1 >= minWordLength)
						                && (maxWordLength == -1 || len1 <= maxWordLength)) {
							for (int l = k + 1; l < tokensJ.size(); l++) {
								String token2 = tokensJ.get(l).toLowerCase();
								if (!tokensI.contains(token2)) {
									int len2 = token2.length();
									if ((minWordLength == -1 || len2 >= minWordLength)
									                && (maxWordLength == -1 || len2 <= maxWordLength)) {
										int levDist = computeLevenshteinDistance(token1, token2);
										if (levDist <= maxDistance && levDist >= minDistance) {
											String[] tuple = new String[] { token1, token2 };
											String s = token1 + "_" + token2;
											if (!closeToplesS.contains(s)) {
												closeTuples.add(tuple);
												closeToplesS.add(s);
												System.out.println("Lev: " + levDist + " String1: " + token1
												                + " String2:" + token2);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}

	}

	private String getDocumentName(final JCas jcas) {
		DocumentMetaData aMetaData = DocumentMetaData.get(jcas);
		return aMetaData.getDocumentId();
	}

}
