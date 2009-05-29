<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:f="http://www.w3.org/2004/09/fresnel-tree"
  xmlns="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="f"
  version="1.0">
 <xsl:output method="xml" encoding="iso-8859-1" indent="yes" doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"/>
 <xsl:template match="/">
  <xsl:apply-templates/>
 </xsl:template>
 <xsl:template match="f:results">
  <html xml:lang="en-US" lang="en-US">
   <head>
    <title>Fresnel Output</title>
    <link rel="stylesheet" type="text/css" href="style.css"/>
   </head>
   <body>
    <xsl:apply-templates select="f:resource"/>
   </body>
  </html>
 </xsl:template>
 <xsl:template match="f:resource">
  <div class="f-resource">
   <div class="f-label"><xsl:value-of select="@label"/></div>
   <xsl:apply-templates select="f:property"/>
  </div>
 </xsl:template>
 <xsl:template match="f:property">
  <div class="f-property">
   <div class="f-label"><xsl:value-of select="@label"/></div>
   <xsl:apply-templates select="f:value"/>
  </div>
 </xsl:template>
 <xsl:template match="f:value">
  <div class="f-value">
   <div class="f-label"><xsl:value-of select="@label"/></div>
   <xsl:apply-templates select="f:resource"/>
  </div>
 </xsl:template>
</xsl:stylesheet>
