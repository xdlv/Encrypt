package xd.fw.encrypt;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

/**
 * Created by Administrator on 2016/5/29.
 */
public class MainForm {
    public MainForm() {
        browseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (fileChooser == null){
                    fileChooser = new JFileChooser();
                    fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                }
                int ret = fileChooser.showOpenDialog(((JButton) e.getSource()).getParent());
                if (ret != JFileChooser.APPROVE_OPTION){
                    return;
                }
                filePath.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });
        encryptButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!checkCondition()){
                    return;
                }
                File file = new File(filePath.getText());
                if (file.isFile()){
                    log("start to encrypt..");
                    encrypt(file, true);
                    log("encryption ends");
                } else {
                    File[] files = file.listFiles();
                    if (files == null || files.length == 0){
                        log("the directory is empty, please choose another.");
                        return;
                    }
                    for (File f: files){
                        encrypt(f, true);
                    }
                }
            }
        });
        decryptButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!checkCondition()){
                    return;
                }

                File file = new File(filePath.getText());
                if (!file.exists()){
                    log("file do not exist:" + file);
                    return;
                }
                if (!file.getName().endsWith(".ec")){
                    log("file must end with .ec");
                    return;
                }
                if (file.isFile()){
                    log("start to decrypt..");
                    encrypt(file, false);
                    log("decryption ends");
                } else {
                    File[] files = file.listFiles();
                    if (files == null || files.length == 0){
                        log("the directory is empty, please choose another.");
                        return;
                    }
                    for (File f: files){
                        encrypt(f, false);
                    }
                }
            }
        });
    }

    private boolean checkCondition(){
        if (filePath.getText().length() < 1 || keyField.getText().length() < 1){
            log("please fill file and key.");
            return false;
        }
        File file = new File(filePath.getText());
        if (!file.exists()){
            log("file do not exist:" + file);
            return false;
        }
        return true;
    }


    private final int SIZE = 1024;
    private void encrypt(File src, boolean encrypt){
        InputStream ins = null;
        OutputStream out = null;
        byte[] buffer = new byte[SIZE];
        try {
            ins = new FileInputStream(src);
            File dest;
            if (encrypt){
                dest = new File(src.getAbsolutePath() + ".ec");
            } else {
                dest = new File(src.getAbsolutePath() + ".dc");
            }
            if (dest.exists() && !dest.delete()){
                throw new RuntimeException("can not delete dest file");
            }
            out = new FileOutputStream(dest,true);
            int length;
            String key = keyField.getText().trim();
            while ((length = ins.read(buffer)) == 1024){
                if (encrypt){
                    out.write(DESUtil.encrypt(buffer,key));
                } else {
                    out.write(DESUtil.decrypt(buffer, key));
                }
            }
            if (length > 0){
                out.write(buffer,0,length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ins != null){
                try {
                    ins.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null){
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void log(String log){
        logArea.append(log + "\n");
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Encrypt / Decrypt Tool");
        frame.setContentPane(new MainForm().contentPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800,500);
        frame.setVisible(true);
    }

    private JTextField filePath;
    private JButton browseButton;
    private JTextArea logArea;
    private JButton encryptButton;
    private JButton decryptButton;
    private JPanel contentPanel;
    private JTextField keyField;
    private JFileChooser fileChooser;
}
