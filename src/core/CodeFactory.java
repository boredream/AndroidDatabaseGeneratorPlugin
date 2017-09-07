package core;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;
import utils.AndroidUtils;
import utils.StringUtils;

import java.util.ArrayList;

public class CodeFactory {

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
        return StringUtils.formatSingleLine(0, "package " + AndroidUtils.getFilePackageName(dir) + ";") +
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
    public static String genCreateTableCode(PsiClass clazz, ArrayList<PsiField> fields, PsiField priKeyField) {
        String tableName = "DataContract." + clazz.getName();

        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.formatSingleLine(0, "public void create" + clazz.getName() + "Table(SQLiteDatabase db) {"));
        sb.append(StringUtils.formatSingleLine(1, "String sql = \"CREATE TABLE IF NOT EXISTS \""));
        sb.append(StringUtils.formatSingleLine(3, "+ " + tableName + ".TABLE_NAME + \"(\""));
        if(priKeyField == null) {
            // 默认主键
            sb.append(StringUtils.formatSingleLine(3, "+ " + tableName + "._ID + \" INTEGER PRIMARY KEY AUTOINCREMENT,\""));
        } else {
            sb.append(StringUtils.formatSingleLine(3, "+ " + tableName + "._ID + \" INTEGER AUTOINCREMENT,\""));
        }

        for (PsiField field : fields) {
            String name = getColumnString(field);
            String type = parseDbType(field);
            if(priKeyField != null && priKeyField.getName().equals(field.getName())) {
                // 有自定义主键
                sb.append(StringUtils.formatSingleLine(3, "+ " + tableName + "." + name + " + \" " + type + " PRIMARY KEY,\""));
            } else {
                sb.append(StringUtils.formatSingleLine(3, "+ " + tableName + "." + name + " + \" " + type + ",\""));
            }
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
        return StringUtils.formatSingleLine(0, "package " + AndroidUtils.getFilePackageName(dir) + ";") +
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
    public static String genBeanColumnsCode(PsiClass clazz, ArrayList<PsiField> fields) {
        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.formatSingleLine(0, "public interface " + clazz.getName() + " extends BaseColumns {"));
        sb.append(StringUtils.formatSingleLine(1, "String TABLE_NAME = \"" + StringUtils.camel2underline(clazz.getName()) + "\";"));
        for (PsiField field : fields) {
            String name = StringUtils.camel2underline(field.getName()).toUpperCase();
            String value = name.toLowerCase();
            sb.append(StringUtils.formatSingleLine(1, "String " + name + " = \"" + value + "\";"));
        }
        sb.append("}");
        return sb.toString().trim();
    }

