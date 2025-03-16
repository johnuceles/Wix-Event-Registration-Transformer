
package org.jc.model;

import lombok.Data;

@Data
public class Ticket {

    public String ticketNumber;
    public Price price;
    public Form form;
    public String ticketDefinitionId;
    public String firstName;
    public String lastName;
    public String checkIn;
}