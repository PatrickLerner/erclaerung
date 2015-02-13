package de.tudarmstadt.awesome.erclaerung.readers;

import java.io.IOException;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.tc.api.io.TCReaderSingleLabel;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationOutcome;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;

/**
 * This class straight up just reads in a text file, without doing much else. This must be done in this extra class
 * since it needs to return a constant to the text classifier that indicates it's outcome needs to be determined.
 * 
 * @author Patrick Lerner
 */
public class UnlabeledTextReader extends TextReader implements TCReaderSingleLabel {
	@Override
	public void getNext(CAS aCAS) throws IOException, CollectionException {
		super.getNext(aCAS);

		JCas jcas;
		try {
			jcas = aCAS.getJCas();
		}
		catch (CASException e) {
			throw new CollectionException();
		}

		TextClassificationOutcome outcome = new TextClassificationOutcome(jcas);
		outcome.setOutcome(getTextClassificationOutcome(jcas));
		outcome.addToIndexes();
	}

	public String getTextClassificationOutcome(JCas jcas) throws CollectionException {
		return Constants.UNKNOWN_OUTCOME;
	}
}
