package im.simo.rubymine.sorbetlsp

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.modules
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServerSupportProvider
import com.intellij.platform.lsp.api.ProjectWideLspServerDescriptor
import org.jetbrains.plugins.ruby.gem.RubyGemExecutionContext
import org.jetbrains.plugins.ruby.ruby.lang.RubyFileType

class SorbetLspServerSupportProvider : LspServerSupportProvider {
    override fun fileOpened(project: Project, file: VirtualFile, serverStarter: LspServerSupportProvider.LspServerStarter) {
        if (file.fileType == RubyFileType.RUBY) {
            serverStarter.ensureServerStarted(SorbetLspServerDescriptor(project))
        }
    }
}

private class SorbetLspServerDescriptor(project: Project) : ProjectWideLspServerDescriptor(project, "Sorbet") {
    override fun isSupportedFile(file: VirtualFile) = file.fileType == RubyFileType.RUBY

    override fun startServerProcess(): OSProcessHandler {
        val module = project.modules[0]
        val mRoot = ModuleRootManager.getInstance(module)
        val sdk = mRoot.sdk!!
        val rubyGemExecutionContext = RubyGemExecutionContext.create(sdk, "sorbet")
                .withModule(module)
                .withWorkingDirPath(project.basePath)
                .withGemScriptName("srb")
                .withArguments("tc", "--lsp")

        val rubyScriptExecutionContext = rubyGemExecutionContext.toRubyScriptExecutionContext()!!
        val processHandler = rubyScriptExecutionContext.createProcessHandler()
        if (processHandler !is OSProcessHandler) {
            throw RuntimeException("hmm... RubyProcessHandler wasn't an OSProcessHandler.")
        }
        LOG.warn(processHandler.toString())
        return processHandler
    }

    override fun createCommandLine(): GeneralCommandLine {
        throw RuntimeException("not implemented.")
    }
}