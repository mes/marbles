<?xml version="1.0" encoding="utf-8"?>
<!--

  Copyright (c) 2009, MediaEvent Services GmbH & Co. KG
  http://mediaeventservices.com
  
  This file is part of Marbles.

  Marbles is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  Marbles is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with Marbles.  If not, see <http://www.gnu.org/licenses/>.

-->
<!DOCTYPE xsl:stylesheet [ <!ENTITY nbsp "&#160;"> ]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:f="http://www.w3.org/2004/09/fresnel-tree" xmlns="http://www.w3.org/1999/xhtml" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:functx="http://www.functx.com" xmlns:fresnelview="http://beckr.org/fresnelview/" exclude-result-prefixes="f" version="2.0">
	<xsl:output method="xhtml" encoding="utf-8" indent="yes" doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"/>

	<!-- Parameters -->
	<xsl:param name="purpose"/> 
	<xsl:param name="isMobile"/> 
	<xsl:param name="assetsURL"/>
	<xsl:param name="serviceURL"/>
	<xsl:param name="mainResource"/>
	<xsl:param name="sessionParams"/>
	<xsl:param name="errorString"/>
	
	<!-- Debugging elements -->
	<xsl:param name="rdfGraph"/> 
	<xsl:param name="fresnelTree"/> 
		
	<!-- Support functions -->
	<xsl:function name="functx:capitalize-first" as="xs:string?" xmlns:functx="http://www.functx.com">
		<xsl:param name="arg" as="xs:string?"/>
		<xsl:sequence select=" 
   concat(upper-case(substring($arg,1,1)),
             substring($arg,2))
 "/>
	</xsl:function>
	<xsl:function name="functx:escape-for-regex" as="xs:string" xmlns:functx="http://www.functx.com">
		<xsl:param name="arg" as="xs:string?"/>
		<xsl:sequence select=" 
   replace($arg,
           '(\.|\[|\]|\\|\||\-|\^|\$|\?|\*|\+|\{|\}|\(|\))','\\$1')
 "/>
	</xsl:function>
	<xsl:function name="functx:contains-word" as="xs:boolean" xmlns:functx="http://www.functx.com">
		<xsl:param name="arg" as="xs:string?"/>
		<xsl:param name="word" as="xs:string"/>
		<xsl:sequence select=" 
   matches(upper-case($arg),
           concat('^(.*\W)?',
                     upper-case(functx:escape-for-regex($word)),
                     '(\W.*)?$'))
 "/>
	</xsl:function>
	
	<xsl:function name="fresnelview:cut-text" as="xs:string">	  <xsl:param name="inputString" as="xs:string?"/> 	  <xsl:param name="maxLength" as="xs:integer"/> 	  <xsl:sequence select="if (string-length($inputString) &lt;= $maxLength)
	  							then $inputString
	  							else concat(substring($inputString, 1, $maxLength), '...')"/>
	</xsl:function>	

	<xsl:function name="fresnelview:reviewstars">
		<xsl:param name="numstars"/>
		<xsl:param name="color"  />	   <!-- sanity check -->
	   <xsl:if test="$numstars &lt;= 10 and $numstars &gt;= 1">	 		<img src="{$assetsURL}img/star-{$color}.png" class="horizontalimg"/>
	 		<xsl:sequence select="fresnelview:reviewstars($numstars - 1, $color)"/>
	  </xsl:if>	</xsl:function>
	
	<xsl:function name="fresnelview:getHost">
		<xsl:param name="uri"/>
		<!-- 
			From http://www.ietf.org/rfc/rfc2396.txt:
			^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
			Modified to require protocol as XQuery does not support REGEX that match empty strings
		-->
		<xsl:analyze-string select="$uri" regex="^(([^:/?#]+):)(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?">		  <xsl:matching-substring>
		  	<xsl:value-of select="regex-group(4)"/>
		  </xsl:matching-substring>		</xsl:analyze-string>										
	</xsl:function>

	
	<xsl:template match="f:results">
		<html xml:lang="en-US" lang="en-US">
			<head>
				<xsl:choose>
					<xsl:when test="f:resource/f:title">
						<title>Marbles - <xsl:value-of select="f:resource/f:title"/></title>
					</xsl:when>
					<xsl:when test="f:resource/@uri">
						<title>Marbles - <xsl:value-of select="f:resource/@uri"/></title>
					</xsl:when>
					<xsl:otherwise>
						<title>Marbles</title>
					</xsl:otherwise>
				</xsl:choose>
			
				<title>Marbles - <xsl:value-of select="f:resource/f:property/f:values/f:value[@class='title']/f:title" disable-output-escaping="yes"/></title>
				<xsl:if test="$purpose = 'abstractPurpose'">
					<meta name="viewport" content="initial-scale=1.0, width=device-width"/>
				</xsl:if>
				<link rel="stylesheet" type="text/css" href="{$assetsURL}css/fresnelView.css"/>
				<link rel="stylesheet" type="text/css" href="{$assetsURL}css/{$purpose}Style.css"/>
				<xsl:if test="$isMobile='true'">
					<link rel="stylesheet" type="text/css" href="{$assetsURL}css/{$purpose}Style-mobile.css"/>
				</xsl:if>
				<xsl:comment>[if lte IE 6]&gt;
					&lt;link href="<xsl:value-of select="$assetsURL"/>css/<xsl:value-of select="$purpose"/>Style-ie6.css" rel="stylesheet" type="text/css" /&gt;
				&lt;![endif]</xsl:comment>
				<script type="text/javascript" src="{$assetsURL}js/prototype.js"></script>
			    <script>
				    <xsl:comment>
						 var isOperaMobile = (navigator.userAgent.indexOf('Opera 8') != -1 &amp;&amp; navigator.userAgent.indexOf('Windows CE') != -1);
						 var isIPhone = (navigator.userAgent.indexOf('Mobile') != -1 &amp;&amp; navigator.userAgent.indexOf('Safari') != -1);
						 
						 function init() {
						 	setupDimensions();
						 	if (isIPhone) {
							    var headID = document.getElementsByTagName("head")[0];								var cssNode = document.createElement('link');								cssNode.type = 'text/css';								cssNode.rel = 'stylesheet';								cssNode.href = '<xsl:value-of select="$assetsURL"/>css/fresnelView-iPhone.css';								headID.appendChild(cssNode);
						 	}
						 }
						 
				    	 function setupDimensions() {
				    	 	if (isOperaMobile) {
								$('canvas').style.width = (document.viewport.getWidth() - 4) + 'px';
								$('canvas').style.height = (document.viewport.getHeight() - 5) + 'px';
				    	 	}
				    	 }
				    	 
				    	 function clearURL(url) {
				    	 	new Ajax.Updater('clear-'+url, '?do=clear&amp;url='+url, { method: 'get' });
				    	 }
				    //</xsl:comment>
			    </script>			</head>
			<body onload="init()" onresize="setupDimensions()">
				<div id="canvas">
					<xsl:if test="$purpose = 'defaultPurpose'">
						<!-- Top navigation -->
				      	<div id="topnavigation">
				      		<a href="http://marbles.sf.net/">
					            <div id="logo">
									<img src="{$assetsURL}img/marbles.png" alt="Marbles"/>
								</div>
							</a>
				            <form action="{$serviceURL}?{$sessionParams}" method="get">
				              <input name="uri" value="{$mainResource}" id="uri" onfocus="select();" autocapitalize="off" autocorrect="off"/>
				              <input id="submit" type="submit" value="Open"/>
				            </form>
						</div>
					</xsl:if>
					
					<!-- Error String -->
					<xsl:if test="$errorString != ''">
						<div id="error" class="mainbox">
							<h1>Error</h1>
							<p><xsl:value-of select="$errorString"/></p>
						</div>
					</xsl:if>
					
					<!-- Resources -->
					<xsl:apply-templates select="f:resource"/>
					
					<!-- Sources -->
					<div id="sources" class="mainbox">
						<h1>Sources</h1>
						<ul>
							<xsl:apply-templates select="fresnelview:sources"/>
						</ul>
					</div>
					
					<!-- Debugging -->
				    <xsl:if test='$rdfGraph'>
					     <h1>RDF Graph</h1>
					     <pre>
						<xsl:value-of select="$rdfGraph"/>
					     </pre>
				    </xsl:if>
				    <xsl:if test='$fresnelTree'>
					     <h1>Fresnel Tree</h1>
					     <pre>
						<xsl:value-of select="$fresnelTree"/>
					     </pre>
					</xsl:if>
				</div>
			</body>
		</html>
	</xsl:template>
	
	<xsl:template match="f:resource">
		<div class="resource mainbox {@class}">
			<!-- Title -->
			<xsl:choose>
				<xsl:when test="f:title">
					<div class="title">
						<a href="{$serviceURL}?{$sessionParams}&amp;uri={encode-for-uri(@uri)}" target="_blank"><xsl:value-of select="f:title"/></a>
					</div>
				</xsl:when>				
				<xsl:when test="@uri">
					<div class="title">
						<a href="{$serviceURL}?{$sessionParams}&amp;uri={encode-for-uri(@uri)}" target="_blank"><xsl:value-of select="@uri"/></a>
					</div>
				</xsl:when>
			</xsl:choose>

			<!-- Property boxes -->
			<!-- Ignore 'exclude' boxes and only take the first abstract right box -->
			<xsl:variable name="abstractRightBoxes" select="f:property[not(functx:contains-word(@class,'exclude')) and functx:contains-word(@class,'abstractRightBox')]"/>
			<xsl:choose>
				<xsl:when test="$purpose = 'abstractPurpose' and count($abstractRightBoxes)>1">
					<xsl:apply-templates select="f:property[not(functx:contains-word(@class,'exclude')) and (not(functx:contains-word(@class,'abstractRightBox')) or . = $abstractRightBoxes[position() = 1])]"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates select="f:property[not(functx:contains-word(@class,'exclude'))]"/>
				</xsl:otherwise>
			</xsl:choose>
			
			<!-- Links -->
			<xsl:if test="$purpose = 'abstractPurpose' and (not(functx:contains-word(../../../@class,'boxed')))">
				<hr/>
				<a href="{$serviceURL}?{$sessionParams}&amp;purpose=photoPurpose&amp;uri={encode-for-uri(@uri)}" target="_blank">Photos of <xsl:value-of select="f:title" disable-output-escaping="yes"/></a><br/>
				<a href="{$serviceURL}?{$sessionParams}&amp;uri={encode-for-uri(@uri)}" target="_blank">Full View</a>
			</xsl:if>
		</div>
	</xsl:template>
	
	<!-- Properties -->
	<xsl:template match="f:property">
			<div class="property {@class}">
		    	<div class="label {f:label/@class}">
		    		<xsl:if test="@inverse = 'true' and $purpose != 'abstractPurpose'">is&nbsp;</xsl:if>
		    		<xsl:choose>
		    			<!-- try to use fresnel-supplied label -->
			    		<xsl:when test='f:label/f:title != ""'>
		    				<a href="{$serviceURL}?{$sessionParams}&amp;uri={encode-for-uri(@uri)}" title="{@uri}" target="_blank"><xsl:value-of select="f:label/f:title" disable-output-escaping="yes"/></a>
			    		</xsl:when>
			    		<xsl:otherwise>
							<a href="{$serviceURL}?{$sessionParams}&amp;uri={encode-for-uri(@uri)}" title="{@uri}" target="_blank"><xsl:value-of select="@uri"/></a>
			        	</xsl:otherwise>
			        </xsl:choose>
			        <xsl:if test="@inverse = 'true'">&nbsp;of</xsl:if>
		    		<xsl:if test="@inverse != 'true' and $purpose = 'abstractPurpose'">:</xsl:if>
		        </div>
	        	<div class="values">
					<ul>
						<!-- only take the first value if requested -->
						<xsl:choose>
							<xsl:when test="$purpose = 'abstractPurpose' and (functx:contains-word(@class,'singleValueInAbstract'))">
								<xsl:apply-templates select="f:values/f:value[position()=1]"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:apply-templates select="f:values/f:value"/>
							</xsl:otherwise>
						</xsl:choose>
					</ul>
				</div>		
			</div>
	</xsl:template>

	<!-- Values -->
	<xsl:template match="f:value">
		<div class="value {@class}">
			<xsl:choose>
				<!-- nested resource -->
				<xsl:when test="f:resource/f:property">
						<xsl:apply-templates select="f:resource"/>
						<xsl:sequence select="fresnelview:source(f:source)"/>				
				</xsl:when>

				<!-- image output type -->
				<xsl:when test="@output-type = 'image'">
					<a href="{f:resource/@uri}" target="_blank"><img src="{f:resource/@uri}"/></a>
					<xsl:sequence select="fresnelview:source(f:source)"/>				
				</xsl:when>
				
				<!-- link output type -->
				<xsl:when test="@output-type = 'link'">
					<a href="{f:resource/@uri}" target="_blank">
						<xsl:choose>
							<xsl:when test="$purpose = 'abstractPurpose'">
								<xsl:value-of select="fresnelview:cut-text(f:resource/f:title, 50)" disable-output-escaping="yes"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="f:resource/f:title" disable-output-escaping="yes"/>
							</xsl:otherwise>
						</xsl:choose>								
					</a>
					<xsl:sequence select="fresnelview:source(f:source)"/>				
				</xsl:when>

				<!-- special classes - could also target uris (http://purl.org/stuff/rev#rating) -->
				<xsl:when test="@class = 'rating'">
					<xsl:sequence select="fresnelview:reviewstars(f:title, 'yellow')"/>
					<xsl:sequence select="fresnelview:reviewstars(../../../f:property/f:values/f:value[position() = 1 and @class='maxrating']/f:title - f:title, 'grey')"/>
					<xsl:sequence select="fresnelview:source(f:source)"/>				
				</xsl:when>
				
				<!-- handled above -->
				<xsl:when test="@class = 'maxrating'">
				</xsl:when>

		 		<!-- other resource without nested properties -->
				<xsl:when test='f:resource/f:title'>
					<li>
		    			<xsl:choose>
			    			<xsl:when test='f:resource/@uri'>
								<a href="{$serviceURL}?{$sessionParams}&amp;uri={encode-for-uri(f:resource/@uri)}" title="{f:resource/@uri}" target="_blank"><xsl:value-of select="f:resource/f:title" disable-output-escaping="yes"/></a>
								<!-- Alias resources -->
								<xsl:if test="f:alias">
								(also at <xsl:for-each select="f:alias">
										<xsl:if test="position() &gt; 1">,&nbsp;</xsl:if>
										<a href="{$serviceURL}?{$sessionParams}&amp;uri={encode-for-uri(.)}" title="{.}" target="_blank"><xsl:value-of select="fresnelview:getHost(.)" disable-output-escaping="yes"/></a>
									</xsl:for-each>)
								</xsl:if>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="f:resource/f:title" disable-output-escaping="yes"/>
							</xsl:otherwise>
						</xsl:choose>							
						<xsl:sequence select="fresnelview:source(f:source)"/>				
					</li>
				</xsl:when>

				<!-- literal -->
				<xsl:when test='f:title'>
					<li>
						<xsl:apply-templates select="f:title"/>		
						<xsl:sequence select="fresnelview:source(f:source)"/>				
					</li>
				</xsl:when>
			</xsl:choose>
		</div>
	</xsl:template>
	
	<!-- Titles -->
	<xsl:template match="f:title">
		<xsl:choose>
			<xsl:when test="../../../f:content/f:first and position() = 1">
				<xsl:value-of select="../../../f:content/f:first" disable-output-escaping="yes"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="../../../f:content/f:before" disable-output-escaping="yes"/>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:value-of select="." disable-output-escaping="yes"/>
		<xsl:choose>
			<xsl:when test="../../../f:content/f:last and position() = last()">
				<xsl:value-of select="../../../f:content/f:last" disable-output-escaping="yes"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="../../../f:content/f:after" disable-output-escaping="yes"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!-- Sources -->
	<xsl:function name="fresnelview:source">
		<xsl:param name="source" as="element()*"/>
		<xsl:for-each select="$source">
			<a class="source" href="#source-{encode-for-uri(f:sourceURI)}"><img src="{$assetsURL}img/{fresnelview:sourceIcon}.png" title="Source: {f:sourceURI}"/></a>
		</xsl:for-each>
	</xsl:function>	
	
	<xsl:template match="fresnelview:source">
		<li>
			<a name="source-{encode-for-uri(fresnelview:uri)}"/>
			<a href="{fresnelview:uri}" target="_blank">
				<xsl:choose>
					<!-- "off" icon for failed sources -->
					<xsl:when test="(fresnelview:status = 'failed' or fresnelview:status = 'pending') and fresnelview:icon castable as xs:integer">
						<img src="{$assetsURL}img/{fresnelview:icon}-off.png"/>
					</xsl:when>
					<xsl:otherwise>
						<img src="{$assetsURL}img/{fresnelview:icon}.png"/>
					</xsl:otherwise>
				</xsl:choose>
			</a>
			<a href="{fresnelview:uri}" target="_blank"><xsl:value-of select="fresnelview:cut-text(fresnelview:uri, 75)"/></a>
			<span class="sourcedetails">
				&nbsp;<xsl:value-of select="fresnelview:status"/>
				<xsl:if test="fresnelview:responseCode != ''">&nbsp;(<xsl:value-of select="fresnelview:responseCode"/>)</xsl:if>
				<xsl:if test="fresnelview:date != ''">,&nbsp;retrieved <xsl:value-of select="fresnelview:date"/>&nbsp;<span class="clearLink" onclick="clearURL('{encode-for-uri(fresnelview:uri)}')" id="clear-{encode-for-uri(fresnelview:uri)}">(clear)</span></xsl:if>
			</span>
		</li>
	</xsl:template>

</xsl:stylesheet>