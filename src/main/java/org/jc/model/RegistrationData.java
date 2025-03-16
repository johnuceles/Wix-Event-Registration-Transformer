
package org.jc.model;

import lombok.Data;

import java.util.List;

@Data
public class RegistrationData {

    public String orderNumber;
    public String created;
    public String status;
    public List<Ticket> tickets;
    public Invoice invoice;
}
