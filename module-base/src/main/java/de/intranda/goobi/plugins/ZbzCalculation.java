package de.intranda.goobi.plugins;

import org.goobi.beans.GoobiProperty;
import org.goobi.beans.GoobiProperty.PropertyOwnerType;
import org.goobi.beans.Process;

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

    private String invoiceAdditionals_label = "Mindestbetrag-Zuschlag";
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
        for (GoobiProperty prop : process.getProperties()) {
            // pages
            if ("Rechnung Digitalisate Einheiten".equals(prop.getPropertyName())) {
                invoicePages_units = Double.parseDouble(prop.getPropertyValue());
            }
            if ("Rechnung Digitalisate Preis".equals(prop.getPropertyName())) {
                invoicePages_price = Double.parseDouble(prop.getPropertyValue());
            }

            // service
            if ("Rechnung Sonstige Dienstleistungen Label".equals(prop.getPropertyName())) {
                invoiceService_label = prop.getPropertyValue();
            }
            if ("Rechnung Sonstige Dienstleistungen Einheiten".equals(prop.getPropertyName())) {
                invoiceService_units = Double.parseDouble(prop.getPropertyValue());
            }
            if ("Rechnung Sonstige Dienstleistungen Preis".equals(prop.getPropertyName())) {
                invoiceService_price = Double.parseDouble(prop.getPropertyValue());
            }

            // additionals
            if ("Rechnung Zusatzaufwände Label".equals(prop.getPropertyName())) {
                invoiceAdditionals_label = prop.getPropertyValue();
            }
            if ("Rechnung Zusatzaufwände Einheiten".equals(prop.getPropertyName())) {
                invoiceAdditionals_units = Double.parseDouble(prop.getPropertyValue());
            }
            if ("Rechnung Zusatzaufwände Preis".equals(prop.getPropertyName())) {
                invoiceAdditionals_price = Double.parseDouble(prop.getPropertyValue());
            }

            // delivery
            if ("Rechnung Versandkosten Einheiten".equals(prop.getPropertyName())) {
                invoiceDelivery_units = Double.parseDouble(prop.getPropertyValue());
            }
            if ("Rechnung Versandkosten Preis".equals(prop.getPropertyName())) {
                invoiceDelivery_price = Double.parseDouble(prop.getPropertyValue());
            }

            // payment
            if ("Rechnung Zahlungsart".equals(prop.getPropertyName())) {
                invoicePayment_type = prop.getPropertyValue();
            }
            if ("Rechnung Bankspesen".equals(prop.getPropertyName())) {
                invoicePayment_price = Double.parseDouble(prop.getPropertyValue());
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
        writeProperty("Rechnung Versandkosten Einheiten", String.valueOf(invoiceDelivery_units));
        writeProperty("Rechnung Versandkosten Preis", String.valueOf(invoiceDelivery_price));
        writeProperty("Rechnung Zahlungsart", invoicePayment_type);
        writeProperty("Rechnung Bankspesen", String.valueOf(invoicePayment_price));

        // calulate the Mindestbetrag-Zuschlag
        double tempSum = invoicePages_units * invoicePages_price;
        if (tempSum < 10) {
            invoiceAdditionals_units = 1;
            invoiceAdditionals_price = 10 - tempSum;
        } else {
            invoiceAdditionals_units = 0;
            invoiceAdditionals_price = 0;
        }
        writeProperty("Rechnung Zusatzaufwände Label", invoiceAdditionals_label);
        writeProperty("Rechnung Zusatzaufwände Einheiten", String.valueOf(invoiceAdditionals_units));
        writeProperty("Rechnung Zusatzaufwände Preis", String.valueOf(invoiceAdditionals_price));

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
        for (GoobiProperty prop : process.getProperties()) {
            if (name.equals(prop.getPropertyName())) {
                prop.setPropertyValue(value);
                PropertyManager.saveProperty(prop);
                return;
            }
        }

        // create a new property as it is not there yet
        GoobiProperty pp = new GoobiProperty(PropertyOwnerType.PROCESS);
        pp.setPropertyName(name);
        pp.setPropertyValue(value);
        pp.setOwner(process);
        PropertyManager.saveProperty(pp);
    }
}
