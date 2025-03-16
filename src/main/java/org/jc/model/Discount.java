
package org.jc.model;

import lombok.Data;

import java.util.List;

@Data
public class Discount {

    public Amount amount;
    public Amount afterDiscount;
    public List<Discounts> discounts;
}