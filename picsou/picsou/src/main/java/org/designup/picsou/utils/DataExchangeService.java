package org.designup.picsou.utils;

import java.util.Map;

public class DataExchangeService {
  private Map<Integer, String> data;
  private int count;
  public String set(String data) {
    int current = ++count;
    this.data.put(count,  data);
    return Integer.toString(current);
  }

  public String get(String key){
    return this.data.get(Integer.parseInt(key));
  }
}
