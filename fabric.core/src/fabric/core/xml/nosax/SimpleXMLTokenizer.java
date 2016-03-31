/*
 * (C) Copyright IBM Corp. 2007
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.core.xml.nosax;

import java.io.IOException;
import java.io.Reader;

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
public class SimpleXMLTokenizer implements IXMLTokenizer {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007";

	/*
	 * Class constants
	 */

	/* Parser states */

	private static final int HEADER_START = 10;

	private static final int HEADER_TAG_START = 20;

	private static final int BODY_TAG_START = 25;

	private static final int COMMENT_START1 = 30;

	private static final int COMMENT_START2 = 40;

	private static final int COMMENT_PART = 50;

	private static final int COMMENT_END1 = 60;

	private static final int COMMENT_END2 = 70;

	private static final int PI_BODY = 80;

	private static final int PI_END = 90;

	private static final int ELEMENT_START_TAG_NAME_START = 100;

	private static final int ELEMENT_START_TAG_NAME_PART = 110;

	private static final int ELEMENT_BODY_START = 120;

	private static final int ELEMENT_BODY_PART = 130;

	private static final int ATTRIBUTE_NAME_START = 140;

	private static final int ATTRIBUTE_NAME_PART = 150;

	private static final int ATTRIBUTE_VALUE_START = 160;

	private static final int ATTRIBUTE_VALUE_PART = 170;

	private static final int SINGLE_TAG_ELEMENT_END = 180;

	private static final int ELEMENT_END_TAG_NAME_START = 190;

	private static final int ELEMENT_END_TAG_NAME_PART = 200;

	/*
	 * Class fields
	 */

	/** The stream containing the XML to be parsed */
	protected Reader xmlStream = null;

	/** The content handler for this tokenizer */
	private IXMLTokenHandler handler = null;

	/*
	 * Class methods
	 */

	/**
	 * Constructs a new instance.
	 */
	public SimpleXMLTokenizer() {

	}

	/**
	 * Parses the XML on the specified input stream.
	 * 
	 * @param xmlStream
	 *            the input stream.
	 * 
	 * @throws IOException
	 * @throws Exception
	 */
	@Override
	public void tokenize(Reader xmlStream) throws IOException, Exception {

		/* Record the stream for use throughout the class */
		this.xmlStream = xmlStream;

		/* Initialize the state of the tokenizer */
		int state = HEADER_START;
		int postCommentState = HEADER_START;

		/* To hold the start character of a literal string */
		int stringDelimiter = 0;

		/* To hold the individual characters of the XML as they are tokenized */
		int c = 0;
		int lastC = -1;

		/* To hold an XML processing instruction as it is tokenized */
		StringBuffer pi = new StringBuffer();

		/* To hold an XML comment as it is tokenized */
		StringBuffer comment = new StringBuffer();

		/* To hold an XML element name as it is tokenized */
		StringBuffer elementName = new StringBuffer();

		/*
		 * To hold the list of attributes for an XML element as they are tokenized
		 */
		XMLAttributes elementAttributes = null;

		/* To hold an XML element attribute name as it is tokenized */
		StringBuffer attributeName = new StringBuffer();

		/* To hold an XML element attribute value as it is tokenized */
		StringBuffer attributeValue = new StringBuffer();

		/* To hold the body characters of an XML element as they are tokenized */
		StringBuffer body = new StringBuffer();

		/* While there are more characters in the input stream... */
		while (lastC != -1 || (c = xmlStream.read()) != -1) {

			/* If there is a saved character... */
			if (lastC != -1) {
				/* Use it now */
				c = lastC;
				lastC = -1;
			}

			switch (state) {

			/* XML header */

			case HEADER_START:

				if (c == '<') {
					/* It could be the start of a processing instruction */
					state = HEADER_TAG_START;
				} else if (notWhiteSpace(c)) {
					unexpectedCharacter(c);
				}
				break;

			case HEADER_TAG_START:

				if (c == '?') {
					/* It is the start of a processing instruction */
					pi.setLength(0);
					state = PI_BODY;
				} else if (c == '!') {
					/* It's the start of a comment */
					state = COMMENT_START1;
				} else if (notWhiteSpace(c)) {
					/*
					 * It's the start of an XML element (which also indicates that we've reached the end of the header
					 * section)
					 */
					lastC = c;
					state = ELEMENT_START_TAG_NAME_START;
					postCommentState = ELEMENT_BODY_START;
				}
				break;

			case BODY_TAG_START:

				if (c == '!') {
					/* It's the start of a comment */
					state = COMMENT_START1;
				} else if (notWhiteSpace(c)) {
					/* It's the start of an XML element */
					lastC = c;
					state = ELEMENT_START_TAG_NAME_START;
				}
				break;

			/*
			 * Processing instructions
			 */

			case PI_BODY:

				if (c == '?') {
					state = PI_END;
				} else {
					pi.append((char) c);
				}
				break;

			case PI_END:

				if (c == '>') {
					handler.xmlProcessingInstruction(pi.toString().trim());
					state = HEADER_START;
				} else {
					pi.append('?');
					pi.append((char) c);
					state = PI_BODY;
				}
				break;

			/*
			 * Comments (looking for a sequence of the form "<!-- ... -->)
			 */

			case COMMENT_START1:

				comment.setLength(0);

				if (c == '-') {
					state = COMMENT_START2;
				} else {
					unexpectedCharacter(c);
				}
				break;

			case COMMENT_START2:

				if (c == '-') {
					state = COMMENT_PART;
				} else {
					unexpectedCharacter(c);
				}
				break;

			case COMMENT_PART:

				if (c == '-') {
					state = COMMENT_END1;
				} else {
					comment.append((char) c);
				}
				break;

			case COMMENT_END1:

				if (c == '-') {
					state = COMMENT_END2;
				} else {
					comment.append('-');
					comment.append((char) c);
					state = COMMENT_PART;
				}
				break;

			case COMMENT_END2:

				if (c == '>') {
					handler.xmlComment(comment.toString().toCharArray());
					state = postCommentState;
				} else {
					comment.append('-');
					comment.append((char) c);
					state = COMMENT_PART;
				}
				break;

			/*
			 * XML element
			 */

			/* The element name */

			case ELEMENT_START_TAG_NAME_START:

				elementName.setLength(0);
				elementAttributes = new XMLAttributes();

				if (isValidNameChar((char) c, true)) {
					elementName.append((char) c);
					state = ELEMENT_START_TAG_NAME_PART;
				} else if (c == '/') {
					state = ELEMENT_END_TAG_NAME_START;
				} else if (notWhiteSpace(c)) {
					unexpectedCharacter(c);
				}
				break;

			case ELEMENT_START_TAG_NAME_PART:

				if (isValidNameChar((char) c, false) || c == ':') {
					elementName.append((char) c);
				} else if (c == '>') {
					handler.xmlElementStart(elementName.toString(), elementAttributes);
					state = ELEMENT_BODY_PART;
				} else if (c == '/') {
					state = SINGLE_TAG_ELEMENT_END;
				} else if (notWhiteSpace(c)) {
					unexpectedCharacter(c);
				} else {
					state = ATTRIBUTE_NAME_START;
				}
				break;

			/* Element attributes */

			case ATTRIBUTE_NAME_START:

				attributeName.setLength(0);

				if (isValidNameChar((char) c, true)) {
					attributeName.append((char) c);
					state = ATTRIBUTE_NAME_PART;
				} else if (c == '>') {
					handler.xmlElementStart(elementName.toString(), elementAttributes);
					state = ELEMENT_BODY_START;
				} else if (c == '/') {
					state = SINGLE_TAG_ELEMENT_END;
				} else if (notWhiteSpace(c)) {
					unexpectedCharacter(c);
				}
				break;

			case ATTRIBUTE_NAME_PART:

				if (isValidNameChar((char) c, false) || (char) c == ':') {
					attributeName.append((char) c);
				} else if (c == '=') {
					state = ATTRIBUTE_VALUE_START;
				} else if (notWhiteSpace(c)) {
					unexpectedCharacter(c);
				}
				break;

			case ATTRIBUTE_VALUE_START:

				attributeValue.setLength(0);

				if (c == '\'' || c == '"') {
					stringDelimiter = c;
					state = ATTRIBUTE_VALUE_PART;
				} else if (notWhiteSpace(c)) {
					unexpectedCharacter(c);
				}
				break;

			case ATTRIBUTE_VALUE_PART:

				if (c == stringDelimiter) {
					elementAttributes.addAttribute(attributeName.toString(), attributeValue.toString());
					state = ATTRIBUTE_NAME_START;
				} else {
					attributeValue.append((char) c);
				}
				break;

			/* The element body */

			case ELEMENT_BODY_START:

				lastC = c;
				state = ELEMENT_BODY_PART;
				break;

			case ELEMENT_BODY_PART:

				if (c == '<') {
					char[] bodyChars = body.toString().toCharArray();
					handler.xmlElementData(bodyChars);
					body.setLength(0);
					state = BODY_TAG_START;
				} else {
					body.append((char) c);
				}
				break;

			/* The end of a single-tag element */

			case SINGLE_TAG_ELEMENT_END:

				if (c == '>') {
					handler.xmlElementStart(elementName.toString(), elementAttributes);
					handler.xmlElementEnd(elementName.toString());
					state = ELEMENT_BODY_START;
				} else if (notWhiteSpace(c)) {
					unexpectedCharacter(c);
				}
				break;

			case ELEMENT_END_TAG_NAME_START:

				elementName.setLength(0);

				if (isValidNameChar((char) c, true)) {
					elementName.append((char) c);
					state = ELEMENT_END_TAG_NAME_PART;
				} else if (notWhiteSpace(c)) {
					unexpectedCharacter(c);
				}
				break;

			case ELEMENT_END_TAG_NAME_PART:

				if (isValidNameChar((char) c, false) || c == ':') {
					elementName.append((char) c);
				} else if (c == '>') {
					handler.xmlElementEnd(elementName.toString());
					state = ELEMENT_BODY_PART;
				} else if (notWhiteSpace(c)) {
					unexpectedCharacter(c);
				}
				break;

			}
		}
	}

	/**
	 * An unexpected character was encountered in the XML input stream.
	 * 
	 * @throws Exception
	 */
	private void unexpectedCharacter(int c) throws Exception {

		throw new Exception("Unexpected character in XML input stream: " + (char) c);
	}

	/**
	 * Checks if the specified character is valid in an element or attribute name.
	 * 
	 * @param c
	 *            the character to test.
	 * 
	 * @param isStart
	 *            flag indicating if this is the start of a name (<code>true</code>), or a part of a name (
	 *            <code>false</code>).
	 * 
	 * @return <code>true</code> if the character is valid, <code>false</code> otherwise.
	 */
	private boolean isValidNameChar(int c, boolean isStart) {

		boolean isValidNameChar = false;

		if (isStart && ((Character.isJavaIdentifierStart(c) && c != '$') || c == ':')) {

			isValidNameChar = true;

		} else {

			switch (c) {

			case ':':
			case '.':
			case '-':

				isValidNameChar = true;
				break;

			case '$':

				break;

			default:

				if (Character.isJavaIdentifierPart(c)) {
					isValidNameChar = true;
				}
			}
		}

		return isValidNameChar;
	}

	/**
	 * Checks if the specified character is <em>not</em> a whitespace character.
	 * 
	 * @param c
	 *            the character to test.
	 * 
	 * @return <code>true</code> if the character is not whitespace, <code>false</code> otherwise.
	 */
	private boolean notWhiteSpace(int c) {

		return !Character.isWhitespace(c);
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