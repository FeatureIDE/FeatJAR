/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-base.
 *
 * base is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * base is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with base. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-base> for further information.
 */
package de.featjar.base.io.xml;

import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import de.featjar.base.io.format.ParseException;
import de.featjar.base.io.format.ParseProblem;
import de.featjar.base.io.input.AInputMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Abstract parser for XML files.
 *
 * @param <T> the type of parsed data
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public abstract class AXMLParser<T> {

    protected List<Problem> parseProblems = new ArrayList<>();

    protected static List<Element> getElements(NodeList nodeList) {
        final ArrayList<Element> elements = new ArrayList<>(nodeList.getLength());
        for (int i = 0; i < nodeList.getLength(); i++) {
            final org.w3c.dom.Node nNode = nodeList.item(i);
            if (nNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                elements.add((Element) nNode);
            }
        }
        return elements;
    }

    protected static List<Element> getElements(final Element element, final String nodeName) {
        return getElements(element.getElementsByTagName(nodeName));
    }

    protected Element getElement(final Element element, final String nodeName) throws ParseException {
        final List<Element> elements = getElements(element, nodeName);
        if (elements.size() > 1) {
            addParseProblem("Multiple nodes of " + nodeName + " defined.", element, Problem.Severity.WARNING);
        } else if (elements.isEmpty()) {
            addParseProblem("Node " + nodeName + " not defined!", element, Problem.Severity.ERROR);
        }
        return elements.get(0);
    }

    protected Result<Element> getElementResult(final Element element, final String nodeName) {
        final List<Element> elements = getElements(element, nodeName);
        if (elements.size() > 1) {
            try {
                addParseProblem("Multiple nodes of " + nodeName + " defined.", element, Problem.Severity.WARNING);
            } catch (ParseException ignored) {
            }
        } else if (elements.isEmpty()) {
            return Result.empty();
        }
        return Result.of(elements.get(0));
    }

    protected Element getDocumentElement(final Document document, final String... nodeNames) throws ParseException {
        final Element element = document.getDocumentElement();
        if (element == null || Arrays.stream(nodeNames).noneMatch(element.getNodeName()::equals)) {
            addParseProblem("Node " + Arrays.toString(nodeNames) + " not defined!", element, Problem.Severity.ERROR);
        }
        return element;
    }

    protected void addParseProblem(String message, org.w3c.dom.Node node, Problem.Severity severity)
            throws ParseException {
        int lineNumber = node != null
                ? Integer.parseInt(node.getUserData(PositionalXMLHandler.LINE_NUMBER_KEY_NAME)
                        .toString())
                : 0;
        if (severity.equals(Problem.Severity.ERROR)) {
            throw new ParseException(message, lineNumber);
        } else {
            parseProblems.add(new ParseProblem(message, severity, lineNumber));
        }
    }

    public Result<T> parse(AInputMapper inputMapper) {
        try {
            parseProblems.clear();
            final Document document =
                    DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            SAXParserFactory.newInstance()
                    .newSAXParser()
                    .parse(new InputSource(inputMapper.get().getReader()), new PositionalXMLHandler(document));
            document.getDocumentElement().normalize();
            return Result.of(parseDocument(document), parseProblems);
        } catch (final ParseException e) {
            return Result.empty(new ParseProblem(e.getMessage(), Problem.Severity.ERROR, e.getLineNumber()));
        } catch (final Exception e) {
            return Result.empty(new Problem(e));
        }
    }

    protected abstract T parseDocument(Document document) throws ParseException;
}
