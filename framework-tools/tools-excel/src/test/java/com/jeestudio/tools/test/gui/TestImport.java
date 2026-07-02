package com.jeestudio.tools.test.gui;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSONArray;
import com.jeestudio.tools.dict.DictHandler;
import com.jeestudio.tools.excel.ExcelImportUtil;
import com.jeestudio.tools.excel.ExcelUtil;
import org.apache.poi.ss.usermodel.Workbook;

import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class TestImport extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JButton 选择文件Button;
    private JButton 导出模板Button;

    public TestImport() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        选择文件Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser("d:/testExcel/");
                fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                int option = fileChooser.showOpenDialog(选择文件Button);
                if (option == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    System.out.println(file);
                    try {
                        FileInputStream fileInputStream = new FileInputStream(file);
                        List<ExcelTable1> table1s = ExcelImportUtil.importData(ExcelTable1.class, fileInputStream, file.getName(), new ArrayList<>());
                        for (ExcelTable1 table1 : table1s) {
                            System.out.println(table1);
                        }
                    } catch (FileNotFoundException fileNotFoundException) {
                        fileNotFoundException.printStackTrace();
                    }

                } else {
                }
            }
        });
        导出模板Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Workbook workbook = ExcelImportUtil.exportTemplate(ExcelTable1.class);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                try {
                    workbook.write(os);
                    FileUtil.writeBytes(os.toByteArray(),"d:/testExcel/import2.xls");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        TestImport dialog = new TestImport();
        ExcelUtil.init(new DictHandler() {
            @Override
            public LinkedHashMap<Object, Object> getDictionaryByDictCode(String dictCode) {
                return new LinkedHashMap<>();
            }

            @Override
            public LinkedHashMap<Object, Object> getDictionaryByDictTable(String dictTable, String dictValue, String dictText, JSONArray orderCondition) {
                return new LinkedHashMap<>();
            }
        });
        dialog.setLocationByPlatform(true);
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
