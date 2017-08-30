package core;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import utils.AndroidUtils;
import utils.PluginUtils;

import java.io.IOException;

public class DatabaseGenerator {

    public static void genCode(PsiFile file, PsiClass clazz) {
        Project project = file.getProject();

        // app包名根目录 ...\app\src\main\java\PACKAGE_NAME\
        VirtualFile baseDir = AndroidUtils.getAppPackageBaseDir(project);

        // 判断根目录下是否有db文件夹
        VirtualFile dbDir = baseDir.findChild("db");
        if(dbDir == null) {
            // 没有就创建一个
            try {
                dbDir = baseDir.createChildDirectory(null, "db");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // SqliteOpenHelper类
        genHelperFile(clazz, project, dbDir);

        // 数据类对应的Columns字段都统一的存在DataContract类中
        genColumnFile(clazz, project, dbDir);

        // 为每个数据类创建一个Dao类，包含基本的CRUD方法
        genDaoCode(clazz, project, dbDir);
    }

    private static void genHelperFile(PsiClass clazz, Project project, VirtualFile dbDir) {
        PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();

        String name = "DatabaseHelper.java";
        VirtualFile virtualFile = dbDir.findChild(name);
        if(virtualFile == null) {
            // 没有就创建一个，第一次使用代码字符串创建个类
            PsiFile initFile = PsiFileFactory.getInstance(project).createFileFromText(
                    name, JavaFileType.INSTANCE, CodeFactory.genSqliteOpenHelperInitCode(dbDir));

            // 加到db目录下
            PsiManager.getInstance(project).findDirectory(dbDir).add(initFile);
            virtualFile = dbDir.findChild(name);
        }

        PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
        // 用拼接的代码生成create table方法
        String createTableCode = CodeFactory.genCreateTableCode(clazz);
        PsiMethod createTableMethod = factory.createMethodFromText(createTableCode, psiFile);
        // 将创建的method添加到DatabaseHelper Class中
        PsiClass fileClass = PluginUtils.getFileClass(psiFile);
        fileClass.add(createTableMethod);
        // 在类中的onCreate方法中调用create table方法
        PsiMethod onCreateMethod = fileClass.findMethodsByName("onCreate", false)[0];
        onCreateMethod.getBody().add(factory.createStatementFromText(createTableMethod.getName() + "(db);", fileClass));
    }

    private static void genColumnFile(PsiClass clazz, Project project, VirtualFile dbDir) {
        PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();

        String name = "DataContract.java";
        VirtualFile virtualFile = dbDir.findChild(name);
        if(virtualFile == null) {
            // 没有就创建一个，第一次使用代码字符串创建个类
            PsiFile initFile = PsiFileFactory.getInstance(project).createFileFromText(
                    name, JavaFileType.INSTANCE, CodeFactory.genDataContractInitCode(dbDir));

            // 加到db目录下
            PsiManager.getInstance(project).findDirectory(dbDir).add(initFile);
            virtualFile = dbDir.findChild(name);
        }

        PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
        // 用拼接的代码生成Columns Class
        String beanColumnsCode = CodeFactory.genBeanColumnsCode(clazz);
        PsiClass beanColumnsClass = factory.createClassFromText(beanColumnsCode, psiFile);
        // 将创建的class添加到DataContract Class中
        PsiClass fileClass = PluginUtils.getFileClass(psiFile);
        fileClass.add(beanColumnsClass.getInnerClasses()[0]);
    }

    private static void genDaoCode(PsiClass clazz, Project project, VirtualFile dbDir) {
        String name = clazz.getName() + "Dao.java";
        // 使用代码字符串创建个类
        PsiFile initFile = PsiFileFactory.getInstance(project).createFileFromText(
                name, JavaFileType.INSTANCE, CodeFactory.genDaoCode(clazz, dbDir));

        // 加到db目录下
        PsiManager.getInstance(project).findDirectory(dbDir).add(initFile);
    }
}
