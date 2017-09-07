package utils;

import ui.ColumnDialog;

import java.awt.*;

public class UiUtils {
    public static void centerDialog(ColumnDialog dialog, int width, int height) {
        dialog.setPreferredSize(new Dimension(width, height));

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); //获取屏幕的尺寸
        int screenWidth = screenSize.width; //获取屏幕的宽
        int screenHeight = screenSize.height; //获取屏幕的高
        dialog.setLocation(screenWidth / 2 - width / 2, screenHeight / 2 - height / 2);//设置窗口居中显示
    }
}