    /**
     * 生成Dao类，包含增删改查基础方法
     */
    public static String genDaoCode(PsiClass clazz, VirtualFile dir) {
        String daoClassName = clazz.getName() + "Dao";

        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.formatSingleLine(0, "package " + AndroidUtils.getFilePackageName(dir) + ";"));
        sb.append("\n");
        sb.append(StringUtils.formatSingleLine(0, "import android.content.ContentValues;"));
        sb.append(StringUtils.formatSingleLine(0, "import android.database.Cursor;"));
        sb.append(StringUtils.formatSingleLine(0, "import android.database.sqlite.SQLiteDatabase;"));
        sb.append(StringUtils.formatSingleLine(0, "import android.database.sqlite.SQLiteStatement;"));
        String dataPackageText = AndroidUtils.getFilePackageName(clazz.getContainingFile().getVirtualFile()) + "." + clazz.getName();
        sb.append(StringUtils.formatSingleLine(0, "import " + dataPackageText + ";"));
        sb.append("\n");
        sb.append(StringUtils.formatSingleLine(0, "import java.util.ArrayList;"));
        sb.append("\n");
        sb.append(StringUtils.formatSingleLine(0, "public class " + daoClassName + " {"));
        sb.append(StringUtils.formatSingleLine(1, "private DatabaseHelper helper;"));
        sb.append(StringUtils.formatSingleLine(1, "private static volatile " + daoClassName + " instance = null;"));
        sb.append("\n");
        sb.append(StringUtils.formatSingleLine(1, "public static " + daoClassName + " getInstance() {"));
        sb.append(StringUtils.formatSingleLine(2, "if (instance == null) {"));
        sb.append(StringUtils.formatSingleLine(3, "synchronized (" + daoClassName + ".class) {"));
        sb.append(StringUtils.formatSingleLine(4, "if (instance == null) {"));
        sb.append(StringUtils.formatSingleLine(5, "instance = new " + daoClassName + "();"));
        sb.append(StringUtils.formatSingleLine(4, "}"));
        sb.append(StringUtils.formatSingleLine(3, "}"));
        sb.append(StringUtils.formatSingleLine(2, "}"));
        sb.append(StringUtils.formatSingleLine(2, "return instance;"));
        sb.append(StringUtils.formatSingleLine(1, "}"));
        sb.append("\n");
        sb.append(StringUtils.formatSingleLine(1, "private " + daoClassName + "() {"));
        sb.append(StringUtils.formatSingleLine(2, "helper = DatabaseHelper.getInstance();"));
        sb.append(StringUtils.formatSingleLine(1, "}"));
        sb.append("\n");
        sb.append(StringUtils.formatSingleLine(1, "public void add" + clazz.getName() + "(" + clazz.getName() + " data) {"));
        sb.append(StringUtils.formatSingleLine(2, "SQLiteDatabase db = helper.getWritableDatabase();"));
        sb.append("\n");
        sb.append(StringUtils.formatSingleLine(2, "ContentValues value = new ContentValues();"));
        for (PsiField field : clazz.getFields()) {
            String text = String.format("value.put(DataContract.%s.%s, %s);",
                    clazz.getName(), getColumnString(field), genDataGetStr(field));
            sb.append(StringUtils.formatSingleLine(2, text));
        }
        sb.append("\n");
        sb.append(StringUtils.formatSingleLine(2, "db.insert(DataContract." + clazz.getName() + ".TABLE_NAME, null, value);"));
        sb.append(StringUtils.formatSingleLine(1, "}"));
        sb.append("\n");
        sb.append(StringUtils.formatSingleLine(1, "public void add" + clazz.getName() + "List(ArrayList<" + clazz.getName() + "> datas) {"));
        sb.append(StringUtils.formatSingleLine(2, "SQLiteDatabase db = helper.getWritableDatabase();"));
        sb.append(StringUtils.formatSingleLine(2, "db.beginTransaction();"));
        sb.append("\n");
        StringBuilder valuesSb = new StringBuilder();
        for (int i = 0; i < clazz.getFields().length; i++) {
            valuesSb.append(i==0?"":", ").append("?");
        }
        sb.append(StringUtils.formatSingleLine(2, "String sql = \"INSERT INTO \" + DataContract." + clazz.getName() + ".TABLE_NAME + \" VALUES ( " + valuesSb.toString() + ")\";"));
        sb.append(StringUtils.formatSingleLine(2, "SQLiteStatement stmt = db.compileStatement(sql);"));
        sb.append("\n");
        sb.append(StringUtils.formatSingleLine(2, "// 事务批处理"));
        sb.append(StringUtils.formatSingleLine(2, "for (" + clazz.getName() + " data : datas) {"));
        for (int i = 0; i < clazz.getFields().length; i++) {
            PsiField field = clazz.getFields()[i];
            String bindMethod = "bindString";
            switch (parseDbType(field)) {
                case "INTEGER":
                    bindMethod = "bindLong";
                    break;
                case "REAL":
                    bindMethod = "bindDouble";
                    break;
            }
            sb.append(StringUtils.formatSingleLine(3, "stmt." + bindMethod + "(" + (i + 1) + ", " + genDataGetStr(field) + ");"));
        }
        sb.append(StringUtils.formatSingleLine(3, "stmt.execute();"));
        sb.append(StringUtils.formatSingleLine(3, "stmt.clearBindings();"));
        sb.append(StringUtils.formatSingleLine(2, "}"));
        sb.append("\n");
        sb.append(StringUtils.formatSingleLine(2, "db.setTransactionSuccessful();"));
        sb.append(StringUtils.formatSingleLine(2, "db.endTransaction();"));
        sb.append(StringUtils.formatSingleLine(1, "}"));
        sb.append("\n");
        sb.append(StringUtils.formatSingleLine(1, "public ArrayList<" + clazz.getName() + "> get" + clazz.getName() + "List() {"));
        sb.append(StringUtils.formatSingleLine(2, "SQLiteDatabase db = helper.getReadableDatabase();"));
        sb.append(StringUtils.formatSingleLine(2, "ArrayList<" + clazz.getName() + "> datas = new ArrayList<>();"));
        sb.append(StringUtils.formatSingleLine(2, "Cursor cursor = null;"));
        sb.append(StringUtils.formatSingleLine(2, "try {"));
        sb.append(StringUtils.formatSingleLine(3, "cursor = db.query(DataContract." + clazz.getName() + ".TABLE_NAME,"));
        sb.append(StringUtils.formatSingleLine(5, "null,"));
        sb.append(StringUtils.formatSingleLine(5, "null,"));
        sb.append(StringUtils.formatSingleLine(5, "null,"));
        sb.append(StringUtils.formatSingleLine(5, "null,"));
        sb.append(StringUtils.formatSingleLine(5, "null,"));
        sb.append(StringUtils.formatSingleLine(5, "null);"));
        sb.append(StringUtils.formatSingleLine(3, "if (cursor != null && cursor.moveToFirst()) {"));
        sb.append(StringUtils.formatSingleLine(4, "do {"));
        sb.append(StringUtils.formatSingleLine(5, "" + clazz.getName() + " data = new " + clazz.getName() + "();"));
        for (PsiField field : clazz.getFields()) {
            sb.append(StringUtils.formatSingleLine(5, genSetDataStr(clazz, field)));
        }
        sb.append(StringUtils.formatSingleLine(5, "datas.add(data);"));
        sb.append(StringUtils.formatSingleLine(4, "} while (cursor.moveToNext());"));
        sb.append(StringUtils.formatSingleLine(3, "}"));
        sb.append(StringUtils.formatSingleLine(2, "} finally {"));
        sb.append(StringUtils.formatSingleLine(3, "if (cursor != null) cursor.close();"));
        sb.append(StringUtils.formatSingleLine(2, "}"));
        sb.append(StringUtils.formatSingleLine(2, "return datas;"));
        sb.append(StringUtils.formatSingleLine(1, "}"));
        sb.append("\n");
        sb.append(StringUtils.formatSingleLine(1, "public void delete" + clazz.getName() + "List() {"));
        sb.append(StringUtils.formatSingleLine(2, "SQLiteDatabase db = helper.getWritableDatabase();"));
        sb.append(StringUtils.formatSingleLine(2, "db.delete(DataContract." + clazz.getName() + ".TABLE_NAME, null, null);"));
        sb.append(StringUtils.formatSingleLine(1, "}"));
        sb.append("\n");
        sb.append(StringUtils.formatSingleLine(0, "}"));

