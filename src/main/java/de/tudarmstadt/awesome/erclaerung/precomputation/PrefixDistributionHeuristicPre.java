package de.tudarmstadt.awesome.erclaerung.precomputation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class PrefixDistributionHeuristicPre {

	private static int MAX_PREFIX_LENGTH = 3;
	private static int MIN_PREFIX_LENGTH = 2;
	private static int MIN_OCCURENCES = 20;
	private File outputRaw = new File("target/prefixHeuristic.txt");
	private PrintWriter writerRaw;

	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException,
	                AnalysisEngineProcessException, ResourceInitializationException {
		System.out.println("PrefixHeuristic stand-alone mode.");
		PrefixDistributionHeuristicPre prefixDisHeu = new PrefixDistributionHeuristicPre();
		prefixDisHeu.computeList();
		System.out.println("Created list in /target/:");
		System.out.println("prefixHeuristic.txt");
	}

	public PrefixDistributionHeuristicPre() throws FileNotFoundException, UnsupportedEncodingException,
	                AnalysisEngineProcessException, ResourceInitializationException {

		writerRaw = new PrintWriter(outputRaw, "UTF-8");
	}

	/**
	 * Computes a list of prefixes for the feature extractor.
	 * 
	 * @throws ResourceInitializationException
	 * @throws AnalysisEngineProcessException
	 */
	public void computeList() throws ResourceInitializationException, AnalysisEngineProcessException {
		long startTime = System.nanoTime();
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(BonnerXMLReader.class,
		                BonnerXMLReader.PARAM_SOURCE_LOCATION, "src/main/resources/bonner_korpora_train/*.xml");
		AnalysisEngineDescription segmenter = AnalysisEngineFactory
		                .createEngineDescription(BreakIteratorSegmenter.class);

		// Collect all jcas
		System.out.println("[HEU-PRE] Collecting documents for Prefix Finding.");
		ArrayList<List<String>> tokensList = new ArrayList<List<String>>();
		List<String> xmlNames = new ArrayList<String>();
		// read Documents
		for (JCas jcas : new JCasIterable(reader)) {
			SimplePipeline.runPipeline(jcas, segmenter);
			tokensList.add(JCasUtil.toText(JCasUtil.select(jcas, Token.class)));
			xmlNames.add(getDocumentName(jcas));
		}

		// find prefixes
		int prefixesFound = 0;
		int initialCapacityStrings = 0;
		int initialCapacityPrefixes = 0;
		for (List<String> tokens : tokensList) {
			initialCapacityPrefixes = initialCapacityPrefixes + (tokens.size() * MAX_PREFIX_LENGTH);
			initialCapacityStrings = initialCapacityStrings + tokens.size();
		}
		int document = 0;
		for (List<String> tokens : tokensList) {
			System.out.println("[HEU-PRE] " + xmlNames.get(document++) + "(" + document + "/" + (tokensList.size())
			                + ")");
			Map<String, Integer> seenPrefixes = new HashMap<String, Integer>(initialCapacityPrefixes);
			List<String> seenStrings = new ArrayList<String>(initialCapacityStrings);
			String[] tokensArray = tokens.toArray(new String[tokens.size()]);
			for (int tokenIndex = 0; tokenIndex < tokensArray.length; tokenIndex++) {
				String token = tokensArray[tokenIndex];
				if (!seenStrings.contains(token)) {
					seenStrings.add(token);
					// If token is longer than permitted maxSize set it to maxSize else to a letter less than the token
					// is long.
					int letterMax = MAX_PREFIX_LENGTH != -1 && token.length() - 1 > MAX_PREFIX_LENGTH ? MAX_PREFIX_LENGTH
					                : token.length() - 2;
					for (int letterIndex = letterMax - 1; letterIndex >= MIN_PREFIX_LENGTH - 1; letterIndex--) {
						String prefix = token.substring(0, letterIndex + 1);
						for (int comTokenIndex = tokenIndex + 1; comTokenIndex < tokensArray.length; comTokenIndex++) {
							if (tokensArray[comTokenIndex].startsWith(prefix)) {
								if (!seenPrefixes.containsKey(prefix)) {
									seenPrefixes.put(prefix, 1);
								}
								else {
									int occurences = seenPrefixes.get(prefix) + 1;
									seenPrefixes.put(prefix, occurences);
									if (occurences == MIN_OCCURENCES) {
										writerRaw.println(prefix);
										prefixesFound++;
									}
								}
							}
						}
					}
				}
			}
		}
		writerRaw.close();
		System.out.println("[HEU-PRE] Found " + prefixesFound + " prefixes.");
		long endTime = System.nanoTime();
		System.out.println("[HEU-PRE] Took " + (endTime - startTime) / 1000000000 + " seconds.");
	}

	private String getDocumentName(final JCas jcas) {
		DocumentMetaData aMetaData = DocumentMetaData.get(jcas);
		return aMetaData.getDocumentId();
	}

}
