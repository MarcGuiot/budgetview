package org.crossbowlabs.globs.xml;

import java.io.IOException;
import java.io.Writer;
import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.ChangeSetVisitor;
import org.crossbowlabs.globs.model.FieldValues;
import org.crossbowlabs.globs.model.Key;
import org.crossbowlabs.saxstack.writer.PrettyPrintRootXmlTag;
import org.crossbowlabs.saxstack.writer.RootXmlTag;
import org.crossbowlabs.saxstack.writer.XmlTag;

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

  public void visitUpdate(Key key, FieldValues values) throws Exception {
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
