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
import fri.servers.hiking.emergencyalert.persistence.HikeFileManager;
import fri.servers.hiking.emergencyalert.persistence.JsonGsonSerializer;
import fri.servers.hiking.emergencyalert.persistence.entities.Hike;
import fri.servers.hiking.emergencyalert.statemachine.StateMachine;
import fri.servers.hiking.emergencyalert.ui.swing.util.SwingLanguage;
import fri.servers.hiking.emergencyalert.ui.swing.util.SwingUtil;
import fri.servers.hiking.emergencyalert.ui.swing.wizard.pages.ActivationPage;
import fri.servers.hiking.emergencyalert.ui.swing.wizard.pages.ContactsPage;
import fri.servers.hiking.emergencyalert.ui.swing.wizard.pages.LanguageAndFileLoadPage;
import fri.servers.hiking.emergencyalert.ui.swing.wizard.pages.MailConfigurationPage;
import fri.servers.hiking.emergencyalert.ui.swing.wizard.pages.MailTextsPage;
import fri.servers.hiking.emergencyalert.ui.swing.wizard.pages.ObservationPage;
import fri.servers.hiking.emergencyalert.ui.swing.wizard.pages.RouteAndTimesPage;
import fri.servers.hiking.emergencyalert.util.Language;

/**
 * Lets edit <code>Hike</code> data, on a number of pages,
 * finally lets start that hike.
 */
public class HikeWizard extends JPanel // must be a JComponent to be found by SwingAlertHomeServer.showConfirmMail
{
    private final StateMachine stateMachine;
    
    private final JFrame frame;
    private final JPanel contentPanel;
    private final JPanel glassPane;
    private final DescriptionArea descriptionArea;
    private final WizardOutline wizardOutline;
    
    private JButton backwardButton;
    private JButton forwardButton;
    
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
        frame.setGlassPane(this.glassPane = buildGlassPane());
        this.descriptionArea = new DescriptionArea();
        this.wizardOutline = new WizardOutline(pages.length);
        
        // START keep order of statements!
        final boolean fileLoaded = loadDefaultHike();
        
        // before building UI, load a language resource-bundle
        final String hikeLanguage = stateMachine.getHike().getAlert().getIso639Language();
        Language.load(hikeLanguage);
        // resource bundle is loaded, can use i18n() from now on
        SwingLanguage.setJOptionPaneButtonLabels();
        
        buildUi();
        
        if (fileLoaded) // language was loaded from persistent hike
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

    
    private boolean loadDefaultHike() {
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
        return fileLoaded;
    }
    
    private String readDefaultHikeJson() {
        try {
            return new HikeFileManager().load();
        }
        catch (IOException e) { // ignore missing default file
            System.err.println(e.getMessage());
            return null;
        }
    }
    
    private void buildUi() {
        final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.3);
        splitPane.setOneTouchExpandable(true);
        add(splitPane, BorderLayout.CENTER);
        splitPane.setLeftComponent(descriptionArea.getAddablePanel());
        splitPane.setRightComponent(contentPanel);
        
        this.backwardButton = new JButton(Trolley.buildPreviousButtonText());
        this.forwardButton = new JButton(Trolley.buildNextButtonText());
        
        SwingUtil.makeComponentFocusable(forwardButton); // click on even disabled button will trigger validation

        final ActionListener browsePagesListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                glassPane.setVisible(true);
                try {
                    final boolean next = (e.getSource() == forwardButton);
                    final int newIndex = (next ? pageIndex + 1 : pageIndex - 1);
                    changePage(newIndex, false);
                }
                finally {
                    glassPane.setVisible(false);
                }
            }
        };
        backwardButton.addActionListener(browsePagesListener);
        forwardButton.addActionListener(browsePagesListener);
        
        // layout
        
        final JPanel buttonGrid = new JPanel(new GridLayout(1, 2, 12, 0)); // gives buttons same size
        buttonGrid.add(backwardButton);
        buttonGrid.add(forwardButton);
        
        final JPanel buttonsAndOutline = new JPanel(new BorderLayout());
        buttonsAndOutline.add(wizardOutline, BorderLayout.NORTH);
        buttonsAndOutline.add(buttonGrid, BorderLayout.CENTER);
        
        final JPanel buttonPanel = new JPanel(new FlowLayout()); // centers buttons
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        buttonPanel.add(buttonsAndOutline);
        
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void changePage(int newIndex, boolean isFirstCall) {
        final boolean goingForward = (isFirstCall || pageIndex < newIndex);
        final AbstractWizardPage oldPage = page();
        
        final Trolley trolley;
        if (isFirstCall) // application startup
            trolley = createTrolley(); // travels through all pages
        else
            if ((trolley = oldPage.leave(goingForward)) != null) // can leave
                contentPanel.remove(oldPage.getAddablePanel());
            else
                return; // page does not allow to skip

        pageIndex = newIndex;
        
        wizardOutline.setHighlight(pageIndex);
        
        final AbstractWizardPage newPage = pages[pageIndex];
        contentPanel.add(newPage.getAddablePanel(), BorderLayout.CENTER);
        
        descriptionArea.loadTextFor(newPage.getClass());
        
        setButtonsEnabled(); // do this before enter() to allow "Next" disabled by page
        
        newPage.enter(trolley, goingForward);
        
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private Trolley createTrolley() {
        return new Trolley(
                stateMachine, 
                descriptionArea,
                new Trolley.PageRequestListener() {
                    @Override
                    public void gotoPage(Class<? extends AbstractWizardPage> requestedPage) {
                        changePage(determinePageIndex(requestedPage), false);
                    }
                },
                forwardButton, 
                backwardButton); // travels through all pages
    }

    private int determinePageIndex(Class<? extends AbstractWizardPage> requestedPage) {
        for (int i = 0; i < pages.length; i++)
            if (pages[i].getClass().equals(requestedPage))
                return i;
        
        throw new IllegalArgumentException("Unknown page class: "+requestedPage);
    }

    private void setButtonsEnabled() {
        backwardButton.setEnabled(pageIndex > 0);
        forwardButton.setEnabled(pageIndex < pages.length - 1);
    }
    
    private AbstractWizardPage page() {
        return pages[pageIndex];
    }
    
    private JPanel buildGlassPane() {
        final JPanel glassPane = new JPanel();
        glassPane.setOpaque(false); // else windowClosing() shows gray window
        final MouseInputAdapter adapter = new MouseInputAdapter() { };
        glassPane.addMouseListener(adapter);
        glassPane.addMouseMotionListener(adapter);
        glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        frame.setGlassPane(glassPane);
        return glassPane;
    }
}