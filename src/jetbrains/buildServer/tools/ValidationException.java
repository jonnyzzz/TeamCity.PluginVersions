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

/**
* Created 04.12.12 20:16
*
* @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
*/
public class ValidationException extends RuntimeException {
  @NotNull
  private final String plugin;
  @NotNull
  private final String message;

  public ValidationException(@NotNull String plugin, @NotNull String message, @NotNull Throwable t) {
    this(plugin, message + ". " + t.getMessage());
    initCause(t);
  }

  public ValidationException(@NotNull String plugin, @NotNull String message) {
    super(plugin + ": " + message);
    this.plugin = plugin;
    this.message = message;
  }

  @NotNull
  public String getPlugin() {
    return plugin;
  }

  @NotNull
  public String getMessage() {
    return message;
  }
}
