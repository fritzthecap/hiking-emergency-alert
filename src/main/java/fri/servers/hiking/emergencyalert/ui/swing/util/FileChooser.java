package fri.servers.hiking.emergencyalert.ui.swing.util;

import java.io.File;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import fri.servers.hiking.emergencyalert.persistence.HikeFileManager;
import fri.servers.hiking.emergencyalert.util.StringUtil;

public class FileChooser
{
    private final JComponent parent;
    private String currentDirectory = new HikeFileManager().getSavePath();
    
    public FileChooser(JComponent parent, String currentDirectory) {
        this.parent = parent;
        
        if (StringUtil.isNotEmpty(currentDirectory))
            this.currentDirectory = currentDirectory;
    }
    
    /**
     * @param singleSelection true for choosing just one file, false for many.
     * @param extension optional, the file extension without leading dot.
     * @return chosen file(s) or null for canceled.
     */
    public File[] open(boolean singleSelection, String extension) {
        if (StringUtil.isNotEmpty(currentDirectory))
            currentDirectory = new HikeFileManager().ensurePathExists(currentDirectory);
        
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
    
    public File save(File suggestedFile) {
        final String directory = suggestedFile.getParent();
        
        final JFileChooser fileChooser = new JFileChooser((directory != null) ? directory : currentDirectory);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setSelectedFile(suggestedFile);
        
        if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            final File file = fileChooser.getSelectedFile();
            if (file.isDirectory())
                return new File(file, suggestedFile.getName());

            return fileChooser.getSelectedFile();
        }
        return null;
    }
}