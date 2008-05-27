package org.crossbowlabs.globs.metamodel.utils;
import java.util.*;
import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.Link;
import org.crossbowlabs.globs.metamodel.links.FieldMappingFunctor;
import org.crossbowlabs.globs.utils.Strings;
import org.crossbowlabs.globs.utils.exceptions.InvalidData;

public class GlobTypeDependencies {

  private List<GlobType> createSequence = new ArrayList<GlobType>();
  private List<GlobType> deleteSequence = new ArrayList<GlobType>();
  private Set<GlobType> postUpdate = new HashSet<GlobType>();

  public GlobTypeDependencies(Collection<GlobType> types) {
    dispatch(types);

    deleteSequence = new ArrayList<GlobType>(createSequence);
    Collections.reverse(deleteSequence);
  }

  public List<GlobType> getCreationSequence() {
    return Collections.unmodifiableList(createSequence);
  }

  public List<GlobType> getUpdateSequence() {
    return Collections.unmodifiableList(createSequence);
  }

  public List<GlobType> getDeletionSequence() {
    return Collections.unmodifiableList(deleteSequence);
  }

  public boolean needsPostUpdate(GlobType type) {
    return postUpdate.contains(type);
  }

  private void dispatch(Collection<GlobType> types) {
    Set<GlobType> done = new HashSet<GlobType>();
    List<GlobType> typeList = new ArrayList<GlobType>(types);
    Collections.sort(typeList, GlobTypeComparator.INSTANCE);
    for (GlobType type : typeList) {
      Map<GlobType, Field> hasCycle = new HashMap<GlobType, Field>();
      if (!processLinks(type, hasCycle, createSequence, done, postUpdate)) {
        throwCycleException(hasCycle);
      }
    }
  }

  private static boolean processLinks(final GlobType objectType,
                                      final Map<GlobType, Field> hasCycle,
                                      final List<GlobType> order,
                                      final Set<GlobType> done,
                                      final Set<GlobType> postUpdate) {
    if (done.contains(objectType)) {
      return true;
    }
    if (hasCycle.containsKey(objectType)) {
      return false;
    }

    for (Link link : objectType.getOutboundLinks()) {
      link.apply(new FieldMappingFunctor() {
        public void process(Field sourceField, Field targetField) {
          hasCycle.remove(objectType);
          hasCycle.put(objectType, sourceField);
          if (!processLinks(targetField.getGlobType(), hasCycle, order, done, postUpdate)) {
            if (sourceField.isRequired()) {
              hasCycle.remove(objectType);
              throwCycleException(hasCycle);
            }
            else {
              postUpdate.add(objectType);
            }
          }
        }
      });
    }

    done.add(objectType);
    order.add(objectType);
    hasCycle.remove(objectType);
    return true;
  }

  private static void throwCycleException(Map<GlobType, Field> hasCycle) {
    throw new InvalidData(
          "Cycles found with required fields:" + Strings.LINE_SEPARATOR + Strings.toString(hasCycle));
  }
}
