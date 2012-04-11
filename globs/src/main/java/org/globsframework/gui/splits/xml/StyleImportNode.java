package org.globsframework.gui.splits.xml;

import org.saxstack.parser.DefaultXmlNode;
import org.saxstack.utils.XmlUtils;
import org.xml.sax.Attributes;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.ui.UIService;
import org.globsframework.gui.splits.impl.DefaultSplitsContext;
import org.globsframework.gui.splits.splitters.DefaultSplitterFactory;
import org.globsframework.utils.Files;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.exceptions.ItemNotFound;

import java.io.InputStreamReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.HashMap;

public class StyleImportNode extends DefaultXmlNode {

  private static Map<String, SplitsContext> cache = new HashMap<String, SplitsContext>(); 

  public StyleImportNode(Attributes attributes, SplitsContext context) {
    String fileName = XmlUtils.getAttrValue("file", attributes);

    SplitsContext cachedContext = cache.get(fileName);
    if (cachedContext == null) {
      cachedContext = loadContext(fileName, context);
    }

    context.getStyles().addAll(cachedContext.getStyles());
    context.getService(UIService.class).addAll(cachedContext.getService(UIService.class));
  }

  private SplitsContext loadContext(String fileName, SplitsContext parentContext) {

    DefaultDirectory localDirectory = new DefaultDirectory(parentContext.getDirectory());
    localDirectory.add(new UIService(parentContext.getDirectory().get(UIService.class)));

    SplitsContext localContext = new DefaultSplitsContext(localDirectory);
    SplitsParser parser = new SplitsParser(localContext, new DefaultSplitterFactory());

    Class referenceClass = parentContext.getReferenceClass();
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

//    cache.put(fileName, localContext);

    return localContext;
  }
}
