package com.gnosia.morphograph;

import org.uispec4j.*;
import org.uispec4j.interception.WindowInterceptor;

import java.io.StringReader;

public class MorphographTest extends UISpecTestCase {
  private ComboBox topicCombo;
  private ComboBox seriesCombo;
  private Window window;

  public void testTopicAndSeriesSelection() throws Exception {
    init(
      "<morphograph>" +
      "  <topic name='Topic One'>" +
      "    <series name='Series 1'/>" +
      "  </topic>" +
      "  <topic name='Topic Two'>" +
      "    <series name='Series 1'/>" +
      "  </topic>" +
      "  <topic name='Topic Three'>" +
      "    <series name='One'/>" +
      "    <series name='Two'/>" +
      "    <series name='Three'/>" +
      "  </topic>" +
      "</morphograph>");

    assertTrue(topicCombo.contentEquals(new String[]{"Topic One", "Topic Two", "Topic Three"}));
    assertTrue(topicCombo.selectionEquals("Topic One"));

    topicCombo.select("Topic Three");
    assertTrue(seriesCombo.contentEquals(new String[]{"One", "Two", "Three"}));
    assertTrue(seriesCombo.selectionEquals("One"));
  }

  public void testInitialSelection() throws Exception {
    init(
      "<morphograph>" +
      "  <topic name='Topic One'>" +
      "    <series name='Series 1'>" +
      "      <exercise name='Exo1_1'" +
      "                exerciseTypeName='input'" +
      "                title='Title 1_1'" +
      "                description='Desc 1_1'" +
      "                example='Example 1_1'>" +
      "        <input title='Question 1_1' answer='R1'/>" +
      "      </exercise>" +
      "    </series>" +
      "  </topic>" +
      "  <topic name='Topic Two'>" +
      "    <series name='Series 2'>" +
      "      <exercise name='Exo2_1'" +
      "                exerciseTypeName='input'" +
      "                title='Title 2_1'" +
      "                description='Desc 2_1'" +
      "                example='Example 2_1'>" +
      "        <input title='Question 2_1' answer='R21'/>" +
      "      </exercise>" +
      "    </series>" +
      "  </topic>" +
      "</morphograph>");

    assertTrue(topicCombo.selectionEquals("Topic One"));
    assertTrue(seriesCombo.selectionEquals("Series 1"));

    checkLabelsPresent("Exo1_1", "Title 1_1", "Desc 1_1", "Example 1_1", "Question 1_1");
    checkLabelsNotPresent("Exo2_1", "Title 2_1", "Desc 2_1", "Example 2_1", "Question 2_1", "Question 2_2");

    TextBox textBox0 = window.getInputTextBox("textField0");
    textBox0.setText("R1");
    assertTrue(textBox0.textEquals("R1"));
    assertTrue(textBox0.backgroundEquals("green"));
  }

  public void testinput() throws Exception {
    init(
      "<morphograph>" +
      "  <topic name='Topic One'>" +
      "    <series name='Series 1'>" +
      "      <exercise name='Exo1_1'" +
      "                exerciseTypeName='input'" +
      "                title='Title 1_1'" +
      "                description='Desc 1_1'" +
      "                example='Example 1_1'>" +
      "        <input title='Question 1_1' answer='R1'/>" +
      "      </exercise>" +
      "    </series>" +
      "  </topic>" +
      "  <topic name='Topic Two'>" +
      "    <series name='Series 2'>" +
      "      <exercise name='Exo2_1'" +
      "                exerciseTypeName='input'" +
      "                title='Title 2_1'" +
      "                description='Desc 2_1'" +
      "                example='Example 2_1'>" +
      "        <input title='Question 2_1' answer='R21'/>" +
      "        <input title='Question 2_2' answer='R22'/>" +
      "      </exercise>" +
      "    </series>" +
      "  </topic>" +
      "</morphograph>");

    topicCombo.select("Topic Two");
    seriesCombo.select("Series 2");

    checkLabelsPresent("Exo2_1", "Title 2_1", "Desc 2_1", "Example 2_1", "Question 2_1", "Question 2_2");
    checkLabelsNotPresent("Exo1_1", "Title 1_1", "Desc 1_1", "Example 1_1");

    TextBox textBox0 = window.getInputTextBox("textField0");
    textBox0.setText("R21");
    assertTrue(textBox0.textEquals("R21"));
    assertTrue(textBox0.backgroundEquals("green"));
  }

