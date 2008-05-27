package org.designup.shrinker;


import org.crossbowlabs.globs.metamodel.properties.Property;
import org.crossbowlabs.globs.metamodel.properties.PropertyHolder;
import org.crossbowlabs.globs.metamodel.utils.DefaultPropertyHolder;
import org.crossbowlabs.globs.metamodel.utils.IdProperty;
import org.crossbowlabs.globs.utils.MultiMap;
import org.crossbowlabs.globs.utils.exceptions.InvalidData;
import org.crossbowlabs.globs.utils.exceptions.UnexpectedApplicationState;

import java.io.*;
import java.util.*;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

public class ClassPathClassRetriever implements DependExtractor.ClassRetreiver {
  private String target;
  private List<String> classToJar = new ArrayList<String>();
  private MultiMap<String, String> dependencies = new MultiMap<String, String>();
  private DirectoryNode entryNode = new RootDirectory();

  public ClassPathClassRetriever(String target) {
    this.target = target;
  }

  public InputStream getCode(String className) {
    StreamableNode streamableNode = getStreamableNode(className);
    return streamableNode == null ? null : streamableNode.getInputStream();
  }

  private StreamableNode getStreamableNode(String className) {
    String[] names = (DependExtractor.undotte(className) + ".class").split("/");
    DirectoryNode node = entryNode;
    for (int i = 0; i < names.length - 1; i++) {
      Node child = node.findChild(names[i]);
      if (child == null) {
        return null;
      }
      node = child.asDirectory();
    }
    Node child = node.findChild(names[names.length - 1]);
    if (child == null) {
      return null;
    }
    return child.asInputStreamable();
  }

  public void add(String dependClassName, String className) {
    classToJar.add(className + ".class");
    if (dependClassName != null) {
      dependencies.put(dependClassName, className);
    }
  }

  public void addPathContent(String path, Boolean isRecursive) {
    String[] names = (DependExtractor.undotte(path)).split("/");
    DirectoryNode node = entryNode;
    for (int i = 0; i < names.length; i++) {
      Node child = node.findChild(names[i]);
      if (child == null) {
        throw new RuntimeException("bad path " + names[i] + " in " + path);
      }
      node = child.asDirectory();
    }
    updateSubContent(node, path);
  }

  private void updateSubContent(DirectoryNode node, String path) {
    Iterator<Node> children = node.getChildren();
    while (children.hasNext()) {
      Node child = children.next();
      if (child.isDirectory()) {
        updateSubContent(child.asDirectory(), path + "/" + child.getName());
      }
      else {
        classToJar.add(path + "/" + child.getName());
      }
    }
  }

