package jetbrains.buildServer.tools;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Text;
import org.jdom2.xpath.XPathFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created 04.12.12 20:33
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class DescriptorChecker {
  private final String myVersion;

  public DescriptorChecker(@NotNull String version) throws ValidationException {
    this.myVersion = version;
  }

  public void validatePluginXml(@NotNull String plugin, @NotNull InputStream is) {
    try {
      final Element doc = XmlUtil.parseDocument(is, false);

      if (!"teamcity-plugin".equals(doc.getName()))
        throw new ValidationException(plugin, "invalid root element");

      Text version = (Text) XPathFactory.instance().compile("/teamcity-plugin/info/version/text()").evaluateFirst(doc);
      if (version == null) {
        throw new ValidationException(plugin, "version was not specified");
      }

      String actualVersion = version.getTextTrim();
      System.out.println(plugin + " -> " + actualVersion);

      if (!myVersion.equals(actualVersion)) {
        throw new ValidationException(plugin, "incorrect plugin version: " + actualVersion);
      }

    } catch (IOException e) {
      throw new ValidationException(plugin, "failed to read teamcity-plugin.xml", e);
    } catch (JDOMException e) {
      throw new ValidationException(plugin, "failed to read teamcity-plugin.xml", e);
    }
  }

}
