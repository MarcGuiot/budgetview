package org.globsframework.wicket;

import org.apache.wicket.model.IDetachable;
import org.globsframework.model.GlobRepository;

/**
 * Proxy for accessing a repository without having to reference (and thus serialize) it.
 * Implementations typically hold a transient reference to a local GlobRepository, and store serializable
 * information that will allow them to reconstruct the repository after it has been detached.
 */
public interface GlobRepositoryLoader extends IDetachable {
  GlobRepository getRepository();
}
