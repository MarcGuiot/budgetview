package org.crossbowlabs.globs.xml;
import com.sun.org.apache.xerces.internal.parsers.SAXParser;
import java.io.Reader;
import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.GlobModel;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.*;
import org.crossbowlabs.globs.model.delta.DefaultChangeSet;
import org.crossbowlabs.globs.model.delta.MutableChangeSet;
import org.crossbowlabs.globs.utils.exceptions.InvalidParameter;
import org.crossbowlabs.globs.utils.exceptions.ItemNotFound;
import org.crossbowlabs.saxstack.parser.*;
import org.xml.sax.Attributes;

public class XmlChangeSetParser {

  private XmlChangeSetParser() {
  }

  public static ChangeSet parse(GlobModel model, Reader reader) {
    RootProxyNode rootNode = new RootProxyNode(model);
    SaxStackParser.parse(new SAXParser(), new XmlBootstrapNode(rootNode, "changes"), reader);
    return rootNode.getChangeSet();
  }

  private static class RootProxyNode extends DefaultXmlNode {
    private FieldConverter fieldConverter = new FieldConverter();
    private MutableChangeSet changeSet = new DefaultChangeSet();
    private GlobModel model;

    private RootProxyNode(GlobModel model) {
      this.model = model;
    }

    public XmlNode getSubNode(String childName, Attributes xmlAttrs) throws ExceptionHolder {
      String typeName = xmlAttrs.getValue("type");
      if (typeName == null) {
        throw new InvalidParameter("Missing attribute 'type' in tag '" + childName + "'");
      }
      GlobType globType = model.getType(typeName);
      FieldValuesBuilder valuesBuilder = FieldValuesBuilder.init();
      final KeyBuilder keyBuilder = KeyBuilder.init(globType);

      processAttributes(keyBuilder, valuesBuilder, xmlAttrs, globType);

      final Key key = keyBuilder.get();
      FieldValues values = valuesBuilder.get();
      if ("create".equals(childName)) {
        changeSet.processCreation(key, values);
      }
      else if ("update".equals(childName)) {
        changeSet.processUpdate(key, values);
      }
      else if ("delete".equals(childName)) {
        changeSet.processDeletion(key, values);
      }

      return super.getSubNode(typeName, xmlAttrs);
    }

    private void processAttributes(KeyBuilder keyBuilder,
                                   FieldValuesBuilder valuesBuilder,
                                   Attributes xmlAttrs,
                                   GlobType globType) {
      int length = xmlAttrs.getLength();
      for (int i = 0; i < length; i++) {
        String xmlAttrName = xmlAttrs.getQName(i);
        String xmlValue = xmlAttrs.getValue(i);

        if ("type".equals(xmlAttrName)) {
          continue;
        }

        Field field = globType.findField(xmlAttrName);
        if (field == null) {
          throw new ItemNotFound(
                "Unknown field '" + xmlAttrName + "' for type '" + globType.getName() + "'");
        }

        Object value = fieldConverter.toObject(field, xmlValue);
        if (field.isKeyField()) {
          keyBuilder.setValue(field, value);
        }
        else {
          valuesBuilder.setObject(field, value);
        }
      }
    }

    public ChangeSet getChangeSet() {
      return changeSet;
    }
  }
}
