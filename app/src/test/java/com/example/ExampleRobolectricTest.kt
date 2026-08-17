package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.plugin.PluginManager
import com.example.plugin.sample.QuickSettingsPlugin
import com.example.theme.ThemeManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read app name from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ModuX", appName)
  }

  @Test
  fun `verify plugin registration and toggle`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val pluginManager = PluginManager(context)
    val plugin = QuickSettingsPlugin()

    pluginManager.registerPlugin(plugin)
    val state = pluginManager.getPluginState(plugin.manifest.id)
    assertNotNull(state)
    assertTrue(state!!.isInstalled)
  }

  @Test
  fun `verify default theme selection`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val themeManager = ThemeManager(context)
    val currentTheme = themeManager.currentTheme.value
    assertNotNull(currentTheme)
    assertEquals("Default Dark", currentTheme.name)
  }
}
