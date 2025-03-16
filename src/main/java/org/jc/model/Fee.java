
package org.jc.model;

import lombok.Data;

@Data
public class Fee {

    public String name;
    public String type;
    public String rate;
    public Amount amount;
}