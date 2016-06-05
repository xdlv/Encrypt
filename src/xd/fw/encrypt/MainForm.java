package xd.fw.encrypt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2016/5/29.
 */
public class MainForm {
    public MainForm() {
        browseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (fileChooser == null) {
                    fileChooser = new JFileChooser();
                    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                }
                int ret = fileChooser.showOpenDialog(((JButton) e.getSource()).getParent());
                if (ret != JFileChooser.APPROVE_OPTION) {
                    return;
                }
                filePath.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });
        encryptButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        execute(true);
                    }
                });
            }
        });
        decryptButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        execute(false);
                    }
                });
            }
        });
    }

    void execute(boolean encrypt) {
        if (!checkCondition()) {
            return;
        }
        File file = new File(filePath.getText());
        if (!file.exists()) {
            log("file do not exist:" + file, -1);
            return;
        }
        buttonEnabled(false);
        ArrayList<File> allFiles = new ArrayList<>();
        searchFile(file,allFiles,encrypt ? null : ".ec");
        File destFile = new File(file, "tmp");
        String key = keyField.getText().trim();

        int i=1;
        for (File f: allFiles){
            log("start to process:" + f.getName(),100 * i++/allFiles.size());
            doEncrypt(f, destFile, encrypt, key);
        }

        JOptionPane.showMessageDialog(contentPanel, "Operation completed successfully. ");
        buttonEnabled(true);
    }

    void searchFile(File dir, ArrayList<File> allFiles, String prefix){
        File[] files = dir.listFiles();
        for (File f: files){
            if (f.isDirectory()){
                searchFile(f,allFiles, prefix);
            } else {
                if (prefix == null || f.getName().endsWith(prefix)){
                    allFiles.add(f);
                }
            }
        }
    }

    void doEncrypt(File src, File dest, boolean encrypt, String key) {
        File destFile;
        String relativePath = getRelativePath(src, dest.getParentFile());
        if (encrypt){
            destFile = new File(dest, relativePath + ".ec");
        } else {
            destFile = new File(dest, relativePath.substring(0,relativePath.length() -3));
        }
        File destParent = destFile.getParentFile();
        if (!destParent.exists() && !destParent.mkdirs()){
            log("can not create destination dir:" + destParent,-1);
            return;
        }
        encrypt(src,destFile,encrypt,key);
    }

    void encrypt(File src, File destFile, boolean encrypt, String key){
        byte[] buffer = new byte[SIZE];
        InputStream ins = null;
        OutputStream out = null;
        try {
            ins = new FileInputStream(src);
            out = new FileOutputStream(destFile, false);
            int length;
            while ((length = ins.read(buffer)) == 1024) {
                if (encrypt) {
                    out.write(DESUtil.encrypt(buffer, key));
                } else {
                    out.write(DESUtil.decrypt(buffer, key));
                }
            }
            if (length > 0) {
                out.write(buffer, 0, length);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (ins != null) {
                try {
                    ins.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    String getRelativePath(File src, File root){
        try {
            return src.getCanonicalPath().substring(root.getCanonicalPath().length());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void buttonEnabled(boolean enabled) {
        browseButton.setEnabled(enabled);
        encryptButton.setEnabled(enabled);
        decryptButton.setEnabled(enabled);
    }

    private boolean checkCondition() {
        if (filePath.getText().length() < 1 || keyField.getText().length() < 16) {
            log("please fill file and key's size must be 16 at least.", -1);
            return false;
        }
        File file = new File(filePath.getText());
        if (!file.exists()) {
            log("file do not exist:" + file, -1);
            return false;
        }
        return true;
    }


    private final int SIZE = 1024;

    private void log(String log, int progress) {
        logArea.append(log + "\n");
        int height = 20;
        Point p = new Point();
        p.setLocation(0, this.logArea.getLineCount() * height);
        jscrollPanel.getViewport().setViewPosition(p);

        if (progress > 0) {
            progressBar.setValue(progress);
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Encrypt / Decrypt Tool");
        frame.setContentPane(new MainForm().contentPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 500);
        frame.setVisible(true);
    }

    private Executor executor = Executors.newSingleThreadExecutor();
    private JTextField filePath;
    private JButton browseButton;
    private JTextArea logArea;
    private JButton encryptButton;
    private JButton decryptButton;
    private JPanel contentPanel;
    private JTextField keyField;
    private JProgressBar progressBar;
    private JScrollPane jscrollPanel;
    private JFileChooser fileChooser;
}
