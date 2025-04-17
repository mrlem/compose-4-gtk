# A Kotlin Compose library for Gtk4 and Adw

![Welcome](https://raw.githubusercontent.com/MMarco94/compose-4-gtk/main/screenshots/welcome.png)

This library provides a Kotlin Compose interface to interact with native Gtk4 and Adw (Libadwaita) components.

TL;DR: with this library, you can declaratively and dynamically create UIs with the GTK and Adw frameworks.

## Usage

This library is still under development, and there's no official release yet.

However, you can add still use this library via JitPack, see https://www.jitpack.io/#MMarco94/compose-4-gtk for
instructions.

The library requires JDK 23 or newer.

For example, on your `build.gradle.kts`:

```kotlin
kotlin {
    jvmToolchain(23)
}

...

dependencies {
    ...
    implementation("com.github.MMarco94:compose-4-gtk:-SNAPSHOT")
    ...
}
```

And your `settings.gradle.kts`:
```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://www.jitpack.io")
    }
}
```

## The basics

An empty window:

```kotlin
fun main(args: Array<String>) {
    application("my.example.HelloApp", args) {
        ApplicationWindow(application, "Test", onClose = ::exitApplication) {
            // Your content here
        }
    }
}
```

----

A window with a header, and three buttons inside:

```kotlin
fun main(args: Array<String>) {
    application("my.example.HelloApp", args) {
        ApplicationWindow(application, "Test", onClose = ::exitApplication) {
            HeaderBar()

            Button("Button 1") { println("Clicked!") }
            Button("Button 2") { println("Clicked!") }
            Button("Button 3") { println("Clicked!") }
        }
    }
}
```

----

An interactive button that shows or hides a label:

```kotlin
fun main(args: Array<String>) {
    application("my.example.HelloApp", args) {
        ApplicationWindow(application, "Test", onClose = ::exitApplication) {
            Box(orientation = Orientation.VERTICAL) {
                HeaderBar()

                var show by remember { mutableStateOf(false) }
                Button(if (show) "Hide" else "Show") {
                    show = !show
                }
                if (show) {
                    Label("A random label that can be hidden")
                }
            }
        }
    }
}
```

To manage the app's state, this library uses the same principles as Kotlin Compose,
see https://developer.android.com/jetpack/compose/state#state-in-composables

----

An `Entry`, that will make all text uppercase:

```kotlin
fun main(args: Array<String>) {
    application("my.example.HelloApp", args) {
        ApplicationWindow(application, "Test", onClose = ::exitApplication) {
            Box(orientation = Orientation.VERTICAL) {
                HeaderBar()

                var text by remember { mutableStateOf("") }
                Entry(
                    text = text,
                    placeholderText = "All text will be uppercase",
                    onTextChange = { text = it.uppercase() },
                )
            }
        }
    }
}
```

This example highlights one of the main properties of Compose:
the state is owned by your app, and not by the GTK Widgets.
This means that the source of truth for what the text should be is the `text` variable.
See https://developer.android.com/jetpack/compose/state#state-hoisting for more details.

In this example, at any point the `Entry` could ever contain a text that is no uppercase.

----

## Example

![Demo](https://raw.githubusercontent.com/MMarco94/compose-4-gtk/main/screenshots/demo.gif)

```kotlin
fun main(args: Array<String>) {
    application("my.example.HelloApp", args) {
        ApplicationWindow(application, "Test", onClose = ::exitApplication) {
            ToastOverlay {
                VerticalBox {
                    HeaderBar()

                    HorizontalClamp {
                        VerticalBox {
                            var text by remember { mutableStateOf("") }
                            Entry(
                                text = text,
                                onTextChange = { text = it },
                                placeholderText = "Inset text here",
                                modifier = Modifier.margin(8)
                            )
                            ScrolledWindow {
                                HorizontalBox {
                                    val tokens = text.split(' ').filter { it.isNotBlank() }
                                    tokens.forEach { token ->
                                        Button(token, modifier = Modifier.margin(8)) {
                                            addToast(Toast.builder().title("Clicked on $token").build())
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
```

