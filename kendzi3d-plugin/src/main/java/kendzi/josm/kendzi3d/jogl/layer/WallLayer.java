/*
 * This software is provided "AS IS" without a warranty of any kind.
 * You use it on your own risk and responsibility!!!
 *
 * This file is shared under BSD v3 license.
 * See readme.txt and BSD3 file for details.
 *
 */

package kendzi.josm.kendzi3d.jogl.layer;

import java.util.ArrayList;
import java.util.List;

import kendzi.jogl.model.render.ModelRender;
import kendzi.jogl.texture.library.TextureLibraryStorageService;
import kendzi.josm.kendzi3d.jogl.model.BarrierWall;
import kendzi.josm.kendzi3d.jogl.model.Model;
import kendzi.josm.kendzi3d.jogl.model.Perspective3D;
import kendzi.josm.kendzi3d.service.MetadataCacheService;

import org.apache.log4j.Logger;
import org.openstreetmap.josm.actions.search.SearchCompiler;
import org.openstreetmap.josm.actions.search.SearchCompiler.Match;
import org.openstreetmap.josm.actions.search.SearchCompiler.ParseError;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;

import com.google.inject.Inject;

/**
 * Layer for fence.
 *
 * @author Tomasz Kędziora (Kendzi)
 */
public class WallLayer implements Layer {

    /** Log. */
    private static final Logger log = Logger.getLogger(WallLayer.class);

    /**
     * Model renderer.
     */
    @Inject
    private ModelRender modelRender;

    /**
     * Metadata cache service.
     */
    @Inject
    private MetadataCacheService metadataCacheService;

    /**
     * Texture library service.
     */
    @Inject
    private TextureLibraryStorageService textureLibraryStorageService;


    /**
     * List of layer models.
     */
    private List<Model> modelList = new ArrayList<Model>();

    private Match wallMatcher;

    {
        try {
            this.wallMatcher = SearchCompiler.compile("(barrier=wall)", false, false);
        } catch (ParseError e) {
            this.wallMatcher = new SearchCompiler.Never();
            log.error(e, e);
        }
    }

    @Override
    public
    Match getNodeMatcher() {
        return null;
    }

    @Override
    public Match getWayMatcher() {
        return this.wallMatcher;
    }

    @Override
    public Match getRelationMatcher() {
        return null;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public List<Model> getModels() {
        return this.modelList;
    }

    @Override
    public void addModel(Node node, Perspective3D pPerspective3D) {
        //
    }

    @Override
    public void addModel(Way way, Perspective3D pPerspective3D) {
        this.modelList.add(new BarrierWall(
                way, pPerspective3D, this.modelRender,
                this.metadataCacheService, this.textureLibraryStorageService));
    }

    @Override
    public void addModel(Relation relation, Perspective3D pPerspective3D) {
        //
    }

    @Override
    public void clear() {
        this.modelList.clear();
    }

    /**
     * @return the modelRender
     */
    public ModelRender getModelRender() {
        return this.modelRender;
    }

    /**
     * @param modelRender the modelRender to set
     */
    public void setModelRender(ModelRender modelRender) {
        this.modelRender = modelRender;
    }

}
