# Identity Reconciliation

A Spring Boot application that identifies and reconciles customer contacts using email and phone number.

## Features

- Creates a primary contact when no matching contact exists
- Creates secondary contacts when new information is provided
- Reconciles contacts using email and phone number
- Merges multiple identities while keeping the oldest contact as primary
- Returns consolidated contact information

## Technologies Used

- Java
- Spring Boot
- Spring Data JPA
- MySQL
- Maven
- Postman
- IntelliJ IDEA

## Database Setup

Create the database in MySQL:
```sql
CREATE DATABASE identity_db;
