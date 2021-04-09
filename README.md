# Electronic Health Certificate Verification App Android

Scan a QR Code from <https://dev.a-sit.at/certservice> and verify the contents of the HCERT structure.

Note: Other contents can not be verified, since the certificate is loaded from <https://dev.a-sit.at/certservice> for now.

A prebuilt version of this App is published at <https://dev.a-sit.at/android/app-debug.apk> and can be installed on any Android device running Android 7.0 or later.

## Dependencies

To pull the dependency of `hcert-kotlin` (<https://github.com/ehn-digital-green-development/hcert-kotlin>), create a personal access token (read <https://docs.github.com/en/packages/guides/configuring-gradle-for-use-with-github-packages>), and add `gpr.user` and `gpr.key` in your `~/.gradle/gradle.properties`. Alternatively, install the dependency locally with `./gradlew publishToMavenLocal` in the directory `hcert-kotlin`.

