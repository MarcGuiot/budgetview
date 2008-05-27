package org.crossbowlabs.globs.metamodel.links;

import org.crossbowlabs.globs.metamodel.Link;

public interface LinkVisitor {
  void visitLink(Link link);

  void visitLinkList(Link link);
}
