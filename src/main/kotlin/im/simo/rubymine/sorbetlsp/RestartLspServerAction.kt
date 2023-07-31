package im.simo.rubymine.sorbetlsp

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.platform.lsp.api.LspServerManager

@Suppress("UnstableApiUsage")
class RestartLspServerAction : AnAction() {
  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project
    if (project == null || project.isDefault) return

    LspServerManager.getInstance(project).stopAndRestartIfNeeded(SorbetLspServerSupportProvider::class.java)

    NotificationGroupManager.getInstance()
      .getNotificationGroup("Sorbet LSP")
      .createNotification("Sorbet LSP Restarted", "", NotificationType.INFORMATION)
      .notify(project)
  }

  override fun update(e: AnActionEvent) {
    super.update(e)
    val project = e.project
    if (project == null || project.isDefault) return

    if (LspServerManager.getInstance(project).getServersForProvider(SorbetLspServerSupportProvider::class.java).isEmpty()) {
      e.presentation.isEnabled = false
    } else {
      e.presentation.isEnabled = true
    }
  }


  override fun getActionUpdateThread(): ActionUpdateThread {
    return ActionUpdateThread.BGT
  }
}