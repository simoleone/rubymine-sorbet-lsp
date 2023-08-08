package im.simo.rubymine.sorbetlsp

import com.intellij.ide.util.PropertiesComponent

class SorbetConfigProperties(private val propertiesComponent: PropertiesComponent) {
  var pluginEnabled: Boolean
    get() = propertiesComponent.getBoolean(PLUGIN_ENABLED_KEY, true)
    set(enabled) {
      propertiesComponent.setValue(PLUGIN_ENABLED_KEY, enabled, true)
    }
  var gotoDefinitionEnabled: Boolean
    get() = propertiesComponent.getBoolean(GOTO_DEFINITION_ENABLED, false)
    set(enabled) {
      propertiesComponent.setValue(GOTO_DEFINITION_ENABLED, enabled, false)
    }

  companion object {
    private val PLUGIN_ENABLED_KEY = SorbetConfigProperties::class.java.getPackageName() + ".enabled"
    private val GOTO_DEFINITION_ENABLED = SorbetConfigProperties::class.java.getPackageName() + ".enableGotoDefinition"
  }
}
