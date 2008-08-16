<?xml version="1.0" encoding="utf-8"?>
<splits xmlns="http://www.globsframework.org/xml/splits.xsd">

  <styles>
    <ui name="background" class="org.globsframework.gui.splits.components.StyledPanelUI"
        topColor="background.top" bottomColor="background.bottom"/>
    <ui name="exoPanel" class="org.globsframework.gui.splits.components.StyledPanelUI"
        topColor="exo.top" bottomColor="exo.bottom"
        borderWidth="1" borderColor="exo.border" cornerRadius="10"/>
    <style selector="label.exotitle" foreground="exo.title" marginBottom="5" marginTop="0"
           fill="none" anchor="center"/>
  </styles>

  <column opaque="false" margin="20">

    <label text="${exo.name}" styleClass="exotitle"/>
    <label text="${exo.title}" styleClass="exotitle" font="Arial,bold,30"
           shadowDirection="northwest" shadowColor="exo.title.shadow"/>
    <label text="${exo.description!}" styleClass="exotitle"/>
    [#if exo.example??]
    <label text="Exemple : ${exo.example!}" styleClass="exotitle"/>
    [/#if]

    <panel ui="exoPanel" opaque="false" marginTop="15">
      <column margin="10" opaque="false">
        <grid>
          [#list exo.getQuestions() as question]
          [#assign y=question_index]
          <label text="${question.getTitle()}" anchor="east" marginLeft="10"
                 gridPos="(0, ${y})" font="-,bold,14" foreground="exo.text"
                 shadowDirection="southeast" shadowColor="exo.text.shadow"/>
          [#assign x=0]
          [#list question.getAnswers() as answer]
          [#assign x=x+1]
          <button ref="${answer.getButtonId()}" gridPos="(${x}, ${y})" opaque="false" fill="horizontal"/>
          [/#list]
          [/#list]
        </grid>
      </column>
    </panel>

    <label text="${exo.comment!}" foreground="exo.title" marginBottom="5" marginTop="20"/>

    <filler fill="vertical" weightY="1.0" opaque="true" background="background.bottom"/>

  </column>
</splits>