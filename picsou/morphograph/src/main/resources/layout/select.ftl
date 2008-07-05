<splits xmlns="http://www.globsframework.org/xml/splits.xsd">

  <styles>
    <ui name="exoPanel" class="org.globsframework.gui.splits.components.StyledPanelUI"
      topColor="exo.top" bottomColor="exo.bottom"
      borderWidth="1" borderColor="exo.border" cornerRadius="10"/>
  </styles>

 <column opaque="false" background="background">

    <label text="${exo.name}" foreground="exo.title" marginBottom="5" marginTop="0"/>
    <label text="${exo.title}" foreground="exo.title" marginBottom="5" marginTop="0" font="Arial,bold,24"/>
    <label text="${exo.description!}" foreground="exo.title" marginBottom="5" marginTop="0"/>
    [#if exo.example??]
    <label text="Exemple : ${exo.example!}" foreground="exo.title" marginBottom="5" marginTop="0"/>
    [/#if]

    <panel ui="exoPanel" opaque="false" marginTop="15">
      <column margin="10" opaque="false">
        <grid>
          [#list exo.getQuestions() as question]
          [#assign y=question_index]
          <label text="${question.getTitle()}" anchor="east" marginLeft="10"
                 gridPos="(0, ${y})"/>
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

  </column>
</splits>