        return sb.toString();
    }

    private static String getColumnString(PsiField field) {
        return StringUtils.camel2underline(field.getName()).toUpperCase();
    }

    /**
     * 将基础类型等转为数据库对应的数据类型
     * (boolean和date都作为String处理)
     */
    private static String parseDbType(PsiField field) {
        String type;
        switch (field.getType().getPresentableText()) {
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

    private static String genDataGetStr(PsiField field) {
        String getMethod;
        if(field.getName().startsWith("is")) {
            getMethod = field.getName() + "()";
        } else {
            getMethod = "get" + StringUtils.firstToUpperCase(field.getName()) + "()";
        }

        String value = "data." + getMethod;
        if (field.getType().equals(PsiType.BOOLEAN)) {
            value += " ? 1 : 0";
        }
        return value;
    }

    private static String genSetDataStr(PsiClass clazz, PsiField field) {
        String type = "String";
        String extra = "";

        switch (field.getType().getPresentableText()) {
            case "int":
            case "Integer":
                type = "Int";
                break;
            case "long":
            case "Long":
                type = "Long";
                break;
            case "float":
            case "Float":
                type = "Float";
                break;
            case "double":
            case "Double":
                type = "Double";
                break;
            case "boolean":
            case "Boolean":
                type = "Int";
                extra = " == 1";
                break;
        }

        String text = "data.%s(cursor.get%s(cursor.getColumnIndex(DataContract.%s.%s))%s);";
        String setMethod = "set" + StringUtils.firstToUpperCase(field.getName());
        if(field.getName().startsWith("is")) {
            setMethod = setMethod.replaceFirst("Is", "");
        }

        return String.format(text, setMethod, type, clazz.getName(), getColumnString(field), extra);
    }

}
