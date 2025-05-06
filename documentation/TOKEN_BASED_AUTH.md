<!-- TOC -->

* [Overview](#overview)

<!-- TOC -->

# Overview

Token based authentication is an alternative to credentials auth where you use a set of tokens to
grant user access to the SDK features.

# Usage

To login a user using access/refresh tokens instead of credentials, use `LoginRequest.WithToken`
when calling `VpnAccount.login(...)`:

```kotlin

// Create the login request
val loginRequest = LoginRequest.WithToken(
    accessToken = "<valid_access_token>",
    refreshToekn = "<valid_refresh_token>"
)

// Call login
val loginResponse = vpnAccount.login(loginRequest).firstOrNull()

// Evaluate the response
when (loginResponse) {

    LoginResponse.Success -> // Successfully logged in with tokens

        EmptyAccessToken -> // Access token string is blank

        EmptyRefreshToken -> // Refresh token string is blank

        InvalidAccessToken -> // The access token is not valid

        InvalidRefreshToken -> // The refresh token is not valid

    // other responses
    // ...
}
```

## Obtaining the tokens

This depends of your use case, please reach to your account manager to find the best solution.

If you want to test this feature, you can use the tokens returned in the credentials login call and
feed them to the token login.

# Limitations

## Handling Expired Access Tokens

The SDK includes a refresh token mechanism that allows the access token to be renewed before it
expires. However, if the token has already expired due to prolonged inactivity
(the user hasn't used the for too long), the client application must re-authenticate the user.

In summary:

- The SDK does **not** automatically refresh expired tokens.
- When an access token expires, the client application is responsible for obtaining a new one by
  re-authenticating the user.

