# Detected API mirror failure

## Basic mirror flow

![Mirrors flow](./assets/mirrors%20flow.png)

## How does mirror error reporting works?

The SDK is designed to query every available mirror in the list before returning
the last HTTP error and mapped to the appropriate failure.

The library doesn't treat mirror failures as isolated events; instead, the query process operates
without any awareness of the specific mirrors being used. Failures are categorized based on errors
encountered during the API call itself, rather than being associated with any particular mirror. 
This approach allows for a more streamlined handling of errors, focusing on the calling process
instead of the specific endpoint.

The exception is when the endpoint returns a HTTP 401 (Unauthorized) or HTTP 429(Too many requests) 
errors; the mirror querying is skipped to avoid service abuse.

## Relevant failures

You have to consider that every failure related to an API call error could be sent by either the
last mirror or the base endpoint, receiving one of these is an indication one failed:

* **fetchGeoLocation()**
    > UnableToFetchGeoLocation
    > 
    > ServiceError
    > 
    > RefreshTokenServiceError

* **updateServers()**
    > ServiceError
    > 
    > UnableToUpdateServers
    > 
    > RefreshTokenServiceError

* **connectToVpn(...)**
    > ConnectToIKEv2Failure
    >
    > ConnectToOpenVpnFailure
    >
    > ConnectToWireGuardFailure
    >
    > ServiceFailure
    >
    > UnableToRefreshToken
    >
    > RefreshTokenServiceError


* **prepareThreatProtection()**
    > UnableToPrepareThreatProtection
    > 
    > RefreshTokenServiceError

* **login(...)**
    > UnableToLogin
    > 
    > ServiceError

* **logout(...)**
    > UnableToLogout

* **refreshToken()**
    > UnableToRefreshToken
    >   
    > ServiceError

* **getCollectionMetadata()**
    > UnableToGetCollectionMetadata
    > 
    > ServiceFailure
    > 
    > RefreshTokenServiceError

* **clearCollectionMetadata()**
    > UnableToClearCollectionMetadata
    > 
    > ServiceFailure
    > 
    > RefreshTokenServiceError

* **putCollectionMetadata(...)**
    > UnableToPutCollectionMetadata
    > 
    > ServiceFailure
    > 
    > RefreshTokenServiceError

* **importLegacyUserData()**
    > UnableToImportLegacyDataFailure
   > 
    > ServiceError



## Sample use cases

Assuming we have a list of a base host and a list of 3 mirrors:

[https://basehost.com, https://mirror1.com, https://mirror2.com, https://mirror2.com]

### Scenario 1

<img src="./assets/mirrors%20flow%20-%20Mirrors%201.png" alt="drawing" width="540"/>

### Scenario 2

<img src="./assets/mirrors%20flow%20-%20Mirrors%202.png" alt="drawing" width="540"/>

### Scenario 3

<img src="./assets/mirrors%20flow%20-%20Mirrors%203.png" alt="drawing" width="540"/>

### Scenario 4

<img src="./assets/mirrors%20flow%20-%20Mirrors%204.png" alt="drawing" width="540"/>