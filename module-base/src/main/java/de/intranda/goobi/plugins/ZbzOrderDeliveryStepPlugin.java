package de.intranda.goobi.plugins;

/**
 * This file is part of a plugin for Goobi - a Workflow tool for the support of mass digitization.
 *
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopConfParser;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.xmlgraphics.util.MimeConstants;
import org.goobi.beans.Process;
import org.goobi.beans.Processproperty;
import org.goobi.beans.Step;
import org.goobi.production.enums.LogType;
import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.PluginReturnValue;
import org.goobi.production.enums.PluginType;
import org.goobi.production.enums.StepReturnValue;
import org.goobi.production.plugin.interfaces.IStepPluginVersion2;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.VariableReplacer;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.UGHException;

@PluginImplementation
@Log4j2
public class ZbzOrderDeliveryStepPlugin implements IStepPluginVersion2 {

    @Getter
    private String title = "intranda_step_zbz_order_delivery";
    @Getter
    private Step step;
    @Getter
    private String returnPath;
    private SubnodeConfiguration config;
    private Process p;
    private transient Fileformat ff;
    private transient VariableReplacer replacer;
    @Getter
    private ZbzCalculation calculation;
    @Getter
    private String currency;
    private boolean paymentFromEurope;

    @Override
    public void initialize(Step step, String returnPath) {
        this.returnPath = returnPath;
        this.step = step;
        config = ConfigPlugins.getProjectAndStepConfig(title, step);

        // Open the metadata file for the process and prepare the VariableReplacer
        try {
            p = step.getProzess();
            ff = p.readMetadataFile();
            replacer = new VariableReplacer(ff.getDigitalDocument(), p.getRegelsatz().getPreferences(), p, null);
            replacer.setSeparator(System.lineSeparator());
        } catch (UGHException | IOException | SwapException e) {
            log.error("Error while executing the zbz delivery plugin", e);
            Helper.addMessageToProcessJournal(getStep().getProcessId(), LogType.ERROR,
                    "An error happend during the zbz delivery: " + e.getMessage());
        }

        calculation = new ZbzCalculation(step.getProzess());
        calculateCurrency();
        calculatePaymentFromEurope();
        log.info("ZbzOrderDelivery step plugin initialized");
    }

    @Override
    public PluginGuiType getPluginGuiType() {
        return PluginGuiType.PART;
    }

    @Override
    public String getPagePath() {
        return "/uii/plugin_step_zbz_order_delivery.xhtml";
    }

    @Override
    public PluginType getType() {
        return PluginType.Step;
    }

    @Override
    public String cancel() {
        return "/uii" + returnPath;
    }

    @Override
    public String finish() {
        return "/uii" + returnPath;
    }

    @Override
    public int getInterfaceVersion() {
        return 0;
    }

    @Override
    public HashMap<String, StepReturnValue> validate() {
        return null;
    }

    public void updateAndPreview() {
        // first update the data
        calculation.update();

        // write to servlet output stream
        try {
            // prepare the output stream
            FacesContext facesContext = FacesContextHelper.getCurrentFacesContext();
            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
            ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
            String contentType = servletContext.getMimeType("preview.pdf");
            response.setContentType(contentType);
            response.setHeader("Content-Disposition", "attachment;filename=\"preview.pdf\"");

            // generate the pdf
            generatePdf(response.getOutputStream());
            facesContext.responseComplete();

        } catch (IOException | PreferencesException | SwapException | DAOException e) {
            log.error("Exception while generating the invoice preview", e);
        }

    }

    @Override
    public boolean execute() {
        PluginReturnValue ret = run();
        return ret != PluginReturnValue.ERROR;
    }

    @Override
    public PluginReturnValue run() {
        boolean successful = true;
        try {
            // get information for target folder
            String resultFolder = p.getConfiguredImageFolder(config.getString("resultFolder", "delivery"));
            Path resultFolderPath = Paths.get(resultFolder);
            if (!StorageProvider.getInstance().isFileExists(resultFolderPath)) {
                StorageProvider.getInstance().createDirectories(resultFolderPath);
            }

            // define FileOutputStream for target file
            File resultFile = new File(resultFolder, config.getString("resultFile", "delivery.pdf"));
            FileOutputStream outStream = new FileOutputStream(resultFile);
            generatePdf(outStream);
            outStream.close();

        } catch (UGHException | IOException | SwapException | DAOException e) {
            log.error("Error while executing the zbz delivery plugin", e);
            Helper.addMessageToProcessJournal(getStep().getProcessId(), LogType.ERROR,
                    "An error happend during the zbz delivery: " + e.getMessage());
        }
        log.debug("ZbzOrderDelivery step plugin executed");
        if (!successful) {
            return PluginReturnValue.ERROR;
        }
        log.debug("ZbzOrderDelivery step plugin did run successfully");
        return PluginReturnValue.FINISH;
    }

    /**
     * do the actual pdf generation to the given stream
     *
     * @throws PreferencesException
     * @throws IOException
     * @throws SwapException
     * @throws DAOException
     * @throws FileNotFoundException
     */
    private void generatePdf(OutputStream outStream) throws PreferencesException, IOException, SwapException, DAOException, FileNotFoundException {
        // create an xml document to allow xslt transformation afterwards
        Document doc = createXmlDocumentOfContent(ff);

        // if debug mode is switched on write that xml file into Goobi temp folder
        if (config.getBoolean("debugMode", false)) {
            writeDocumentToFile(doc, "delivery_in.xml");
        }

        // get xml as output stream
        XMLOutputter outp = new XMLOutputter();
        outp.setFormat(Format.getPrettyFormat());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        outp.output(doc, out);
        out.close();

        // prepare files for xslt transformation
        String xsltFile = ConfigurationHelper.getInstance().getXsltFolder() + config.getString("xslt", "delivery.xslt");
        //            File resultFile = new File(ConfigurationHelper.getInstance().getTemporaryFolder(), "delivery.pdf");

        Path xsltfile = Paths.get(xsltFile);
        if (!StorageProvider.getInstance().isFileExists(xsltfile)) {
            throw new IOException("Error while executing the zbz delivery plugin. XSLT file not found: " + xsltfile.toString());
        }

        // prepare streams for xslt transformation
        StreamSource source = new StreamSource(new ByteArrayInputStream(out.toByteArray()));
        StreamSource transformSource = new StreamSource(xsltfile.toFile().getAbsolutePath());
        FopConfParser parser = this.initializeFopConfParser();
        if (parser == null) {
            throw new IOException("Error while executing the zbz delivery plugin. Parser could not be created.");
        }

        //build the fop factory with the user options
        FopFactoryBuilder builder = parser.getFopFactoryBuilder();
        FopFactory fopFactory = builder.build();

        // execute xslt transformation
        try {
            Transformer xslfoTransformer;
            FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
            foUserAgent.setTargetResolution(300);
            Fop fop;
            xslfoTransformer = getTransformer(transformSource);
            fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, outStream);
            Result res = new SAXResult(fop.getDefaultHandler());
            xslfoTransformer.transform(source, res);
            outStream.flush();
        } catch (FOPException e) {
            throw new IOException("FOPException occured", e);
        } catch (TransformerException e) {
            throw new IOException("TransformerException occured", e);
        }
    }

    /**
     * create an XML Document of all contentfields
     * 
     * @param contentFields
     * @return
     * @throws PreferencesException
     */
    private Document createXmlDocumentOfContent(Fileformat ff) throws PreferencesException {
        Element mainElement = new Element("goobi");
        Document doc = new Document(mainElement);

        // generally add the process id
        Element e1 = new Element("processId");
        e1.setText(String.valueOf(p.getId()));
        mainElement.addContent(e1);

        // generally add the process title
        Element e2 = new Element("processTitle");
        e2.setText(p.getTitel());
        mainElement.addContent(e2);

        // generally add the process creation date
        Element e3 = new Element("processDate");
        e3.setText(p.getErstellungsdatumAsString());
        mainElement.addContent(e3);

        // generally add the number of images for the process
        Element e4 = new Element("processFiles");
        e4.setText(String.valueOf(String.valueOf(p.getSortHelperImages())));
        mainElement.addContent(e4);

        // add all properties
        Element pe = new Element("properties");
        mainElement.addContent(pe);
        for (Processproperty prop : p.getEigenschaften()) {
            Element e = new Element("property");
            e.setAttribute("name", prop.getTitel());
            e.setText(prop.getWert());
            pe.addContent(e);
        }

        // add all metadata
        Element me = new Element("metadatalist");
        mainElement.addContent(me);
        for (Metadata m : ff.getDigitalDocument().getLogicalDocStruct().getAllMetadata()) {
            Element e = new Element("metadata");
            e.setAttribute("name", m.getType().getName());
            e.setText(m.getValue());
            me.addContent(e);
        }

        // calculate everything
        List<ZbzInvoiceItem> calcs = getInvoicing();
        DecimalFormat df = new DecimalFormat("0.00");

        // add all calculations
        Element ce = new Element("invoicing");
        mainElement.addContent(ce);
        for (ZbzInvoiceItem item : calcs) {
            Element e = new Element("item");
            e.setAttribute("label", item.getLabel());
            e.setAttribute("units", item.getUnits());
            e.setAttribute("price", df.format(item.getPrice()));
            e.setAttribute("sum", df.format(item.getSum()));
            ce.addContent(e);
        }

        // add a total price
        Element sum = new Element("total");
        sum.setAttribute("label", "Gesamt");
        sum.setAttribute("sum", df.format(calculation.getTotal()));
        ce.addContent(sum);

        // add payment details
        Element cur = new Element("payment");
        cur.setAttribute("currency", currency);
        ce.addContent(cur);

        return doc;
    }

    /**
     * get currency based on the question where the delivery is send to
     *
     * @return
     */
    private void calculateCurrency() {
        String country = replacer.replace("{process.Land}").toLowerCase();

        switch (country) {
            case "schweiz":
            case "switzerland":
                currency = " CHF";

            case "usa":
            case "amerika":
                currency = " $";

            default:
                currency = " €";
        }
    }

    /**
     * get information if payment comes out of europe
     *
     * @return
     */
    private void calculatePaymentFromEurope() {
        String country = replacer.replace("{process.Land}").toLowerCase();

        switch (country) {
            case "deutschland":
            case "niederlande":
            case "österreich":
            case "schweiz":
                paymentFromEurope = true;

            default:
                paymentFromEurope = false;
        }
    }

    //    /**
    //     * get pricing for delivery based on delivery price method
    //     *
    //     * @return
    //     */
    //    private ZbzInvoiceItem getDeliveryCosts() {
    //        String delivery = replacer.replace("{process.Lieferart}").toLowerCase();
    //
    //        if ("post".equals(delivery)) {
    //            double priceDelivery = 7;
    //            return new ZbzInvoiceItem("Lieferung per Post", "pauschal", priceDelivery, priceDelivery);
    //        }
    //
    //        if ("cd/dvd".equals(delivery)) {
    //            double priceDelivery = 25;
    //            return new ZbzInvoiceItem("Lieferung als CD / DVD", "pauschal", priceDelivery, priceDelivery);
    //        }
    //
    //        return null;
    //    }

    /**
     * calculate the entire pricing to generate an invoice
     * 
     * @return
     */
    private List<ZbzInvoiceItem> getInvoicing() {

        List<ZbzInvoiceItem> calcs = new ArrayList<ZbzInvoiceItem>();

        // pages
        calcs.add(new ZbzInvoiceItem("Erstellung der Digitalisate", doubleToString(calculation.getInvoicePages_units()),
                calculation.getInvoicePages_price(),
                calculation.getInvoicePages_total()));

        // Service
        if (calculation.getInvoiceService_total() > 0) {
            calcs.add(new ZbzInvoiceItem("Sonstige Dienstleistungen", doubleToString(calculation.getInvoiceService_units()),
                    calculation.getInvoiceService_price(),
                    calculation.getInvoiceService_total()));
        }

        // additionals
        if (calculation.getInvoiceAdditionals_total() > 0) {
            calcs.add(new ZbzInvoiceItem("Zusatzaufwände", doubleToString(calculation.getInvoiceAdditionals_units()),
                    calculation.getInvoiceAdditionals_price(),
                    calculation.getInvoiceAdditionals_total()));
        }

        // delivery
        if (calculation.getInvoiceDelivery_total() > 0) {
            calcs.add(new ZbzInvoiceItem("Versandkosten", doubleToString(calculation.getInvoiceDelivery_units()),
                    calculation.getInvoiceDelivery_price(),
                    calculation.getInvoiceDelivery_total()));
        }

        // bank
        if (calculation.getInvoicePayment_total() > 0) {
            calcs.add(new ZbzInvoiceItem("Bankspesen (" + calculation.getInvoicePayment_type() + ")", "10%",
                    calculation.getInvoicePayment_price(),
                    calculation.getInvoicePayment_total()));
        }

        return calcs;
    }

    /**
     * write xml document into the file system
     *
     * @param doc
     * @param filename
     * @throws IOException
     * @throws FileNotFoundException
     */
    private void writeDocumentToFile(Document doc, String filename) throws IOException {
        XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
        File f = new File(ConfigurationHelper.getInstance().getTemporaryFolder(), filename);
        try (FileOutputStream fileOutputStream = new FileOutputStream(f)) {
            xmlOutputter.output(doc, fileOutputStream);
        }
    }

    /**
     * Initializes and returns the FOP parser. The config.xml file from goobi/xslt is used to setup the parser.
     *
     * @return The configured parser
     */
    private FopConfParser initializeFopConfParser() {
        File xconf = new File(ConfigurationHelper.getInstance().getXsltFolder() + "config.xml");
        try {
            //parsing configuration
            return new FopConfParser(xconf);
        } catch (Exception e) {
            log.error(e);
            return null;
        }
    }

    /**
     * internal method to get a transformer object
     * 
     * @param streamSource
     * @return
     */
    private static Transformer getTransformer(StreamSource streamSource) {
        // setup the xslt transformer
        net.sf.saxon.TransformerFactoryImpl impl = new net.sf.saxon.TransformerFactoryImpl();
        try {
            return impl.newTransformer(streamSource);
        } catch (TransformerConfigurationException exception) {
            log.error(exception);
        }
        return null;
    }

    /**
     * convert a double to String and only use digits if needed
     *
     * @param d
     * @return
     */
    private String doubleToString(double d) {
        if (d == (long) d) {
            return String.format("%d", (long) d);
        } else {
            DecimalFormat df = new DecimalFormat("0.00");
            return df.format(d);
        }
    }
}
