package fri.servers.hiking.emergencyalert.ui.swing.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

/**
 * A TabbedPane that shows an "Add" button to add more tabs,
 * and a "Remove" button on each tab to close it.
 */
public class EditableTabbedPane extends JTabbedPane
{
    /** Overriders create this when adding a new tab. */
    public record NewTab(String title, JComponent component)
    {
    }
    
    private static Icon closeIcon = UIManager.getIcon("InternalFrame.closeIcon");
    
    private String removeButtonTooltip;
    
    public EditableTabbedPane() {
        this("Add Tab", "Remove Tab");
    }
    
    public EditableTabbedPane(String addButtonTooltip, String removeButtonTooltip) {
        this.removeButtonTooltip = removeButtonTooltip;
        
        addTab("", null); // first tab will be "+"
        
        final JButton addButton = new JButton("+");
        addButton.setToolTipText(addButtonTooltip);
        addButton.setContentAreaFilled(false);
        addButton.setBorderPainted(false);
        addButton.setFocusable(false);
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final int newIndex = getTabCount() - 1;
                final NewTab newTabPanel = newTab(newIndex);
                if (newTabPanel != null) {
                    addTab(newTabPanel.title(), newTabPanel.component());
                    setSelectedIndex(newIndex);
                }
            }
        });
        setTabComponentAt(0, addButton);
    }

    /** Overridden to always add directly before "+" tab button. */
    @Override
    public void addTab(String title, Component component) {
        final int index = getTabCount() - 1;
        if (index >= 0) { // not first call when only "+" tab is present
            insertTab(title, null, component, null, index);
            if (shouldAddCloseButtonAt(index))
                setTabComponentAt(index, buildCloseTabButton(title, component));
        }
        else {
            super.addTab(title, component);
        }
    }
    
    /** Does not deliver the "+" tab. */
    public List<JComponent> getTabs() {
        final List<JComponent> tabs = new ArrayList<>();
        for (int i = 0; i < getTabCount() - 1; i++)
            tabs.add((JComponent) getComponentAt(i));
        return tabs;
    }
    
    /** Does not let remove the "+" tab. */
    @Override
    public void removeTabAt(int index) {
        if (index < getTabCount() - 1) {
            super.removeTabAt(index);
            setSelectedIndex(Math.max(0, index - 1));
        }
    }
    
    /** Overridden to set tab title also to tab-Component. */
    @Override
    public void setTitleAt(int index, String title) {
        super.setTitleAt(index, title);
        
        final Component tabComponent = getTabComponentAt(index);
        if (tabComponent instanceof CloseableTabHeader)
            ((CloseableTabHeader) tabComponent).titleLabel.setText(title);
    }
    
    @Override
    public void setSelectedIndex(int index) {
        if (index < getTabCount() - 1)
            super.setSelectedIndex(index);
    }
    
    /** Override this to create a new tab. */
    protected NewTab newTab(int tabIndex) {
        return new NewTab("Tab "+tabIndex, new JLabel("Component at "+tabIndex, JLabel.CENTER));
    }

    /** @return true when a close button should be present at given index, else false. */
    protected boolean shouldAddCloseButtonAt(int index) {
        return true;
    }

    /**
     * Override this to listen to tab closing.
     * @param tabComponent the tab about to be closed.
     * @param title the title of the tab to remove.
     * @return true when tab was actually closed, false when vetoed.
     */
    protected void closeTab(String title, Component tabComponent) {
        for (int i = 0; i < getTabCount(); i++) {
            if (getComponentAt(i) == tabComponent) {
                removeTabAt(i);
                return;
            }
        }
    }


    private JComponent buildCloseTabButton(String title, final Component tabComponent) {
        return new CloseableTabHeader(title, removeButtonTooltip, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final List<JComponent> tabs = getTabs();
                String title = "";
                for (int i = 0; i < tabs.size(); i++)
                    if (tabs.get(i) == tabComponent)
                        title = getTitleAt(i);
                closeTab(title, tabComponent);
            }
        });
    }
    
    
    private static class CloseableTabHeader extends JPanel
    {
        final JLabel titleLabel;
        final JButton closeButton;
        
        public CloseableTabHeader(String title, String tooltip, ActionListener closeListener) {
            super(new BorderLayout());
            
            setOpaque(false);
            
            closeButton = new JButton(closeIcon);
            closeButton.setBorder(null);
            closeButton.setBorderPainted(false);
            closeButton.setToolTipText(tooltip);
            closeButton.addActionListener(closeListener);
            
            titleLabel = new JLabel(title);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
            
            add(titleLabel, BorderLayout.CENTER);
            add(closeButton, BorderLayout.EAST);
        }
    }

    
    /** Test main. */
    public static void main(String [] args) {
        EditableTabbedPane tabbedPane = new EditableTabbedPane();
        for (int i = 0; i < 2; i++)
            tabbedPane.addTab("Tab "+i, new JLabel("Tab "+i, JLabel.CENTER));

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(tabbedPane);
        frame.setSize(400, 200);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}