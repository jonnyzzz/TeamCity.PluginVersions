package jetbrains.buildServer.tools;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Created 04.12.12 20:41
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public interface PluginChecker {
  void check(@NotNull File file);
}
