## General ##

The service exposes a REST API that gives commission with respect to client. 
The amount given in the request is converted form base currency(part of request body) to EUR with available exchange rate.
Following are the rules applicable for commission:
RULE 1 :
  - Commission = 0.5% of amount in EUR, if calculated commission < 5% then round it off to 5%.
RULE 2 :
  - If Client id is 42 then commission is 5%, else Zero.
RULE 3 :
  - If for the given client id, the monthly turnover has reached more than 1000, then commission is 3*, else Zero.

Calculate commission based on Rule applicable :
  - If client id is not 42 and its monthly turnover >= 1000 then RULE 3 applies.
  - if the client id is 42 and
    - its monthly turnover < 1000 then RULE 2 applies.
    - its monthly turnover > 1000 then maximum of RULE 1 and RULE 3 is applied. 

## Technical Design ##

Endpoints : 
    Post : `/transaction`
Database : Postgres
Tests : Unit, Integration



## Setup ##

If you are running a Windows machine, Following are the prerequisites:
  - Install any Linux distro on a VM(WSL2).
  - Install Docker Desktop on Windows for monitoring purpose. If you are using WSL2, enable the Integration with the Distro in Docker settings.
  - Install Docker on VM.
  - Install make on VM 
  - Navigate to the project base location and run `make db app`

## Testing ##

Testing is possible via swagger. On your web browser enter on:
http://localhost:8080/swagger-ui/

Request body example:
{
  "date": "2021-01-01",
  "amount": "100.00",
  "currency": "EUR",
  "client_id": 42
}

If the post request takes longer than a second, Fallback method is called to give a viable response back to the client.

