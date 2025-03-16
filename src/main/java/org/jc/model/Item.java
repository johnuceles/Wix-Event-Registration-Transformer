
package org.jc.model;

import lombok.Data;

import java.util.List;

@Data
public class Item {

    public String id;
    public Integer quantity;
    public String name;
    public Price price;
    public Total total;
    public Discount discount;
    public Amount tax;
    public List<Fee> fees;
}