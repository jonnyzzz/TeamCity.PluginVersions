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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.zip.ZipFile;

/**
 * Created 04.12.12 20:02
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class IOUtil {
  /**
   * Closes a resource if it is not null
   * All possible IOExceptions are ignored
   * @param e resource to close
   */
  public static void close(@Nullable Closeable e) {
    if (e != null) {
      try {
        e.close();
      } catch (IOException e1) {
        // ignore
      }
    }
  }

  /**
   * Closes a resource if it is not null
   * All possible IOExceptions are ignored
   * @param e resource to close
   * @since 8.0
   */
  public static void close(@Nullable ZipFile e) {
    if (e != null) {
      try {
        e.close();
      } catch (IOException e1) {
        // ignore
      }
    }
  }

  /**
   * Closes all not-null resources
   * All possible IOExceptions are ignored
   * @param toClose resources to close
   * @since 7.0
   */
  public static void closeAll(@NotNull Closeable... toClose) {
    for (Closeable e : toClose) {
      if (e != null) {
        try {
          e.close();
        } catch (IOException e1) {
          // ignore
        }
      }
    }
  }


  public static void copyStreams(@NotNull final InputStream input,
                                 @NotNull final OutputStream output) throws IOException {
    final byte[] buffer = new byte[65536];
    int read = 0;
    while (read != -1) {
      read = input.read(buffer, 0, buffer.length);
      if (read != -1) output.write(buffer, 0, read);
    }
  }
}
