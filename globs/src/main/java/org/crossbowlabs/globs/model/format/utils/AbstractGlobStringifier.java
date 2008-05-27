package org.crossbowlabs.globs.model.format.utils;

import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.format.GlobStringifier;
import org.crossbowlabs.globs.utils.Utils;

import java.util.Comparator;

public abstract class AbstractGlobStringifier implements GlobStringifier {
  private Comparator<Glob> comparator;

  protected AbstractGlobStringifier() {
  }

  protected AbstractGlobStringifier(Comparator<Glob> comparator) {
    this.comparator = comparator;
  }

  public Comparator<Glob> getComparator(final GlobRepository globRepository) {
    if (comparator != null) {
      return comparator;
    }
    return new Comparator<Glob>() {
      public int compare(Glob glob1, Glob glob2) {
        return Utils.compare(AbstractGlobStringifier.this.toString(glob1, globRepository),
                             AbstractGlobStringifier.this.toString(glob2, globRepository));
      }
    };
  }
}
