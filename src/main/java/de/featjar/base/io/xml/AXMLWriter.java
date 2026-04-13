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

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Result;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;

/**
 * Abstract writer for XML files.
 *
 * @param <T> the type of written data
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public abstract class AXMLWriter<T> {

    protected static final Pattern completeTagPattern = Pattern.compile("<(\\w+)[^/]*>.*</\\1.*>");
    protected static final Pattern incompleteTagPattern = Pattern.compile("(<\\w+[^/>]*>)|(</\\w+[^>]*>)");

    protected abstract void writeDocument(T object, Document doc);

    public Result<String> serialize(T object) {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setIgnoringComments(true);
        dbf.setIgnoringElementContentWhitespace(false);
        dbf.setCoalescing(true);
        dbf.setExpandEntityReferences(true);
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
        } catch (final ParserConfigurationException pce) {
            return Result.empty(pce);
        }
        final Document doc = db.newDocument();
        writeDocument(object, doc);

        try (StringWriter stringWriter = new StringWriter()) {
            final StreamResult streamResult = new StreamResult(stringWriter);
            final TransformerFactory factory = TransformerFactory.newInstance();
            factory.setAttribute("indent-number", 4);
            final Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(doc), streamResult);
            return Result.of(prettyPrint(streamResult.getWriter().toString()));
        } catch (final IOException | TransformerException e) {
            return Result.empty(e);
        }
    }

    protected String prettyPrint(String text) {
        final StringBuilder result = new StringBuilder();
        int indentLevel = 0;
        try (final BufferedReader reader = new BufferedReader(new StringReader(text))) {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                final String trimmedLine = line.trim();
                if (!trimmedLine.isEmpty()) {
                    if (completeTagPattern.matcher(trimmedLine).matches()) {
                        appendLine(result, indentLevel, trimmedLine);
                    } else {
                        final Matcher matcher = incompleteTagPattern.matcher(trimmedLine);
                        int start = 0;
                        while (matcher.find()) {
                            appendLine(result, indentLevel, trimmedLine.substring(start, matcher.start()));
                            final String openTag = matcher.group(1);
                            final String closeTag = matcher.group(2);
                            if (openTag != null) {
                                appendLine(result, indentLevel, openTag);
                                indentLevel++;
                            } else if (closeTag != null) {
                                indentLevel--;
                                appendLine(result, indentLevel, closeTag);
                            }
                            start = matcher.end();
                        }
                        appendLine(result, indentLevel, trimmedLine.substring(start));
                    }
                }
            }
        } catch (final IOException e) {
            FeatJAR.log().error(e);
        }
        return result.toString();
    }

    private void appendLine(final StringBuilder result, int indentLevel, String line) {
        final String trimmedLine = line.trim();
        if (!trimmedLine.isEmpty()) {
            result.append("\t".repeat(Math.max(0, indentLevel)));
            result.append(trimmedLine);
            result.append("\n");
        }
    }
}
