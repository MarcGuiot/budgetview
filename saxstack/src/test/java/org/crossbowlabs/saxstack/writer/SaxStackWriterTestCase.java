package org.globsframework.saxstack.writer;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class SaxStackWriterTestCase extends TestCase {
  protected Category root;

  protected void setUp() throws Exception {
    super.setUp();
    root = new Category("contacts",
                        new Contact[]{
                          new Contact("me", "512")
                        },
                        new Category[]{
                          new Category("family",
                                       new Contact[]{
                                         new Contact("Grandpa", "567")
                                       },
                                       new Category[]{
                                         new Category("home",
                                                      new Contact[]{
                                                        new Contact("Bart", "756"),
                                                        new Contact("Homer", "757")
                                                      },
                                                      new Category[0])
                                       }),
                          new Category("work",
                                       new Contact[]{
                                         new Contact("Ralph", "123")
                                       },
                                       new Category[0])
                        });
  }

  protected void checkOutput(XmlRootBuilder xmlBuilder, String expectedOutput) throws Exception {
    checkOutputWithNoFilter(xmlBuilder, expectedOutput);
    checkOutputWithNullFilter(xmlBuilder, expectedOutput);
  }

  private void checkOutputWithNoFilter(XmlRootBuilder xmlBuilder, String expectedOutput) throws Exception {
    StringWriter writer = new StringWriter();
    write(writer, xmlBuilder);
    XmlTestUtils.assertEquals(expectedOutput, writer.toString());
  }

  private void checkOutputWithNullFilter(XmlRootBuilder xmlBuilder, String expectedOutput) throws Exception {
    StringWriter writer = new StringWriter();
    write(writer, xmlBuilder, null);
    XmlTestUtils.assertEquals(expectedOutput, writer.toString());
  }

  protected void checkOutput(XmlRootBuilder xmlBuilder,
                             String filter,
                             String expectedOutput) throws Exception {
    StringWriter writer = new StringWriter();
    write(writer, xmlBuilder, new PathFilter(filter));
    XmlTestUtils.assertEquals(expectedOutput,
                              writer.toString());
  }

  protected void checkOutputIsEmpty(XmlRootBuilder xmlBuilder, String filter) throws IOException {
    StringWriter writer = new StringWriter();
    write(writer, xmlBuilder, new PathFilter(filter));
    assertEquals("", writer.toString());
  }

  private void write(StringWriter writer, XmlRootBuilder xmlBuilder, PathFilter pathFilter) throws IOException {
    new SaxStackWriter(writer).write(xmlBuilder, pathFilter);
  }

  private void write(StringWriter writer, XmlRootBuilder xmlBuilder) throws IOException {
    new SaxStackWriter(writer).write(xmlBuilder);
  }

  protected static class Category {
    private String name;
    private List subCategories;
    private List contacts;
    private String additionalInfo;

    public Category(String name) {
      this.name = name;
      contacts = new ArrayList();
      subCategories = new ArrayList();
    }

    public Category(String name, Contact[] contacts, Category[] categories) {
      this.name = name;
      this.contacts = Arrays.asList(contacts);
      this.subCategories = Arrays.asList(categories);
    }

    public String getName() {
      return name;
    }

    public List getContacts() {
      return contacts;
    }

    public List getSubCategories() {
      return subCategories;
    }

    public String getAdditionalInfo() {
      return additionalInfo;
    }

    public void addInfo(String addtioalInfo) {
      this.additionalInfo = addtioalInfo;
    }

    public void addCategory(Category category) {
      subCategories.add(category);
    }
  }

  protected static class Contact {
    private String name;
    private String phone;

    public Contact(String name, String phone) {
      this.name = name;
      this.phone = phone;
    }

    public String getName() {
      return name;
    }

    public String getPhone() {
      return phone;
    }

  }


}