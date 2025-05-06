# ConsumerVPN

This document will describe the theming aspects that can be updated in order
to customize the TV app's theme.

## Current approach

This version of ConsumerVPN is built using [Material3][1] guidelines. And 
[Jetpack Compose][2].

The base theme was generated using the [Material3 Theme builder][3] in combination with
the [Material3 Design Kit][4], the theme builder allows you to choose a base combination
of colors to be used by the components. The Material3 kit file allows a preview of all
the base components available, you can also add extended colors to the theme builder.

Typography can also be generated in the theme and all this configuration can be exported
as `Export -> Jetpack Compose (Theme.kt)`

## Generated Theme files

1. Color.kt
2. Theme.kt
3. Type.kt

### Color.kt file

This file contains the Tokens that will be referenced by the Theme.kt class when creating
the App's theme, by default the Material3 plugin will generate automatic number of schemes
in `dark/light` variants (3 if no extender colors were added or 6 otherwise), for TV we will only use the dark variant.

The basic tokens are name according to Material3, usage on each component can be referenced 
on the [Material3][1] guidelines page.

You can remove any variant not needed in your implementation, this App is only using the 
base implementation with extended colors. We recommend using the suffix `dark` on any
extended color to be easily recognizable. 

Extended colors are named based on purpose so they can be easily understandable.

```kotlin
//Dark scheme tokens example
val primaryDark = Color(0xFFA3C9FE)
val onPrimaryDark = Color(0xFF00315C)
val primaryContainerDark = Color(0xFF1F4876)
//Extended Colors Dark
val colorWindowBackgroundGradientStartEndDark = Color(0xFF141720)
val colorWindowBackgroundGradientCenterDark = Color(0xFF3E414B)
val colorDisconnectButtonDark = Color(0xFFc9352e)
```

### Type.kt file

This class allows you to define a custom Typography to use in the app, when using the
[Material3 Theme builder][3] you can select a Typography to be used by the app, this
will generate the needed cert and the different typographies used by material 3 modifying 
the base font family with the desired one.

Something worth mentioning is that this file is the place were we can customize the 
different Typography properties as needed, size, letter spacing, line height and so on.

### Theme.kt file

This is the most important class for the customization of the app, since here we will be
using the other classes in order to create the app theme, we have some important components
that will be described next

#### Color Schemes

[Material3][1] uses this color schemes as the base for all the base components, if we set
a custom color scheme (we can define multiple depending on the app's need, like a dark/light
scheme if needed) all the base components we create will by default use this set of colors
without any extra configuration needed.

This implementation uses a combination of base(Material3) and extended Color Scheme in dark variants. For the extended schemes an object `ColorFamily` was created to contain
all the custom colors we needed. This object can be extended as needed to add more colors.

The tokens that we added to the Color.kt are referenced here.

#### LocalComposition

We define a localComposition of Colors, this gives a default color scheme to be used by the 
app in case the theme has not been started properly. This app uses the extendedDark color 
scheme by Default

```kotlin
val LocalColors = staticCompositionLocalOf { extendedDark }
```

#### App theme composable function

Then we have the theme setup function.

```kotlin
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = extendedDark
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.scheme.background.toArgb()
            window.decorView.setBackgroundColor(colorScheme.scheme.background.toArgb())
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    CompositionLocalProvider(LocalColors provides colorScheme) {
        MaterialTheme(
            colorScheme = colorScheme.scheme,
            typography = AppTypography,
            content = content
        )
    }
}
```

Here initially the colorScheme is defined, next we have a section that defines the statusBar color, we
use the same background color for the statusBar to give an edge to edge aspect by design, we also set the window background color here.

Lastly we we create a `CompositionLocalProvider` with the selected scheme, in this 
section the MaterialTheme is created, passing the generated colorScheme, typography
and other values that we want to define in the app theme. Dimens and Shapes can also 
be customized here.

## Adding a new color to the theme

If a new color is needed to customize the app this can be done following this simple steps.

1. A color should be added to the `ColorFamily` object that exist in Theme.kt, our recommendation
for naming is to use a purpose specific name.
```kotlin
@Immutable
data class ColorFamily(
    //Previously defined colors are here
    val myNewColor: Color,
)
```
1. This new color now needs to be defined as a token in the `Color.kt` file
```kotlin
//Extended Colors Dark
val myNewColorDark = Color(0xFF141720)
```
1. Now we need to modify the `ExtendedColorScheme` object/s on the `Theme.kt` class
referencing the newly added tokens.
```kotlin
val extendedDark = ExtendedColorScheme(
    extendedColors = ColorFamily(
        //Other extended colors present here
        myNewColor = myNewColorDark
    ),
    scheme = darkScheme
)
```

## Referencing the theme values withing the app.

After finishing setting up the theme this need to be called on the app. This application
is build using compose navigation, so we only use one `MainActivity`, in the onCreate()
method we define a compose navigation graph and inside `setContent` we reference our theme
created in `Theme.kt`.
```kotlin
AppTheme {
    //Everything defined within will use the values passed to our theme
}
```
Since we only use this activity to contain the app NavigationController and the bottom
navigation objects we don't need to instantiate it anywhere else.
Inside a `Composable` object like a screen we can reference the color or typographies 
like...
```kotlin
    LocalColors.current.extendedColors.gradientStartEnd
    MaterialTheme.typography.headlineLarge
    //Material Colors can also be accessed like this (extended colors cannot)
    MaterialTheme.colorScheme.primary
```

## Splash Screen

The splash screen is a custom implementation, currently, TV doesnt support Android 12's Splash screen.

The splash uses `LocalColors.current.scheme.background` as background color and 
`R.drawable.ic_splash_logo` as the central logo, you can change these resources to customize it.


[1]: https://m3.material.io/
[2]: https://developer.android.com/training/tv/playback/compose
[3]: https://www.figma.com/community/plugin/1034969338659738588/material-theme-builder
[4]: https://www.figma.com/community/file/1035203688168086460
[5]: https://developer.android.com/reference/kotlin/androidx/core/splashscreen/SplashScreen