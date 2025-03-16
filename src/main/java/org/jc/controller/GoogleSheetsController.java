package org.jc.controller;

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
        return googleSheetService.appendData(registrationData);
    }
}