package org.saxstack.sample;

import junit.framework.TestCase;
import org.saxstack.parser.DefaultXmlNode;
import org.saxstack.parser.SaxStackParser;
import org.saxstack.parser.XmlBootstrapNode;
import org.saxstack.parser.XmlNode;
import org.saxstack.utils.XmlUtils;
import org.xml.sax.Attributes;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class SampleParsingTest extends TestCase {
  public void test() throws Exception {
    String input =
      "<contacts>" +
      "  <contact firstName='Homer' lastName='Simpson'>" +
      "    <email address='dad@simpsons.net'/>'" +
      "    <email address='homer@simpsons.net'/>'" +
      "  </contact>" +
      "  <contact firstName='Bart' lastName='Simpson'>" +
      "    <email address='bart@simpsons.net'/>'" +
      "  </contact>" +
      "</contacts>";

    List contacts = new ArrayList();
    ContactsNode root = new ContactsNode(contacts);
    SaxStackParser.parse(XmlUtils.getXmlReader(), new XmlBootstrapNode(root, "contacts"), new StringReader(input));

    assertEquals(2, contacts.size());
    assertEquals("Homer Simpson [dad@simpsons.net, homer@simpsons.net]", contacts.get(0).toString());
    assertEquals("Bart Simpson [bart@simpsons.net]", contacts.get(1).toString());
  }

  private static class Contact {
    private String firstName;
    private String lastName;
    private List emails = new ArrayList();

    public Contact(String firstName, String lastName) {
      this.firstName = firstName;
      this.lastName = lastName;
    }

    public void addEmail(String email) {
      emails.add(email);
    }

    public List getEmails() {
      return emails;
    }

    public String getFirstName() {
      return firstName;
    }

    public String getLastName() {
      return lastName;
    }

    public String toString() {
      return firstName + " " + lastName + " " + emails;
    }
  }

  private static class ContactsNode extends DefaultXmlNode {
    private List contacts;

    public ContactsNode(List contacts) {
      this.contacts = contacts;
    }

    public XmlNode getSubNode(String childName, Attributes xmlAttrs) {
      if (childName.equals("contact")) {
        return new ContactNode(xmlAttrs, contacts);
      }
      return super.getSubNode(childName, xmlAttrs);
    }
  }

  private static class ContactNode extends DefaultXmlNode {
    private Contact contact;

    public ContactNode(Attributes xmlAttrs, List contacts) {
      String firstName = XmlUtils.getAttrValue("firstName", xmlAttrs, "");
      String lastName = XmlUtils.getAttrValue("lastName", xmlAttrs, "");
      contact = new Contact(firstName, lastName);
      contacts.add(contact);
    }

    public XmlNode getSubNode(String childName, Attributes xmlAttrs) {
      if (childName.equals("email")) {
        return new EmailNode(xmlAttrs, contact);
      }
      return super.getSubNode(childName, xmlAttrs);
    }
  }

  private static class EmailNode extends DefaultXmlNode {
    public EmailNode(Attributes xmlAttrs, Contact contact) {
      String email = XmlUtils.getAttrValue("address", xmlAttrs);
      contact.addEmail(email);
    }
  }
}
