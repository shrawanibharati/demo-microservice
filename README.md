## General ##

Transaction : The service exposes a REST API that gives an amount converted from given currency to EUR.
If the monthly turnover for the client is more than 1000 EUR then 5% commission is applied on the amount.

House management : the service exposes REST API to perform crud operations house entity.

## Technical Design ##

Transaction Endpoints : 
    Post : `/transaction`

House Endpoints :
    Posy, Get, Get all, Put, Delete : `/house`
    save all : `/house/all`
    sort by amount : `/house/sort?column={column_name}&order={asc/desc}`

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

Transaction Request body example:
{
  "date": "2021-01-01",
  "amount": "100.00",
  "currency": "EUR",
  "client_id": 42
}

House Request body example:
{
"date": "2021-01-01",
"amount": "100.00",
"currency": "EUR"
}

Transaction fallback : If the post request takes longer than 10 second, Fallback method is called to give a viable response back to the client.

