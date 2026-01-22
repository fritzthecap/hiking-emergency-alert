package fri.servers.hiking.emergencyalert.ui.swing.wizard;

import static fri.servers.hiking.emergencyalert.util.Language.i18n;
import java.awt.BorderLayout;
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
import fri.servers.hiking.emergencyalert.persistence.Hike;
import fri.servers.hiking.emergencyalert.persistence.HikeFileManager;
import fri.servers.hiking.emergencyalert.persistence.JsonGsonSerializer;
import fri.servers.hiking.emergencyalert.statemachine.StateMachine;
import fri.servers.hiking.emergencyalert.ui.swing.wizard.pages.*;

/**
 * Lets edit <code>Hike</code> data, on a number of pages,
 * finally lets start that hike.
 */
public class HikeWizard extends JPanel
{
    private final JFrame frame;
    private final StateMachine stateMachine;
    private final JPanel contentPanel;
    private final JSplitPane leftSplitPane;
    private final JSplitPane rightSplitPane;
    
    private JButton previousButton;
    private JButton nextButton;
    
    private AbstractWizardPage[] pages = new AbstractWizardPage[] {
        new LanguagePage(),
        new ContactsPage(),
        new MailTextsPage(),
        new IntervalsPage(),
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
        
        leftSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        leftSplitPane.setResizeWeight(0.33);
        leftSplitPane.setOneTouchExpandable(true);
        leftSplitPane.setLeftComponent(new JPanel()); // TODO: HTML description texts
        
        rightSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        rightSplitPane.setResizeWeight(0.5);
        rightSplitPane.setOneTouchExpandable(true);
        rightSplitPane.setLeftComponent(contentPanel);
        rightSplitPane.setRightComponent(new JPanel()); // TODO: consoles
        
        leftSplitPane.setRightComponent(rightSplitPane);
        
        add(leftSplitPane, BorderLayout.CENTER);
        
        buildUi();
        
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (page().windowClosing()) {
                    frame.dispose(); // exits only when no thread is running!
                    System.exit(0);
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

    private void buildUi() {
        final String defaultHikeJson = readDefaultHikeJson();
        if (defaultHikeJson != null) {
            try {
                final Hike hike = new JsonGsonSerializer<Hike>().fromJson(defaultHikeJson, Hike.class);
                stateMachine.getUserInterface().registerHike(hike);
                pageIndex = mailConfigurationPageIndex(); // for entering password
            }
            catch (IOException e) {
                JOptionPane.showMessageDialog(frame, e.toString());
            }
        }
        contentPanel.add(buildButtonBar(), BorderLayout.SOUTH);
        changePage(0, true);
    }
    
    private JPanel buildButtonBar() {
        this.previousButton = new JButton("< "+i18n("Previous"));
        this.nextButton = new JButton(i18n("Next")+" >");
        final ActionListener skipListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final boolean next = (e.getSource() == nextButton);
                final int newIndex = (next ? pageIndex + 1 : pageIndex - 1);
                changePage(newIndex, false);

            }
        };
        previousButton.addActionListener(skipListener);
        nextButton.addActionListener(skipListener);
        
        final JPanel grid = new JPanel(new GridLayout(1, 2, 12, 0)); // gives buttons same size
        grid.add(previousButton);
        grid.add(nextButton);
        
        final JPanel panel = new JPanel(new FlowLayout()); // centers buttons
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(grid);
        
        return panel;
    }

    private String readDefaultHikeJson() {
        try {
            return new HikeFileManager().load();
        }
        catch (IOException e) { // ignore missing default file
            return null;
        }
    }
    
    private int mailConfigurationPageIndex() {
        for (int i = 0; i < pages.length; i++)
            if (pages[i] instanceof MailConfigurationPage)
                return i;
        throw new IllegalStateException("MailConfigurationPage not in pages!");
    }

    private void changePage(int newIndex, boolean isFirstCall) {
        final boolean goingForward = (pageIndex < newIndex);
        final AbstractWizardPage oldPage = page();
        
        final Trolley trolley;
        if (isFirstCall)
            trolley = new Trolley(stateMachine);
        else
            if ((trolley = oldPage.leave(goingForward)) != null) // can leave
                contentPanel.remove(oldPage.getAddablePanel());
            else
                return; // page does not allow to skip

        final AbstractWizardPage newPage = pages[newIndex];
        contentPanel.add(newPage.getAddablePanel(), BorderLayout.CENTER);
        pageIndex = newIndex;
        
        newPage.enter(trolley, goingForward);
        
        contentPanel.revalidate();
        contentPanel.repaint();
        
        setButtonsEnabled();
    }
    
    private void setButtonsEnabled() {
        previousButton.setEnabled(pageIndex > 0);
        nextButton.setEnabled(pageIndex < pages.length - 1);
    }
    
    private AbstractWizardPage page() {
        return pages[pageIndex];
    }
}