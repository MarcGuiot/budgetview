package org.globsframework.gui.splits.xml;

import org.saxstack.parser.DefaultXmlNode;
import org.saxstack.utils.XmlUtils;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.styles.Selector;
import org.globsframework.gui.splits.styles.StyleContext;
import org.globsframework.utils.Strings;
import org.globsframework.utils.exceptions.InvalidFormat;
import org.xml.sax.Attributes;

public class StyleXmlNode extends DefaultXmlNode {

  public StyleXmlNode(SplitsContext context, Attributes xmlAttrs) {
    String id = XmlUtils.getAttrValue("id", xmlAttrs, null);
    String selector = XmlUtils.getAttrValue("selector", xmlAttrs, null);
    if (Strings.isNullOrEmpty(selector)) {
      throw new InvalidFormat("A style selector cannot be empty");
    }

    SplitProperties properties = SplitsParser.createProperties(xmlAttrs, "selector", "id");

    StyleContext styleContext = context.getStyles();
    styleContext.createStyle(id, Selector.parseSequence(selector), properties);
  }
}
