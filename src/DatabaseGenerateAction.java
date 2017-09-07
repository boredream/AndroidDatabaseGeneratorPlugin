import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import core.DatabaseGenerator;
import ui.ColumnDialog;
import utils.PluginUtils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class DatabaseGenerateAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        PsiFile file = e.getData(PlatformDataKeys.PSI_FILE);

        PsiClass clazz = PluginUtils.getFileClass(file);

        ColumnDialog dialog = new ColumnDialog(clazz);
        dialog.setOnGenerateListener((fields, priKeyField) -> WriteCommandAction.runWriteCommandAction(project, () -> {
            DatabaseGenerator.genCode(file, clazz, fields, priKeyField);
        }));
        dialog.pack();
        dialog.setVisible(true);
    }
}
