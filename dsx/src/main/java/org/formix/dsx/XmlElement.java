/****************************************************************************
 * Copyright 2009-2014 Jean-Philippe Gravel, P. Eng. CSDP
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/
package org.formix.dsx;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.StringEscapeUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

public class XmlElement implements XmlContent {

	public static XmlElement create(String name) {
		return new XmlElement(name);
	}

	private static SAXParser createSaxParser()
			throws ParserConfigurationException, SAXNotRecognizedException,
			SAXNotSupportedException, SAXException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(false);

		factory.setValidating(false);
		factory.setFeature("http://xml.org/sax/features/namespaces", false);
		factory.setFeature("http://xml.org/sax/features/validation", false);
		factory.setFeature(
				"http://apache.org/xml/features/nonvalidating/load-dtd-grammar",
				false);
		factory.setFeature(
				"http://apache.org/xml/features/nonvalidating/load-external-dtd",
				false);

		SAXParser parser = factory.newSAXParser();
		return parser;
	}

	public static XmlElement readXML(File file) throws XmlException {
		try {
			Reader reader = new FileReader(file);
			XmlElement element = readXML(reader);
			reader.close();
			return element;
		} catch (FileNotFoundException e) {
			throw new XmlException("Invalid file " + file, e);
		} catch (IOException e) {
			throw new XmlException("Unable to read the file " + file, e);
		}
	}

	public static XmlElement readXML(Reader reader) throws XmlException {
		return readXML(reader, new XmlContentAdapter());
	}

	public static XmlElement readXML(Reader reader, XmlContentListener listener)
			throws XmlException {
		XmlHandler handler = new XmlHandler(listener);
		try {
			SAXParser parser = createSaxParser();
			parser.setProperty("http://xml.org/sax/properties/lexical-handler",
					handler);
			parser.parse(new InputSource(reader), handler);
			return handler.getRootElement();
		} catch (SAXException e) {
			String message = "A parser problem occured";
			if (handler.peekTopElement() != null) {
				message += ", node = " + handler.peekTopElement().toString();
			}
			throw new XmlException(message, e);
		} catch (IOException e) {
			throw new XmlException("A reader problem occured.", e);
		} catch (ParserConfigurationException e) {
			throw new XmlException("A parser configuration has been detected.",
					e);
		}
	}

	public static XmlElement readXML(String xml) throws XmlException {
		try {
			Reader reader = new StringReader(xml);
			XmlElement element = readXML(reader);
			reader.close();
			return element;
		} catch (IOException e) {
			throw new XmlException(
					"A problem occured when closing the reader.", e);
		}
	}

	private Map<String, String> attributes;
	private List<XmlContent> childs;
	private long id;
	private String name;

	public XmlElement(String name) {
		this.setName(name);
		this.attributes = new LinkedHashMap<String, String>();
		this.childs = new ArrayList<XmlContent>();
		this.id = -1;
	}

	public void addAttribute(String name, String value) {
		this.attributes.put(name, value);
	}

	public XmlCDATA addCDATA(String text) {
		XmlCDATA cdata = new XmlCDATA(text);
		this.addChild(cdata);
		return cdata;
	}

	public XmlElement addChild(XmlContent child) {
		this.childs.add(child);
		return this;
	}

	public XmlComment addComment(String text) {
		XmlComment comment = new XmlComment(text);
		this.addChild(comment);
		return comment;
	}

	public XmlElement addElement(String name) {
		XmlElement element = new XmlElement(name);
		this.addChild(element);
		return element;
	}

	public XmlText addText(String text) {
		XmlText txt = new XmlText(text);
		this.addChild(txt);
		return txt;
	}

	/**
	 * Compare the actual XmlElement along whit it's childs.
	 * 
	 * @param o
	 *            The other element to do the comparison with.
	 * 
	 * @return True if bots XmlElements are equals, contains the same number of
	 *         childs and all those childs are equal to each other, recursively.
	 */
	public boolean deepEquals(XmlElement o) {
		if (!this.equals(o))
			return false;

		if (this.childs.size() != o.childs.size())
			return false;

		for (XmlContent c1 : this.childs) {
			boolean atLeastOneEqual = false;

			for (int i = 0; i < o.childs.size() && !atLeastOneEqual; i++) {
				XmlContent c2 = o.childs.get(i);
				if (c1 instanceof XmlElement && c2 instanceof XmlElement) {
					XmlElement e1 = (XmlElement) c1;
					XmlElement e2 = (XmlElement) c2;
					atLeastOneEqual = e1.deepEquals(e2);
				} else if (c1.equals(c2)) {
					atLeastOneEqual = true;
				}
			}

			if (!atLeastOneEqual)
				return false;
		}

		return true;
	}

	/**
	 * Two XmlElements are equals if they have the same name, the same number of
	 * attributes and all those attributes contains the same values.
	 * Sub-elements are not compared.
	 * 
	 * @param o
	 *            The object to do the comparison with.
	 * 
	 * @return true if both XmlElement are equal, false otherwise.
	 */
	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (!(o instanceof XmlElement))
			return false;

		XmlElement other = (XmlElement) o;

		if (!this.name.equals(other.name))
			return false;

		if (this.attributes.size() != other.attributes.size())
			return false;

		for (String key : this.attributes.keySet()) {
			if (!other.attributes.containsKey(key))
				return false;
			if (!this.attributes.get(key).equals(other.attributes.get(key)))
				return false;
		}

		return true;
	}

	public boolean hasAttribute(String name) {
		return this.attributes.containsKey(name);
	}

	public String getAttribute(String name) {
		return this.attributes.get(name);
	}

	public Map<String, String> getAttributes() {
		return this.attributes;
	}

	public XmlContent getChild(int index) {
		return this.childs.get(index);
	}

	public List<XmlContent> getChilds() {
		return this.childs;
	}

	public String getValue(String elementName) {
		StringBuilder sb = new StringBuilder();
		List<XmlContent> grandChilds = this.getElement(elementName).getChilds();
		for (XmlContent xmlContent : grandChilds) {
			sb.append(xmlContent.toString());
		}
		return sb.toString();
	}

	public XmlElement getElement(String name) {
		return this.getElement(name, 0);
	}

	public XmlElement getElement(String name, int index) {
		int counter = 0;
		for (XmlContent child : this.childs) {
			if (child instanceof XmlElement) {
				XmlElement element = (XmlElement) child;
				if (element.getName().equals(name)) {
					if (counter == index)
						return element;
					counter++;
				}
			}
		}
		return null;
	}

	public List<XmlElement> getElements(String name) {
		ArrayList<XmlElement> elements = new ArrayList<XmlElement>();
		for (XmlContent child : this.childs) {
			if (child instanceof XmlElement) {
				XmlElement element = (XmlElement) child;
				if (element.getName().equals(name))
					elements.add(element);
			}
		}
		return elements;
	}

	@Override
	public long getId() {
		if (this.id == -1) {
			this.id = IdGen.nextId();
		}
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public XmlElement setAttribute(String name, String value) {
		this.attributes.put(name, value);
		return this;
	}

	public void setName(String name) {
		if (name.matches(".*\\s.*"))
			throw new IllegalArgumentException(
					"The element's name can't contain white spaces.");
		this.name = name;
	}

	@Override
	public String toString() {
		return this.toXml();
	}

	@Override
	public String toXml() {
		StringWriter sw = new StringWriter();
		try {
			this.write(sw);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return sw.toString();
	}

	@Override
	public void write(Writer writer) throws IOException {
		writer.append("<");
		writer.append(this.name);

		for (String name : this.attributes.keySet()) {
			writer.write(' ');
			this.writeAttribute(writer, name);
		}

		if (this.childs.size() == 0)
			writer.append("/");

		writer.append(">");

		for (XmlContent content : this.childs)
			content.write(writer);

		if (this.childs.size() > 0) {
			writer.append("</");
			writer.append(this.name);
			writer.append(">");
		}
	}

	private void writeAttribute(Writer writer, String name) throws IOException {
		writer.append(name);
		String value = this.attributes.get(name);
		if (value != null) {
			writer.append("=\"");
			writer.append(StringEscapeUtils.escapeXml(value));
			writer.append("\"");
		}
	}

}
