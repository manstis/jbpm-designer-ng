<%
  String queryString = request.getQueryString();
  String redirectURL = "org.jbpm.designer.jBPMDesignerNGShowcase/designer.html?" + ( queryString == null ? "" : queryString );
  response.sendRedirect( redirectURL );
%>
