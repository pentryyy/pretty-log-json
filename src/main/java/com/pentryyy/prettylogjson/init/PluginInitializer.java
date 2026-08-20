package com.pentryyy.prettylogjson.init;

import com.intellij.ide.ApplicationInitializedListener;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;

public class PluginInitializer implements ApplicationInitializedListener {

    @Override
    public void componentsInitialized() {
        ApplicationManager.getApplication().invokeLater(() -> {
            ActionManager am = ActionManager.getInstance();
            AnAction formatAction = am.getAction("FormatLogAction");
            if (formatAction == null) {
                System.err.println("Pretty Log JSON: действие не найдено!");
                return;
            }

            String[] possibleGroupIds = {
                    "MainToolBar",
                    "MainToolbar",
                    "Toolbar",
                    "EditorToolbar",
                    "NavBarToolbar"
            };

            boolean added = false;
            for (String groupId : possibleGroupIds) {
                DefaultActionGroup group = (DefaultActionGroup) am.getAction(groupId);
                if (group == null) {
                    break;
                }
                if (!group.containsAction(formatAction)) {
                    group.add(formatAction);
                    System.out.println("Pretty Log JSON: добавлено в группу " + groupId);
                } else {
                    System.out.println("Pretty Log JSON: уже есть в группе " + groupId);
                }
                added = true;
                break;
            }

            if (!added) {
                System.err.println("Pretty Log JSON: не удалось найти ни одну группу для панели!");
            }
        });
    }
}
