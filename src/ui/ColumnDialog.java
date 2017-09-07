package ui;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.ui.GuiUtils;
import sun.plugin.util.UIUtil;
import utils.UiUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

public class ColumnDialog extends JDialog {

    public static final int ITEM_HEIGHT = 25;

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JPanel header;
    private JPanel list;
    private JPanel footer;
    private JCheckBox cbAll;
    private JCheckBox cbPrimaryKey;

    public ColumnDialog(PsiClass clazz) {
        UiUtils.centerDialog(this, 600, 400);
        setLocationRelativeTo(null);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        // header
        createHeader();

        // list
        createItem(null); // default _ID
        for (PsiField field : clazz.getFields()) {
            createItem(field);
        }

        // footer
        createFooter();

        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
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
    private void createItem(PsiField field) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, ITEM_HEIGHT));

        panel.add(Box.createHorizontalStrut(10));

        JCheckBox checkBox1 = new JCheckBox();
        checkBox1.setPreferredSize(new Dimension(40, ITEM_HEIGHT));
        checkBox1.setSelected(true);
        if(field != null) {
            checkBoxes.add(checkBox1);
        } else {
            checkBox1.setEnabled(false);
        }
        panel.add(checkBox1);

        JLabel label2 = new JLabel();
        if(field != null) {
            label2.setText(field.getName());
        } else {
            label2.setText("_ID [default]");
            label2.setEnabled(false);
        }
        label2.setPreferredSize(new Dimension(80, ITEM_HEIGHT));
        panel.add(label2);

        panel.add(Box.createHorizontalGlue());

        JRadioButton radioButton = new JRadioButton();
        buttonGroup.add(radioButton);
        if(field == null) {
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

    private void onOK() {
// add your code here
        dispose();
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
    }
}
