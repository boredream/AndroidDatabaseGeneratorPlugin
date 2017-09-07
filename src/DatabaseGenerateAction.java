import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import ui.ColumnDialog;
import utils.PluginUtils;

public class DatabaseGenerateAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        PsiFile file = e.getData(PlatformDataKeys.PSI_FILE);

        PsiClass clazz = PluginUtils.getFileClass(file);

        ColumnDialog dialog = new ColumnDialog(clazz);
        dialog.pack();
        dialog.setVisible(true);

//        WriteCommandAction.runWriteCommandAction(project, () -> {
//            DatabaseGenerator.genCode(file, clazz);
//        });
    }
}
