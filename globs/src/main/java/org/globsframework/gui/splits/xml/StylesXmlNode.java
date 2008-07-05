package org.globsframework.gui.splits.xml;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.styles.Selector;
import org.globsframework.gui.splits.styles.StyleService;
import org.globsframework.utils.Strings;
import org.globsframework.utils.exceptions.InvalidFormat;
import org.saxstack.parser.DefaultXmlNode;
import org.saxstack.parser.XmlNode;
import org.saxstack.utils.XmlUtils;
import org.xml.sax.Attributes;

public class StylesXmlNode extends DefaultXmlNode {

  private SplitsContext context;

  public StylesXmlNode(SplitsContext context) {
    this.context = context;
  }

  public XmlNode getSubNode(String childName, Attributes xmlAttrs) {
    if (childName.equals("style")) {
      return new StyleXmlNode(context, xmlAttrs);
    }
    if (childName.equals("ui")) {
      return new UIXmlNode(context, xmlAttrs);
    }
    throw new SplitsException("Invalid child tag <" + childName + "> for tag <styles>");
  }
}
