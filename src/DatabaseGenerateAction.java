import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import utils.DbUtils;
import utils.PluginUtils;

public class DatabaseGenerateAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        PsiFile file = e.getData(PlatformDataKeys.PSI_FILE);
        Project project = file.getProject();

        PsiClass clazz = PluginUtils.getFileClass(file);

        WriteCommandAction.runWriteCommandAction(project, () -> {
            DbUtils.genCode(file, clazz);
        });

//        DbUtils.genCreateTableCode(clazz);
    }
}
