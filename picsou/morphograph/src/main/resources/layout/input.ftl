<?xml version="1.0" encoding="utf-8"?>
<splits xmlns="http://www.globsframework.org/xml/splits.xsd">

  <styles>
    <ui name="styled" class="org.globsframework.gui.splits.components.StyledPanelUI"
      topColor="exo.top" bottomColor="exo.bottom" borderWidth="1" borderColor="exo.border" cornerRadius="10"/>
  </styles>

  <column opaque="false" margin="20">

    <label text="${exo.name}" foreground="exo.title" marginBottom="5" marginTop="0"/>
    <label text="${exo.title}" foreground="exo.title" marginBottom="5" marginTop="0" font="Arial,bold,24"
           shadowDirection="northwest" shadowColor="exo.title.shadow"/>
    <label text="${exo.description!}" foreground="exo.title" marginBottom="5" marginTop="0"/>
    [#if exo.example??]
    <label text="Exemple : ${exo.example}" foreground="exo.title" marginBottom="5" marginTop="0"/>
    [/#if]

    <panel ui="styled" opaque="false" marginTop="15">
      <column margin="15">
        <grid>
          [#list exo.getQuestions() as question]
          <label text="${question.getTitle()}" anchor="east" marginLeft="10" marginTop="5" marginBottom="5"
                 gridPos="(0, ${question_index})" foreground="exo.text" font="-,bold,14"
                 shadowDirection="southeast" shadowColor="exo.text.shadow"/>
          <textField name="${question.getTextFieldId()}"
          [#if question.answer??]
          action="${question.getActionId()}"
          [/#if]
          gridPos="(1, ${question_index})"/>
          [#if question.answer??]
          <button text="Valider" action="${question.getActionId()}"
                  gridPos="(2, ${question_index})" opaque="false"/>
          [/#if]
          [/#list]
        </grid>
      </column>
    </panel>

    <label text="${exo.comment!}" foreground="exo.title" marginBottom="5" marginTop="20"/>

    <filler fill="vertical" weightY="1.0" opaque="true" background="background.bottom"/>
    
  </column>
</splits>