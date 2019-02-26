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

import java.io.File;
import java.util.*;

/**
 * Created 04.12.12 19:51
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class PluginsChecker {



  public static void main(String[] args) {
    System.out.println("##teamcity[testSuiteStarted name='PluginVersions']");
    try {
      main2(args);
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    } finally {
      System.out.println("##teamcity[testSuiteFinished name='PluginVersions']");
    }
  }

  private static void main2(String[] args) {
    System.out.println("TeamCity plugins checker");

    if (args.length != 2) {
      usage();
      System.exit(1);
    }

    final File pluginsPath = new File(args[0]);
    final String version = args[1];

    System.out.println("Checking plugin versions in " + pluginsPath + " for version: " + version);

    if (!pluginsPath.isDirectory()) {
      System.err.println("Failed to find plugins path: " + pluginsPath);
      System.exit(2);
    }

    final DescriptorChecker checker = new DescriptorChecker(version);
    final PluginChecker[] zipChecker = {new ZipPluginChecker(checker), new UnpackedPluginChecker(checker)};

    final File[] files = pluginsPath.listFiles();
    if (files == null) {
      throw new RuntimeException("Failed to list files under " + pluginsPath);
    }
    Arrays.sort(files);

    final List<ValidationException> errors = new ArrayList<ValidationException>();
    for (File file : files) {
      if (file.isHidden()) continue;

      String name = CheckerUtils.normalizeName(file.getName());
      System.out.println("Scanning: " + name);
      System.out.println("##teamcity[testStarted name='" + name + "' captureStandardOutput='true'] ");

      for (PluginChecker ch : zipChecker) {
        try {
          ch.check(file);
        } catch (ValidationException e) {
          System.out.println("##teamcity[testFailed name='" + name + "' message='" + e.getOurMessage() + "' details='" + e.getMessage() + "']");
          errors.add(e);
        }
      }
      System.out.println("##teamcity[testFinished name='" + name + "'] ");
      System.out.println();
      System.out.flush();
    }

    if (errors.isEmpty()) {
      System.exit(0);
      return;
    }

    System.out.println();
    System.out.println("Errors:");
    Collections.sort(errors, EXCEPTION_COMPARATOR);
    for (ValidationException error : errors) {
      System.out.println(error.getMessage());
    }

    System.exit(2);
  }

  private static void usage() {
    System.out.println();
    System.out.println("java -jar TeamCity.PluginVersions.jar <teamcity directory> <version>");
  }


  private static final Comparator<ValidationException> EXCEPTION_COMPARATOR = new Comparator<ValidationException>() {
    @Override
    public int compare(ValidationException o1, ValidationException o2) {
      final String p1 = o1.getPlugin();
      final String p2 = o2.getPlugin();
      return p1.compareToIgnoreCase(p2);
    }
  };

}
