package org.globsframework.utils.stream;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReplacementInputStreamBuilder {
  private PreNode rootNode = new PreNode(-1);
  private int maxSize;

  public ReplacementInputStreamBuilder() {
  }

  public void replace(String from, String to) {
    replace(from.getBytes(), to.getBytes());
  }

  public void replace(byte[] from, byte[] to) {
    maxSize = Math.max(maxSize, to.length);
    PreNode node = rootNode;
    for (byte b : from) {
      node = node.push(b);
    }

    int[] toInt = new int[to.length];
    for (int i = 0, length = to.length; i < length; i++) {
      toInt[i] = to[i];
    }
    node.set(toInt);
  }


  public ReplacementInputStream create(InputStream stream) {
    return new ReplacementInputStream(stream, rootNode.create(new ArrayList<Integer>()), maxSize);
  }


  static class PreNode {
    int from;
    private Map<Integer, PreNode> child = new HashMap<Integer, PreNode>();
    private int[] to;

    PreNode(int from) {
      this.from = from;
    }

    public PreNode push(int b) {
      if (!child.containsKey(b)) {
        child.put(b, new PreNode(b));
      }
      return child.get(b);
    }

    public void set(int[] to) {
      this.to = to;
    }

    public Node create(List<Integer> from) {
      if (this.from != -1) {
        from.add(this.from);
      }
      try {
        if (child.isEmpty()) {
          return new Terminal(convert(from), to);
        }
        if (child.size() == 1) {
          Map.Entry<Integer, PreNode> next = child.entrySet().iterator().next();
          if (to != null) {
            return new TermWithOneChildNode(to, convert(from), next.getKey(), next.getValue().create(from));
          }
          else {
            return new OneChildNode(convert(from), next.getKey(), next.getValue().create(from));
          }
        }
        ManyChildNode many;
        if (to != null) {
          many = new TermManyChildNode(to, convert(from));
        }
        else {
          many = new ManyChildNode(convert(from));
        }
        for (Map.Entry<Integer, PreNode> entry : child.entrySet()) {
          many.add(entry.getKey(), entry.getValue().create(from));
        }
        return many;
      }
      finally {
        if (this.from != -1) {
          from.remove(from.size() - 1);
        }
      }
    }

    private int[] convert(List<Integer> from) {
      if (from.isEmpty()) {
        return null;
      }
      int[] ints = new int[from.size()];
      int i = 0;
      for (Integer integer : from) {
        ints[i] = integer;
        i++;
      }
      return ints;
    }
  }
}


