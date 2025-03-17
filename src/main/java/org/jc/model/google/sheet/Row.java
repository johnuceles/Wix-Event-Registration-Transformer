package org.jc.model.google.sheet;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class Row {

    String orderNumber;
    String orderDateTime;
    String firstName;
    String lastName;
    String email;
    String ticketType;
    String ticketNumber;
    String ticketPrice;
    String benefit;
    String coupon;
    String tax;
    String totalTicketPrice;
    String wixServiceFee;
    String ticketRevenue;
    String paymentStatus;
    String checkedIn;
    String seatInformation;
    String phoneNumber;
    String parish;
    String attendTheSessionIn;
    String comments;
}