package im.simo.rubymine.sorbetlsp

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.rootManager
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServerDescriptor
import com.intellij.platform.lsp.api.LspServerSupportProvider
import org.jetbrains.plugins.ruby.gem.RubyGemExecutionContext
import org.jetbrains.plugins.ruby.gem.bundler.BundlerGemInfrastructure
import org.jetbrains.plugins.ruby.gem.util.BundlerUtil
import org.jetbrains.plugins.ruby.ruby.lang.RubyFileType
import org.jetbrains.plugins.ruby.ruby.sdk.RubySdkUtil

@Suppress("UnstableApiUsage")
class SorbetServerSupportProvider : LspServerSupportProvider {
  override fun fileOpened(
    project: Project, file: VirtualFile, serverStarter: LspServerSupportProvider.LspServerStarter
  ) {
    if (file.fileType == RubyFileType.RUBY) {
      val lspServerDescriptor = SorbetLspServerDescriptor.tryCreate(project, file) ?: return
      serverStarter.ensureServerStarted(lspServerDescriptor)
    }
  }
}

@Suppress("UnstableApiUsage")
private class SorbetLspServerDescriptor(
  val sorbetConfigProperties: SorbetConfigProperties,
  val executionContext: RubyGemExecutionContext,
  project: Project,
  roots: Array<out VirtualFile>
) : LspServerDescriptor(
  project, "Sorbet", *roots
) {
  override fun isSupportedFile(file: VirtualFile) = file.fileType == RubyFileType.RUBY

  override val lspGoToDefinitionSupport: Boolean
    get() = sorbetConfigProperties.gotoDefinitionEnabled

  override fun startServerProcess(): OSProcessHandler {
    val processHandler = executionContext.toRubyScriptExecutionContext()!!.createProcessHandler()
    if (processHandler !is OSProcessHandler) {
      throw RuntimeException("hmm... RubyProcessHandler wasn't an OSProcessHandler.")
    }
    return processHandler
  }

  override fun createCommandLine(): GeneralCommandLine {
    throw NotImplementedError("startServerProcess is implemented instead.")
  }

  companion object {
    private const val GEM_NAME = "sorbet"
    private const val GEM_SCRIPT_NAME = "srb"
    private val GEM_SCRIPT_ARGS = listOf("tc", "--lsp")

    fun createGemExecutionContext(sdk: Sdk, project: Project, file: VirtualFile): RubyGemExecutionContext? {
      val module = ModuleUtilCore.findModuleForFile(file, project)
      val gemfile = BundlerUtil.getGemfile(module) ?: return null // bail if no Gemfile
      if (BundlerGemInfrastructure.hasMissingGems(gemfile)) { // bail if bundle install needs to be run
        return null
      }

      return RubyGemExecutionContext.tryCreate(null, module, GEM_NAME)
        // NB: Use the Gemfile's parent directory as safest option for typical sorbet project structure.
        //     Using project content root can get tricky since there may (rarely) be multiple to choose from.
        ?.withWorkingDir(gemfile.parent)
        ?.withGemScriptName(GEM_SCRIPT_NAME)
        ?.withArguments(GEM_SCRIPT_ARGS)
    }

    fun tryCreate(project: Project, file: VirtualFile): SorbetLspServerDescriptor? {
      val sorbetConfigProperties = SorbetConfigProperties(PropertiesComponent.getInstance(project))
      if (!sorbetConfigProperties.pluginEnabled) return null

      val sdk = RubySdkUtil.getFileSdk(project, file) ?: return null
      val executionContext = createGemExecutionContext(sdk, project, file) ?: return null
      return SorbetLspServerDescriptor(
        sorbetConfigProperties, executionContext, project, executionContext.module!!.rootManager.contentRoots
      )
    }
  }
}