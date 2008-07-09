package org.globsframework.xml;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSetVisitor;
import org.globsframework.model.FieldValues;
import org.globsframework.model.Key;
import org.globsframework.model.FieldValuesWithPrevious;
import org.saxstack.writer.PrettyPrintRootXmlTag;
import org.saxstack.writer.RootXmlTag;
import org.saxstack.writer.XmlTag;

import java.io.IOException;
import java.io.Writer;

public class XmlChangeSetVisitor implements ChangeSetVisitor {
  private final XmlTag changesTag;
  private final FieldConverter converter = new FieldConverter();

  public XmlChangeSetVisitor(XmlTag changesTag) {
    this.changesTag = changesTag;
  }

  public XmlChangeSetVisitor(Writer writer, int indent) {
    try {
      XmlTag root = new PrettyPrintRootXmlTag(writer, indent);
      changesTag = root.createChildTag("changes");
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public XmlChangeSetVisitor(Writer writer) {
    try {
      XmlTag root = new RootXmlTag(writer);
      changesTag = root.createChildTag("changes");
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void visitCreation(Key key, FieldValues values) throws Exception {
    dumpChanges("create", key, values);
  }

  public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
    dumpChanges("update", key, values);
  }

  public void visitDeletion(Key key, FieldValues values) throws Exception {
    dumpChanges("delete", key, values);
  }

  private void dumpChanges(String change, Key key, FieldValues values) throws IOException {
    XmlTag tag = changesTag.createChildTag(change);
    tag.addAttribute("type", key.getGlobType().getName());
    dumpFieldValues(tag, key.getGlobType(), key);
    dumpFieldValues(tag, key.getGlobType(), values);
    tag.end();
  }

  private void dumpFieldValues(final XmlTag tag, final GlobType type, FieldValues values) throws IOException {
    try {
      values.apply(new FieldValues.Functor() {
        public void process(Field field, Object value) throws IOException {
          if (value != null) {
            tag.addAttribute(field.getName(), converter.toString(field, value));
          }
        }
      });
    }
    catch (IOException e) {
      throw e;
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void complete() {
    try {
      changesTag.end();
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
