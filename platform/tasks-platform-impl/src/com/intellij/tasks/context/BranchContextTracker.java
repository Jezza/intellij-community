package com.intellij.tasks.context;

import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.TransactionGuard;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.BranchChangeListener;
import com.intellij.openapi.vcs.VcsConfiguration;
import com.intellij.tasks.BranchInfo;
import com.intellij.tasks.LocalTask;
import com.intellij.tasks.TaskBundle;
import com.intellij.tasks.TaskManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BranchContextTracker implements BranchChangeListener {

  public static final NotificationGroup NOTIFICATION = new NotificationGroup(
    "Branch Context group", NotificationDisplayType.BALLOON, true);

  private final Project myProject;
  private String myLastBranch;

  public BranchContextTracker(@NotNull Project project) {
    myProject = project;
  }

  private WorkingContextManager getContextManager() {
    return WorkingContextManager.getInstance(myProject);
  }

  @Override
  public void branchWillChange(@NotNull String branchName) {
    myLastBranch = branchName;
    getContextManager().saveContext(getContextName(branchName), null);
  }

  @Override
  public void branchHasChanged(@NotNull String branchName) {
    VcsConfiguration vcsConfiguration = VcsConfiguration.getInstance(myProject);
    if (!vcsConfiguration.RELOAD_CONTEXT) return;

    // check if the task is already switched
    TaskManager manager = TaskManager.getManager(myProject);
    if (manager != null) {
      LocalTask task = manager.getActiveTask();
      List<BranchInfo> branches = task.getBranches(false);
      if (branches.stream().anyMatch(info -> branchName.equals(info.name)))
        return;
    }

    String contextName = getContextName(branchName);
    if (!getContextManager().hasContext(contextName)) return;

    TransactionGuard.submitTransaction(myProject, () -> switchContext(branchName, contextName));
  }

  private void switchContext(@NotNull String branchName, String contextName) {
    WorkingContextManager contextManager = getContextManager();
    contextManager.clearContext();
    contextManager.loadContext(contextName);

    Notification notification =
      NOTIFICATION.createNotification("Workspace associated with branch '" + branchName + "' has been restored", NotificationType.INFORMATION);
    if (myLastBranch != null && contextManager.hasContext(getContextName(myLastBranch))) {
      notification.addAction(new NotificationAction(() -> TaskBundle.message("action.Anonymous.text.rollback")) {
        @Override
        public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
          contextManager.clearContext();
          contextManager.loadContext(getContextName(myLastBranch));
        }
      });
    }
    notification.addAction(new NotificationAction(() -> TaskBundle.message("action.Anonymous.text.configure.tree.dots")) {
      @Override
      public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
        new ConfigureBranchContextDialog(myProject).show();
      }
    }).setContextHelpAction(new AnAction("What is a workspace?", "A workspace is a set of opened files, the current run configuration, and breakpoints.", null) {
      @Override
      public void actionPerformed(@NotNull AnActionEvent e) {

      }
    }).notify(myProject);
  }

  @NotNull
  private static String getContextName(String branchName) {
    return "__branch_context_" + branchName;
  }

}
