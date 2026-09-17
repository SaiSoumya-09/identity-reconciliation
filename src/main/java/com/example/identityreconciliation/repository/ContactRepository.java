package com.example.identityreconciliation.repository;

import com.example.identityreconciliation.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, Long>{//so that sql ops are performed automatically
    List<Contact> findByEmailOrPhoneNumber(
            String email,
            String phoneNumber
    );
    List<Contact> findByLinkedId(Long linkedId);
    List<Contact> findByEmail(String email);
    List<Contact> findByPhoneNumber(String phoneNumber);
}
