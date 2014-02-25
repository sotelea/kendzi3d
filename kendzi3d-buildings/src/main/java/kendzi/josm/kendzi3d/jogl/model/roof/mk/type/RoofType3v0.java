/*
 * This software is provided "AS IS" without a warranty of any kind. You use it
 * on your own risk and responsibility!!! This file is shared under BSD v3
 * license. See readme.txt and BSD3 file for details.
 */

package kendzi.josm.kendzi3d.jogl.model.roof.mk.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import kendzi.jogl.model.factory.MeshFactory;
import kendzi.jogl.model.factory.MeshFactoryUtil;
import kendzi.jogl.texture.dto.TextureData;
import kendzi.josm.kendzi3d.jogl.model.roof.mk.RoofMaterials;
import kendzi.josm.kendzi3d.jogl.model.roof.mk.RoofTypeOutput;
import kendzi.josm.kendzi3d.jogl.model.roof.mk.dormer.space.RectangleRoofHooksSpaces;
import kendzi.josm.kendzi3d.jogl.model.roof.mk.measurement.MeasurementKey;
import kendzi.math.geometry.Plane3d;
import kendzi.math.geometry.line.LinePoints2d;
import kendzi.math.geometry.polygon.MultiPolygonList2d;
import kendzi.math.geometry.polygon.PolygonList2d;
import kendzi.math.geometry.polygon.PolygonWithHolesList2d;
import kendzi.math.geometry.polygon.split.PolygonSplitUtil;
import kendzi.math.geometry.polygon.split.SplitPolygons;

/**
 * Roof type 3.0.
 * 
 * @author Tomasz Kędziora (Kendzi)
 * 
 */
public class RoofType3v0 extends RectangleRoofTypeBuilder {

    @Override
    public RoofTypeOutput buildRectangleRoof(RectangleRoofTypeConf conf) {

        Double l1 = getLenghtMetersPersent(conf.getMeasurements(), MeasurementKey.LENGTH_1, conf.getRecHeight(),
                conf.getRecHeight() * 0.2);

        Double h1 = getHeightDegreesMeters(conf.getMeasurements(), MeasurementKey.HEIGHT_1, 0, l1, 60);

        Double h2 = getHeightDegreesMeters(conf.getMeasurements(), MeasurementKey.HEIGHT_2, 0, conf.getRecHeight() - l1, 10);

        return build(conf.getBuildingPolygon(), conf.getRecHeight(), conf.getRecWidth(), conf.getRectangleContur(), h1, h2, l1,
                conf.getRoofTextureData());

    }

    /**
     * @param pBorderList
     * @param pRecHeight
     * @param pRecWidth
     * @param pRectangleContur
     * @param h1
     * @param l1
     * @param roofTextureData
     * @return
     */
    protected RoofTypeOutput build(PolygonWithHolesList2d buildingPolygon, double pRecHeight, double pRecWidth,
            Point2d[] pRectangleContur, double h1, double h2, double l1, RoofMaterials roofTextureData) {

        double height = Math.max(h1, h2);

        MeshFactory meshBorder = createFacadeMesh(roofTextureData);
        MeshFactory meshRoof = createRoofMesh(roofTextureData);

        TextureData facadeTexture = roofTextureData.getFacade().getTextureData();
        TextureData roofTexture = roofTextureData.getRoof().getTextureData();

        Point2d rightMiddlePoint = new Point2d(pRecWidth, l1);

        Point2d leftMiddlePoint = new Point2d(0, l1);

        LinePoints2d mLine = new LinePoints2d(leftMiddlePoint, rightMiddlePoint);

        Vector3d nt = new Vector3d(0, pRecHeight - l1, -h2);
        nt.normalize();

        Vector3d nb = new Vector3d(0, l1, h1);
        nb.normalize();

        List<Point2d> pBorderList = buildingPolygon.getOuter().getPoints();

        PolygonList2d borderPolygon = new PolygonList2d(pBorderList);

        SplitPolygons middleSplit = PolygonSplitUtil.split(borderPolygon, mLine);

        MultiPolygonList2d topMP = middleSplit.getTopMultiPolygons();
        MultiPolygonList2d bottomMP = middleSplit.getBottomMultiPolygons();

        Point3d planeLeftPoint = new Point3d(leftMiddlePoint.x, height, -leftMiddlePoint.y);

        Point3d planeRightPoint = new Point3d(rightMiddlePoint.x, height, -rightMiddlePoint.y);

        Plane3d planeTop = new Plane3d(planeRightPoint, nt);
        Plane3d planeBottom = new Plane3d(planeLeftPoint, nb);

        Vector3d roofBottomLineVector = new Vector3d(pRecWidth, 0, 0);

        Vector3d roofTopLineVector = new Vector3d(-pRecWidth, 0, 0);

        MeshFactoryUtil.addPolygonToRoofMesh(meshRoof, topMP, planeTop, roofTopLineVector, roofTexture);
        MeshFactoryUtil.addPolygonToRoofMesh(meshRoof, bottomMP, planeBottom, roofBottomLineVector, roofTexture);

        List<Point2d> borderSplit = RoofTypeUtil.splitBorder(borderPolygon, mLine);

        List<Double> borderHeights = calcHeightList(borderSplit, mLine, planeTop, planeBottom);

        // //******************

        RoofTypeUtil.makeRoofBorderMesh(

        borderSplit, borderHeights,

        meshBorder, facadeTexture);

        RoofTypeOutput rto = new RoofTypeOutput();
        rto.setHeight(Math.max(h1, h2));

        rto.setMesh(Arrays.asList(meshBorder, meshRoof));

        RectangleRoofHooksSpaces rhs = buildRectRoofHooksSpace(pRectangleContur, new PolygonPlane(bottomMP, planeBottom), null,
                new PolygonPlane(topMP, planeTop), null);

        rto.setRoofHooksSpaces(rhs);

        return rto;
    }

    private List<Double> calcHeightList(List<Point2d> pSplitBorder, LinePoints2d mLine, Plane3d planeTop, Plane3d planeBottom) {

        List<Double> borderHeights = new ArrayList<Double>(pSplitBorder.size());
        for (Point2d point : pSplitBorder) {

            double height = calcHeight(point, mLine, planeTop, planeBottom);

            borderHeights.add(height);

        }

        return borderHeights;
    }

    /**
     * Calc height of point in border.
     * 
     * @param point
     * @param mLine
     * @param planeTop
     * @param planeBottom
     * @return
     */
    private double calcHeight(Point2d point, LinePoints2d mLine, Plane3d planeTop, Plane3d planeBottom) {

        double x = point.x;
        double z = -point.y;

        if (mLine.inFront(point)) {

            return planeTop.calcYOfPlane(x, z);
        } else {

            return planeBottom.calcYOfPlane(x, z);
        }
    }
}
