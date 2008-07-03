package org.globsframework.gui.splits;

import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.font.FontService;
import org.globsframework.gui.splits.styles.StyleService;
import org.globsframework.gui.splits.utils.DummyIconLocator;
import org.globsframework.gui.splits.utils.DummyTextLocator;
import org.uispec4j.UISpecTestCase;

import javax.swing.*;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.awt.*;
import java.io.Reader;
import java.io.StringReader;

public abstract class SplitsTestCase extends UISpecTestCase {
  protected SplitsBuilder builder;
  protected ColorService colorService = new ColorService();
  protected FontService fontService = new FontService();
  protected DummyIconLocator iconLocator = new DummyIconLocator();
  protected DummyTextLocator textLocator = new DummyTextLocator();
  protected StyleService styleService = new StyleService();

  protected JTable aTable = new JTable();
  protected JList aList = new JList();
  protected JButton aButton = new JButton();

  protected void setUp() throws Exception {
    super.setUp();
    aTable.setName("aTable");
    aList.setName("aList");
    aButton.setName("aButton");
    builder = new SplitsBuilder(colorService, iconLocator, textLocator, fontService);
  }

  protected <T extends Component> T parse(final String xml) throws Exception {
    validateDocument(toStream(xml));
    return builder.setSource(complete(xml)).<T>load();
  }

  protected Component parseWithoutSchemaValidation(String xml) throws Exception {
    return builder.setSource(complete(xml)).load();
  }

  protected StringReader toStream(String xml) {
    return new StringReader(complete(xml));
  }

  private String complete(String xml) {
    return "<splits>" + xml + "</splits>";
  }

  protected void validateDocument(Reader reader) throws Exception {
    String schemaLang = "http://www.w3.org/2001/XMLSchema";
    SchemaFactory factory = SchemaFactory.newInstance(schemaLang);
    Schema schema = factory.newSchema(new StreamSource(getClass().getResourceAsStream("/splits.xsd")));
    Validator validator = schema.newValidator();
    validator.validate(new StreamSource(reader));
  }

  protected void checkParsingError(String xml, String error) throws Exception {
    try {
      parse(xml);
      fail();
    }
    catch (Exception e) {
      assertTrue(e.getMessage().contains(error));
    }
  }

  protected GridBagConstraints getConstraints(Component parent, JComponent component) {
    JPanel panel = (JPanel)parent;
    GridBagLayout layout = (GridBagLayout)panel.getLayout();
    return layout.getConstraints(component);
  }
}