  public void complete(List<String> ressources, List<String> pathToIgnore) {
    for (Iterator<Map.Entry<String, List<String>>> iterator = dependencies.values(); iterator.hasNext();) {
      Map.Entry<String, List<String>> entry = iterator.next();
      System.out.println("Key " + entry.getKey());
      for (String s : entry.getValue()) {
        System.out.println("     " + s);
      }
    }
    classToJar.addAll(ressources);
    markClassToSelected();
    markPathToIgnore(pathToIgnore);
    try {
      JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(target));
      entryNode.call(new JarNodeFunctor(jarOutputStream));
      jarOutputStream.close();
    }
    catch (IOException e) {
      throw new InvalidData(e);
    }
  }

  private void markPathToIgnore(List<String> pathToIgnore) {
    for (String aPathToIgnore : pathToIgnore) {
      String[] strings = aPathToIgnore.split("/");
      DirectoryNode sourceNode = entryNode;
      for (String name : strings) {
        Node child = sourceNode.findChild(name);
        if (child == null) {
          sourceNode = null;
          break;
        }
        sourceNode = child.asDirectory();
      }
      if (sourceNode != entryNode && sourceNode != null) {
        sourceNode.updateProperty(forceIgnore, true);
      }
    }
  }

  private void markClassToSelected() {
    for (String className : classToJar) {
      String[] strings = className.split("/");
      DirectoryNode sourceNode = entryNode;
      for (int i = 0; i < strings.length - 1; i++) {
        Node child = sourceNode.findChild(strings[i]);
        if (child == null) {
          break;
        }
        sourceNode = child.asDirectory();
      }
      Node child = sourceNode.findChild(strings[strings.length - 1]);
      if (child != null) {
        if (className.endsWith("/")) {
          child.call(new NodeFunctor() {
            public boolean enter(DirectoryNode node) {
              return true;
            }

            public void process(StreamableNode node) {
              node.updateProperty(isToBeImported, true);
            }

            public void leave(DefaultDirectory node, boolean enterReturnValue) {
            }
          });
        }
        else {
          child.updateProperty(isToBeImported, Boolean.TRUE);
        }
      }
    }
  }

  static Property<Node, Boolean> isToBeImported = new IdProperty<Node, Boolean>("isImported", 0) {
  };

  static Property<Node, Boolean> forceIgnore = new IdProperty<Node, Boolean>("forceIgnore", 1) {
  };

  static interface NodeFunctor {
    boolean enter(DirectoryNode node);

    void process(StreamableNode node);

    void leave(DefaultDirectory node, boolean enterReturnValue);
  }

  static interface Node extends PropertyHolder<Node> {
    String getName();

    boolean isDirectory();

    boolean isInputStreamble();

    DirectoryNode asDirectory();

    StreamableNode asInputStreamable();

    public void call(NodeFunctor nodeFunctor);
  }

  static interface DirectoryNode extends Node {
    boolean canEnter(String name);

    Node findChild(String name);

    void add(Node node);

    DirectoryNode getOrCreate(String node);

    boolean contains(String name);

    Node getChild(String name);

    Iterator<Node> getChildren();
  }

  static class DefaultDirectory extends DefaultPropertyHolder<Node> implements DirectoryNode {
    private Map<String, Node> child = new HashMap<String, Node>();
    private String name;

    public DefaultDirectory(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

    public boolean isDirectory() {
      return true;
    }

    public boolean isInputStreamble() {
      return false;
    }

    public DirectoryNode asDirectory() {
      return this;
    }

    public StreamableNode asInputStreamable() {
      throw new UnexpectedApplicationState(name + " not streamable");
    }

    public void call(NodeFunctor nodeFunctor) {
      if (nodeFunctor.enter(this)) {
        callOnChild(nodeFunctor);
        nodeFunctor.leave(this, true);
      }
      else {
        nodeFunctor.leave(this, false);
      }
    }

    protected void callOnChild(NodeFunctor nodeFunctor) {
      for (Node node : child.values()) {
        node.call(nodeFunctor);
      }
    }

    public boolean canEnter(String name) {
      return child.containsKey(name);
    }

    public Node findChild(String name) {
      return child.get(name);
    }

    public void add(Node node) {
      child.put(node.getName(), node);
    }

    public DirectoryNode getOrCreate(String name) {
      if (!child.containsKey(name)) {
        child.put(name, new DefaultDirectory(name));
      }
      return child.get(name).asDirectory();
    }

    public boolean contains(String name) {
      return child.containsKey(name);
    }

    public Node getChild(String name) {
      Node node = child.get(name);
      if (node == null) {
        throw new UnexpectedApplicationState("For " + name);
      }
      return node;
    }

    public Iterator<Node> getChildren() {
      return child.values().iterator();
    }
  }

  static class RootDirectory extends DefaultDirectory {

    public RootDirectory() {
      super(null);
    }

    public void call(NodeFunctor nodeFunctor) {
      callOnChild(nodeFunctor);
    }
  }

  static interface StreamableVisitor {

    void visitZip(ZipStreamableEntryNode zipStreamableEntryNode);

    void visitFile(FileNode fileNode);
  }

  static interface StreamableNode extends Node {
    InputStream getInputStream();

    void visit(StreamableVisitor visitor);
  }

  static abstract class DefaultStreamableNode extends DefaultPropertyHolder<Node> implements StreamableNode {
    private String name;

    protected DefaultStreamableNode(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

    public boolean isDirectory() {
      return false;
    }

    public boolean isInputStreamble() {
      return true;
    }

    public DirectoryNode asDirectory() {
      throw new UnexpectedApplicationState(name + " not a directory");
    }

    public StreamableNode asInputStreamable() {
      return this;
    }

    public void call(NodeFunctor nodeFunctor) {
      nodeFunctor.process(this);
    }
  }

  static class ZipStreamableEntryNode extends DefaultStreamableNode {
    private JarFile jarFile;
    private ZipEntry entry;

    public ZipStreamableEntryNode(String name, JarFile jarFile, ZipEntry entry) {
      super(name);
      this.jarFile = jarFile;
      this.entry = entry;
    }

    public InputStream getInputStream() {
      try {
        return jarFile.getInputStream(entry);
      }
      catch (IOException e) {
        throw new UnexpectedApplicationState("for " + entry.getName() + " in " + jarFile.getName(), e);
      }
    }

    public void visit(StreamableVisitor visitor) {
      visitor.visitZip(this);
    }

    public String getJarName() {
      return jarFile.getName();
    }
  }

  public void init(Set<String> jarToIgnore, String classPath) throws IOException {
    for (String path : classPath.split(":")) {
      Boolean ignored = false;
      File file = new File(path);
      if (file.isFile()) {
        if (jarToIgnore.contains(file.getName())) {
          ignored = true;
        }
        JarFile jarFile = new JarFile(file);
        Enumeration entries = jarFile.entries();
        while (entries.hasMoreElements()) {
          ZipEntry entry = (ZipEntry) entries.nextElement();
          DirectoryNode node = entryNode;
          String[] name = entry.getName().split("/");
          for (int i = 0; i < name.length - 1; i++) {
            node = node.getOrCreate(name[i]);
          }
          if (entry.isDirectory()) {
            node.getOrCreate(name[name.length - 1]);
          }
          else {
            ZipStreamableEntryNode child = new ZipStreamableEntryNode(name[name.length - 1], jarFile, entry);
            node.add(child);
            child.updateProperty(forceIgnore, ignored);
          }
        }
      }
      else {
        for (String jarOrPath : jarToIgnore) {
          if (file.getAbsolutePath().indexOf(jarOrPath) != -1) {
            ignored = true;
            break;
          }
        }
        readFiles(entryNode, file, ignored);
      }
    }
  }

  private void readFiles(DirectoryNode root, File directory, Boolean ignored) {
    File[] files = directory.listFiles();
    for (File file : files) {
      if (file.isDirectory()) {
        readFiles(root.getOrCreate(file.getName()), file, ignored);
      }
      else {
        FileNode fileNode = new FileNode(file);
        root.add(fileNode);
        fileNode.updateProperty(forceIgnore, ignored);
      }
    }
  }

  private static class FileNode extends DefaultStreamableNode {
    private File file;

    public FileNode(File file) {
      super(file.getName());
      this.file = file;
    }

    public InputStream getInputStream() {
      try {
        return new FileInputStream(file);
      }
      catch (FileNotFoundException e) {
        throw new UnexpectedApplicationState("for " + file.getName(), e);
      }
    }

    public void visit(StreamableVisitor visitor) {
      visitor.visitFile(this);
    }

    public String getPath() {
      return file.getAbsolutePath();
    }
  }

  private static class JarNodeFunctor implements NodeFunctor {
    private Stack<Boolean> pathOfImportedState = new Stack<Boolean>();
    private Stack<String> path;
    private StringBuilder cachePath = new StringBuilder();
    private JarOutputStream jarOutputStream;

    public JarNodeFunctor(JarOutputStream jarOutputStream) {
      this.jarOutputStream = jarOutputStream;
      path = new Stack<String>();
      pathOfImportedState.push(Boolean.TRUE);
    }

    public boolean enter(DirectoryNode node) {
      try {
        path.add(node.getName());
        cachePath.append(node.getName()).append("/");
        if (node.getProperty(forceIgnore, Boolean.FALSE)) {
          pathOfImportedState.push(Boolean.FALSE);
        }
        else if (node.getProperty(isToBeImported, Boolean.FALSE)) {
          jarOutputStream.putNextEntry(new ZipEntry(cachePath.toString()));
          pathOfImportedState.push(Boolean.TRUE);
          return true;
        }
        else {
          pathOfImportedState.push(pathOfImportedState.peek());
        }
      }
      catch (IOException e) {
        throw new InvalidData(e);
      }
      return true;
    }

    public void process(StreamableNode node) {
      try {
        if (node.getProperty(forceIgnore, Boolean.FALSE) ||
            !node.getProperty(isToBeImported, Boolean.FALSE) ||
            !pathOfImportedState.peek()) {
          return;
        }
        InputStream inputStream = node.getInputStream();
        jarOutputStream.putNextEntry(new ZipEntry(cachePath.toString() + node.getName()));
        int c;
        while ((c = inputStream.read()) != -1) {
          jarOutputStream.write(c);
        }
      }
      catch (IOException e) {
        throw new InvalidData(e);
      }
    }

    public void leave(DefaultDirectory node, boolean enterReturnValue) {
      path.pop();
      pathOfImportedState.pop();
      cachePath.setLength(0);
      for (String s : path) {
        cachePath.append(s).append("/");
      }
    }
  }
}

