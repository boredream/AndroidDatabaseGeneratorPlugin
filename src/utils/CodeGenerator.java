package utils;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;

public class CodeGenerator {

    /**
     * 生成DatabaseHelper初始代码
     *
     * <pre>
     * package com.boredream.plugindemo.db;
     *
     * import android.database.sqlite.SQLiteDatabase;
     * import android.database.sqlite.SQLiteOpenHelper;
     *
     * public class DatabaseHelper extends SQLiteOpenHelper {
     *     private static String DB_NAME = "INPUT YOUR DB FILE NAME";
     *     private static final int DB_VERSION = 1;
     *
     *     private static volatile DatabaseHelper instance = null;
     *
     *     public static DatabaseHelper getInstance() {
     *         if (instance == null) {
     *             synchronized (DatabaseHelper.class) {
     *                 if (instance == null) {
     *                     instance = new DatabaseHelper();
     *                 }
     *             }
     *         }
     *         return instance;
     *     }
     *
     *     private DatabaseHelper() {
     *         // TODO: 2017/8/30 user your application context
     *         super(BaseApplication.getInstance(), DB_NAME, null, DB_VERSION);
     *     }
     *
     *     @Override
     *     public void onCreate(SQLiteDatabase db) {
     *     }
     *
     *     @Override
     *     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
     *     }
     * }
     * </pre>
     */
    public static String genSqliteOpenHelperInitCode(VirtualFile dir) {
        return StringUtils.formatSingleLine(0, "package " + AndroidUtils.getFilePackagePath(dir) + ";") +
                "\n" +
                StringUtils.formatSingleLine(0, "import android.database.sqlite.SQLiteDatabase;") +
                StringUtils.formatSingleLine(0, "import android.database.sqlite.SQLiteOpenHelper;") +
                "\n" +
                StringUtils.formatSingleLine(0, "public class DatabaseHelper extends SQLiteOpenHelper {") +
                "\n" +
                StringUtils.formatSingleLine(1, "// TODO: input your db file name") +
                StringUtils.formatSingleLine(1, "private static String DB_NAME = \"INPUT YOUR DB FILE NAME\";") +
                StringUtils.formatSingleLine(1, "private static final int DB_VERSION = 1;") +
                "\n" +
                StringUtils.formatSingleLine(1, "private static volatile DatabaseHelper instance = null;") +
                StringUtils.formatSingleLine(1, "public static DatabaseHelper getInstance() {") +
                StringUtils.formatSingleLine(2, "if (instance == null) {") +
                StringUtils.formatSingleLine(3, "synchronized (DatabaseHelper.class) {") +
                StringUtils.formatSingleLine(4, "if (instance == null) {") +
                StringUtils.formatSingleLine(5, "instance = new DatabaseHelper();") +
                StringUtils.formatSingleLine(4, "}") +
                StringUtils.formatSingleLine(3, "}") +
                StringUtils.formatSingleLine(2, "}") +
                StringUtils.formatSingleLine(2, "return instance;") +
                StringUtils.formatSingleLine(1, "}") +
                "\n" +
                StringUtils.formatSingleLine(1, "private DatabaseHelper() {") +
                StringUtils.formatSingleLine(2, "// TODO: user your application context") +
                StringUtils.formatSingleLine(2, "super(BaseApplication.getInstance(), DB_NAME, null, DB_VERSION);") +
                StringUtils.formatSingleLine(1, "}") +
                "\n" +
                StringUtils.formatSingleLine(1, "@Override") +
                StringUtils.formatSingleLine(1, "public void onCreate(SQLiteDatabase db) {") +
                StringUtils.formatSingleLine(1, "}") +
                "\n" +
                StringUtils.formatSingleLine(1, "@Override") +
                StringUtils.formatSingleLine(1, "public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {") +
                StringUtils.formatSingleLine(1, "}") +
                "\n" +
                "}";
    }

