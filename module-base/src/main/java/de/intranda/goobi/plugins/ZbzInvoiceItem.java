package de.intranda.goobi.plugins;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZbzInvoiceItem {
    private String label;
    private String units;
    private double price;
    private double sum;
}
