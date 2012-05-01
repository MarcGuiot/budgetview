package org.designup.picsou.model.util;

import org.globsframework.utils.collections.Range;

import java.security.InvalidParameterException;

public class OpenMonthRange extends Range<Integer> {
  public OpenMonthRange(Integer min, Integer max) throws InvalidParameterException {
    super(min, max);
  }
}
