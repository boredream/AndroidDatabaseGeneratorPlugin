package utils;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;

public class CodeGenerator {

    /**
     * 生成DataContract类初始代码
     * <pre>
     * public final class DataContract {
     *
     *     private DataContract() {
     *         // private
     *     }
     *
     *}
     * </pre>
     */
    public static String genDataContractInitCode(VirtualFile dir) {
        return StringUtils.formatSingleLine(0, "package " + AndroidUtils.getFilePackagePath(dir) + ";") +
                "\n" +
                StringUtils.formatSingleLine(0, "public final class DataContract {") +
                "\n" +
                StringUtils.formatSingleLine(1, "private DataContract() {") +
                StringUtils.formatSingleLine(2, "// private") +
                StringUtils.formatSingleLine(1, "}") +
                "\n" +
                "}";
    }

    public static String genJavaBeanColumnsCode(PsiClass clazz) {
        StringBuilder sbColumn = new StringBuilder();
        sbColumn.append(StringUtils.formatSingleLine(0, "String TABLE_NAME = \"" + StringUtils.camel2underline(clazz.getName()) + "\";"));
        for (PsiField field : clazz.getFields()) {
            String name = StringUtils.camel2underline(field.getName()).toUpperCase();
            String value = name.toLowerCase();
            sbColumn.append(StringUtils.formatSingleLine(0, "String " + name + " = \"" + value + "\";"));
        }
        return sbColumn.toString();
    }

}
