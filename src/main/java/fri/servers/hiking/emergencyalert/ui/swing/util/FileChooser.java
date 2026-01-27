package fri.servers.hiking.emergencyalert.ui.swing.util;

import java.io.File;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import fri.servers.hiking.emergencyalert.util.StringUtil;

public class FileChooser
{
    private final JComponent parent;
    private String currentDirectory;
    
    public FileChooser(JComponent parent, String currentDirectory) {
        this.parent = parent;
        this.currentDirectory = currentDirectory;
    }
    
    /**
     * @param singleSelection true for choosing just one file, false for many.
     * @param extension optional, the file extension without leading dot.
     * @return chosen file(s) or null for canceled.
     */
    public File[] open(boolean singleSelection, String extension) {
        final JFileChooser fileChooser = new JFileChooser(currentDirectory);
        fileChooser.setMultiSelectionEnabled(singleSelection);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        if (StringUtil.isNotEmpty(extension))
            fileChooser.setFileFilter(new FileNameExtensionFilter("."+extension, extension));

        if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            final File[] files = fileChooser.getSelectedFiles();
            // save directory for next call
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