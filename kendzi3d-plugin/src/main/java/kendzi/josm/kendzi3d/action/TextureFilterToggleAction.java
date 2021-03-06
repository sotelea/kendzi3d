/*
 * This software is provided "AS IS" without a warranty of any kind.
 * You use it on your own risk and responsibility!!!
 *
 * This file is shared under BSD v3 license.
 * See readme.txt and BSD3 file for details.
 *
 */

package kendzi.josm.kendzi3d.action;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonModel;

import kendzi.jogl.texture.TextureCacheService;
import kendzi.jogl.texture.TextureCacheServiceImpl;

import org.openstreetmap.josm.actions.JosmAction;

import com.google.inject.Inject;

import static org.openstreetmap.josm.tools.I18n.*;

/**
 * Texture filter toggle action.
 *
 * @author Tomasz Kędziora (Kendzi)
 *
 */
public class TextureFilterToggleAction extends JosmAction {

    /**
     * Button models.
     */
    private final List<ButtonModel> buttonModels = new ArrayList<ButtonModel>();

    /**
     * Texture cache service.
     */
    private TextureCacheService textureCacheService;

    // FIXME: replace with property Action.SELECTED_KEY when migrating to
    // Java 6
    private boolean selected;

    /**
     * Constructor of debug toggle action.
     */
    @Inject
    public TextureFilterToggleAction(TextureCacheService textureCacheService) {
        super(tr("Texture filter"), "1306318261_debugger__24", tr("Enable/disable texture filter"),
        // Shortcut.registerShortcut("menu:view:wireframe",
        // tr("Toggle Wireframe view"),KeyEvent.VK_W, Shortcut.GROUP_MENU),
                null, true /* register shortcut */
        );
        this.selected = true;
        // Main.pref.getBoolean("draw.wireframe", false);
        notifySelectedState();

        this.textureCacheService = textureCacheService;

        setTextureFilter(this.selected);
    }

    /**
     * @param pModel button model
     */
    public void addButtonModel(ButtonModel pModel) {
        if (pModel != null && !this.buttonModels.contains(pModel)) {
            this.buttonModels.add(pModel);
            pModel.setSelected(this.selected);
        }
    }

    /**
     * @param pModel button model
     */
    public void removeButtonModel(ButtonModel pModel) {
        if (pModel != null && this.buttonModels.contains(pModel)) {
            this.buttonModels.remove(pModel);
        }
    }

    /**
     *
     */
    protected void notifySelectedState() {
        for (ButtonModel model : this.buttonModels) {
            if (model.isSelected() != this.selected) {
                model.setSelected(this.selected);
            }
        }
    }

    /**
     *
     */
    protected void toggleSelectedState() {
        this.selected = !this.selected;
        // Main.pref.put("draw.wireframe", this.selected);
        notifySelectedState();

        setTextureFilter(this.selected);
    }

    /**
     * @param pEnable enable filter
     */
    private void setTextureFilter(boolean pEnable) {
        // getTextureCacheService().setTextureFilter(pEnable);
        if (this.textureCacheService instanceof TextureCacheServiceImpl) {
            ((TextureCacheServiceImpl) this.textureCacheService).setTextureFilter(pEnable);
            this.textureCacheService.clear();
        } else {
            throw new RuntimeException("unsupported textureCacheService");
        }
    }

    @Override
    public void actionPerformed(ActionEvent pE) {
        toggleSelectedState();
    }

    @Override
    protected void updateEnabledState() {
        // setEnabled(Main.map != null && Main.main.getEditLayer() != null);
    }

    /**
     * Is selected.
     *
     * @return selected
     */
    public boolean isSelected() {
        return this.selected;
    }

    /**
     * If can be in debug mode.
     *
     * @return debug mode
     */
    public boolean canDebug() {
        return true;
    }
}
