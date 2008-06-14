package org.globsframework.metamodel.links;

import org.globsframework.metamodel.Link;

public interface LinkVisitor {
  void visitLink(Link link);

  void visitLinkList(Link link);
}
