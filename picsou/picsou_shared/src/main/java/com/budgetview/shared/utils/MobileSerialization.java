package com.budgetview.shared.utils;

import org.globsframework.metamodel.GlobType;

import java.util.HashMap;
import java.util.Map;

public class MobileSerialization {
  private int majorVersion;
  private int minorVersion;
  private Map<GlobType, PicsouGlobSerializer> serializers = new HashMap<GlobType, PicsouGlobSerializer>();

  public MobileSerialization(int majorVersion, int minorVersion) {
    this.majorVersion = majorVersion;
    this.minorVersion = minorVersion;
  }

  public int getMajorVersion() {
    return majorVersion;
  }

  public int getMinorVersion() {
    return minorVersion;
  }
}
