package org.globsframework.model.format;

import org.globsframework.model.*;
import org.globsframework.utils.TablePrinter;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.*;

public class ChangeSetPrinter {

  public static void printStats(ChangeSet changeSet) {
    printStats(changeSet, new OutputStreamWriter(System.out), "");
  }

  public static void printStats(ChangeSet changeSet, Writer writer, String indent) {
    final Map<String, Stat> stats = new HashMap<String, Stat>();
    changeSet.safeVisit(new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        getStat(key).create();
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        getStat(key).update();
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        getStat(key).delete();
      }

      private Stat getStat(Key key) {
        String typeName = key.getGlobType().getName();
        Stat stat = stats.get(typeName);
        if (stat == null) {
          stat = new Stat(typeName);
          stats.put(typeName, stat);
        }
        return stat;
      }
    });

    List<Object[]> rows = new ArrayList<Object[]>();
    for (Stat stat : stats.values()) {
      rows.add(stat.toArray());
    }
    TablePrinter.print(Stat.getHeaderRow(), rows, true, new PrintWriter(writer), indent);
  }

  private static class Stat {
    private String typeName;
    private int creations;
    private int deletions;
    private int updates;

    public Stat(String typeName) {
      this.typeName = typeName;
    }

    public void create() {
      creations++;
    }

    public void delete() {
      deletions++;
    }

    public void update() {
      updates++;
    }

    public static Object[] getHeaderRow() {
      return new Object[]{"","create", "update", "delete"};
    }

    public Object[] toArray() {
      return new String[]{typeName, toString(creations), toString(updates), toString(deletions)};
    }

    private String toString(int value) {
      return value == 0 ? "" : Integer.toString(value);
    }
  }
}
