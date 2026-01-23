package fri.servers.hiking.emergencyalert.ui.swing.util;

import java.io.File;
import javax.swing.JComponent;
import javax.swing.JFileChooser;

public class FileChooser
{
    private final JComponent parent;
    private String currentDirectory;
    
    public FileChooser(JComponent parent, String currentDirectory) {
        this.parent = parent;
        this.currentDirectory = currentDirectory;
    }
    
    public File[] open() {
        final JFileChooser fileChooser = new JFileChooser(currentDirectory);
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            final File[] files = fileChooser.getSelectedFiles();
            currentDirectory = files[0].getParent();
            return files;
        }
        
        return null;
    }
    
//    public File save() {
//        final JFileChooser fileChooser = new JFileChooser(currentDirectory);
//        fileChooser.setMultiSelectionEnabled(false);
//        
//        final int response = fileChooser.showSaveDialog(parent);
//        if (response == JFileChooser.APPROVE_OPTION)
//            return fileChooser.getSelectedFile();
//        
//        return null;
//    }
}