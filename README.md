# I18nStrings

I18nStrings is a turn-key Gradle plugin for Android projects, providing comprehensive internationalization and multilingual support. By integrating this easy-to-use plugin, you can instantly enable various features for language localization in your project, making it more accessible to users worldwide.

## Features

- Multilingual translation: I18nStrings supports translating your project's text into multiple languages and allows for incremental translations.
- Translation Quality Assurance: I18nStrings guarantees translation quality through various measures and outputs translation quality reports to help you monitor and improve the localization process.
- Language Switching: I18nStrings provides an in-app solution for switching languages, improving the user experience for international users.
- Beta Language Support: I18nStrings supports Beta language translation and includes disclaimers, providing you with more flexibility in localizing your app.

## Quick Start

Follow these steps to quickly get started with i18nstrings:

1. Add the I18nStrings dependency to your project's root directory build.gradle:
```kotlin
dependencies {
    ...
    classpath("com.appkim.i18nstrings:plugin:0.0.6")
}
```

2. Enable I18nStrings in your main module's build.gradle (usually the app module):
```kotlin
plugins {
    ...
    id("com.appkim.i18nstrings")
}
```

3. Configure I18nStrings in your main module's build.gradle:
```kotlin
i18nStrings {
    release = "zh-rCN, fr, es" // Specify the languages to translate. The translation files will be output to the corresponding values folders.
    beta = "ja, pt" // Specify the languages to translate for beta testing. The translation files will be output to the main project's assets directory by default.
    betaPath = "../out/test" // You can change the output path of the Beta translation files.
    excludeProjects = "mylibrary" // Exclude specific modules from translation (by default, all modules are translated).
}
```

4. Run the task:
    - Command Line: `./gradlew :app:i18nStrings`
    - Android Studio: Go to the right-side gradle window, select app (main module name) -> other -> i18nStrings, and double-click to run.

## License

I18nStrings is licensed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).

Whether you're developing an app for local users or a global audience, I18nStrings makes localization a breeze. Start using I18nStrings today and bring your app to the world!