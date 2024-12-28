package com.strangeone101.platinumarenas;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.function.Function;

public class DebugMain {

    public static void main(String[] args) {
        System.out.println("If you can see this, a GUI has now been opened. Please use that. \n\n" +
                "No, I will not add console support for debug features lol");

        String option1 = "Decompress Arena";
        String option2 = "Compress Arena";
        String option3 = "Exit";
        int value = JOptionPane.showOptionDialog(null, "Please select a task", "PlatinumArena File IO", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[] {option1, option2, option3}, option1);

        if (value == JOptionPane.CANCEL_OPTION) {
            System.exit(0);
        }

        try {
            Function<byte[], byte[]> function = Util::compress;
            String textFunction = "compress";
            String outExtension = "datc";
            String inExtension = "dat";

            if (value == JOptionPane.YES_OPTION) {
                function = Util::decompress;
                textFunction = "decompress";
                outExtension = "dat";
                inExtension = "datc";
            }

            File pt = new File("PlatinumArenas");
            JFileChooser chooser = new JFileChooser();
            if (pt.exists()) {
                File arenas = new File(pt, "Arenas");
                chooser = new JFileChooser(arenas);
            }
            chooser.setDialogTitle("Choose arena to " + textFunction);
            chooser.setFileFilter(new FileNameExtensionFilter("Arena Files", inExtension));
            int result = chooser.showOpenDialog(null);
            if (result == 0) {
                File file = chooser.getSelectedFile();

                byte[] bytes = Files.readAllBytes(file.toPath());

                byte[] newBytes = function.apply(bytes);

                if (newBytes == null) {
                    JOptionPane.showMessageDialog(null, "An error occurred " + textFunction + "ing the file!", "Error", JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }

                File newFile = new File(file.getParentFile(), file.getName() + "." + outExtension);

                Files.write(newFile.toPath(), newBytes, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE, StandardOpenOption.CREATE);

                JOptionPane.showMessageDialog(null, "Successfully " + textFunction + "ed arena to new file " + newFile.getName() + "!", "Sucess!", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }


    }
}
