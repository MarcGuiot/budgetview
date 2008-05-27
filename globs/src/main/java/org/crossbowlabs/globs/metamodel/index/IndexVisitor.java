package org.crossbowlabs.globs.metamodel.index;

public interface IndexVisitor {

  void visiteUniqueIndex(UniqueIndex index);

  void visiteNotUniqueIndex(NotUniqueIndex index);
}
