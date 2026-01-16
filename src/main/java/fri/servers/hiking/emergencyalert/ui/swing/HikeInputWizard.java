package fri.servers.hiking.emergencyalert.ui.swing;

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
import fri.servers.hiking.emergencyalert.persistence.Hike;
import fri.servers.hiking.emergencyalert.persistence.HikeFileManager;
import fri.servers.hiking.emergencyalert.persistence.JsonGsonSerializer;
import fri.servers.hiking.emergencyalert.statemachine.StateMachine;
import fri.servers.hiking.emergencyalert.ui.swing.wizardpages.AbstractWizardPage;
import fri.servers.hiking.emergencyalert.ui.swing.wizardpages.LanguagePage;
import fri.servers.hiking.emergencyalert.ui.swing.wizardpages.MailConfigurationPage;

/**
 * Lets edit <code>Hike</code> data and then start that hike.
 */
public class HikeInputWizard extends JPanel
{
    private final JFrame frame;
    private final StateMachine stateMachine;
    
    private AbstractWizardPage page;
    private JButton previousButton;
    private JButton nextButton;
    
    public HikeInputWizard(JFrame frame, StateMachine stateMachine) {
        super(new BorderLayout());
        
        this.frame = frame;
        this.stateMachine = stateMachine;
        
        buildUi();
        
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (page.windowClosing()) {
                    frame.dispose(); // exits only when no thread is running!
                    System.exit(0);
                }
            }
        });
    }

    private void buildUi() {
        final String defaultHikeJson = readDefaultHikeJson();
        if (defaultHikeJson != null) {
            try {
                final Hike hike = new JsonGsonSerializer<Hike>().fromJson(defaultHikeJson, Hike.class);
                stateMachine.getUserInterface().registerHike(hike);
                page = new MailConfigurationPage();
            }
            catch (IOException e) {
                JOptionPane.showMessageDialog(frame, e.toString());
            }
        }
        
        if (page == null)
            page = new LanguagePage();
        
        page.setData(stateMachine);
        
        final JPanel buttonBar = buildButtonBar();
        
        add(page, BorderLayout.CENTER);
        add(buttonBar, BorderLayout.SOUTH);
    }
    
    private JPanel buildButtonBar() {
        this.previousButton = new JButton("< "+i18n("Previous"));
        previousButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setPage(page.getPreviousPage());
            }
        });
        
        this.nextButton = new JButton(i18n("Next")+" >");
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setPage(page.getNextPage());
            }
        });
        
        final JPanel grid = new JPanel(new GridLayout(1, 2, 12, 0)); // gives buttons same size
        grid.add(previousButton);
        grid.add(nextButton);
        
        final JPanel panel = new JPanel(new FlowLayout()); // centers buttons
        final int SPACE = 10;
        panel.setBorder(BorderFactory.createEmptyBorder(SPACE, SPACE, SPACE, SPACE));
        panel.add(grid);
        
        setButtonsEnabled();
        
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
    
    private void setPage(AbstractWizardPage otherPage) {
        remove(page);
        page = otherPage;
        add(page, BorderLayout.CENTER);
        
        revalidate();
        repaint();
        
        setButtonsEnabled();
    }
    
    private void setButtonsEnabled() {
        previousButton.setEnabled(page.hasPreviousPage());
        nextButton.setEnabled(page.hasNextPage());
    }
}