# CU Bus Guide

Champaign-Urbana's unofficial CUMTD Android app!

**[Install on Google Play](https://play.google.com/store/apps/details?id=com.teamparkin.mtdapp)**

*Note:* Version numbers for Android tools, gradle, etc. were hastily increased before dumping the project to GitHub.
Therefore, some of the functionality of this code may be different/broken compared to the version currently on the Play Store. This will be fixed.

## Development

### API Keys

CU Bus Guide uses several APIs.

- [Google Maps Android API v2](https://developers.google.com/maps/documentation/android/)
- [Google Places API](https://developers.google.com/places/)
- [Google Street View Image API](https://developers.google.com/maps/documentation/streetview/)
- [Champaign-Urbana Mass Transit District API](https://developer.cumtd.com/)

You'll need API keys for each of these APIs to develop.
After obtaining your API keys, you'll need to add them to the project.

1. An example keys file is found in [api_keys.xml.example](api_keys.xml.example).
2. Copy this file to 2 locations:
  1. `main/src/debug/res/values/api_keys.xml`
  2. `main/src/main/res/values/api_keys.xml`
3. Replace the sample keys with your API keys in **each** `api_keys.xml` file.

