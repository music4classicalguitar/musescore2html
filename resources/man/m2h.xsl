<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  version="2.0">
  
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="code[@class='Fl' and not(@id)]">
    <xsl:copy>
    	<xsl:element name="a">
    	  <xsl:attribute name="href">
    	    <xsl:value-of select="concat('#',substring-after(.,'-'))"/>
    	  </xsl:attribute>
    	  <xsl:value-of select="."/>
    	</xsl:element>
    </xsl:copy>
  </xsl:template>
  
</xsl:stylesheet>