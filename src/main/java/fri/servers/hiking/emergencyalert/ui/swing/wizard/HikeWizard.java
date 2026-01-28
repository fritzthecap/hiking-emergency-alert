package fri.servers.hiking.emergencyalert.ui.swing.wizard;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.MouseInputAdapter;
import fri.servers.hiking.emergencyalert.persistence.Hike;
import fri.servers.hiking.emergencyalert.persistence.HikeFileManager;
import fri.servers.hiking.emergencyalert.persistence.JsonGsonSerializer;
import fri.servers.hiking.emergencyalert.statemachine.StateMachine;
import fri.servers.hiking.emergencyalert.ui.swing.util.SwingUtil;
import fri.servers.hiking.emergencyalert.ui.swing.wizard.pages.*;
import fri.servers.hiking.emergencyalert.util.Language;
import fri.servers.hiking.emergencyalert.util.StringUtil;

/**
 * Lets edit <code>Hike</code> data, on a number of pages,
 * finally lets start that hike.
 */
public class HikeWizard extends JPanel
{
    private final StateMachine stateMachine;
    
    private final JFrame frame;
    private final JPanel contentPanel;
    private final JPanel glassPane;
    
    private JButton previousButton;
    private JButton nextButton;
    
    private AbstractWizardPage[] pages = new AbstractWizardPage[] {
        new LanguageAndFileLoadPage(),
        new ContactsPage(),
        new MailTextsPage(),
        new MailConfigurationPage(),
        new RouteAndTimesPage(),
        new ActivationPage(),
        new ObservationPage(),
    };
    private int pageIndex;
    
    public HikeWizard(JFrame frame, StateMachine stateMachine) {
        super(new BorderLayout());
        
        this.frame = frame;
        this.stateMachine = stateMachine;
        
        this.contentPanel = new JPanel(new BorderLayout());
        SwingUtil.makeComponentFocusable(contentPanel); // lets focus shift away from input fields
        
        glassPane = new JPanel();
        glassPane.setOpaque(false); // else windowClosing() shows gray window
        final MouseInputAdapter adapter = new MouseInputAdapter() { };
        glassPane.addMouseListener(adapter);
        glassPane.addMouseMotionListener(adapter);
        glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        frame.setGlassPane(glassPane);
        
        final JSplitPane leftSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        leftSplitPane.setResizeWeight(0.4);
        leftSplitPane.setOneTouchExpandable(true);
        leftSplitPane.setLeftComponent(new JPanel()); // TODO: HTML description texts
        
        leftSplitPane.setRightComponent(contentPanel);
        
        add(leftSplitPane, BorderLayout.CENTER);
        
        // START keep order of statements!
        final boolean fileLoaded = readDefaultHikeAndLoadLanguage();
        buildUi();
        if (fileLoaded)
            pageIndex = 1; // skip language/file page
        
        changePage(pageIndex, true);
        // END keep order of statements!
        
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                glassPane.setVisible(true);
                try {
                    if (page().windowClosing()) {
                        frame.dispose(); // exits only when no thread is running!
                        System.exit(0);
                    }
                }
                finally {
                    glassPane.setVisible(false);
                }
            }
        });
    }

    /**
     * UserInterface received the alert confirmation mail notification.
     * ObservationPage must change its state now.
     * A dialog rendering the mail will be shown afterwards by calling class.
     */
    public void alertConfirmed() {
        if (page() instanceof ObservationPage)
            ((ObservationPage) page()).alertConfirmed();
    }

    
    private boolean readDefaultHikeAndLoadLanguage() {
        final String defaultHikeJson = readDefaultHikeJson();
        boolean fileLoaded = false;
        if (defaultHikeJson != null) {
            try {
                final Hike recentHike = new JsonGsonSerializer<Hike>().fromJson(defaultHikeJson, Hike.class);
                final Hike newHike = new Hike(); // do not use recent route and times
                newHike.setAlert(recentHike.getAlert()); // but reuse contacts and mail configuration
                
                stateMachine.getUserInterface().registerHike(newHike);
                fileLoaded = true;
            }
            catch (Exception e) {
                JOptionPane.showMessageDialog(frame, e.toString());
            }
        }
        
        // before building UI, load a language resource-bundle
        final String hikeLanguage = stateMachine.getHike().getAlert().getIso639Language();
        if (StringUtil.isNotEmpty(hikeLanguage))
            Language.load(hikeLanguage);
        else
            Language.load(); // default language
        
        return fileLoaded;
    }
    
    private String readDefaultHikeJson() {
        try {
            final HikeFileManager hikeFileManager = new HikeFileManager();
            final String json = hikeFileManager.load();
            return json;
        }
        catch (IOException e) { // ignore missing default file
            System.err.println(e.toString());
            return null;
        }
    }
    
    private void buildUi() {
        this.previousButton = new JButton(Trolley.buildPreviousButtonText()); // is disabled only 
        
        this.nextButton = new JButton(Trolley.buildNextButtonText());
        SwingUtil.makeComponentFocusable(nextButton); // click on even disabled button will trigger validation

        final ActionListener browsePagesListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                glassPane.setVisible(true);
                try {
                    final boolean next = (e.getSource() == nextButton);
                    final int newIndex = (next ? pageIndex + 1 : pageIndex - 1);
                    changePage(newIndex, false);
                }
                finally {
                    glassPane.setVisible(false);
                }
            }
        };
        previousButton.addActionListener(browsePagesListener);
        nextButton.addActionListener(browsePagesListener);
        
        final JPanel grid = new JPanel(new GridLayout(1, 2, 12, 0)); // gives buttons same size
        grid.add(previousButton);
        grid.add(nextButton);
        
        final JPanel panel = new JPanel(new FlowLayout()); // centers buttons
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(grid);
        
        contentPanel.add(panel, BorderLayout.SOUTH);
    }

    private void changePage(int newIndex, boolean isFirstCall) {
        final boolean goingForward = (isFirstCall || pageIndex < newIndex);
        final AbstractWizardPage oldPage = page();
        
        final Trolley trolley;
        if (isFirstCall)
            trolley = new Trolley(stateMachine, nextButton, previousButton);
        else
            if ((trolley = oldPage.leave(goingForward)) != null) // can leave
                contentPanel.remove(oldPage.getAddablePanel());
            else
                return; // page does not allow to skip

        final AbstractWizardPage newPage = pages[newIndex];
        contentPanel.add(newPage.getAddablePanel(), BorderLayout.CENTER);
        pageIndex = newIndex;
        
        setButtonsEnabled(); // do this before enter() to allow "Next" disabled by page
        
        newPage.enter(trolley, goingForward);
        
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private void setButtonsEnabled() {
        previousButton.setEnabled(pageIndex > 0);
        nextButton.setEnabled(pageIndex < pages.length - 1);
    }
    
    private AbstractWizardPage page() {
        return pages[pageIndex];
    }
}