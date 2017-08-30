package utils;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import entity.ClassInfo;

import java.io.IOException;

public class DbUtils {

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

    }

    private static void genHelperFile(PsiClass clazz, Project project, VirtualFile dbDir) {
        PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();

        PsiFile psiFile;
        String name = "DatabaseHelper.java";
        VirtualFile virtualFile = dbDir.findChild(name);
        if(virtualFile == null) {
            // 没有就创建一个，第一次使用代码字符串创建个类
            psiFile = PsiFileFactory.getInstance(project).createFileFromText(
                    name, JavaFileType.INSTANCE, CodeGenerator.genSqliteOpenHelperInitCode(dbDir));

            // 加到db目录下
            PsiManager.getInstance(project).findDirectory(dbDir).add(psiFile);
        } else {
            // 已有，直接转换类型
            psiFile = PsiManager.getInstance(project).findFile(virtualFile);
        }

        // 用拼接的代码生成create table方法
        String createTableCode = CodeGenerator.genCreateTableCode(clazz);
        PsiMethod createTableMethod = factory.createMethodFromText(createTableCode, psiFile);
        // 将创建的method添加到DatabaseHelper Class中
        PluginUtils.getFileClass(psiFile).add(createTableMethod);
    }

    private static void genColumnFile(PsiClass clazz, Project project, VirtualFile dbDir) {
        PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();

        PsiFile psiFile;
        String name = "DataContract.java";
        VirtualFile virtualFile = dbDir.findChild(name);
        if(virtualFile == null) {
            // 没有就创建一个，第一次使用代码字符串创建个类
            psiFile = PsiFileFactory.getInstance(project).createFileFromText(
                    name, JavaFileType.INSTANCE, CodeGenerator.genDataContractInitCode(dbDir));

            // 加到db目录下
            PsiManager.getInstance(project).findDirectory(dbDir).add(psiFile);
        } else {
            // 已有，直接转换类型
            psiFile = PsiManager.getInstance(project).findFile(virtualFile);
        }

        // TODO: 2017/8/30 import BaseColumns
        // 用拼接的代码生成Columns Class
        String beanColumnsCode = CodeGenerator.genBeanColumnsCode(clazz);
        PsiClass beanColumnsClass = factory.createClassFromText(beanColumnsCode, psiFile);
        // 将创建的class添加到DataContract Class中
        PluginUtils.getFileClass(psiFile).add(beanColumnsClass.getInnerClasses()[0]);
    }

    private static ClassInfo.ClassField getPrimaryKey(ClassInfo info) {
        for (ClassInfo.ClassField field : info.fields) {
            if (field.isPrimaryKey) {
                return field;
            }
        }
        return null;
    }

    private static String formatString(String name, String value) {
        String str = "String %s = \"%s\";";
        return String.format(str, name, value);
    }

    private static void genCURDCode(PsiClass clazz) {
        // get content value
        StringBuilder sbContentValue = new StringBuilder();
        sbContentValue.append(StringUtils.formatSingleLine(1,
                "private ContentValues getContentValues(" + clazz.getName() + " data) {"));
        sbContentValue.append(StringUtils.formatSingleLine(2,
                "ContentValues value = new ContentValues();"));

        for (PsiField field : clazz.getFields()) {
            String name = getColumnString(field);
            sbContentValue.append(StringUtils.formatSingleLine(2,
                    "value.put(DataContract." + clazz.getName() + "." + name + ", " + getDataGetMethod(field) + ");"));
        }
        sbContentValue.append(StringUtils.formatSingleLine(2, "return value;"));
        sbContentValue.append(StringUtils.formatSingleLine(1, "}"));
        System.out.println(sbContentValue.toString());


        // add
        StringBuilder sbAdd = new StringBuilder();
        sbAdd.append(StringUtils.formatSingleLine(1,
                "public void add" + clazz.getName() + "(" + clazz.getName() + " data) {"));
        sbAdd.append(StringUtils.formatSingleLine(2,
                "SQLiteDatabase db = helper.getWritableDatabase();"));
        sbAdd.append("\n");
        sbAdd.append(StringUtils.formatSingleLine(2,
                "ContentValues value = getContentValues(data);"));
        sbAdd.append(StringUtils.formatSingleLine(2,
                "db.insert(DataContract." + clazz.getName() + ".TABLE_NAME, null, value);"));
        sbAdd.append(StringUtils.formatSingleLine(1, "}"));
        System.out.println(sbAdd.toString());


        // TODO: 2017/8/29  
//        PsiField primaryKeyField = getPrimaryKey(info);
        PsiField primaryKeyField = null;
        if (primaryKeyField != null) {
            // 有自定义主键，才提供更新方法
            // update
            StringBuilder sbUpdate = new StringBuilder();
            sbUpdate.append(StringUtils.formatSingleLine(1,
                    "public void update" + clazz.getName() + "(" + clazz.getName() + " data) {"));
            sbUpdate.append(StringUtils.formatSingleLine(2,
                    "SQLiteDatabase db = helper.getWritableDatabase();"));
            sbUpdate.append("\n");
            sbUpdate.append(StringUtils.formatSingleLine(2,
                    "ContentValues value = getContentValues(data);"));
            sbUpdate.append(StringUtils.formatSingleLine(2,
                    "db.update(DataContract." + clazz.getName() + ".TABLE_NAME,"));
            sbUpdate.append(StringUtils.formatSingleLine(4, "value,"));
            sbUpdate.append(StringUtils.formatSingleLine(4,
                    "DataContract." + clazz.getName() + "." + getColumnString(primaryKeyField) + " + \"=?\","));
//            sbUpdate.append(StringUtils.formatSingleLine(4,
//                    "new String[]{" + valueOfGet(primaryKeyField, "data." + primaryKeyField.getGetMethod()) + "});"));
            sbUpdate.append(StringUtils.formatSingleLine(1, "}"));
            System.out.println(sbUpdate.toString());


            // add or update
            StringBuilder sbAddOrUpdate = new StringBuilder();
            sbAddOrUpdate.append(StringUtils.formatSingleLine(1,
                    "public void addOrUpdate" + clazz.getName() + "(" + clazz.getName() + " data) {"));
//            sbAddOrUpdate.append(StringUtils.formatSingleLine(2,
//                    "if(get" + clazz.getName() + "(data." + primaryKeyField.getGetMethod() + ") != null) {"));
            sbAddOrUpdate.append(StringUtils.formatSingleLine(3,
                    "update" + clazz.getName() + "(data);"));
            sbAddOrUpdate.append(StringUtils.formatSingleLine(2,
                    "} else {"));
            sbAddOrUpdate.append(StringUtils.formatSingleLine(3,
                    "add" + clazz.getName() + "(data);"));
            sbAddOrUpdate.append(StringUtils.formatSingleLine(2, "}"));
            sbAddOrUpdate.append(StringUtils.formatSingleLine(1, "}"));
            System.out.println(sbAddOrUpdate.toString());
        }


        // add data list
        StringBuilder sbAddList = new StringBuilder();
        sbAddList.append(StringUtils.formatSingleLine(1,
                "public void add" + clazz.getName() + "List(ArrayList<" + clazz.getName() + "> datas) {"));
        sbAddList.append(StringUtils.formatSingleLine(2,
                "SQLiteDatabase db = helper.getWritableDatabase();"));
        sbAddList.append("\n");

        sbAddList.append(StringUtils.formatSingleLine(2,
                "db.beginTransaction();"));
        sbAddList.append("\n");

        sbAddList.append(StringUtils.formatSingleLine(2,
                "String sql = \"INSERT INTO \" + DataContract." + clazz.getName() + ".TABLE_NAME +"));
        StringBuilder sbValue = new StringBuilder();
        sbValue.append("\" VALUES (");
        for (int i = 0; i < clazz.getFields().length; i++) {
            PsiField field = clazz.getFields()[i];
            if (i == clazz.getFields().length - 1) {
                sbAdd.append(StringUtils.formatSingleLine(4,
                        "+ DataContract." + clazz.getName() + "." + getColumnString(field) + " + \") \""));
                sbValue.append("?)\";");
            } else {
                // TODO: 2016/11/19 要考虑哪些字段要剔除
                sbAdd.append(StringUtils.formatSingleLine(4,
                        "+ DataContract." + clazz.getName() + "." + getColumnString(field) + " + \", \""));
                sbValue.append("?, ");
            }
        }
        sbAddList.append(StringUtils.formatSingleLine(4, sbValue.toString()));
        sbAddList.append(StringUtils.formatSingleLine(2,
                "SQLiteStatement stmt = db.compileStatement(sql);"));
        sbAddList.append("\n");

        sbAddList.append(StringUtils.formatSingleLine(2, "// 事务批处理"));
        sbAddList.append(StringUtils.formatSingleLine(2, "for (" + clazz.getName() + " data : datas) {"));

        for (int i = 0; i < clazz.getFields().length; i++) {
            PsiField field = clazz.getFields()[i];
            // TODO: 2017/8/29  
            String bindMethod = "bindString";
//            switch (ClassUtils.parseDbType(field)) {
//                case "INTEGER":
//                    bindMethod = "bindLong";
//                    break;
//                case "REAL":
//                    bindMethod = "bindDouble";
//                    break;
//                case "TEXT":
//                default:
//                    bindMethod = "bindString";
//                    break;
//            }

            // TODO: 2016/11/19 有index的存在，要考虑哪些字段要剔除
            sbAddList.append(StringUtils.formatSingleLine(3,
                    "stmt." + bindMethod + "(" + (i + 1) + ", " + getDataGetMethod(field) + ");"));
        }
        sbAddList.append(StringUtils.formatSingleLine(3,
                "stmt.execute();"));
        sbAddList.append(StringUtils.formatSingleLine(3,
                "stmt.clearBindings();"));
        sbAddList.append(StringUtils.formatSingleLine(2,
                "}"));
        sbAddList.append("\n");

        sbAddList.append(StringUtils.formatSingleLine(2,
                "db.setTransactionSuccessful();"));
        sbAddList.append(StringUtils.formatSingleLine(2,
                "db.endTransaction();"));
        sbAddList.append(StringUtils.formatSingleLine(1,
                "}"));
        System.out.println(sbAddList.toString());


//        if (primaryKeyField != null) {
//            // 有自定义主键，才提供根据主键获取对象方法
//            // get data
//            StringBuilder sbGet = new StringBuilder();
//            sbGet.append(StringUtils.formatSingleLine(1, "public " + clazz.getName() +
//                    " get" + clazz.getName() + "(" + primaryKeyField.type + " key) {"));
//            sbGet.append(StringUtils.formatSingleLine(2,
//                    "SQLiteDatabase db = helper.getReadableDatabase();"));
//            sbGet.append(StringUtils.formatSingleLine(2,
//                    clazz.getName() + " data = null;"));
//            sbGet.append(StringUtils.formatSingleLine(2,
//                    "Cursor cursor = null;"));
//            sbGet.append(StringUtils.formatSingleLine(2,
//                    "try {"));
//            sbGet.append(StringUtils.formatSingleLine(3,
//                    "cursor = db.query(DataContract." + clazz.getName() + ".TABLE_NAME,"));
//            sbGet.append(StringUtils.formatSingleLine(5,
//                    "null,"));
//            sbGet.append(StringUtils.formatSingleLine(5,
//                    "DataContract." + clazz.getName() + "." + getColumnString(primaryKeyField) + " + \"=?\","));
//            sbGet.append(StringUtils.formatSingleLine(5,
//                    "new String[]{" + valueOfGet(primaryKeyField, "key") + "},"));
//            sbGet.append(StringUtils.formatSingleLine(5,
//                    "null,"));
//            sbGet.append(StringUtils.formatSingleLine(5,
//                    "null,"));
//            sbGet.append(StringUtils.formatSingleLine(5,
//                    "null);"));
//
//            sbGet.append(StringUtils.formatSingleLine(3,
//                    "if (cursor != null && cursor.moveToFirst()) {"));
//            sbGet.append(StringUtils.formatSingleLine(4,
//                    "data = new " + clazz.getName() + "();"));
//            sbGet.append(getDbSetDataStr(info, 4));
//            sbGet.append(StringUtils.formatSingleLine(3,
//                    "}"));
//            sbGet.append(StringUtils.formatSingleLine(2,
//                    "} finally {"));
//            sbGet.append(StringUtils.formatSingleLine(3,
//                    "if (cursor != null) cursor.close();"));
//            sbGet.append(StringUtils.formatSingleLine(2,
//                    "}"));
//            sbGet.append(StringUtils.formatSingleLine(2,
//                    "return data;"));
//            sbGet.append(StringUtils.formatSingleLine(1,
//                    "}"));
//            System.out.println(sbGet.toString());
//        }

        // TODO get data list
        StringBuilder sbGetList = new StringBuilder();
        sbGetList.append(StringUtils.formatSingleLine(1,
                "public ArrayList<" + clazz.getName() + "> get" + clazz.getName() + "List() {"));
        sbGetList.append(StringUtils.formatSingleLine(2,
                "SQLiteDatabase db = helper.getReadableDatabase();"));
        sbGetList.append(StringUtils.formatSingleLine(2,
                "ArrayList<" + clazz.getName() + "> datas = new ArrayList<>();"));
        sbGetList.append(StringUtils.formatSingleLine(2,
                "Cursor cursor = null;"));
        sbGetList.append(StringUtils.formatSingleLine(2,
                "try {"));
        sbGetList.append(StringUtils.formatSingleLine(3,
                "cursor = db.query(DataContract." + clazz.getName() + ".TABLE_NAME,"));
        sbGetList.append(StringUtils.formatSingleLine(5,
                "null,"));
        sbGetList.append(StringUtils.formatSingleLine(5,
                "null,"));
        sbGetList.append(StringUtils.formatSingleLine(5,
                "null,"));
        sbGetList.append(StringUtils.formatSingleLine(5,
                "null,"));
        sbGetList.append(StringUtils.formatSingleLine(5,
                "null,"));
        sbGetList.append(StringUtils.formatSingleLine(5,
                "null);"));

        sbGetList.append(StringUtils.formatSingleLine(3,
                "if (cursor != null && cursor.moveToFirst()) {"));
        sbGetList.append(StringUtils.formatSingleLine(4,
                "do {"));
        sbGetList.append(StringUtils.formatSingleLine(5,
                clazz.getName() + " data = new " + clazz.getName() + "();"));
        sbGetList.append(getDbSetDataStr(clazz, 5));
        sbGetList.append(StringUtils.formatSingleLine(5,
                "datas.add(data);"));
        sbGetList.append(StringUtils.formatSingleLine(4,
                "} while (cursor.moveToNext());"));
        sbGetList.append(StringUtils.formatSingleLine(3,
                "}"));
        sbGetList.append(StringUtils.formatSingleLine(2,
                "} finally {"));
        sbGetList.append(StringUtils.formatSingleLine(3,
                "if (cursor != null) cursor.close();"));
        sbGetList.append(StringUtils.formatSingleLine(2,
                "}"));
        sbGetList.append(StringUtils.formatSingleLine(2,
                "return datas;"));
        sbGetList.append(StringUtils.formatSingleLine(1,
                "}"));
        System.out.println(sbGetList.toString());


        // TODO: 2017/8/29
//        if (primaryKeyField != null) {
//            // 有自定义主键，才提供根据主键删除对象方法
//            // delete data
//            StringBuilder sbDelete = new StringBuilder();
//            sbDelete.append(StringUtils.formatSingleLine(1,
//                    "public void delete" + clazz.getName() + "(" + clazz.getName() + " data) {"));
//            sbDelete.append(StringUtils.formatSingleLine(2,
//                    "SQLiteDatabase db = helper.getWritableDatabase();"));
//            sbDelete.append(StringUtils.formatSingleLine(2,
//                    "db.delete(DataContract." + clazz.getName() + ".TABLE_NAME,"));
//            sbDelete.append(StringUtils.formatSingleLine(4,
//                    "DataContract." + clazz.getName() + "." + getColumnString(primaryKeyField) + " + \"=?\","));
//            sbDelete.append(StringUtils.formatSingleLine(4,
//                    "new String[]{" + valueOfGet(primaryKeyField, "data." + primaryKeyField.getGetMethod()) + "});"));
//            sbDelete.append(StringUtils.formatSingleLine(1,
//                    "}"));
//            System.out.println(sbDelete.toString());
//        }


        // delete data list
        StringBuilder sbDeleteList = new StringBuilder();
        sbDeleteList.append(StringUtils.formatSingleLine(1, "public void delete" + clazz.getName() + "List() {"));
        sbDeleteList.append(StringUtils.formatSingleLine(2,
                "SQLiteDatabase db = helper.getWritableDatabase();"));
        sbDeleteList.append(StringUtils.formatSingleLine(2,
                "db.delete(DataContract." + clazz.getName() + ".TABLE_NAME, null, null);"));
        sbDeleteList.append(StringUtils.formatSingleLine(1,
                "}"));
        System.out.println(sbDeleteList.toString());

    }

    private static String getDbSetDataStr(PsiClass clazz, int tableNum) {
        StringBuilder sbDbSetDataStr = new StringBuilder();

        for (PsiField field : clazz.getFields()) {
            String cursorGetStr;
            switch (field.getName()) {
                case "int":
                case "Integer":
                    cursorGetStr = "cursor.getInt(cursor.getColumnIndex(DataContract."
                            + clazz.getName() + "." + getColumnString(field) + "))";
                    break;
                case "long":
                case "Long":
                    cursorGetStr = "cursor.getLong(cursor.getColumnIndex(DataContract."
                            + clazz.getName() + "." + getColumnString(field) + "))";
                    break;
                case "float":
                case "Float":
                    cursorGetStr = "cursor.getFloat(cursor.getColumnIndex(DataContract."
                            + clazz.getName() + "." + getColumnString(field) + "))";
                    break;
                case "double":
                case "Double":
                    cursorGetStr = "cursor.getDouble(cursor.getColumnIndex(DataContract."
                            + clazz.getName() + "." + getColumnString(field) + "))";
                    break;
                case "boolean":
                case "Boolean":
                    cursorGetStr = "cursor.getInt(cursor.getColumnIndex(DataContract."
                            + clazz.getName() + "." + getColumnString(field) + ")) == 1";
                    break;
                case "String":
                default:
                    cursorGetStr = "cursor.getString(cursor.getColumnIndex(DataContract."
                            + clazz.getName() + "." + getColumnString(field) + "))";
                    break;
            }
            // TODO: 2017/8/29  
//            sbDbSetDataStr.append(StringUtils.formatSingleLine(tableNum,
//                    "data." + String.format(field.getSetMethod(), cursorGetStr) + ";"));
        }
        return sbDbSetDataStr.toString();
    }

    private static String getDataGetMethod(PsiField field) {
//        String value = "data." + field.getGetMethod();
//        if (field.type.equals("boolean") || field.type.equals("Boolean")) {
//            value += " ? 1 : 0";
//        }
//        return value;

        // TODO: 2017/8/29  
        return null;
    }

    private static String getColumnString(PsiField field) {
        return StringUtils.camel2underline(field.getName()).toUpperCase();
    }

    private static String valueOfGet(ClassInfo.ClassField field, String value) {
        if (field.type.equals("String")) {
            return value;
        } else {
            return "String.valueOf(" + value + ")";
        }
    }
}
