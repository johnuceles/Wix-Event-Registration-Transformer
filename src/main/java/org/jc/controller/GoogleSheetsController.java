package org.jc.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.jc.model.RegistrationData;
import org.jc.service.GoogleSheetService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/sheets")
public class GoogleSheetsController {

    private final GoogleSheetService googleSheetService;

    public GoogleSheetsController(GoogleSheetService googleSheetService) {
        this.googleSheetService = googleSheetService;
    }

    @RequestMapping(value = "/append", method = RequestMethod.POST)
    public String appendData(@RequestBody RegistrationData registrationData) {
        logJSON(registrationData);
        return googleSheetService.appendData(registrationData);
    }

    private void logJSON(RegistrationData registrationData) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        try {
            log.info("Registration data: {}", mapper.writeValueAsString(registrationData));
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
    }
}