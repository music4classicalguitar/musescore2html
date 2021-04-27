<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="2.0">
	
	<xsl:output method="html" encoding="UTF-8"/>
	
	<!--<xsl:template match="text()[not(normalize-space())]"/>-->
	
	<xsl:variable name="translations">
		<translations>
			<translation>
				<value_en>NAME</value_en>
				<value_nl>NAAM</value_nl>
			</translation>
			<translation>
				<value_en>SYNOPSIS</value_en>
				<value_nl>KORTE_INHOUD</value_nl>
			</translation>
			<translation>
				<value_en>DESCRIPTION</value_en>
				<value_nl>OMSCHRIJVING</value_nl>
			</translation>
			<translation>
				<value_en>IMPLEMENTATION_NOTES</value_en>
				<value_nl>OPMERKINGEN_T.A.V._DE_IMPLEMENTATIE</value_nl>
			</translation>
			<translation>
				<value_en>LICENSE</value_en>
				<value_nl>LICENTIE</value_nl>
			</translation>
			<translation>
				<value_en>FILES</value_en>
				<value_nl>BESTANDEN</value_nl>
			</translation>
			<translation>
				<value_en>EXAMPLES</value_en>
				<value_nl>VOORBEELDEN</value_nl>
			</translation>
			<translation>
				<value_en>ERRORS</value_en>
				<value_nl>FOUTEN</value_nl>
			</translation>
			<translation>
				<value_en>DIAGNOSTICS</value_en>
				<value_nl>DIAGNOSTIEK</value_nl>
			</translation>
		</translations>
	</xsl:variable>
	
	<xsl:template name="translate">
		<xsl:param name="value"/>
		<xsl:choose>
			<xsl:when test="$translations/translations/translation[value_nl=$value]">
				<xsl:value-of select="$translations/translations/translation[value_nl=$value]/value_en[1]"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$value"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="@id">
		<xsl:attribute name="id">
			<xsl:call-template name="translate">
				<xsl:with-param name="value" select="."/>
			</xsl:call-template>
		</xsl:attribute>
	</xsl:template>
	
	<xsl:template match="@href">
		<xsl:attribute name="href">
			<xsl:value-of select="'#'"/>
			<xsl:call-template name="translate">
				<xsl:with-param name="value" select="substring(.,2)"/>
			</xsl:call-template>
		</xsl:attribute>
	</xsl:template>
	
	<xsl:template match="code[@class='Fl' and not(@id)]">
		<xsl:copy>
			<xsl:element name="a">
				<xsl:attribute name="href">
					<xsl:value-of select="'#'"/>
					<xsl:call-template name="translate">
						<xsl:with-param name="value" select="substring(.,2)"/>
					</xsl:call-template>
				</xsl:attribute>
				<xsl:value-of select="."/>
			</xsl:element>
		</xsl:copy>
	</xsl:template>
	
</xsl:stylesheet>