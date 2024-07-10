<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" version="1.1" exclude-result-prefixes="fo">
	<xsl:output method="xml" indent="yes"/>
	<xsl:template match="/">
		<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
			
			<!-- LAYOUT -->
			<fo:layout-master-set>
				<fo:simple-page-master master-name="A4" page-width="21cm" page-height="29.7cm" margin-left="2cm" margin-top="2cm" margin-right="2cm">
					<fo:region-body font-size="9pt" />
				</fo:simple-page-master>
			</fo:layout-master-set>
			<!-- // LAYOUT -->
			
			<!-- CONTENT -->
			<fo:page-sequence master-reference="A4">
				<fo:flow flow-name="xsl-region-body" font-family="georgia, opensans, unicode" font-size="9pt" >
					
					<!-- LETTER HEAD -->
					<fo:block-container position="fixed" left="14cm" top="0.2cm">
						<fo:block>
							<fo:external-graphic src="letterhead.png" content-width="57mm"/>
						</fo:block>
					</fo:block-container>
					<!-- LETTER HEAD -->

					<!-- LOGOS 
					<fo:block-container position="fixed" left="2cm" top="28cm">
						<fo:block>
							<fo:external-graphic src="logo.png" content-width="22mm"/>
						</fo:block>
					</fo:block-container>
					<fo:block-container position="fixed" left="17cm" top="28.6cm">
						<fo:block font-size="7pt">
							https://goobi.io
						</fo:block>
					</fo:block-container>
					-->
					<!-- LOGOS -->
					
					<!-- HEADER -->
					<fo:block text-align="left" font-weight="bold" font-size="14pt" margin-top="25pt" margin-bottom="30pt">
				         <xsl:text>Rechnung Digitalisierungszentrum</xsl:text>
					</fo:block>
					<!-- // HEADER -->

					<!-- JOB INFORMATION -->
				
					<fo:table line-height="11pt" table-layout="fixed">
						<fo:table-column column-width="3cm"/>
						<fo:table-column column-width="15cm"/>
						<fo:table-body>
							
							<!-- CATALOGUE ID -->
							<fo:table-row>
								<fo:table-cell>
									<fo:block>Bestellnummer:</fo:block>
								</fo:table-cell>
								<fo:table-cell>
									<fo:block>
										<xsl:value-of select="//metadatalist/metadata[@name = 'CatalogIDDigital']" />
									</fo:block>
								</fo:table-cell>
							</fo:table-row>
							<!-- // CATALOGUE ID -->

							<!-- PROCESS DATE -->
							<fo:table-row>
								<fo:table-cell>
									<fo:block>Datum:</fo:block>
								</fo:table-cell>
								<fo:table-cell>
									<fo:block>
										<xsl:value-of select="//processDate" />
									</fo:block>
								</fo:table-cell>
							</fo:table-row>
							<!-- // PROCESS DATE -->
							
							<!-- PROCESS ID 
							<fo:table-row>
								<fo:table-cell>
									<fo:block>Interne Vorgangs-ID:</fo:block>
								</fo:table-cell>
								<fo:table-cell>
									<fo:block>
										<xsl:value-of select="//processId" />
									</fo:block>
								</fo:table-cell>
							</fo:table-row>
							-->
							<!-- // PROCESS ID -->
							
							<!-- PROCESS TITLE 
							<fo:table-row>
								<fo:table-cell>
									<fo:block>Interner Vorgangstitel:</fo:block>
								</fo:table-cell>
								<fo:table-cell>
									<fo:block>
										<xsl:value-of select="//processTitle" />
									</fo:block>
								</fo:table-cell>
							</fo:table-row>
							-->
							<!-- // PROCESS TITLE -->
						
						</fo:table-body>
					</fo:table>
					<!-- // JOB INFORMATION -->

					<!-- HORIZONTAL SEPARATOR -->
					<fo:block border-bottom="1pt solid #cccccc" margin="30pt 0 0 0"/>

					<!-- CONTACT INFORMATION -->
					<fo:block margin-top="15pt" margin-bottom="10pt" font-weight="bold" font-size="10pt">
						<xsl:text>Kontaktdaten</xsl:text>
					</fo:block>

					<fo:table line-height="12pt" table-layout="fixed">
						<fo:table-column column-width="50%"/>
						<fo:table-column column-width="50%"/>
						<fo:table-body>
							<fo:table-row>

								<!-- LEFT AREA -->
								<fo:table-cell>
									<fo:table line-height="12pt" table-layout="fixed">
										<fo:table-column column-width="3cm"/>
										<fo:table-column column-width="15cm"/>
										<fo:table-body>
											
											<!-- ORDERER ADDRESS -->
											<fo:table-row>
												<fo:table-cell>
													<fo:block>Auftraggeber/in:</fo:block>
												</fo:table-cell>
												<fo:table-cell>
													<fo:block>
														<!-- Call the template to split multiple lines -->
														<xsl:call-template name="splitLines">
															<xsl:with-param name="inputString" select="//properties/property[@name = 'Name und Adresse']" />
														</xsl:call-template>
													</fo:block>
													<fo:block/>
												</fo:table-cell>
											</fo:table-row>
											<!-- // ORDERER ADDRESS -->

											<!-- ORDERER COUNTRY -->
											<fo:table-row>
												<fo:table-cell>
													<fo:block>Land:</fo:block>
												</fo:table-cell>
												<fo:table-cell>
													<fo:block>
														<xsl:value-of select="//properties/property[@name = 'Land']" />
													</fo:block>
												</fo:table-cell>
											</fo:table-row>
											<!-- // ORDERER COUNTRY -->
											
										</fo:table-body>
									</fo:table>
								</fo:table-cell>
								<!-- // LEFT AREA -->

								<!-- RIGHT AREA -->
								<fo:table-cell>
									<fo:table line-height="12pt" table-layout="fixed">
										<fo:table-column column-width="3cm"/>
										<fo:table-column column-width="15cm"/>
										<fo:table-body>
											
											<!-- INVOICE ADDRESS -->
											<xsl:variable name="invoice" select="//properties/property[@name = 'Abweichende Rechnungsadresse']" />
											<xsl:if test="$invoice != ''">
												<fo:table-row>
													<fo:table-cell padding-bottom="9pt">
														<fo:block>Rechnungsadresse:</fo:block>
													</fo:table-cell>
													<fo:table-cell padding-bottom="9pt">
														<fo:block>
															<!-- Call the template to split multiple lines -->
															<xsl:call-template name="splitLines">
																<xsl:with-param name="inputString" select="$invoice" />
															</xsl:call-template>
														</fo:block>
													</fo:table-cell>
												</fo:table-row>
											</xsl:if>
											<!-- // INVOICE ADDRESS -->

											<!-- TAX NUMBER -->
											<xsl:variable name="tax" select="//properties/property[@name = 'Steuernummer']" />
											<xsl:if test="$tax != ''">
												<fo:table-row>
													<fo:table-cell>
														<fo:block>Steuernummer:</fo:block>
													</fo:table-cell>
													<fo:table-cell>
														<fo:block>
															<xsl:value-of select="$tax" />
														</fo:block>
													</fo:table-cell>
												</fo:table-row>
											</xsl:if>
											<!-- // DELIVERY TAX NUMBER -->

											<!-- MAIL -->
											<fo:table-row>
												<fo:table-cell>
													<fo:block>E-Mail:</fo:block>
												</fo:table-cell>
												<fo:table-cell>
													<fo:block>
														<xsl:value-of select="//properties/property[@name = 'E-Mail']" />
													</fo:block>
												</fo:table-cell>
											</fo:table-row>
											<!-- // MAIL -->
											
											<!-- PHONE -->
											<xsl:variable name="phone" select="//properties/property[@name = 'Telefon']" />
											<xsl:if test="$phone != ''">
												<fo:table-row>
													<fo:table-cell>
														<fo:block>Telefon:</fo:block>
													</fo:table-cell>
													<fo:table-cell>
														<fo:block>
															<xsl:value-of select="$phone" />
														</fo:block>
													</fo:table-cell>
												</fo:table-row>
											</xsl:if>
											<!-- // PHONE -->
										</fo:table-body>
									</fo:table>
								</fo:table-cell>
								<!-- //RIGHT AREA -->

							</fo:table-row>

						</fo:table-body>
					</fo:table>
					<!-- // CONTACT INFORMATION -->
					
					<!-- HORIZONTAL SEPARATOR -->
					<fo:block border-bottom="1pt solid #cccccc" margin="15pt 0"/>

					<!-- ORDER CONTENT -->
					<fo:block margin-top="25pt" margin-bottom="10pt" font-weight="bold" font-size="10pt">
						<xsl:text>Umfang der Bestellung</xsl:text>
					</fo:block>
					<fo:table line-height="11pt" table-layout="fixed">
						<fo:table-column column-width="17cm"/>
						
						<fo:table-header background-color="#dddddd" border-width="1pt" border-style="solid">
							<fo:table-row>
								<fo:table-cell padding="3pt" border-width="1pt" border-style="solid" border-color="#bbb">
									<fo:block>Signatur</fo:block>
								</fo:table-cell>
							</fo:table-row>
						</fo:table-header>

						<fo:table-body background-color="#ffffff" border-width="1pt" border-style="solid">
							
							<!-- Call the template to split multiple orders -->
							<xsl:variable name="content" select="//properties/property[@name = 'Umfang der Bestellung']" />
							<xsl:call-template name="splitOrders">
								<xsl:with-param name="inputString" select="$content" />
							</xsl:call-template>

							<!-- Output the value in case it is empty -->
							<xsl:if test="$content = ''">
								<fo:table-row>
									<fo:table-cell padding="3pt" border-width="1pt" border-style="solid" border-color="#bbb">
										<fo:block>
											<xsl:text>- kein Bestellumfang definiert -</xsl:text>
										</fo:block>
									</fo:table-cell>
								</fo:table-row>
							</xsl:if>
						</fo:table-body>
						
					</fo:table>
					<!-- // ORDER CONTENT -->

					<!-- ORDER DETAILS -->
					<fo:block margin-top="25pt" margin-bottom="10pt" font-weight="bold" font-size="10pt">
						<xsl:text>Details</xsl:text>
					</fo:block>
					<fo:table line-height="11pt" table-layout="fixed">
						<fo:table-column column-width="3cm"/>
						<fo:table-column column-width="14cm"/>
						<fo:table-body>
							
							<!-- FORMAT -->
							<fo:table-row>
								<fo:table-cell background-color="#dddddd" padding="3pt" border-width="1pt" border-style="solid" border-color="#bbb">
									<fo:block>Format:</fo:block>
								</fo:table-cell>
								<fo:table-cell padding="3pt" border-width="1pt" border-style="solid" border-color="#bbb">
									<fo:block>
										<xsl:value-of select="//properties/property[@name = 'Dateiformat']" />
									</fo:block>
								</fo:table-cell>
							</fo:table-row>
							<!-- // FORMAT -->
							
							<!-- COLOR -->
							<fo:table-row>
								<fo:table-cell background-color="#dddddd" padding="3pt" border-width="1pt" border-style="solid" border-color="#bbb">
									<fo:block>Farbe:</fo:block>
								</fo:table-cell>
								<fo:table-cell padding="3pt" border-width="1pt" border-style="solid" border-color="#bbb">
									<fo:block>
										<xsl:value-of select="//properties/property[@name = 'Farbe']" />
									</fo:block>
								</fo:table-cell>
							</fo:table-row>
							<!-- // COLOR -->
							
							<!-- QUALITY -->
							<fo:table-row>
								<fo:table-cell background-color="#dddddd" padding="3pt" border-width="1pt" border-style="solid" border-color="#bbb">
									<fo:block>Qualität:</fo:block>
								</fo:table-cell>
								<fo:table-cell padding="3pt" border-width="1pt" border-style="solid" border-color="#bbb">
									<fo:block>
										<xsl:value-of select="//properties/property[@name = 'Digitalisat']" />
									</fo:block>
								</fo:table-cell>
							</fo:table-row>
							<!-- // QUALITY -->

							<!-- PRINT LASER -->
							<fo:table-row>
								<fo:table-cell background-color="#dddddd" padding="3pt" border-width="1pt" border-style="solid" border-color="#bbb">
									<fo:block>Ausdruck Laser:</fo:block>
								</fo:table-cell>
								<fo:table-cell  padding="3pt" border-width="1pt" border-style="solid" border-color="#bbb">
									<fo:block>
										<xsl:value-of select="//properties/property[@name = 'Ausdrucke Laser']" />
									</fo:block>
								</fo:table-cell>
							</fo:table-row>
							<!-- // PRINT LASER -->
							
							<!-- PRINT INK -->
							<fo:table-row>
								<fo:table-cell background-color="#dddddd" padding="3pt" border-width="1pt" border-style="solid" border-color="#bbb">
									<fo:block>Ausdruck Tinte:</fo:block>
								</fo:table-cell>
								<fo:table-cell padding="3pt" border-width="1pt" border-style="solid" border-color="#bbb">
									<fo:block>
										<xsl:value-of select="//properties/property[@name = 'Ausdrucke Tinte']" />
									</fo:block>
								</fo:table-cell>
							</fo:table-row>
							<!-- // PRINT INK -->
							
							<!-- PRINT NEWSPAPER -->
							<fo:table-row>
								<fo:table-cell background-color="#dddddd" padding="3pt" border-width="1pt" border-style="solid" border-color="#bbb">
									<fo:block>Ausdruck Zeitung:</fo:block>
								</fo:table-cell>
								<fo:table-cell padding="3pt" border-width="1pt" border-style="solid" border-color="#bbb">
									<fo:block>
										<xsl:value-of select="//properties/property[@name = 'Ausdrucke Zeitung']" />
									</fo:block>
								</fo:table-cell>
							</fo:table-row>
							<!-- // PRINT NEWSPAPER -->

							<!-- MICROFILM -->
							<fo:table-row>
								<fo:table-cell background-color="#dddddd" padding="3pt" border-width="1pt" border-style="solid" border-color="#bbb">
									<fo:block>Silberfilm-Kopie:</fo:block>
								</fo:table-cell>
								<fo:table-cell padding="3pt" border-width="1pt" border-style="solid" border-color="#bbb">
									<fo:block>
										<fo:instream-foreign-object content-width="10pt" content-height="10pt">
											<svg xmlns="http://www.w3.org/2000/svg"  viewBox="0 0 448 512">
												<!-- checkbox on -->
												<xsl:if test="//properties/property[@name = 'Mikrofilm-Duplikat'] ='Silberfilm-Kopie'">
													<path d="M400 32H48C21.5 32 0 53.5 0 80v352c0 26.5 21.5 48 48 48h352c26.5 0 48-21.5 48-48V80c0-26.5-21.5-48-48-48zm0 400H48V80h352v352zm-35.9-241.7L191.5 361.5c-4.7 4.7-12.3 4.6-17-.1l-90.8-91.5c-4.7-4.7-4.6-12.3 .1-17l22.7-22.5c4.7-4.7 12.3-4.6 17 .1l59.8 60.3 141.4-140.2c4.7-4.7 12.3-4.6 17 .1l22.5 22.7c4.7 4.7 4.6 12.3-.1 17z"/>
												</xsl:if>
												<!-- checkbox off -->
												<xsl:if test="not(//properties/property[@name = 'Mikrofilm-Duplikat'] ='Silberfilm-Kopie')">
													<path d="M400 32H48C21.5 32 0 53.5 0 80v352c0 26.5 21.5 48 48 48h352c26.5 0 48-21.5 48-48V80c0-26.5-21.5-48-48-48zm-6 400H54c-3.3 0-6-2.7-6-6V86c0-3.3 2.7-6 6-6h340c3.3 0 6 2.7 6 6v340c0 3.3-2.7 6-6 6z"/>
												</xsl:if>
											</svg>
										</fo:instream-foreign-object>
									</fo:block>
								</fo:table-cell>
							</fo:table-row>
							<!-- // MICROFILM -->

							<!-- DELIVERY -->
							<fo:table-row>
								<fo:table-cell background-color="#dddddd" padding="3pt" border-width="1pt" border-style="solid" border-color="#bbb">
									<fo:block>Lieferung:</fo:block>
								</fo:table-cell>
								<fo:table-cell padding="3pt" border-width="1pt" border-style="solid" border-color="#bbb">
									<fo:block>
										<xsl:value-of select="//properties/property[@name = 'Lieferart']" />
									</fo:block>
								</fo:table-cell>
							</fo:table-row>
							<!-- // DELIVERY -->
							
						</fo:table-body>
					</fo:table>
					<!-- // ORDER DETAILS -->

					<!-- PRICING -->
					<fo:block margin-top="25pt" margin-bottom="10pt" font-weight="bold" font-size="10pt">
						<xsl:text>Kostenaufstellung</xsl:text>
					</fo:block>

					<fo:table line-height="11pt" table-layout="fixed">
						<fo:table-column column-width="8cm"/>
						<fo:table-column column-width="3cm"/>
						<fo:table-column column-width="3cm"/>
						<fo:table-column column-width="3cm"/>
						
						<fo:table-header background-color="#dddddd" border-width="1pt" border-style="solid">
							<fo:table-row>
								<fo:table-cell padding="3pt" border-width="1pt" border-style="solid" border-color="#bbb">
									<fo:block>Posten</fo:block>
								</fo:table-cell>
								<fo:table-cell padding="3pt" border-width="1pt" border-style="solid" border-color="#bbb">
									<fo:block text-align="right" >Einheiten</fo:block>
								</fo:table-cell>
								<fo:table-cell padding="3pt" border-width="1pt" border-style="solid" border-color="#bbb">
									<fo:block text-align="right" >Preis pro Einheit</fo:block>
								</fo:table-cell>
								<fo:table-cell padding="3pt" border-width="1pt" border-style="solid" border-color="#bbb">
									<fo:block text-align="right" >Summe</fo:block>
								</fo:table-cell>
							</fo:table-row>
						</fo:table-header>
							
						<fo:table-body>

							<!-- PRICE ITEM LIST -->
							<xsl:for-each select="//invoicing/item">
								<fo:table-row>
									<fo:table-cell padding="3pt" border-width="1pt" border-style="solid" border-color="#bbb">
										<fo:block>
											<xsl:value-of select="@label"/>
										</fo:block>
									</fo:table-cell>
									<fo:table-cell text-align="right" padding="3pt" border-width="1pt" border-style="solid" border-color="#bbb">
										<fo:block>
											<xsl:value-of select="@units"/>
										</fo:block>
									</fo:table-cell>
									<fo:table-cell text-align="right" padding="3pt" border-width="1pt" border-style="solid" border-color="#bbb">
										<fo:block>
											<xsl:value-of select="@price"/>
											<xsl:value-of select="//invoicing/payment/@currency"/>
										</fo:block>
									</fo:table-cell>
									<fo:table-cell text-align="right" padding="3pt" border-width="1pt" border-style="solid" border-color="#bbb">
										<fo:block>
											<xsl:value-of select="@sum"/>
											<xsl:value-of select="//invoicing/payment/@currency"/>
										</fo:block>
									</fo:table-cell>
								</fo:table-row>
							</xsl:for-each>
							<!-- // PRICE ITEM LIST -->
							
							<!-- TOTAL -->
							<fo:table-row>
								<fo:table-cell padding="3pt" border-width="1pt" border-style="solid" border-color="#bbb" number-columns-spanned="3" font-weight="bold">
									<fo:block><xsl:value-of select="//invoicing/total/@label"/></fo:block>
								</fo:table-cell>
								<fo:table-cell text-align="right" padding="3pt" border-width="1pt" border-style="solid" border-color="#bbb" font-weight="bold">
									<fo:block><xsl:value-of select="//invoicing/total/@sum"/><xsl:value-of select="//invoicing/payment/@currency"/></fo:block>
								</fo:table-cell>
							</fo:table-row>
							<!-- // TOTAL -->
							
						</fo:table-body>
					</fo:table>
					<!-- // PRICING -->

				</fo:flow>
			</fo:page-sequence>
			<!-- // CONTENT -->

		</fo:root>
	</xsl:template>

	<!-- Template to split multiple lines by linebreak -->
	<xsl:template name="splitLines">
		<!-- Parameters -->
		<xsl:param name="inputString"/>
		
		<!-- Split the input string at semicolons -->
		<xsl:variable name="values" select="substring-before(concat($inputString, '&#xD;&#xA;'), '&#xD;&#xA;')" />
		
		<!-- Output the value -->
		<xsl:if test="$values != ''">
			<fo:block>
				<xsl:value-of select="$values"/>
			</fo:block>
			<!-- Recursive call to process the remaining string -->
			<xsl:call-template name="splitLines">
				<xsl:with-param name="inputString" select="substring-after($inputString, '&#xD;&#xA;')" />
			</xsl:call-template>
		</xsl:if>
	</xsl:template>

	<!-- Template to split multiple orders as table -->
	<xsl:template name="splitOrders">
		<!-- Parameters -->
		<xsl:param name="inputString"/>
		
		<!-- Split the input string at semicolons -->
		<xsl:variable name="values" select="substring-before(concat($inputString, '&#xD;&#xA;'), '&#xD;&#xA;')" />
		
		<!-- Output the value -->
		<xsl:if test="$values != ''">
			<fo:table-row>
				<fo:table-cell padding="3pt" border-width="1pt" border-style="solid" border-color="#bbb">
					<fo:block>
						<xsl:value-of select="$values"/>
					</fo:block>
				</fo:table-cell>
			</fo:table-row>

			<!-- Recursive call to process the remaining string -->
			<xsl:call-template name="splitOrders">
				<xsl:with-param name="inputString" select="substring-after($inputString, '&#xD;&#xA;')" />
			</xsl:call-template>
		</xsl:if>

	</xsl:template>
</xsl:stylesheet>
