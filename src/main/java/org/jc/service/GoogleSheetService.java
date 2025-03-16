package org.jc.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import lombok.extern.slf4j.Slf4j;
import org.jc.model.InputValue;
import org.jc.model.Item;
import org.jc.model.RegistrationData;
import org.jc.model.Ticket;
import org.jc.model.google.sheet.Row;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.jc.util.GoogleSheetUtil.convertToList;

@Service
@Slf4j
public class GoogleSheetService {

    private static final String APPLICATION_NAME = "Google Sheets API Demo";
    private static final String SPREADSHEET_ID = "1Kw-GEb3SSuESFq0GRC3S9KPYRD4a08qtsMe3eePjltI";
    private static final String SHEET_NAME = "Sheet1";
    private static final String JSON_CREDENTIALS_FILE = "src/main/resources/dark-balancer-453704-d9-0262e680ed5e.json";

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();


    public String appendData(@RequestBody RegistrationData registrationData) {
        if(nonNull(registrationData) && nonNull(registrationData.getInvoice()) && isNotEmpty(registrationData.getInvoice().getItems())) {
            registrationData.getInvoice().getItems().forEach(item -> {
                Optional<Ticket> optionalTicket = getTicket(registrationData.getTickets(), item.getId());
                optionalTicket.ifPresent(ticket -> {
                    Row row = createRow(registrationData, ticket, item);
                    List<List<Object>> body = convertToList(row);
                    ValueRange input = new ValueRange().setValues(body);
                    try {
                        getSheets().spreadsheets().values()
                                .append(SPREADSHEET_ID, SHEET_NAME, input)
                                .setValueInputOption("RAW")
                                .setInsertDataOption("INSERT_ROWS")
                                .execute();
                    } catch (IOException | GeneralSecurityException e) {
                        throw new RuntimeException(e);
                    }
                });
            });
        }
        return "Filtered nested data appended successfully!";
    }

    private Sheets getSheets() throws IOException, GeneralSecurityException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = GoogleCredential.fromStream(new FileInputStream(JSON_CREDENTIALS_FILE))
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));
        return new Sheets.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    protected Optional<Ticket> getTicket(List<Ticket> tickets, String itemId) {
        return tickets.stream()
                .filter(ticket1 -> ticket1.getTicketDefinitionId().equals(itemId))
                .findFirst();
    }

    protected Row createRow(RegistrationData registrationData, Ticket ticket, Item item) {
        Row row = new Row();

        row.setTicketRevenue(getRevenue(registrationData));
        row.setPaymentStatus(registrationData.getStatus());
        row.setOrderNumber(registrationData.getOrderNumber());
        row.setOrderDate(registrationData.getCreated());

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

    private String getTotalTicketPrice(Item item) {
        if (nonNull(item.getTotal())) {
            return item.getTotal().getAmount();
        }
        return null;
    }

    private String getTax(Item item) {
        if(nonNull(item.getTax())) {
            return item.getTax().getAmount();
        }
        return null;
    }

    private String getPrice(Item item) {
        if (nonNull(item.getPrice())) {
            return item.getPrice().getAmount();
        }
        return null;
    }

    private String getRevenue(RegistrationData registrationData) {
        if(nonNull(registrationData.getInvoice().getRevenue())) {
            return registrationData.getInvoice().getRevenue().getAmount();
        }
        return null;
    }

    private String getWixServiceFee(Item item) {
        if (isNotEmpty(item.getFees())) {
            return item.getFees().stream()
                    .filter(fee -> "WIX_FEE".equalsIgnoreCase(fee.getName()))
                    .findFirst()
                    .map(fee -> fee.getAmount().getAmount())
                    .orElse(null);
        }
        return null;
    }

    private String getCoupon(Item item) {
        if(nonNull(item.getDiscount()) &&
                isNotEmpty(item.getDiscount().getDiscounts()) &&
                item.getDiscount().getDiscounts().stream().findFirst().isPresent() &&
                nonNull(item.getDiscount().getDiscounts().stream().findFirst().get().getCoupon())) {
            return item.getDiscount().getDiscounts().stream().findFirst().get().getCoupon().getCode();
        }
        return null;
    }

    private Optional<InputValue> getValue(List<InputValue> inputValues, String inputName) {
        return inputValues.stream()
                .filter(inputValue -> inputValue.getInputName().equals(inputName))
                .findFirst();
    }
}