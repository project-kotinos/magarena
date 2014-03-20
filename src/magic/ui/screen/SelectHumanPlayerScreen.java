package magic.ui.screen;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import magic.model.player.HumanPlayer;
import magic.model.player.PlayerProfile;
import magic.model.player.PlayerProfiles;
import magic.ui.screen.interfaces.IActionBar;
import magic.ui.screen.interfaces.IPlayerProfileConsumer;
import magic.ui.screen.interfaces.IStatusBar;
import magic.ui.screen.widget.MenuButton;
import magic.ui.widget.player.HumanPlayerJList;

@SuppressWarnings("serial")
public class SelectHumanPlayerScreen
    extends SelectPlayerAbstractScreen
    implements IStatusBar, IActionBar {

    private HumanPlayerJList profilesJList;
    private final PlayerProfile playerProfile;

    // CTR
    public SelectHumanPlayerScreen(final IPlayerProfileConsumer consumer, final PlayerProfile playerProfile) {
        super(consumer);
        this.playerProfile = playerProfile;
        refreshProfilesJList(playerProfile);
    }

    /* (non-Javadoc)
     * @see magic.ui.screen.PlayerScreenUtil#getProfilesListPanel()
     */
    @Override
    protected JPanel getProfilesListPanel() {
        profilesJList = new HumanPlayerJList();
        profilesJList.addMouseListener(getMouseAdapter());
        return new ContainerPanel(profilesJList);
    }

    private MouseAdapter getMouseAdapter() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    doNextAction();
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        };
    }

    private void refreshProfilesJList(final PlayerProfile playerProfile) {
        profilesJList.setListData(getPlayerProfilesArray());
        setSelectedListItem(playerProfile);
    }

    private HumanPlayer[] getPlayerProfilesArray() {
        final List<PlayerProfile> sortedPlayersList = getSortedPlayersList();
        return sortedPlayersList.toArray(new HumanPlayer[sortedPlayersList.size()]);
    }

    /* (non-Javadoc)
     * @see magic.ui.screen.SelectPlayerAbstractScreen#createDefaultPlayerProfiles()
     */
    @Override
    protected void createDefaultPlayerProfiles() throws IOException {
        final HumanPlayer profile = new HumanPlayer();
        profile.setPlayerName(getDefaultPlayerProfileName());
        profile.save();
    }

    private String getDefaultPlayerProfileName() {
        final String systemUserName = System.getProperty("user.name");
        return systemUserName == null ? "Player" : systemUserName;
    }

    /* (non-Javadoc)
     * @see magic.ui.IMagStatusBar#getScreenCaption()
     */
    @Override
    public String getScreenCaption() {
        return "Select Player";
    }

    /* (non-Javadoc)
     * @see magic.ui.IMagActionBar#getLeftAction()
     */
    @Override
    public MenuButton getLeftAction() {
        return new MenuButton("Cancel", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                getFrame().closeActiveScreen(false);
            }
        });
    }

    /* (non-Javadoc)
     * @see magic.ui.IMagActionBar#getRightAction()
     */
    @Override
    public MenuButton getRightAction() {
        return new MenuButton("Select", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                doNextAction();
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });
    }

    /* (non-Javadoc)
     * @see magic.ui.IMagActionBar#getMiddleActions()
     */
    @Override
    public List<MenuButton> getMiddleActions() {
        final List<MenuButton> buttons = new ArrayList<MenuButton>();
        buttons.add(new MenuButton("New", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doNewPlayerProfile();
            }
        }, "Create a new player profile."));
        buttons.add(new MenuButton("Edit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doEditPlayerProfile();
                profilesJList.repaint();
            }
        }, "Update name and duel settings for selected player."));
        buttons.add(new MenuButton("Delete", new DeletePlayerAction(), "Delete selected player profile."));
        buttons.add(getAvatarActionButton());
        return buttons;
    }

    private void doNewPlayerProfile() {
        final String newName = (String)JOptionPane.showInputDialog(
                getFrame(),
                "<html><b>Player Name</b><br></html>",
                "New Player",
                JOptionPane.PLAIN_MESSAGE,
                null, null, null);
        if (newName != null && !newName.trim().isEmpty()) {
            final PlayerProfile newProfile = new HumanPlayer();
            newProfile.setPlayerName(newName);
            newProfile.save();
            PlayerProfiles.getPlayerProfiles().put(newProfile.getId(), newProfile);
            refreshProfilesJList(newProfile);
        }
    }

    private void doEditPlayerProfile() {
        final PlayerProfile profile = getSelectedPlayerProfile();
        final String newName = (String)JOptionPane.showInputDialog(
                getFrame(),
                "<html><b>Player Name</b><br></html>",
                "Update Player",
                JOptionPane.PLAIN_MESSAGE,
                null, null, profile.getPlayerName());
        if (newName != null && !newName.trim().isEmpty()) {
            profile.setPlayerName(newName.trim());
            profile.save();
            if (profile.equals(playerProfile)) {
                consumer.setPlayerProfile(getSelectedPlayerProfile());
            }
        }
    }

    private HumanPlayer getSelectedPlayerProfile() {
        return profilesJList.getSelectedValue();
    }

    /* (non-Javadoc)
     * @see magic.ui.MagScreen#canScreenClose()
     */
    @Override
    public boolean isScreenReadyToClose(final AbstractScreen nextScreen) {
        return true;
    }

    /* (non-Javadoc)
     * @see magic.ui.screen.IAvatarImageConsumer#setSelectedAvatarPath(java.nio.file.Path)
     */
    @Override
    public void setSelectedAvatarPath(final Path imagePath) {
        final PlayerProfile profile = getSelectedPlayerProfile();
        updateAvatarImage(imagePath, profile);
        PlayerProfiles.refreshMap();
        refreshProfilesJList(profile);
        consumer.setPlayerProfile(getSelectedPlayerProfile());
    }

    /* (non-Javadoc)
     * @see magic.ui.screen.SelectPlayerScreen#getPlayerType()
     */
    @Override
    protected String getPlayerType() {
        return "human";
    }

    /* (non-Javadoc)
     * @see magic.ui.screen.SelectPlayerAbstractScreen#doNextAction()
     */
    @Override
    protected void doNextAction() {
        consumer.setPlayerProfile(getSelectedPlayerProfile());
        getFrame().closeActiveScreen(false);
    }

    /* (non-Javadoc)
     * @see magic.ui.screen.interfaces.IStatusBar#getStatusPanel()
     */
    @Override
    public JPanel getStatusPanel() {
        return null;
    }

    /* (non-Javadoc)
     * @see magic.ui.screen.SelectPlayerAbstractScreen#getPreferredWidth()
     */
    @Override
    protected int getPreferredWidth() {
        return 420;
    }

    @Override
    protected JList<? extends PlayerProfile> getProfilesJList() {
        return profilesJList;
    }

    @Override
    protected HashMap<String, PlayerProfile> getPlayerProfilesMap() {
        return PlayerProfiles.getHumanPlayerProfiles();
    }

    @Override
    protected void refreshProfilesJList() {
        refreshProfilesJList(null);
    }

}
