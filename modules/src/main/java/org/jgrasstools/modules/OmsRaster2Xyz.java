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

import java.io.BufferedWriter;
import java.io.FileWriter;

import javax.media.jai.iterator.RandomIter;

import oms3.annotations.Author;
import oms3.annotations.Description;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Keywords;
import oms3.annotations.Label;
import oms3.annotations.License;
import oms3.annotations.Name;
import oms3.annotations.Status;

import org.geotools.coverage.grid.GridCoordinates2D;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.jgrasstools.gears.libs.modules.JGTConstants;
import org.jgrasstools.gears.libs.modules.JGTModel;
import org.jgrasstools.gears.utils.RegionMap;
import org.jgrasstools.gears.utils.coverage.CoverageUtilities;
import org.opengis.geometry.DirectPosition;

@Description("Convert a raster to XYZ triplets.")
@Author(name = "Andrea Antonello", contact = "http://www.hydrologis.com")
@Keywords("Raster, Conversion")
@Label(JGTConstants.RASTERPROCESSING)
@Name("_raster2xyz")
@Status(Status.CERTIFIED)
@License("General Public License Version 3 (GPLv3)")
public class OmsRaster2Xyz extends JGTModel {

    @Description("The map to convert.")
    @In
    public GridCoverage2D inRaster;

    @Description("The file into which to save the result.")
    @In
    public String inFile;

    @Description("Flag to remove novalues.")
    @In
    public boolean doRemovenv = true;

    @SuppressWarnings("nls")
    @Execute
    public void process() throws Exception {
        checkNull(inRaster);

        RandomIter rasterIter = CoverageUtilities.getRandomIterator(inRaster);
        RegionMap regionMap = CoverageUtilities.getRegionParamsFromGridCoverage(inRaster);
        GridGeometry2D gridGeometry = inRaster.getGridGeometry();

        int cols = regionMap.getCols();
        int rows = regionMap.getRows();

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(inFile));
            for( int r = 0; r < rows; r++ ) {
                for( int c = 0; c < cols; c++ ) {
                    double elevation = rasterIter.getSampleDouble(c, r, 0);
                    if (doRemovenv && JGTConstants.isNovalue(elevation)) {
                        continue;
                    }
                    DirectPosition position = gridGeometry.gridToWorld(new GridCoordinates2D(c, r));
                    double[] coordinate = position.getCoordinate();

                    StringBuilder sb = new StringBuilder();
                    sb.append(coordinate[0]);
                    sb.append("\t");
                    sb.append(coordinate[1]);
                    sb.append("\t");
                    sb.append(elevation);
                    sb.append("\n");
                    writer.write(sb.toString());
                }
            }
        } finally {
            if (writer != null)
                writer.close();
        }

    }

}
