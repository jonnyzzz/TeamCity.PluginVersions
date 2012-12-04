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

import org.jdom2.Element;
import org.jdom2.Text;
import org.jdom2.xpath.XPathFactory;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created 04.12.12 19:51
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class PluginsChecker {


  public static void main(String[] args) {
    try {
      main2(args);
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }

  private static void main2(String[] args) {
    System.out.println("TeamCity plugins checker");

    if (args.length != 2) {
      usage();
      System.exit(1);
    }

    final File home = new File(args[0]);
    final String version = args[1];

    System.out.println("Checking plugin versions in " + home + " for version: " + version);

    final File pluginsPath = new File(home, "webapps/ROOT/WEB-INF/plugins");
    if (!pluginsPath.isDirectory()) {
      System.err.println("Failed to find plugins path: " + pluginsPath);
      System.exit(2);
    }

    processUnpackedPlugins(pluginsPath);
    processZipPlugins(pluginsPath);

    System.exit(0);
  }

  private static void processUnpackedPlugins(@NotNull final File zipHome) {
    final File[] files = zipHome.listFiles(PLUGIN_FILTER);
    if (files == null) {
      throw new RuntimeException("Failed to list files under " + zipHome);
    }

    for (File file : files) {
      try {
        processUnpackedPlugin(file);
      } catch (ValidationException e) {
        System.out.println(e.getMessage());
      }
    }
  }

  private static void processUnpackedPlugin(File file) {
    final File config = new File(file, "teamcity-plugin.xml");
    if (!config.isFile()) {
      throw new ValidationException(config.getName(), "teamcity-plugin.xml is not contained in .zip");
    }

    final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    FileInputStream bus = null;
    try {
      bus = new FileInputStream(config);
      IOUtil.copyStreams(bus, bos);
      IOUtil.close(bos);
    } catch (Exception e) {
      throw new ValidationException(config.getName(), "Failed to read teamcity-plugin.xml");
    } finally {
      IOUtil.close(bus);
      IOUtil.close(bos);
    }

    validatePluginXml(file.getName(), new ByteArrayInputStream(bos.toByteArray()));
  }

  private static void processZipPlugins(@NotNull final File zipHome) {
    final File[] files = zipHome.listFiles(ZIP_FILTER);
    if (files == null) {
      throw new RuntimeException("Failed to list files under " + zipHome);
    }

    for (File file : files) {
      try {
        processZipPlugin(file);
      } catch (ValidationException e) {
        System.out.println(e.getMessage());
      }
    }
  }


  private static void processZipPlugin(@NotNull final File zip) {
    final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try {
      final ZipFile file = new ZipFile(zip);
      final ZipEntry ze = file.getEntry("teamcity-plugin.xml");
      if (ze == null) {
        throw new ValidationException(zip.getName(), "teamcity-plugin.xml is not contained in .zip");
      }

      IOUtil.copyStreams(file.getInputStream(ze), bos);
      IOUtil.close(file);
      IOUtil.close(bos);

    } catch (Exception e) {
      throw new ValidationException(zip.getName(), "Failed to unpack teamcity-plugin.xml from .zip");
    }

    validatePluginXml(zip.getName(), new ByteArrayInputStream(bos.toByteArray()));
  }

  private static void validatePluginXml(@NotNull String plugin, @NotNull InputStream is) {
    try {
      final Element doc = XmlUtil.parseDocument(is, false);

      if (!"teamcity-plugin".equals(doc.getName()))
        throw new ValidationException(plugin, "invalid root element");

      Text version = (Text)XPathFactory.instance().compile("/teamcity-plugin/info/version/text()").evaluateFirst(doc);
      if (version == null) {
        throw new ValidationException(plugin, "version was not specified");
      }

      String actualVersion = version.getTextTrim();
      System.out.println(plugin + " -> " + actualVersion);
    } catch (Exception e) {
      throw new ValidationException(plugin, "failed to read teamcity-plugin.xml", e);
    }
  }

  private static void usage() {
    System.out.println();
    System.out.println("java -jar TeamCity.PluginVersions <teamcity directory> <version>");
  }


  public static final FileFilter ZIP_FILTER = new FileFilter() {
    @Override
    public boolean accept(File pathname) {
      return pathname.getName().endsWith(".zip") && pathname.isFile();
    }
  };

  public static final FileFilter PLUGIN_FILTER = new FileFilter() {
    @Override
    public boolean accept(File pathname) {
      return pathname.isDirectory();
    }
  };
}
