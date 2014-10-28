package net.thepinguin.jp.xml.base;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import net.thepinguin.jp.xml.pom.Visitable;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class Document extends DefaultHandler {
	
	Element _document = new Element("document");
	Stack<Element> _stack = new Stack<Element>();
	
	public void startDocument() throws SAXException {
		_stack.push(_document);
	}

	public void endDocument() throws SAXException {
		_stack.pop();
	}

	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		Element e = new Element(qName, new Attribute(attributes), uri, localName);
		Element top = _stack.peek();
		top.addElement(top, e);
		_stack.push(e);
	}

	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		_stack.pop();
	}

	public void characters(char ch[], int start, int length)
			throws SAXException {
		String s = String.copyValueOf(ch, start, length).trim();
		Element top = _stack.peek();
		top.setValue(s);
	}

	public void ignorableWhitespace(char ch[], int start, int length)
			throws SAXException {
//		System.out.println(ch);
	}

	public String toString(){
		return _document.toString(0);
	}

	public <T> List<Visitable<T>> findElement(Visitable<T> visitor) {
		return _document.findElement(visitor);
	}

}