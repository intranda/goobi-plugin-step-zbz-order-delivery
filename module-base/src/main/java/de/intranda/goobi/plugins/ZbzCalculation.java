package de.intranda.goobi.plugins;

import org.goobi.beans.Process;
import org.goobi.beans.Processproperty;

import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.PropertyManager;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ZbzCalculation {
    private Process process;

    private double invoicePages_units = 0;
    private double invoicePages_price = 0;
    private double invoicePages_total = 0;

    private String invoiceService_label = "Sonstige Dienstleistungen";
    private double invoiceService_units = 0;
    private double invoiceService_price = 0;
    private double invoiceService_total = 0;

    private String invoiceAdditionals_label = "Zusatzaufwände";
    private double invoiceAdditionals_units = 0;
    private double invoiceAdditionals_price = 0;
    private double invoiceAdditionals_total = 0;

    private double invoiceDelivery_units = 0;
    private double invoiceDelivery_price = 0;
    private double invoiceDelivery_total = 0;

    private String invoicePayment_type = "";
    private double invoicePayment_price = 0;
    private double invoicePayment_total = 0;

    private String currency;
    private double total = 0;

    /**
     * Constructor
     *
     * @param process
     */
    public ZbzCalculation(Process process) {
        this.process = process;
        readInvoiceProperties();
    }

    /**
     * read all important invoicing information from properties
     */
    public void readInvoiceProperties() {
        process = ProcessManager.getProcessById(process.getId());
        for (Processproperty prop : process.getEigenschaften()) {

            // pages
            if ("Rechnung Digitalisate Einheiten".equals(prop.getTitel())) {
                invoicePages_units = Double.parseDouble(prop.getWert());
            }
            if ("Rechnung Digitalisate Preis".equals(prop.getTitel())) {
                invoicePages_price = Double.parseDouble(prop.getWert());
            }

            // service
            if ("Rechnung Sonstige Dienstleistungen Label".equals(prop.getTitel())) {
                invoiceService_label = prop.getWert();
            }
            if ("Rechnung Sonstige Dienstleistungen Einheiten".equals(prop.getTitel())) {
                invoiceService_units = Double.parseDouble(prop.getWert());
            }
            if ("Rechnung Sonstige Dienstleistungen Preis".equals(prop.getTitel())) {
                invoiceService_price = Double.parseDouble(prop.getWert());
            }

            // additionals
            if ("Rechnung Zusatzaufwände Label".equals(prop.getTitel())) {
                invoiceAdditionals_label = prop.getWert();
            }
            if ("Rechnung Zusatzaufwände Einheiten".equals(prop.getTitel())) {
                invoiceAdditionals_units = Double.parseDouble(prop.getWert());
            }
            if ("Rechnung Zusatzaufwände Preis".equals(prop.getTitel())) {
                invoiceAdditionals_price = Double.parseDouble(prop.getWert());
            }

            // delivery
            if ("Rechnung Versandkosten Einheiten".equals(prop.getTitel())) {
                invoiceDelivery_units = Double.parseDouble(prop.getWert());
            }
            if ("Rechnung Versandkosten Preis".equals(prop.getTitel())) {
                invoiceDelivery_price = Double.parseDouble(prop.getWert());
            }

            // payment
            if ("Rechnung Zahlungsart".equals(prop.getTitel())) {
                invoicePayment_type = prop.getWert();
            }
            if ("Rechnung Bankspesen".equals(prop.getTitel())) {
                invoicePayment_price = Double.parseDouble(prop.getWert());
            }
        }

        // if the number of pages is still 0 try to count these
        if (invoicePages_units == 0) {
            invoicePages_units = process.getSortHelperImages();
        }

        invoicePages_total = invoicePages_units * invoicePages_price;
        invoiceService_total = invoiceService_units * invoiceService_price;
        invoiceAdditionals_total = invoiceAdditionals_units * invoiceAdditionals_price;
        invoiceDelivery_total = invoiceDelivery_units * invoiceDelivery_price;

        switch (invoicePayment_type) {
            case "Paypal":
                currency = " CHF";
                invoicePayment_price =
                        (invoicePages_total + invoiceService_total + invoiceAdditionals_total + invoiceDelivery_total) * 0.1;
                invoicePayment_total = invoicePayment_price;
                break;

            case "Rechnung EUR":
                currency = " €";
                invoicePayment_price = 0;
                invoicePayment_total = 0;
                break;

            default:
                currency = " CHF";
                invoicePayment_price = 0;
                invoicePayment_total = 0;
                break;
        }

        total = invoicePages_total + invoiceService_total + invoiceAdditionals_total + invoiceDelivery_total + invoicePayment_total;
    }

    /**
     * update all properties with the user entries
     */
    public void update() {
        writeProperty("Rechnung Digitalisate Einheiten", String.valueOf(invoicePages_units));
        writeProperty("Rechnung Digitalisate Preis", String.valueOf(invoicePages_price));
        writeProperty("Rechnung Sonstige Dienstleistungen Label", invoiceService_label);
        writeProperty("Rechnung Sonstige Dienstleistungen Einheiten", String.valueOf(invoiceService_units));
        writeProperty("Rechnung Sonstige Dienstleistungen Preis", String.valueOf(invoiceService_price));
        writeProperty("Rechnung Zusatzaufwände Label", invoiceAdditionals_label);
        writeProperty("Rechnung Zusatzaufwände Einheiten", String.valueOf(invoiceAdditionals_units));
        writeProperty("Rechnung Zusatzaufwände Preis", String.valueOf(invoiceAdditionals_price));
        writeProperty("Rechnung Versandkosten Einheiten", String.valueOf(invoiceDelivery_units));
        writeProperty("Rechnung Versandkosten Preis", String.valueOf(invoiceDelivery_price));
        writeProperty("Rechnung Zahlungsart", invoicePayment_type);
        writeProperty("Rechnung Bankspesen", String.valueOf(invoicePayment_price));

        readInvoiceProperties();
    }

    /**
     * create or update a property
     * 
     * @param name
     * @param value
     */
    private void writeProperty(String name, String value) {

        // update existing property if available
        for (Processproperty prop : process.getEigenschaften()) {
            if (name.equals(prop.getTitel())) {
                prop.setWert(value);
                PropertyManager.saveProcessProperty(prop);
                return;
            }
        }

        // create a new property as it is not there yet
        Processproperty pp = new Processproperty();
        pp.setTitel(name);
        pp.setWert(value);
        pp.setProzess(process);
        PropertyManager.saveProcessProperty(pp);
    }
}
