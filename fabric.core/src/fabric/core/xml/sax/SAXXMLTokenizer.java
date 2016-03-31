/*
 * (C) Copyright IBM Corp. 2007
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.core.xml.sax;

import java.io.IOException;
import java.io.Reader;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import fabric.core.xml.IXMLTokenHandler;
import fabric.core.xml.IXMLTokenizer;
import fabric.core.xml.XMLAttributes;

/**
 * XML tokenizer. This class is intended for use when no standard parser, e.g. a SAX parser, is available in the target
 * system.
 * <p>
 * <strong>Note:</strong> this is a very simplistic tokenizer that does not attempt to replace SAX. For example it does
 * not implement many XML features, including fundamentals like name spaces.
 * </p>
 */
public class SAXXMLTokenizer extends DefaultHandler implements IXMLTokenizer {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007";

	/*
	 * Class fields
	 */

	/** The content handler for this tokenizer */
	private IXMLTokenHandler handler = null;

	/*
	 * Class methods
	 */

	/**
	 * Constructs a new instance.
	 */
	public SAXXMLTokenizer() {

	}

	/**
	 * Parses the XML on the specified input stream using SAX.
	 * 
	 * @param xmlStream
	 *            the input stream.
	 * 
	 * @throws IOException
	 * @throws Exception
	 */
	@Override
	public void tokenize(Reader xmlStream) throws IOException, Exception {

		XMLReader xmlReader = XMLReaderFactory.createXMLReader();
		xmlReader.setContentHandler(this);
		xmlReader.setErrorHandler(this);
		xmlReader.parse(new InputSource(xmlStream));

	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String,
	 * org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String namespace, String namespaceName, String basicName, Attributes elementAttributes) {

		/* Capture the element's attributes */

		XMLAttributes xmlElementAttributes = new XMLAttributes();

		/* For each SAX attribute... */
		for (int a = 0; a < elementAttributes.getLength(); a++) {
			String attributeName = elementAttributes.getLocalName(a);
			String attributeValue = elementAttributes.getValue(a);

			/* Create a tokenizer attribute */
			xmlElementAttributes.addAttribute(attributeName, attributeValue);
		}

		/* Invoke the handler */
		handler.xmlElementStart(namespaceName, xmlElementAttributes);
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {

		/* Extract the array segment into its own array */
		char[] characters = new char[length];
		System.arraycopy(ch, start, characters, 0, length);

		/* Invoke the handler */
		handler.xmlElementData(characters);

	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String namespace, String namespaceName, String basicName) {

		/* Invoke the handler */
		handler.xmlElementEnd(namespaceName);

	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#processingInstruction(java.lang.String, java.lang.String)
	 */
	@Override
	public void processingInstruction(String target, String data) throws SAXException {

		/* Invoke the handler */
		handler.xmlProcessingInstruction(target + " " + data);

	}

	/**
	 * Sets the handler for this tokenizer.
	 * 
	 * @param handler
	 *            the new content handler.
	 */
	@Override
	public void setHandler(IXMLTokenHandler handler) {
		this.handler = handler;
	}

	/**
	 * Gets the content handler for this tokenizer.
	 * 
	 * @return the content handler.
	 */
	@Override
	public IXMLTokenHandler getHandler() {
		return handler;
	}
}