  public void testInputWithNoAnswer() throws Exception {
    init(
      "<morphograph>" +
      "  <topic name='Topic One'>" +
      "    <series name='Series 1'>" +
      "      <exercise name='Exo1_1'" +
      "                exerciseTypeName='input'" +
      "                title='Title 1_1'" +
      "                description='Desc 1_1'" +
      "                example='Example 1_1'>" +
      "        <input title='Question 1_1'/>" +
      "      </exercise>" +
      "    </series>" +
      "  </topic>" +
      "</morphograph>");

    checkLabelsPresent("Exo1_1", "Title 1_1", "Desc 1_1", "Example 1_1");

    assertNotNull(window.getInputTextBox("textField0"));

    try {
      window.getButton("Valider");
      fail();
    }
    catch (ItemNotFoundException e) {
    }
  }

  public void testSelect() throws Exception {
    init(
      "<morphograph>" +
      "  <topic name='Topic One'>" +
      "    <series name='Series 1'>" +
      "      <exercise name='Exo1'" +
      "                exerciseTypeName='select'" +
      "                title='Title 1'" +
      "                description='Desc 1'" +
      "                example='Example 1'>" +
      "        <select title='T1' alt1='A1_1' alt2='A1_2' alt3='A1_3' answers='2'/>" +
      "        <select title='T2' alt1='A2_1' alt2='A2_2' alt3='A2_3' alt4='A2_4' alt5='A2_5' answers='2'/>" +
      "      </exercise>" +
      "    </series>" +
      "  </topic>" +
      "</morphograph>");

    checkLabelsPresent("Exo1", "Title 1", "Desc 1", "Example 1", "T1", "T2");

    Button a1_2Button = window.getButton("A1_2");
    a1_2Button.click();
    assertTrue(a1_2Button.foregroundEquals("green"));
    checkButtonsInactive("A1_1", "A1_3");

    Button a2_4Button = window.getButton("A2_4");
    a2_4Button.click();
    assertTrue(a2_4Button.foregroundEquals("red"));
    assertTrue(window.getButton("A2_2").foregroundEquals("green"));
    checkButtonsInactive("A2_1", "A2_3", "A2_5");
  }

  public void testMultiSelect() throws Exception {
    init(
      "<morphograph>" +
      "  <topic name='Topic One'>" +
      "    <series name='Series 1'>" +
      "      <exercise name='Exo1'" +
      "                exerciseTypeName='select'" +
      "                title='Title 1'" +
      "                description='Desc 1'" +
      "                example='Example 1'>" +
      "        <select title='T1' alt1='A1_1' alt2='A1_2' alt3='A1_3' alt4='A1_4' alt5='A1_5' answers='2,4'/>" +
      "        <select title='T2' alt1='A2_1' alt2='A2_2' alt3='A2_3' alt4='A2_4' alt5='A2_5' answers='2,4'/>" +
      "      </exercise>" +
      "    </series>" +
      "  </topic>" +
      "</morphograph>");

    checkLabelsPresent("Exo1", "Title 1", "Desc 1", "Example 1", "T1", "T2");

    Button a1_2Button = window.getButton("A1_2");
    a1_2Button.click();
    assertTrue(a1_2Button.foregroundEquals("green"));

    Button a1_4Button = window.getButton("A1_4");
    a1_4Button.click();
    assertTrue(a1_4Button.foregroundEquals("green"));

    checkButtonsInactive("A1_1", "A1_3", "A1_5");

    Button a2_2Button = window.getButton("A2_2");
    a2_2Button.click();
    assertTrue(a2_2Button.foregroundEquals("green"));

    Button a2_5Button = window.getButton("A2_5");
    a2_5Button.click();
    assertTrue(a2_5Button.foregroundEquals("red"));

    assertTrue(window.getButton("A2_4").foregroundEquals("green"));

    checkButtonsInactive("A2_1", "A2_3");
  }

  private void checkButtonsInactive(String... labels) {
    for (String label : labels) {
      Button button = window.getButton(label);
      assertTrue(button.foregroundEquals("black"));
      assertFalse("Button " + label + " still enabled", button.isEnabled());
    }
  }

  private void checkLabelsPresent(String... labels) {
    for (String label : labels) {
      assertTrue(window.containsLabel(label));
    }
  }

  private void checkLabelsNotPresent(String... labels) {
    for (String label : labels) {
      assertFalse(window.containsLabel(label));
    }
  }

  private void init(String xml) {
    final Trigger trigger = new MorphographRunnerTrigger(xml);
    setAdapter(new UISpecAdapter() {
      private Window window;

      public Window getMainWindow() {
        if (window == null) {
          window = WindowInterceptor.run(trigger);
        }
        return window;
      }
    });
    window = getMainWindow();
    topicCombo = window.getComboBox("topic");
    seriesCombo = window.getComboBox("series");
  }

  private static class MorphographRunnerTrigger implements Trigger {
    private String content;

    public MorphographRunnerTrigger(String content) {
      this.content = content;
    }

    public void run() throws Exception {
      Morphograph.run(new StringReader(content));
    }
  }

}
