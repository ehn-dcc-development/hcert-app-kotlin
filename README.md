# Electronic Health Certificate Verification App Android

Scan a QR Code from <https://dgc.a-sit.at/ehn> and verify the contents of the HCERT structure.

## Dependencies

To pull the dependency of `hcert-kotlin` (<https://github.com/ehn-dcc-development/hcert-kotlin>), create a personal access token (read <https://docs.github.com/en/packages/guides/configuring-gradle-for-use-with-github-packages>), and add `gpr.user` and `gpr.key` in your `~/.gradle/gradle.properties`. Alternatively, install the dependency locally with `./gradlew publishToMavenLocal` in the directory `hcert-kotlin`.

## Libraries

This library uses the following dependencies:
 - [Android](https://android.googlesource.com/), under the Apache-2.0 License
 - [AndroidX](https://github.com/androidx/androidx), under the Apache-2.0 License
 - [Android Material Components](https://github.com/material-components/material-components-android), under the Apache-2.0 License
 - [Kotlin](https://github.com/JetBrains/kotlin), under the Apache-2.0 License
 - [ZXing Android Embedded](https://github.com/journeyapps/zxing-android-embedded), under the Apache-2.0 License
 - [JUnit](https://github.com/junit-team/junit5), under the Eclipse Public License v2.0
