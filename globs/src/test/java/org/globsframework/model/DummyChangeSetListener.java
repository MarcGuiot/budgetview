package org.globsframework.model;

import junit.framework.Assert;
import org.globsframework.metamodel.GlobType;
import org.globsframework.utils.Strings;
import org.globsframework.utils.TestUtils;

import java.util.Arrays;
import java.util.List;

public class DummyChangeSetListener implements ChangeSetListener {
  private ChangeSet lastChanges;
  private List<GlobType> lastResetTypes;

  public void globsChanged(ChangeSet changeSet, GlobRepository globRepository) {
    lastChanges = changeSet;
  }

  public void globsReset(GlobRepository globRepository, List<GlobType> changedTypes) {
    lastResetTypes = changedTypes;
  }

  public void assertLastChangesEqual(String expectedXml) {
    if (Strings.isNullOrEmpty(expectedXml)) {
      assertNoChanges();
      return;
    }
    if (lastChanges == null) {
      Assert.fail("No changes received");
    }
    GlobTestUtils.assertChangesEqual(lastChanges, expectedXml);
  }

  public void assertLastChangesEqual(GlobType type, String expectedXml) {
    if (lastChanges == null) {
      Assert.fail("No changes received");
    }
    GlobTestUtils.assertChangesEqual(lastChanges, type, expectedXml);
  }

  public void assertLastChangesEqual(List<Key> keys, String expectedXml) {
    if (lastChanges == null) {
      Assert.fail("No changes received");
    }
    GlobTestUtils.assertChangesEqual(lastChanges, keys, expectedXml);
  }

  public void assertNoChanges() {
    if ((lastChanges != null) && !lastChanges.isEmpty()) {
      Assert.fail("Unexpected changes: " + lastChanges);
    }
  }

  public ChangeSet getLastChanges() {
    return lastChanges;
  }

  public void reset() {
    lastChanges = null;
  }

  public void assertResetListEquals(GlobType... types) {
    this.lastChanges = null;
    if (lastResetTypes == null) {
      Assert.fail("reset was not called");
    }
    TestUtils.assertSetEquals(lastResetTypes, Arrays.asList(types));
  }
}
