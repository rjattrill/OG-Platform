<#escape x as x?html>
<@page title="Securities">


<#-- SECTION Add security -->
<@section title="Add security">
  <@form method="POST" action="${uris.securities()}">
  <p>
    <#if err_typeMissing??><div class="err">The type must be entered</div></#if>
    <@rowin label="Type"><input type="text" size="30" maxlength="80" name="type" value="" /></@rowin>
    <#if err_nameMissing??><div class="err">The name must be entered</div></#if>
    <@rowin label="Name"><input type="text" size="30" maxlength="80" name="name" value="" /></@rowin>
    <#if err_idschemeMissing??><div class="err">The scheme type must be entered</div></#if>
    <@rowin label="Scheme type"><input type="text" size="30" maxlength="80" name="idscheme" value="" /></@rowin>
    <#if err_idvalueMissing??><div class="err">The scheme id must be entered</div></#if>
    <@rowin label="Scheme id"><input type="text" size="30" maxlength="80" name="idvalue" value="" /></@rowin>
    <@rowin><input type="submit" value="Add" /></@rowin>
  </p>
  </@form>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.securities()}">Security search</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
