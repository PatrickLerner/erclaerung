package de.tudarmstadt.awesome.erclaerung.readers;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.tc.api.io.TCReaderSingleLabel;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationOutcome;

/**
 * This is a reader component for the Bonner Frühneuhochdeutschkorpus in its (horrible) XML encoding.
 * 
 * @author Patrick Lerner
 */
public class BonnerXMLReader extends TextReader implements TCReaderSingleLabel {
	public String getTextClassificationOutcome(JCas jcas) throws CollectionException {
		return jcas.getDocumentLanguage();
	}

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

			StringWriter writer = new StringWriter();
			IOUtils.copy(is, writer, "UTF8");
			String xmlContent = writer.toString();

			System.out.println("[BONN-XML] Reading file: " + res.getPath());

			// because the source XML-Files are incorrect, we need to hot-patch them here
			// for them to be read in correctly. This is obviously far from ideal, but the best
			// solution for ease-of-use purposes as the modified xml files would not be distributable
			// by a third party
			if (res.getPath().equals("243.xml") || res.getPath().equals("257.xml"))
				xmlContent = xmlContent.replaceAll(" zweit ", " zweit=\"zweit\" ");

			InputStream stream = new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8));

			InputSource source = new InputSource(stream);
			source.setPublicId(res.getLocation());
			source.setSystemId(res.getLocation());
			parser.parse(source, handler);

			TextClassificationOutcome outcome = new TextClassificationOutcome(jcas);
			outcome.setOutcome(getTextClassificationOutcome(jcas));
			outcome.addToIndexes();
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
		private boolean inSprachraumElement = false;
		private boolean firstInLine = false;

		public TextExtractor(final JCas jcas) {
			this.jcas = jcas;
		}

		@Override
		public void characters(char[] aCh, int aStart, int aLength) throws SAXException {
			if (this.inSprachraumElement) {
				this.jcas.setDocumentLanguage(new String(aCh, aStart, aLength));
				return;
			}
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
			this.inSprachraumElement = qName.equals("Sprachraum");
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
