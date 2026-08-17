package com.example

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.theme.ThemeDefinition
import com.example.ui.theme.ModuxTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val sampleTheme = ThemeDefinition(
      id = "test_dark",
      name = "Default Dark",
      author = "ModuX Core",
      description = "Test theme",
      isDark = true,
      backgroundHex = "#0D1117",
      surfaceHex = "#161B22",
      cardHex = "#21262D",
      textPrimaryHex = "#F0F6FC",
      textSecondaryHex = "#8B949E",
      accentHex = "#6366F1",
      borderHex = "#30363D"
    )

    composeTestRule.setContent {
      ModuxTheme(themeDefinition = sampleTheme) {
        Text("ModuX Customization Hub")
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
