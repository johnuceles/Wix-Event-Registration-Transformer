
package org.jc.model;

import lombok.Data;

import java.util.List;

@Data
public class Invoice {

    public List<Item> items;
    public Discount discount;
    public Object tax;
    public SubTotal subTotal;
    public GrandTotal grandTotal;
    public List<Fee> fees;
    public Revenue revenue;
}