package com.example.identityreconciliation.service;

import com.example.identityreconciliation.dto.IdentifyRequest;
import com.example.identityreconciliation.dto.IdentifyResponse;
import com.example.identityreconciliation.entity.Contact;
import com.example.identityreconciliation.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ContactService {
    @Autowired
    private ContactRepository contactRepository;
    public IdentifyResponse identify(IdentifyRequest request) {//take re from idreq as i/p and response as o/p
        String email = request.getEmail();
        String phoneNumber = request.getPhoneNumber();
        //Find existing conatcts that match the incoming email or  phone number
        List<Contact> matchingContacts = contactRepository.findByEmailOrPhoneNumber(email, phoneNumber);//search db for matching emila or number

        //If no matching contact exists, create a primary contact
        if (matchingContacts.isEmpty()) {
            Contact newContact = new Contact();
            newContact.setEmail(email);
            newContact.setPhoneNumber(phoneNumber);
            newContact.setLinkedId(null);
            newContact.setLinkPrecedence("primary");
            LocalDateTime now = LocalDateTime.now();
            newContact.setCreatedAt(now);
            newContact.setUpdatedAt(now);

            Contact savedContact = contactRepository.save(newContact);
            return buildCompleteResponse(savedContact);
        }
        //Find the primary contacts associated with the mathcing records
        List<Contact> primaryContacts = new ArrayList<>();
        for (Contact contact : matchingContacts) {
            if ("primary".equals(contact.getLinkPrecedence())) {
                primaryContacts.add(contact);
            } else {
                Contact primaryContact = contactRepository.findById(contact.getLinkedId()).orElse(null);
                if (primaryContact != null) {
                    primaryContacts.add(primaryContact);
                }
            }
        }
        primaryContacts = primaryContacts.stream().distinct().toList();
        //Select the oldest primary contact to remain as the primary identity
        Contact primaryContact = primaryContacts.get(0);
        //Convert other primary contacts into secondary contacts underbthe oldest primary
        for (Contact contact : primaryContacts) {
            if (contact.getCreatedAt().isBefore(primaryContact.getCreatedAt())) {
                primaryContact = contact;
            }
        }
        for (Contact contact : primaryContacts) {
            if (!contact.getId().equals(primaryContact.getId())) {
                contact.setLinkedId(primaryContact.getId());
                contact.setLinkPrecedence("secondary");
                contact.setUpdatedAt(LocalDateTime.now());
                contactRepository.save(contact);

                List<Contact> secondaryContacts = contactRepository.findByLinkedId(contact.getId());
                for (Contact sec : secondaryContacts) {
                    sec.setLinkedId(primaryContact.getId());
                    sec.setUpdatedAt(LocalDateTime.now());
                    contactRepository.save(sec);
                }
            }
        }
        //Chck whether the incoming email and phone number are already present
        boolean emailExists = false;
        boolean phoneExists = false;
        for (Contact contact : matchingContacts) {
            if (email != null && email.equals(contact.getEmail())) {
                emailExists = true;
            }
            if (phoneNumber != null && phoneNumber.equals(contact.getPhoneNumber())) {
                phoneExists = true;
            }
        }
        //Create a secondary contact when the request contains new information
        if (!emailExists || !phoneExists) {
            Contact secondaryContact = new Contact();
            secondaryContact.setEmail(email);
            secondaryContact.setPhoneNumber(phoneNumber);
            secondaryContact.setLinkedId(primaryContact.getId());
            secondaryContact.setLinkPrecedence("secondary");

            LocalDateTime now = LocalDateTime.now();
            secondaryContact.setCreatedAt(now);
            secondaryContact.setUpdatedAt(now);

            contactRepository.save(secondaryContact);
        }
        //Return the complete consolidated identity
        return buildCompleteResponse(primaryContact);
    }
    private IdentifyResponse buildCompleteResponse(Contact primaryContact) {

        IdentifyResponse response = new IdentifyResponse();

        response.setPrimaryContactId(primaryContact.getId());

        List<String> emails = new ArrayList<>();
        List<String> phoneNumbers = new ArrayList<>();
        List<Long> secondaryIds = new ArrayList<>();
        if (primaryContact.getEmail() != null) {
            emails.add(primaryContact.getEmail());
        }

        if (primaryContact.getPhoneNumber() != null) {
            phoneNumbers.add(primaryContact.getPhoneNumber());
        }
        List<Contact> secondaryContacts =
                contactRepository.findByLinkedId(primaryContact.getId());

        for (Contact contact : secondaryContacts) {

            if (contact.getEmail() != null &&
                    !emails.contains(contact.getEmail())) {
                emails.add(contact.getEmail());
            }

            if (contact.getPhoneNumber() != null &&
                    !phoneNumbers.contains(contact.getPhoneNumber())) {
                phoneNumbers.add(contact.getPhoneNumber());
            }

            secondaryIds.add(contact.getId());
        }

        response.setEmails(emails);
        response.setPhoneNumbers(phoneNumbers);
        response.setSecondaryContactIds(secondaryIds);

        return response;
    }
}
