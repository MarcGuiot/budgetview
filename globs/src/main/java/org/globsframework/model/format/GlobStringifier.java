package org.globsframework.model.format;

import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;

import java.util.Comparator;

public interface GlobStringifier {
  String toString(Glob glob, GlobRepository repository);

  Comparator<Glob> getComparator(GlobRepository repository);

  GlobStringifier EMPTY = new GlobStringifier() {
    public String toString(Glob glob, GlobRepository repository) {
      return "";
    }

    public Comparator<Glob> getComparator(GlobRepository repository) {
      return new Comparator<Glob>() {
        public int compare(Glob glob1, Glob glob2) {
          return 0;
        }
      };
    }
  };
}
