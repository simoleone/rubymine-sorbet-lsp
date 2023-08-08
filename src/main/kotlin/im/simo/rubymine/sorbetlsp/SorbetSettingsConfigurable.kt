package im.simo.rubymine.sorbetlsp

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.platform.lsp.api.LspServerManager
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.selected

class SorbetSettingsConfigurable(private val project: Project) : BoundConfigurable("Sorbet LSP") {
  override fun createPanel(): DialogPanel {
    val sorbetConfigProperties = SorbetConfigProperties(PropertiesComponent.getInstance(project))
    lateinit var pluginEnabledCheckbox: Cell<JBCheckBox>;

    return panel {
      row {
        pluginEnabledCheckbox = checkBox("Enable Sorbet LSP").bindSelected(sorbetConfigProperties::pluginEnabled)
      }
      row {
        checkBox("Enable Goto Definition").bindSelected(sorbetConfigProperties::gotoDefinitionEnabled)
          .enabledIf(pluginEnabledCheckbox.selected)
          .comment("Disabled by default. Often breaks built-in go to definition.")
      }

      onApply { LspServerManager.getInstance(project).stopAndRestartIfNeeded(SorbetServerSupportProvider::class.java) }
    }
  }
}