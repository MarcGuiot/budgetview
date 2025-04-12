package com.designup.siteweaver.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class FileTree {

  public interface Functor {

    void createDirectory(String name) throws IOException;

    void enterDirectory(String name) throws IOException;

    void gotoParentDirectory() throws IOException;

    void updateFile(String name, InputStream inputStream) throws IOException;

    void deleteFile(String name) throws IOException;
  }

  private Node rootNode = new Node("/", NodeAction.NONE);

  public void update(String path, InputStream stream) {
    String[] items = split(path);
    Node node = getLeafDir(items);
    node.createUpdateChild(items[items.length - 1], stream);
  }

  public void delete(String path) {
    String[] items = split(path);
    Node node = getLeafDir(items);
    node.createDeleteChild(items[items.length - 1]);
  }

  private Node getLeafDir(String[] items) {
    Node node = rootNode;
    for (int i = 0; i < items.length - 1; i++) {
      node = node.getChild(items[i], NodeAction.CREATE_DIR);
    }
    return node;
  }

  public void apply(Functor functor) throws IOException {
    for (Node node : rootNode.getChildren()) {
      doApply(node, functor);
    }
  }

  private void doApply(Node node, Functor functor) throws IOException {
    switch (node.action) {
      case NONE:
        break;
      case CREATE_DIR:
        functor.createDirectory(node.name);
        break;
      case ENTER_DIR:
        functor.enterDirectory(node.name);
        break;
      case UPDATE:
        functor.updateFile(node.name, node.stream);
        break;
      case DELETE:
        functor.deleteFile(node.name);
        break;
      default:
        throw new RuntimeException("Unexpected action " + node.action);
    }

    if (node.isDirectory()) {
      for (Node child : node.getChildren()) {
        doApply(child, functor);
      }
      functor.gotoParentDirectory();
    }
  }

  public String[] split(String path) {
    if (path.startsWith("/")) {
      path = path.substring(1);
    }
    return path.split("/");
  }

  private class Node implements Comparable<Node> {
    private String name;
    private NodeAction action;
    private InputStream stream;
    private Map<String, Node> children = new HashMap<String, Node>();

    private Node(String name, NodeAction action) {
      this(name, action, null);
    }

    private Node(String name, NodeAction action, InputStream stream) {
      this.name = name;
      this.action = action;
      this.stream = stream;
    }

    public Node getChild(String name, NodeAction action) {
      Node subNode = children.get(name);
      if (subNode == null) {
        subNode = new Node(name, action);
        children.put(name, subNode);
      }
      return subNode;
    }

    public void createUpdateChild(String childName, InputStream stream) {
      Node subNode = children.get(childName);
      if (subNode != null) {
        throw new RuntimeException("Node " + childName + " already exists");
      }

      subNode = new Node(childName, NodeAction.UPDATE, stream);
      children.put(childName, subNode);
    }

    public void createDeleteChild(String childName) {
      Node subNode = children.get(childName);
      if (subNode != null) {
        throw new RuntimeException("Node " + childName + " already exists");
      }

      subNode = new Node(childName, NodeAction.DELETE);
      children.put(childName, subNode);
    }

    public Collection<Node> getChildren() {
      SortedSet<Node> result = new TreeSet<Node>();
      result.addAll(children.values());
      return result;
    }

    public boolean isDirectory() {
      return action.equals(NodeAction.CREATE_DIR) || action.equals(NodeAction.ENTER_DIR);
    }

    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      Node node = (Node)o;

      if (!name.equals(node.name)) {
        return false;
      }

      return true;
    }

    public int hashCode() {
      return name.hashCode();
    }

    public int compareTo(Node o) {
      if (isDirectory() && !o.isDirectory()) {
        return 1;
      }
      if (!isDirectory() && o.isDirectory()) {
        return -1;
      }
      return name.compareTo(o.name);
    }
  }

  private enum NodeAction {
    NONE,
    CREATE_DIR,
    ENTER_DIR,
    UPDATE,
    DELETE
  }
}
