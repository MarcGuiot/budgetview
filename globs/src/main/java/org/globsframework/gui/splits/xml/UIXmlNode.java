package org.globsframework.gui.splits.xml;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.ui.UIService;
import org.globsframework.utils.Strings;
import org.globsframework.utils.exceptions.InvalidFormat;
import org.saxstack.parser.DefaultXmlNode;
import org.saxstack.utils.XmlUtils;
import org.xml.sax.Attributes;

public class UIXmlNode extends DefaultXmlNode {

  public UIXmlNode(SplitsContext context, Attributes xmlAttrs) {
    String name = XmlUtils.getAttrValue("name", xmlAttrs);
    if (Strings.isNullOrEmpty(name)) {
      throw new InvalidFormat("An UI definition must be given a non-empty 'name' attribute");
    }

    String className = XmlUtils.getAttrValue("class", xmlAttrs);
    if (Strings.isNullOrEmpty(className)) {
      throw new InvalidFormat("An UI definition must have a 'class' attribute " +
                              "referring to the ComponentUI subclass to use");
    }

    SplitProperties properties = SplitsParser.createProperties(xmlAttrs, "name", "class");

    UIService service = context.getService(UIService.class);
    service.registerUI(name, className, properties);
  }
}