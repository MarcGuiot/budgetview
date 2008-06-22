package org.globsframework.gui.splits.xml;

import org.saxstack.parser.DefaultXmlNode;
import org.saxstack.utils.XmlUtils;
import org.xml.sax.Attributes;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.splitters.DefaultSplitterFactory;
import org.globsframework.utils.Files;
import org.globsframework.utils.exceptions.ItemNotFound;

import java.io.InputStreamReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;

public class StyleImportNode extends DefaultXmlNode {
  public StyleImportNode(Attributes attributes, SplitsContext context) {
    String fileName = XmlUtils.getAttrValue("file", attributes);

    SplitsParser parser = new SplitsParser(context, new DefaultSplitterFactory());

    Class referenceClass = context.getReferenceClass();
    if (referenceClass != null) {
      parser.parse(new InputStreamReader(Files.getStream(referenceClass, fileName)));
    }
    else {
      File file = new File(fileName);
      try {
        parser.parse(new FileReader(file));
      }
      catch (FileNotFoundException e) {
        throw new ItemNotFound("Could not find file: " + fileName);
      }
    }
  }
}
