package de.tudarmstadt.awesome.erclaerung.feature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;

/**
 * Uses a predefined list to create a feature distribution.
 * 
 * @author Manuel
 *
 */
public class ManualWordListDFE extends FeatureExtractorResource_ImplBase implements DocumentFeatureExtractor {

	public static final String FN_MANUAL_PREFIX = "Manual_";

	public List<Feature> extract(JCas jcas) throws TextClassificationException {
		List<String> tokens = JCasUtil.toText(JCasUtil.select(jcas, Token.class));
		List<Feature> featList = new ArrayList<Feature>();
		File wordListFile = new File("src/main/resources/manual_list/manualList.txt");
		List<String> lines = null;
		try {
			lines = Files.readLines(wordListFile, Charsets.UTF_8);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		Map<String, Integer> occurences = new HashMap<String, Integer>(lines.size() * 2);

		// Collect manual word List
		for (String line : lines) {
			if (line == "" || line.contains("Insert") || line.contains("Delete") || line.contains("Substitution"))
				; // Do nothing
			else if (line.contains("->")) {
				String[] words = line.split("->");
				occurences.put(words[0], 0);
				occurences.put(words[1], 0);
			}
			else {
				occurences.put(line.trim(), 0);
			}
		}
		for (String token : tokens) {
			String tokenLower = token.toLowerCase();
			if (occurences.keySet().contains(tokenLower)) {
				occurences.put(tokenLower, occurences.get(tokenLower) + 1);
			}
		}
		for (String key : occurences.keySet()) {
			featList.add(new Feature(FN_MANUAL_PREFIX + key, occurences.get(key) * 1000 / occurences.keySet().size()));
		}

		return featList;
	}

}
