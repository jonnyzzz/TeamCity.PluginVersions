/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.tools;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jetbrains.annotations.NotNull;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Created 04.12.12 20:01
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class XmlUtil {
  /**
   * Writes text to file. Throws IO exception if error occurred.
   * @since 7.1
   * @param file file to write text to
   * @param text text to write in file
   * @param encoding encoding
   * @throws java.io.IOException if error occurred
   */
  public static void writeFile(@NotNull final File file, @NotNull final String text, @NotNull final String encoding) throws IOException {
    OutputStreamWriter writer = null;
    try {
      writer = new OutputStreamWriter(new FileOutputStream(file), encoding);
      writer.write(text);
    } finally {
      IOUtil.close(writer);
    }
  }

  public static void readXmlFile(@NotNull final File file, @NotNull final Processor p) {
    if (file.exists()) {
      try {
        getProcessedDocument(file, p);
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  public interface Processor {
    void process(Element rootElement);
  }


  @NotNull
  private static Document getProcessedDocument(@NotNull final File file, @NotNull final Processor p) throws JDOMException, IOException {
    final Document document = parseDocument(file).getDocument();
    p.process(document.getRootElement());
    return document;
  }

  @NotNull
  private static SAXBuilder getBuilder(final boolean validate) {
    SAXBuilder builder = new SAXBuilder(validate);
    builder.setFeature("http://xml.org/sax/features/namespaces", true);
    builder.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
    builder.setEntityResolver(new DefaultHandler() {
      @Override
      public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException {
        String dtdFileName = new File(systemId).getName();
        InputStream dtdStream = getClass().getClassLoader().getResourceAsStream(dtdFileName);
        if (dtdStream != null) {
          return new InputSource(dtdStream);
        }

        return super.resolveEntity(publicId, systemId);
      }
    });

    return builder;
  }

  public static Element parseDocument(@NotNull final File file) throws JDOMException, IOException {
    return parseDocument(file, false);
  }

  public static Element parseDocument(@NotNull final File file, boolean validate) throws JDOMException, IOException {
    return getBuilder(validate).build(file).getRootElement();
  }

  public static Element parseDocument(@NotNull final InputStream input, boolean validate) throws JDOMException, IOException {
    return getBuilder(validate).build(input).getRootElement();
  }

  public static void processXmlFile(@NotNull final File file, @NotNull final Processor p) {
    if (file.exists()) {
      try {
        saveDocument(getProcessedDocument(file, p), file);
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  public static void saveDocument(@NotNull final Document document, @NotNull final File file) throws IOException {
    FileOutputStream fos = new FileOutputStream(file);
    try {
      XmlUtil.saveDocument(document, fos);
    } finally {
      fos.close();
    }
  }

  public static void saveDocument(@NotNull Document document, @NotNull OutputStream os) throws IOException {
    OutputStreamWriter writer = new OutputStreamWriter(os, Charset.forName("UTF-8"));

    try {
      final Format format = Format.getPrettyFormat();
      format.setLineSeparator(System.getProperty("line.separator"));
      format.setEncoding("UTF-8");
      new XMLOutputter(format).output(document, writer);
    } finally {
      writer.flush();
      writer.close();
    }
  }

}
