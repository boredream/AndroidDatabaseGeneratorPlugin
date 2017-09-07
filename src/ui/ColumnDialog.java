package ui;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.sun.deploy.panel.RadioPropertyGroup;
import utils.UiUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class ColumnDialog extends JDialog {

    public static final int ITEM_HEIGHT = 25;

    private PsiClass clazz;

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JPanel header;
    private JPanel list;
    private JPanel footer;
    private JCheckBox cbAll;

    public ColumnDialog(PsiClass clazz) {
        UiUtils.centerDialog(this, 600, 400);
        setLocationRelativeTo(null);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        this.clazz = clazz;

        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        // header
        createHeader();

        // list
        createItem(-1); // default _ID
        for (int i = 0; i < clazz.getFields().length; i++) {
            createItem(i);
        }

        // footer
        createFooter();

        buttonOK.addActionListener(e -> performGenerate());
        buttonCancel.addActionListener(e -> dispose());
    }

    private void createHeader() {
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));

        header.add(Box.createHorizontalStrut(10));

        JLabel label1 = new JLabel("use");
        label1.setPreferredSize(new Dimension(40, ITEM_HEIGHT));
        header.add(label1);

        JLabel label2 = new JLabel("name");
        label2.setPreferredSize(new Dimension(80, ITEM_HEIGHT));
        header.add(label2);

        header.add(Box.createHorizontalGlue());

        JLabel label3 = new JLabel("primary key");
        header.add(label3);

        header.add(Box.createHorizontalStrut(10));
    }

    private ArrayList<JCheckBox> checkBoxes = new ArrayList<>();
    private ButtonGroup buttonGroup = new ButtonGroup();
    private int priKeyPosition;
    private void createItem(int position) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, ITEM_HEIGHT));

        panel.add(Box.createHorizontalStrut(10));

        JCheckBox checkBox1 = new JCheckBox();
        checkBox1.setPreferredSize(new Dimension(40, ITEM_HEIGHT));
        checkBox1.setSelected(true);
        if(position >= 0) {
            checkBoxes.add(checkBox1);
        } else {
            checkBox1.setEnabled(false);
        }
        panel.add(checkBox1);

        JLabel label2 = new JLabel();
        if(position >= 0) {
            label2.setText(clazz.getFields()[position].getName());
        } else {
            label2.setText("_ID [default]");
            label2.setEnabled(false);
        }
        label2.setPreferredSize(new Dimension(80, ITEM_HEIGHT));
        panel.add(label2);

        panel.add(Box.createHorizontalGlue());

        JRadioButton radioButton = new JRadioButton();
        radioButton.addChangeListener(e -> {
            if(radioButton.isSelected()) {
                priKeyPosition = position;
            }
        });
        buttonGroup.add(radioButton);
        if(position == -1) {
            radioButton.setSelected(true);
        }
        panel.add(radioButton);

        panel.add(Box.createHorizontalStrut(10));

        list.add(panel);
    }

    private void createFooter() {
        footer.setLayout(new BoxLayout(footer, BoxLayout.X_AXIS));
        footer.add(Box.createHorizontalStrut(10));

        cbAll = new JCheckBox("use all");
        cbAll.setSelected(true);
        cbAll.addChangeListener(e -> {
            for (JCheckBox checkBox : checkBoxes) {
                checkBox.setSelected(cbAll.isSelected());
            }
        });
        footer.add(cbAll);
    }

    private void performGenerate() {
        ArrayList<PsiField> fields = new ArrayList<>();
        PsiField priKeyField = null;
        for (int i = 0; i < checkBoxes.size(); i++) {
            if(checkBoxes.get(i).isSelected()) {
                fields.add(clazz.getFields()[i]);
                if(i == priKeyPosition) {
                    priKeyField = clazz.getFields()[i];
                }
            }
            buttonGroup.getSelection();
        }

        if(onGenerateListener != null) {
            onGenerateListener.onGenerate(fields, priKeyField);
        }
        dispose();
    }

    private OnGenerateListener onGenerateListener;

    public void setOnGenerateListener(OnGenerateListener onGenerateListener) {
        this.onGenerateListener = onGenerateListener;
    }

    public interface OnGenerateListener {
        void onGenerate(ArrayList<PsiField> fields, PsiField priKeyField);
    }
}
