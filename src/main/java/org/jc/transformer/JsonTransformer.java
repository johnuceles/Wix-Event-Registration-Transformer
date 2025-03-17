package org.jc.transformer;

import org.jc.model.InputValue;
import org.jc.model.Item;
import org.jc.model.RegistrationData;
import org.jc.model.Ticket;
import org.jc.model.google.sheet.Row;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

public class JsonTransformer {

    public static Row createRow(RegistrationData registrationData, Ticket ticket, Item item) {
        Row row = new Row();

        row.setTicketRevenue(getRevenue(registrationData));
        row.setPaymentStatus(registrationData.getStatus());
        row.setOrderNumber(registrationData.getOrderNumber());
        row.setOrderDateTime(convertToEST(registrationData.getTimestamp()));

        row.setTicketPrice(getPrice(item));
        row.setWixServiceFee(getWixServiceFee(item));
        row.setCoupon(getCoupon(item));
        row.setTax(getTax(item));
        row.setTotalTicketPrice(getTotalTicketPrice(item));
        row.setTicketType(item.getName());

        row.setTicketNumber(ticket.getTicketNumber());
        row.setCheckedIn(ticket.getCheckIn());

        List<InputValue> formInputValues = ticket.getForm().getInputValues();
        getValue(formInputValues, "firstName").map(InputValue::getValue).ifPresent(row::setFirstName);
        getValue(formInputValues, "lastName").map(InputValue::getValue).ifPresent(row::setLastName);
        getValue(formInputValues, "email").map(InputValue::getValue).ifPresent(row::setEmail);
        getValue(formInputValues, "phone").map(InputValue::getValue).ifPresent(row::setPhoneNumber);
        getValue(formInputValues, "comment").map(InputValue::getValue).ifPresent(row::setComments);
        getValue(formInputValues, "custom-75d7b78ce8c01d6a").map(InputValue::getValue).ifPresent(row::setParish);
        getValue(formInputValues, "custom-b6e48abf549d32bc").map(InputValue::getValue).ifPresent(row::setAttendTheSessionIn);

        return  row;
    }

    private static String getTotalTicketPrice(Item item) {
        if (nonNull(item.getTotal())) {
            return item.getTotal().getAmount();
        }
        return null;
    }

    private static String getTax(Item item) {
        if(nonNull(item.getTax())) {
            return item.getTax().getAmount();
        }
        return null;
    }

    private static String getPrice(Item item) {
        if (nonNull(item.getPrice())) {
            return item.getPrice().getAmount();
        }
        return null;
    }

    private static String getRevenue(RegistrationData registrationData) {
        if(nonNull(registrationData.getInvoice().getRevenue())) {
            return registrationData.getInvoice().getRevenue().getAmount();
        }
        return null;
    }

    private static String getWixServiceFee(Item item) {
        if (isNotEmpty(item.getFees())) {
            return item.getFees().stream()
                    .filter(fee -> "WIX_FEE".equalsIgnoreCase(fee.getName()))
                    .findFirst()
                    .map(fee -> fee.getAmount().getAmount())
                    .orElse(null);
        }
        return null;
    }

    private static String getCoupon(Item item) {
        if(nonNull(item.getDiscount()) &&
                isNotEmpty(item.getDiscount().getDiscounts()) &&
                item.getDiscount().getDiscounts().stream().findFirst().isPresent() &&
                nonNull(item.getDiscount().getDiscounts().stream().findFirst().get().getCoupon())) {
            return item.getDiscount().getDiscounts().stream().findFirst().get().getCoupon().getCode();
        }
        return null;
    }

    private static Optional<InputValue> getValue(List<InputValue> inputValues, String inputName) {
        return inputValues.stream()
                .filter(inputValue -> inputValue.getInputName().equals(inputName))
                .findFirst();
    }

    private static String convertToEST(ZonedDateTime timestamp) {
        ZoneId estZone = ZoneId.of("America/New_York");
        return timestamp.withZoneSameInstant(estZone).toLocalDateTime().toString();
    }
}