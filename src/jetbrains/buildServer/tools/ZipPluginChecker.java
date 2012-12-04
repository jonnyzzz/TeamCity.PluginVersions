package jetbrains.buildServer.tools;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created 04.12.12 20:41
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class ZipPluginChecker implements PluginChecker {
  private final DescriptorChecker myCheck;

  public ZipPluginChecker(@NotNull DescriptorChecker myCheck) {
    this.myCheck = myCheck;
  }

  public void check(@NotNull final File zip) {
    if (!zip.getName().endsWith(".zip") || !zip.isFile()) return;

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

    myCheck.validatePluginXml(zip.getName(), new ByteArrayInputStream(bos.toByteArray()));
  }
}
