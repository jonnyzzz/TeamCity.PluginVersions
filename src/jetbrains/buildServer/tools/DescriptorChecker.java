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

  public void validatePluginXml(@NotNull String pluginName, @NotNull InputStream is) {
    try {
      final Element doc = XmlUtil.parseDocument(is, false);
      checkRootElement(doc, pluginName);
      checkPluginVersion(doc, pluginName);
      checkVendorName(doc, pluginName);
      checkVendorUrl(doc, pluginName);
    } catch (IOException e) {
      throw new ValidationException(pluginName, "failed to read teamcity-plugin.xml", e);
    } catch (JDOMException e) {
      throw new ValidationException(pluginName, "failed to read teamcity-plugin.xml", e);
    }
  }

  private void checkRootElement(Element doc, String pluginName) {
    if (!"teamcity-plugin".equals(doc.getName()))
      throw new ValidationException(pluginName, "invalid root element");
  }

  private void checkPluginVersion(Element doc, String pluginName) {
    Text version = (Text) XPathFactory.instance().compile("/teamcity-plugin/info/version/text()").evaluateFirst(doc);
    if (version == null) {
      throw new ValidationException(pluginName, "version was not specified");
    }

    String actualVersion = version.getTextTrim();
    System.out.println(pluginName + " -> " + actualVersion);

    if (!myVersion.equals(actualVersion)) {
      throw new ValidationException(pluginName, "incorrect plugin version: " + actualVersion);
    }
  }

  private void checkVendorName(Element doc, String pluginName) {
    Text jetbrainsName = (Text) XPathFactory.instance().compile("/teamcity-plugin/info/vendor/name/text()").evaluateFirst(doc);
    if (jetbrainsName == null) {
      throw new ValidationException(pluginName, "no plugin vendor");
    } else {
      String name = jetbrainsName.getTextTrim();
      if (!name.contains("JetBrains")) {
        throw new ValidationException(pluginName, "incorrect plugin vendor: " + jetbrainsName.getTextTrim());
      }
    }
  }

  private void checkVendorUrl(Element doc, String pluginName) {
    Text jetbrainsUrl = (Text) XPathFactory.instance().compile("/teamcity-plugin/info/vendor/url/text()").evaluateFirst(doc);
    boolean isApplicableUrl = false;
    if (jetbrainsUrl == null) {
      throw new ValidationException(pluginName, "no plugin vendor url");
    }

    if (jetbrainsUrl.getTextTrim().contains("http://www.jetbrains.com")) {
      System.out.println(System.out.format("##teamcity[message text='%s: current plugin vendor url is %s, " +
                      "consider using https instead' status='WARNING']", pluginName, jetbrainsUrl.getTextTrim()));
      isApplicableUrl = true;
    } else if (jetbrainsUrl.getTextTrim().contains("https://www.jetbrains.com")) {
      isApplicableUrl = true;
    }

    if (!isApplicableUrl) {
      System.out.println("##teamcity[message text='compiler error' status='ERROR']");
      throw new ValidationException(pluginName, "incorrect plugin vendor url: " + jetbrainsUrl.getTextTrim());
    }
  }

}
