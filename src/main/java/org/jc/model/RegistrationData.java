
package org.jc.model;

import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;

@Data
public class RegistrationData {

    public String orderNumber;
    public ZonedDateTime timestamp;
    public String status;
    public List<Ticket> tickets;
    public Invoice invoice;
}
