/*
 * This file is part of JGrasstools (http://www.jgrasstools.org)
 * (C) HydroloGIS - www.hydrologis.com 
 * 
 * JGrasstools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jgrasstools.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import oms3.annotations.Author;
import oms3.annotations.Description;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Keywords;
import oms3.annotations.Label;
import oms3.annotations.License;
import oms3.annotations.Name;
import oms3.annotations.Out;
import oms3.annotations.Status;
import oms3.annotations.UI;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.grid.DefaultGridFeatureBuilder;
import org.geotools.grid.GridFeatureBuilder;
import org.geotools.grid.Grids;
import org.geotools.grid.Lines;
import org.geotools.grid.oblong.Oblongs;
import org.geotools.grid.ortholine.LineOrientation;
import org.geotools.grid.ortholine.OrthoLineDef;
import org.geotools.referencing.CRS;
import org.jgrasstools.gears.libs.exceptions.ModelsIllegalargumentException;
import org.jgrasstools.gears.libs.modules.JGTConstants;
import org.jgrasstools.gears.libs.modules.JGTModel;
import org.jgrasstools.gears.libs.monitor.IJGTProgressMonitor;
import org.jgrasstools.gears.utils.features.FeatureUtilities;
import org.jgrasstools.gears.utils.geometry.GeometryUtilities;
import org.jgrasstools.gears.utils.math.NumericsUtilities;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;

@Description("Generates grid of lines or polygons.")
@Author(name = "Andrea Antonello", contact = "http://www.hydrologis.com")
@Keywords("Vector, Grid")
@Label(JGTConstants.VECTORPROCESSING)
@Status(Status.CERTIFIED)
@Name("_gridgenerator")
@License("http://www.gnu.org/licenses/gpl-3.0.html")
public class OmsGridsGenerator extends JGTModel {

    @Description("Optional vector map from which to take the bounds (if supplied, all other bounds related parameter are ignored). This needs pRows and pCols to be defined.")
    @In
    public SimpleFeatureCollection inVector = null;

    @Description("Optional raster map from which to take the bounds (if supplied, all other bounds related parameter are ignored. This needs pRows and pCols to be defined.")
    @In
    public GridCoverage2D inRaster = null;

    @Description("The lower left longitude.")
    @In
    public double pLon = 0.0;

    @Description("The lower left latitude.")
    @In
    public double pLat = 0.0;

    @Description("The grid cell width.")
    @In
    public double pWidth = 1.0;

    @Description("The grid cell height.")
    @In
    public double pHeight = 1.0;

    @Description("The number of rows.")
    @In
    public int pRows = 10;

    @Description("The number of cols.")
    @In
    public int pCols = 10;

    @Description("The vertex spacing to use.")
    @In
    public Double pSpacing = null;

    @Description("Output type: 0 = polygons, 1 = lines, 2 = points (default = 0).")
    @In
    public int pType = 0;

    @Description("The code defining the coordinate reference system, composed by authority and code number (ex. EPSG:4328). Applied in the case the file is missing.")
    @UI(JGTConstants.CRS_UI_HINT)
    @In
    public String pCode;

    @Description("The grid.")
    @Out
    public SimpleFeatureCollection outMap = null;

    @Execute
    public void process() throws Exception {

        boolean isSquare;
        double s;
        double n;
        double w;
        double e;
        CoordinateReferenceSystem crs;
        if (inVector != null) {
            ReferencedEnvelope bounds = inVector.getBounds();
            crs = inVector.getSchema().getCoordinateReferenceSystem();
            s = bounds.getMinY();
            n = bounds.getMaxY();
            w = bounds.getMinX();
            e = bounds.getMaxX();
            pWidth = bounds.getWidth() / pCols;
            pHeight = bounds.getHeight() / pRows;
        } else if (inRaster != null) {
            Envelope2D bounds = inRaster.getGridGeometry().getEnvelope2D();
            crs = inRaster.getCoordinateReferenceSystem();
            s = bounds.getMinY();
            n = bounds.getMaxY();
            w = bounds.getMinX();
            e = bounds.getMaxX();
            pWidth = bounds.getWidth() / pCols;
            pHeight = bounds.getHeight() / pRows;
        } else {
            checkNull(pCode);
            s = pLat;
            n = pLat + pRows * pHeight;
            w = pLon;
            e = pLon + pCols * pWidth;
            crs = CRS.decode(pCode);
        }
        isSquare = NumericsUtilities.dEq(pWidth, pHeight) ? true : false;

        outMap = FeatureCollections.newCollection();

        ReferencedEnvelope env = new ReferencedEnvelope(w, e, s, n, crs);

        pm.beginTask("Generating grid...", IJGTProgressMonitor.UNKNOWN);

        GridFeatureBuilder builder = new DefaultGridFeatureBuilder(crs);
        SimpleFeatureSource grid;
        switch( pType ) {
        case 0:
            if (isSquare) {
                if (pSpacing != null) {
                    grid = Grids.createSquareGrid(env, pWidth, pSpacing);
                } else {
                    grid = Grids.createSquareGrid(env, pWidth);
                }
            } else {
                if (pSpacing != null) {
                    grid = Oblongs.createGrid(env, pWidth, pHeight, pSpacing, builder);
                } else {
                    grid = Oblongs.createGrid(env, pWidth, pHeight, builder);
                }
            }
            outMap = grid.getFeatures();
            break;
        case 1:
        case 2:
            List<OrthoLineDef> lineDefs = Arrays.asList(//
                    new OrthoLineDef(LineOrientation.VERTICAL, 1, pHeight), //
                    new OrthoLineDef(LineOrientation.HORIZONTAL, 1, pWidth) //
                    );
            if (pSpacing != null) {
                grid = Lines.createOrthoLines(env, lineDefs, pSpacing, builder);
            } else {
                grid = Lines.createOrthoLines(env, lineDefs);
            }
            outMap = grid.getFeatures();
            GeometryFactory gf = GeometryUtilities.gf();
            List<Geometry> geoms = FeatureUtilities.featureCollectionToGeometriesList(outMap, true, null);
            List<LineString> verticals = new ArrayList<LineString>();
            List<LineString> horizontals = new ArrayList<LineString>();
            for( Geometry geometry : geoms ) {
                Envelope envelope = geometry.getEnvelopeInternal();
                Coordinate first = new Coordinate(envelope.getMinX(), envelope.getMinY());
                Coordinate last = new Coordinate(envelope.getMaxX(), envelope.getMaxY());
                LineString lineString = gf.createLineString(new Coordinate[]{first, last});
                if (envelope.getWidth() > envelope.getHeight()) {
                    horizontals.add(lineString);
                } else {
                    verticals.add(lineString);
                }
            }

            if (pType == 1) {
                createLines(crs, verticals, horizontals);
            } else {
                createPoints(crs, gf, verticals, horizontals);
            }

            break;
        default:
            throw new ModelsIllegalargumentException("The supplied pType is not supported.", this);
        }

        pm.done();
    }

    private void createPoints( CoordinateReferenceSystem crs, GeometryFactory gf, List<LineString> verticals,
            List<LineString> horizontals ) {
        outMap = FeatureCollections.newCollection();
        SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
        b.setName("points");
        b.setCRS(crs);
        b.add("the_geom", Point.class);
        b.add("id", Long.class);
        SimpleFeatureType type = b.buildFeatureType();
        SimpleFeatureBuilder fbuilder = new SimpleFeatureBuilder(type);

        Geometry gVer = gf.createMultiLineString(verticals.toArray(new LineString[0]));
        Geometry gHor = gf.createMultiLineString(horizontals.toArray(new LineString[0]));

        Geometry intersection = gHor.intersection(gVer);

        long index = 0;
        int numGeometries = intersection.getNumGeometries();
        for( int i = 0; i < numGeometries; i++ ) {
            Geometry geometry = intersection.getGeometryN(i);
            Object[] values = new Object[]{geometry, index++};
            fbuilder.addAll(values);
            SimpleFeature feature = fbuilder.buildFeature(null);
            outMap.add(feature);
        }
    }

    private void createLines( CoordinateReferenceSystem crs, List<LineString> verticals, List<LineString> horizontals ) {
        outMap = FeatureCollections.newCollection();
        SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
        b.setName("lines");
        b.setCRS(crs);
        b.add("the_geom", MultiLineString.class);
        b.add("id", Long.class);
        SimpleFeatureType type = b.buildFeatureType();
        SimpleFeatureBuilder fbuilder = new SimpleFeatureBuilder(type);

        long index = 0;
        for( LineString lineString : horizontals ) {
            Object[] values = new Object[]{lineString, index++};
            fbuilder.addAll(values);
            SimpleFeature feature = fbuilder.buildFeature(null);
            outMap.add(feature);
        }
        for( LineString lineString : verticals ) {
            Object[] values = new Object[]{lineString, index++};
            fbuilder.addAll(values);
            SimpleFeature feature = fbuilder.buildFeature(null);
            outMap.add(feature);
        }
    }
}
