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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.jc.transformer.JsonTransformer.createRow;
import static org.jc.util.GoogleSheetUtil.convertToList;

@Service
@Slf4j
public class GoogleSheetService {

    @Value("${google.privateKey.json}")
    private String privateKeyJson;


    private static final String APPLICATION_NAME = "Google Sheets API Demo";
    private static final String SPREADSHEET_ID = "1Kw-GEb3SSuESFq0GRC3S9KPYRD4a08qtsMe3eePjltI";
    private static final String SHEET_NAME = "Sheet1";

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

    protected Optional<Ticket> getTicket(List<Ticket> tickets, String itemId) {
        return tickets.stream()
                .filter(ticket1 -> ticket1.getTicketDefinitionId().equals(itemId))
                .findFirst();
    }

    private Sheets getSheets() throws IOException, GeneralSecurityException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        InputStream inputStream = new ByteArrayInputStream(privateKeyJson.getBytes());
        Credential credential = GoogleCredential.fromStream(inputStream)
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));
        return new Sheets.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}