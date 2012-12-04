package jetbrains.buildServer.tools;

import org.jetbrains.annotations.NotNull;

import java.io.*;

/**
 * Created 04.12.12 20:40
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class UnpackedPluginChecker implements PluginChecker {
  private final DescriptorChecker myCheck;

  public UnpackedPluginChecker(DescriptorChecker myCheck) {
    this.myCheck = myCheck;
  }

  @Override
  public void check(@NotNull final File file) {
    if (!file.isDirectory()) return;

    final File config = new File(file, "teamcity-plugin.xml");
    if (!config.isFile()) {
      throw new ValidationException(file.getName(), "teamcity-plugin.xml is not contained in .zip");
    }

    final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    FileInputStream bus = null;
    try {
      bus = new FileInputStream(config);
      IOUtil.copyStreams(bus, bos);
      IOUtil.close(bos);
    } catch (IOException e) {
      throw new ValidationException(file.getName(), "Failed to read teamcity-plugin.xml");
    } finally {
      IOUtil.close(bus);
      IOUtil.close(bos);
    }

    myCheck.validatePluginXml(file.getName(), new ByteArrayInputStream(bos.toByteArray()));
  }
}

