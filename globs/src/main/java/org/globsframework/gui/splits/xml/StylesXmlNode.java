package org.globsframework.gui.splits.xml;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.styles.Selector;
import org.globsframework.gui.splits.styles.StyleService;
import org.globsframework.utils.Strings;
import org.globsframework.utils.exceptions.InvalidFormat;
import org.saxstack.parser.DefaultXmlNode;
import org.saxstack.parser.XmlNode;
import org.saxstack.utils.XmlUtils;
import org.xml.sax.Attributes;

public class StylesXmlNode extends DefaultXmlNode {

  private final XmlNode NO_CHILD_NODE = new DefaultXmlNode() {
    public XmlNode getSubNode(String childName, Attributes xmlAttrs) {
      throw new InvalidFormat("<style> tags cannot have children");
    }
  };
  protected StyleService styleService;

  public StylesXmlNode(SplitsContext context) {
    styleService = context.getStyleService();
  }

  public XmlNode getSubNode(String childName, Attributes xmlAttrs) {
    if (!childName.equals("style")) {
      throw new InvalidFormat("<styles> tags can only have <style> children");
    }

    String selector = XmlUtils.getAttrValue("selector", xmlAttrs);
    if (Strings.isNullOrEmpty(selector)) {
      throw new InvalidFormat("A style selector cannot be empty");
    }
    SplitProperties properties = SplitsParser.createProperties(xmlAttrs, "selector");
    styleService.createStyle(Selector.parseSequence(selector), properties);

    return NO_CHILD_NODE;
  }
}
