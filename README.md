# Tiny Ledger

## Introduction

The purpose of this project is to implement an API for a ledger with the
following operations:

* Deposit
* Withdraw
* View Balance
* View History

This application is developed as an exercise, so it was made as simple as possible
to meet the exercise requirements.

The API receives the account ID of an account as key for every call.

## Tech Stack

The project was implemented with SpringBoot only, so it's not necessary
to install anything besides a JVM to run this project.

It's needed Java 17+ to run this application

## Requirements

1. It must not withdraw an amount if account doesn't have enough funds
2. It must not accept a non-existent account ID
3. It must not accept a non-positive amount (zero or negative)

## Usage

1. Clone this repository
2. Run `cd <REPO_PATH>/tiny-ledger`
3. Run `./gradlew bootRun` (if Windows run .\gradlew.bat bootRun)
4. You can send API requests using tools such as curl, Postman, etc

To run unit tests:
`./gradlew test`

### Examples

PS: There is an account with ID 1 already created hardcoded. See _Assumptions_ section

#### Deposit

Request:

`curl -H "Content-Type: application/json" --request POST --data '{"amount": "10.0"}' http://localhost:8080/accounts/1/deposit`

Response:

`{"id":"66de0b21-e522-4951-9f00-36f17c23fdef","creation":"2026-09-25T16:48:56.812196","accountId":"1","amount":10.0,"type":"CREDIT","balanceBefore":0,"balanceAfter":10.0}`

#### Withdraw

Request:

`curl -H "Content-Type: application/json" --request POST --data '{"amount": "5.0"}' http://localhost:8080/accounts/1/withdraw`

Response:

`{"id":"5ec79c74-6ca5-4142-a6c4-fbb3898f260c","creation":"2026-09-25T16:51:06.841102","accountId":"1","amount":5.0,"type":"DEBIT","balanceBefore":10.0,"balanceAfter":5.0}`

#### View Balance

Request:

`curl http://localhost:8080/accounts/1/balance`

Response:

`{"balanceAt":"2026-09-25T16:52:28.325771","accountId":"1","balance":5.0}`

#### View History

Request:

`curl http://localhost:8080/accounts/1/history`

Response:

`[{"id":"66de0b21-e522-4951-9f00-36f17c23fdef","creation":"2026-09-25T16:48:56.812196","accountId":"1","amount":10.0,"type":"CREDIT","balanceBefore":0,"balanceAfter":10.0},{"id":"5ec79c74-6ca5-4142-a6c4-fbb3898f260c","creation":"2026-09-25T16:51:06.841102","accountId":"1","amount":5.0,"type":"DEBIT","balanceBefore":10.0,"balanceAfter":5.0}]`

#### Invalid amount

Request:

`curl -H "Content-Type: application/json" --request POST --data '{"amount": "0.0"}' http://localhost:8080/accounts/1/deposit`

Response:

HTTP 400: `{"reason":"Invalid amount: 0.0"}`

PS: Same error happens in _withdraw_ operation

#### Account not found

`curl -H "Content-Type: application/json" --request POST --data '{"amount": "10.0"}' http://localhost:8080/accounts/2/deposit`

Response:

HTTP 404: `{"reason":"Account not found for accountId: 2"}`

PS: Similar error happens in all operations, if account not found

#### Insufficient Funds

Request:

`curl -H "Content-Type: application/json" --request POST --data '{"amount": "100000.0"}' http://localhost:8080/accounts/1/withdraw`

Response:

HTTP 400: `{"reason":"Insufficient funds for accountId: 1"}`

PS: In this example, account had less than $100000 in its balance

## Assumptions and Simplifications

* The application doesn't have an API to manage accounts, so there is an account being added
hardcoded in `com.dbflabs.finance.ledger.repository.LedgerRepository`. The account has the ID `1`.
However, there is a validation of existence of the account IDs, so sending another account ID will cause and error.
* The transactions are not stored in a Database, but in in-memory data structures.
There are 2 Hashmaps, one to store the account objects, and another to store the Transaction per account ID.
* Concurrency was not handled for the sake of simplicity, so the project is not thread safe,
so if multiple deposits/withdraws happen concurrently, the final result is unpredictable.
* There is no pagination/limit implemented in "View history" operation, which could
cause "Out of memory" errors in real/live environments because of potentially huge quantity of 
transactions for a single account.
* There is no authentication/authorization implementation, which is a must-have for real applications.
Without that, a system can potentially leak user data, and be subject to attacks easily.
* There is no rate limiting implemented in the APIs, which could let system exposed to DoS attacks and bad usage
