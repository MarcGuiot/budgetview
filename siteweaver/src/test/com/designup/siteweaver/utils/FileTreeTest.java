package com.designup.siteweaver.utils;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class FileTreeTest extends TestCase {

  private InputStream stream;

  public void setUp() throws Exception {
    stream = new ByteArrayInputStream(new byte[0]);
  }

  public void test() throws Exception {
    FileTree tree = new FileTree();
    tree.update("/root1.html", stream);
    tree.update("/sub1/page1.html", stream);
    tree.update("/sub1/sub11/sub111/page111b.html", stream);
    tree.update("/root2.html", stream);
    tree.update("/sub1/sub11/sub111/page111a.html", stream);
    tree.delete("/sub2/sub.txt");

    checkFunctor(tree,
                 "update file root1.html\n" +
                 "update file root2.html\n" +
                 "create directory sub1\n" +
                 "update file page1.html\n" +
                 "create directory sub11\n" +
                 "create directory sub111\n" +
                 "update file page111a.html\n" +
                 "update file page111b.html\n" +
                 "goto parent directory\n" +
                 "goto parent directory\n" +
                 "goto parent directory\n" +
                 "create directory sub2\n" +
                 "delete file sub.txt\n" +
                 "goto parent directory\n");
  }

  private void checkFunctor(FileTree tree, String expected) throws Exception {
    DummyTreeFunctor functor = new DummyTreeFunctor();
    tree.apply(functor);
    assertEquals(expected, functor.builder.toString());
  }

  private class DummyTreeFunctor implements FileTree.Functor {

    private StringBuilder builder = new StringBuilder();

    public void createDirectory(String name) {
      builder.append("create directory " + name + "\n");
    }

    public void enterDirectory(String name) {
      builder.append("enter directory " + name + "\n");
    }

    public void gotoParentDirectory() {
      builder.append("goto parent directory\n");
    }

    public void updateFile(String name, InputStream inputStream) {
      builder.append("update file " + name + "\n");
    }

    public void deleteFile(String name) {
      builder.append("delete file " + name + "\n");
    }
  }
}