    /**
     * 生成创建数据库表单方法代码
     *
     * <pre>
     * String sql = "CREATE TABLE IF NOT EXISTS "
     *        DataContract.USER.TABLE_NAME + "("
     *        DataContract.USER._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
     *        DataContract.USER.USERNAME + " TEXT,"
     *        DataContract.USER.IS_MALE + " TEXT,"
     *        DataContract.USER.AGE + " TEXT"
     *        + ")";
     * </pre>
     */
    public static String genCreateTableCode(PsiClass clazz) {
        String tableName = "DataContract." + clazz.getName();

        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.formatSingleLine(0, "public void create" + clazz.getName() + "Table(SQLiteDatabase db) {"));
        sb.append(StringUtils.formatSingleLine(1, "String sql = \"CREATE TABLE IF NOT EXISTS \""));
        sb.append(StringUtils.formatSingleLine(3, "+ " + tableName + ".TABLE_NAME + \"(\""));
        sb.append(StringUtils.formatSingleLine(3, "+ " + tableName + "._ID + \" INTEGER PRIMARY KEY AUTOINCREMENT,\""));

        for (PsiField field : clazz.getFields()) {
            String name = getColumnString(field);
            String type = parseDbType(field);
            sb.append(StringUtils.formatSingleLine(3, "+ " + tableName + "." + name + " + \" " + type + ",\""));
        }
        sb.replace(sb.lastIndexOf(",\""), sb.lastIndexOf(",\"") + 2, "\"\n\t\t+ \")\";");
        sb.append(StringUtils.formatSingleLine(1, "db.execSQL(sql);"));
        sb.append(StringUtils.formatSingleLine(0, "}"));
        return sb.toString();
    }

    /**
     * 生成DataContract文件初始代码
     *
     * <pre>
     * package com.boredream.plugindemo.db;
     *
     * public final class DataContract {
     *     private DataContract() {
     *         // private
     *     }
     *}
     * </pre>
     */
    public static String genDataContractInitCode(VirtualFile dir) {
        return StringUtils.formatSingleLine(0, "package " + AndroidUtils.getFilePackagePath(dir) + ";") +
                "\n" +
                StringUtils.formatSingleLine(0, "import android.provider.BaseColumns;") +
                "\n" +
                StringUtils.formatSingleLine(0, "public final class DataContract {") +
                "\n" +
                StringUtils.formatSingleLine(1, "private DataContract() {") +
                StringUtils.formatSingleLine(2, "// private") +
                StringUtils.formatSingleLine(1, "}") +
                "\n" +
                "}";
    }

    /**
     * 生成数据类字段Class内容代码
     *
     * <pre>
     * String TABLE_NAME = "User";
     * String USERNAME = "username";
     * String IS_MALE = "is_male";
     * String AGE = "age";
     * </pre>
     */
    public static String genBeanColumnsCode(PsiClass clazz) {
        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.formatSingleLine(0, "public interface " + clazz.getName() + " extends BaseColumns {"));
        sb.append(StringUtils.formatSingleLine(1, "String TABLE_NAME = \"" + StringUtils.camel2underline(clazz.getName()) + "\";"));
        for (PsiField field : clazz.getFields()) {
            String name = StringUtils.camel2underline(field.getName()).toUpperCase();
            String value = name.toLowerCase();
            sb.append(StringUtils.formatSingleLine(1, "String " + name + " = \"" + value + "\";"));
        }
        sb.append("}");
        return sb.toString().trim();
    }

    private static String getColumnString(PsiField field) {
        return StringUtils.camel2underline(field.getName()).toUpperCase();
    }

    /**
     * 将基础类型等转为数据库对应的数据类型
     * (boolean和date都作为字符类型处理)
     */
    private static String parseDbType(PsiField field) {
        String type;
        switch (field.getName()) {
            case "int":
            case "Integer":
            case "long":
            case "Long":
                type = "INTEGER";
                break;
            case "float":
            case "Float":
            case "double":
            case "Double":
                type = "REAL";
                break;
            case "boolean":
            case "Boolean":
                type = "INTEGER";
                break;
            default:
                type = "TEXT";
                break;
        }
        return type;
    }

}
