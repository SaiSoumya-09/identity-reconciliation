package com.example.identityreconciliation.controller;

import com.example.identityreconciliation.dto.IdentifyRequest;
import com.example.identityreconciliation.dto.IdentifyResponse;
import com.example.identityreconciliation.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class ContactController {

    @Autowired
    private ContactService contactService;

    @PostMapping("/identify")
    public IdentifyResponse identify(@RequestBody IdentifyRequest request) {

        return contactService.identify(request);
    }
}
