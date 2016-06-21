# TODO List

## Gradle

- Add repositories for the JAR files in lib folder when possible

## Using the new tools

- Make a 'material' application icon and place it into `mipmaps` folders
- Upgrade min SDK version to 15 (covers most devices nowadays)

## Fix new SDK features warnings

- See warnings on the `application` tag inside the `AndroidManifest.xml` file
- HttpClient is deprecated as of SDK 23. It should be replaced with code using `URLConnection` or
  a library which allows replacing it.