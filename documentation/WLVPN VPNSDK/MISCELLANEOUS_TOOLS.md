# Miscellaneous Tools

## Metadata

### Overview

The SDK allows customers store custom user metadata in the backend.


### Manipulating metadata

The interface `VpnAccount` exposes methods that allow:

- Get metadata, through the method: `VpnAccount.getCollectionMetadata()`
- Put metadata, through the method: `VpnAccount.putCollectionMetadata(metadata)`
- Clear metadata, through the method: `VpnAccount.clearCollectionMetadata()`

### Limitations

- As of 4th of December of 2023, it’s not possible to add more than 5 keys to the account metadata.
- The maximum size for the key is 64
- The maximum size of the value is 1024

### Sample code

#### Getting metadata

```kotlin
viewModelScope.launch(Dispatchers.IO) {
    vpnAccount.getCollectionMetadata().collectLatest {

        when (it) {
            is VpnAccount.GetCollectionMetadataResponse.Success -> {
                Timber.d("Metadata obtained: $it")
            }

            is VpnAccount.GetCollectionMetadataResponse.ServiceFailure -> {
                Timber.e("Could not obtain metadata, the service response contains an error: ${it.code} ${it.reason}")
            }

            is VpnAccount.GetCollectionMetadataResponse.UnableToGetCollectionMetadata -> {
                Timber.e(it.throwable, "Could not obtain metadata, exception received.")
            }
        }
    }
}
```

#### Putting metadata

```kotlin
viewModelScope.launch(Dispatchers.IO) {
    vpnAccount.putCollectionMetadata(AccountMetadata( mapOf("key1" to "value1"))).collectLatest {

        when (it) {
            is VpnAccount.PutCollectionMetadataResponse.Success -> {
                Timber.d("Metadata set correctly.")
            }
    
            is VpnAccount.PutCollectionMetadataResponse.ServiceFailure -> {
                Timber.e("Could not set metadata, the service response contains an error: ${it.code} ${it.reason}")
            }
    
            is VpnAccount.PutCollectionMetadataResponse.UnableToPutCollectionMetadata -> {
                Timber.e(it.throwable , "Could not set metadata, exception received.")
            }
        }
    }
}

```


#### Clearing metadata

```kotlin
viewModelScope.launch(Dispatchers.IO) {
    vpnAccount.clearCollectionMetadata().collectLatest {

        when (it) {
            is VpnAccount.ClearCollectionMetadataResponse.Success -> {
                Timber.d("Metadata cleared correctly")
            }

            is VpnAccount.ClearCollectionMetadataResponse.ServiceFailure -> {
                Timber.e("Could not clear metadata, the service contains an error: ${it.code} ${it.reason}")
            }

            is VpnAccount.ClearCollectionMetadataResponse.UnableToClearCollectionMetadata -> {
                Timber.e(it.throwable, "Could not obtain metadata, exception received.")
            }
        }
    }
}
```