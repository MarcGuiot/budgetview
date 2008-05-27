package org.crossbowlabs.globs.model.format;

import org.crossbowlabs.globs.metamodel.Link;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.format.utils.AbstractGlobStringifier;

public class GlobLinkStringifier extends AbstractGlobStringifier {
  private GlobStringifier targetStringifier;
  private Link link;

  public GlobLinkStringifier(Link link, GlobStringifier targetStringifier) {
    this.link = link;
    this.targetStringifier = targetStringifier;
  }

  public String toString(Glob glob, GlobRepository repository) {
    return targetStringifier.toString(repository.findLinkTarget(glob, link), repository);
  }
}
