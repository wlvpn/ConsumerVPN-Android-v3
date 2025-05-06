# Server update logic
The VPN SDKs are designed to locally execute a load-balancing algorithm to select the best server to connect to.

It’s recommended for the client of the SDK to implement the following server list update mechanisms.

## Upon App Launch within 30 Minutes:

When the user opens the application, the system should automatically check for server updates if it has been more than 30 minutes since the last update. This ensures that users always have access to the most recent server information when initiating a connection.

## Background Updates Every 12 Hours:

Perform background server updates every 12 hours, even if the user hasn't actively opened the app. This proactive approach ensures that the server list is regularly refreshed, aligning with users' potential usage patterns and minimizing the likelihood of presenting a loading screen during active sessions.

## Manual User-Initiated Updates:

Allow the user to update the server list manually. (The update should bypassing the 30 minutes checkup)