<splits>
  <column opaque="false" background="background">

    <label text="${exo.name}" foreground="exo.title" marginBottom="5" marginTop="0"/>
    <label text="${exo.title}" foreground="exo.title" marginBottom="5" marginTop="0" font="Arial,bold,24"/>
    <label text="${exo.description!}" foreground="exo.title" marginBottom="5" marginTop="0"/>
    [#if exo.example??]
    <label text="Exemple : ${exo.example}" foreground="exo.title" marginBottom="5" marginTop="0"/>
    [/#if]

    <styledPanel topColor="exo.top" bottomColor="exo.bottom" opaque="false" marginTop="15"
                 borderWidth="1" borderColor="exo.border" cornerRadius="10">
      <column margin="15">
        <grid>
          [#list exo.getQuestions() as question]
          <label text="${question.getTitle()}" anchor="east" marginLeft="10" marginTop="5" marginBottom="5"
                 gridPos="(0, ${question_index})"/>
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
    </styledPanel>

    <label text="${exo.comment!}" foreground="exo.title" marginBottom="5" marginTop="20"/>

  </column>
</splits>