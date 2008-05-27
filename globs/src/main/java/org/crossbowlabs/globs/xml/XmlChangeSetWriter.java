package org.crossbowlabs.globs.xml;

import java.io.Writer;
import java.io.IOException;
import java.util.List;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.ChangeSet;
import org.crossbowlabs.globs.model.ChangeSetVisitor;
import org.crossbowlabs.globs.model.Key;

public class XmlChangeSetWriter {
  private XmlChangeSetWriter() {
  }

  public static void write(ChangeSet changeSet, Writer writer) {
    doWrite(changeSet, writer, new ContentDumper() {
      public void process(ChangeSet changeSet, ChangeSetVisitor visitor) {
        changeSet.safeVisit(visitor);
      }
    });
  }

  public static void write(ChangeSet changeSet, final List<Key> keys, Writer writer) {
    doWrite(changeSet, writer, new ContentDumper() {
      public void process(ChangeSet changeSet, ChangeSetVisitor visitor) {
        for (Key key : keys) {
          changeSet.safeVisit(key, visitor);
        }
      }
    });
  }

  public static void write(ChangeSet changeSet, final GlobType type, Writer writer) {
    doWrite(changeSet, writer, new ContentDumper() {
      public void process(ChangeSet changeSet, ChangeSetVisitor visitor) {
        changeSet.safeVisit(type, visitor);
      }
    });
  }

  public static void prettyWrite(ChangeSet changeSet, Writer writer) {
    XmlChangeSetVisitor visitor = new XmlChangeSetVisitor(writer, 2);
    changeSet.safeVisit(visitor);
    visitor.complete();
    try {
      writer.flush();
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static void doWrite(ChangeSet changeSet, Writer writer, ContentDumper dumper) {
    XmlChangeSetVisitor visitor = new XmlChangeSetVisitor(writer);
    dumper.process(changeSet, visitor);
    visitor.complete();
  }

  private interface ContentDumper {
    void process(ChangeSet changeSet, ChangeSetVisitor visitor);
  }
}
