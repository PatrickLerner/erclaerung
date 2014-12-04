package de.tudarmstadt.awesome.erclaerung;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;

/**
 * This is a reader component for the Bonner Frühneuhochdeutschkorpus in its (horrible) XML encoding.
 * 
 * @author Patrick Lerner
 */
public class BonnerXMLReader extends ResourceCollectionReaderBase {
	public void getNext(CAS aCAS) throws IOException, CollectionException {
		// get the next file and initialize cas
		Resource res = nextFile();
		initCas(aCAS, res);

		InputStream is = null;

		try {
			JCas jcas = aCAS.getJCas();

			is = res.getInputStream();

			// Create handler
			TextExtractor handler = new TextExtractor(jcas);

			// Parser XML
			SAXParserFactory pf = SAXParserFactory.newInstance();
			SAXParser parser = pf.newSAXParser();

			InputSource source = new InputSource(is);
			source.setPublicId(res.getLocation());
			source.setSystemId(res.getLocation());
			parser.parse(source, handler);

			// Set up language
			if (getConfigParameterValue(PARAM_LANGUAGE) != null) {
				aCAS.setDocumentLanguage((String) getConfigParameterValue(PARAM_LANGUAGE));
			}
		}
		catch (CASException e) {
			throw new CollectionException(e);
		}
		catch (ParserConfigurationException e) {
			throw new CollectionException(e);
		}
		catch (SAXException e) {
			throw new IOException(e);
		}
		finally {
			closeQuietly(is);
		}
	}

	public static class TextExtractor extends DefaultHandler {
		private JCas jcas;

		private final StringBuilder buffer = new StringBuilder();

		private boolean inMorphElement = false;
		private boolean firstInLine = false;

		public TextExtractor(final JCas jcas) {
			this.jcas = jcas;
		}

		@Override
		public void characters(char[] aCh, int aStart, int aLength) throws SAXException {
			// reject everything that is not in a "morph" tag (i.e. the garbage)
			if (!this.inMorphElement)
				return;

			if (!this.firstInLine)
				buffer.append(' ');
			this.firstInLine = false;

			buffer.append(aCh, aStart, aLength);
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			this.inMorphElement = qName.equals("morph");
			if (qName.equals("zeile")) {
				this.buffer.append("\n");
				this.firstInLine = true;
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) {
			this.inMorphElement = false;
		}

		@Override
		public void endDocument() throws SAXException {
			this.jcas.setDocumentText(this.buffer.toString());
		}

		protected StringBuilder getBuffer() {
			return this.buffer;
		}
	}
}
