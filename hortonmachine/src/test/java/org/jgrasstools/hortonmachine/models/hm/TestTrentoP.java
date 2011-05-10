package org.jgrasstools.hortonmachine.models.hm;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.jgrasstools.gears.io.shapefile.ShapefileFeatureReader;
import org.jgrasstools.gears.io.timeseries.TimeSeriesReader;
import org.jgrasstools.hortonmachine.modules.networktools.trento_p.TrentoP;
import org.jgrasstools.hortonmachine.modules.networktools.trento_p.utils.Constants;
import org.jgrasstools.hortonmachine.utils.HMTestCase;
import org.jgrasstools.hortonmachine.utils.HMTestMaps;
import org.joda.time.DateTime;

/**
 * A test case for the trentoP-java model.
 * 
 * @author Daniele Andreis
 * 
 */
public class TestTrentoP extends HMTestCase {

    private final static double TOLL = 0.009;

    private final static double[] INTERNAL_PARAMETERS = {1.2, 4, 40, 0.005, 0.15, 1, 30, 0.001, 0.01, 1.0, 4.43, 1.5, 0.38,
            0.001, 1, 0.2, 0.4};

    /**
     * This is a block of parameter that usually are used in the simulation of
     * {@link TrentoP}.
     */
    private static double a = 60.4;
    private static double n = 0.61;
    private static double tau = 2.5;
    private static double g = 0.8;
    private static short align = 0;

    /**
     * Test project 1.
     * 
     * Check the result in the project mode, for the file Mulinu.geo.
     * 
     * @throws Exception
     */
    public void testProject1() throws Exception {
        double[][] result = null;
        double[] globalparameters = INTERNAL_PARAMETERS;
        TrentoP trento_P = new TrentoP();
        // set parameters;
        trento_P.pTest = 0; // project
        trento_P.pA = a;
        trento_P.pN = n;
        trento_P.pTau = tau;
        trento_P.pG = g;
        trento_P.pAlign = align;
        trento_P.pMinimumDepth = globalparameters[0];
        trento_P.pMaxJunction = (int) globalparameters[1];
        trento_P.pJMax = (int) globalparameters[2];
        trento_P.pAccuracy = globalparameters[3];
        trento_P.tDTp = globalparameters[4];
        trento_P.tpMin = globalparameters[5];
        trento_P.tpMax = globalparameters[6];
        trento_P.pEpsilon = globalparameters[7];
        trento_P.pMinG = globalparameters[8];
        trento_P.pMinDischarge = globalparameters[9];
        trento_P.pMaxTheta = globalparameters[10];
        trento_P.pCelerityFactor = globalparameters[11];
        trento_P.pExponent = globalparameters[12];
        trento_P.pTolerance = globalparameters[13];
        trento_P.pC = globalparameters[14];
        trento_P.pGamma = globalparameters[15];
        trento_P.pEspInflux = globalparameters[16];
        trento_P.inDiameters = HMTestMaps.PIPE;
        trento_P.pOutPipe = 16;
        URL net = this.getClass().getClassLoader().getResource("TestTrentoP1.shp");
        File netFile = new File(net.toURI());
        ShapefileFeatureReader netReader = new ShapefileFeatureReader();
        netReader.file = netFile.getAbsolutePath();
        netReader.readFeatureCollection();
        SimpleFeatureCollection netFC = netReader.geodata;
        // set global parameters
        // verify
        // SimpleFeatureCollection netFC=Utility.readShp(netFile);
        trento_P.inPipes = netFC;
        trento_P.process();
        result = trento_P.getResults();
        checkMatrixEqual(result, HMTestMaps.project1, TOLL);

    }

    /**
     * Test project 1, rectangular.
     * 
     * Check the result in the project mode, for the file Mulinu.geo.
     * 
     * @throws Exception
     */
    public void testProject1Rect() throws Exception {
        double[][] result = null;
        double[] globalparameters = INTERNAL_PARAMETERS;
        TrentoP trento_P = new TrentoP();
        // set parameters;
        trento_P.pTest = 0; // project
        trento_P.pA = a;
        trento_P.pN = n;
        trento_P.pTau = tau;
        trento_P.pG = g;
        trento_P.pAlign = align;
        trento_P.pMinimumDepth = globalparameters[0];
        trento_P.pMaxJunction = (int) globalparameters[1];
        trento_P.pJMax = (int) globalparameters[2];
        trento_P.pAccuracy = globalparameters[3];
        trento_P.tDTp = globalparameters[4];
        trento_P.tpMin = globalparameters[5];
        trento_P.tpMax = globalparameters[6];
        trento_P.pEpsilon = globalparameters[7];
        trento_P.pMinG = globalparameters[8];
        trento_P.pMinDischarge = globalparameters[9];
        trento_P.pMaxTheta = globalparameters[10];
        trento_P.pCelerityFactor = globalparameters[11];
        trento_P.pExponent = globalparameters[12];
        trento_P.pTolerance = globalparameters[13];
        trento_P.pC = globalparameters[14];
        trento_P.pGamma = globalparameters[15];
        trento_P.pEspInflux = globalparameters[16];
        trento_P.inDiameters = HMTestMaps.PIPE;
        trento_P.pOutPipe = 16;
        URL net = this.getClass().getClassLoader().getResource("TestTrentoP1Rect.shp");
        File netFile = new File(net.toURI());
        ShapefileFeatureReader netReader = new ShapefileFeatureReader();
        netReader.file = netFile.getAbsolutePath();
        netReader.readFeatureCollection();
        SimpleFeatureCollection netFC = netReader.geodata;
        // set global parameters
        // verify
        // SimpleFeatureCollection netFC=Utility.readShp(netFile);
        trento_P.inPipes = netFC;
        trento_P.process();
        result = trento_P.getResults();
        checkMatrixEqual(result, HMTestMaps.project1Rectangular, TOLL);

    }

    /**
     * Test project 1, rectangular.
     * 
     * Check the result in the project mode, for the file Mulinu.geo.
     * 
     * @throws Exception
     */
    public void testProject1Trap() throws Exception {
        double[][] result = null;
        double[] globalparameters = INTERNAL_PARAMETERS;
        TrentoP trento_P = new TrentoP();
        // set parameters;
        trento_P.pTest = 0; // project
        trento_P.pA = a;
        trento_P.pN = n;
        trento_P.pTau = tau;
        trento_P.pG = g;
        trento_P.pAlign = align;
        trento_P.pMinimumDepth = globalparameters[0];
        trento_P.pMaxJunction = (int) globalparameters[1];
        trento_P.pJMax = (int) globalparameters[2];
        trento_P.pAccuracy = globalparameters[3];
        trento_P.tDTp = globalparameters[4];
        trento_P.tpMin = globalparameters[5];
        trento_P.tpMax = globalparameters[6];
        trento_P.pEpsilon = globalparameters[7];
        trento_P.pMinG = globalparameters[8];
        trento_P.pMinDischarge = globalparameters[9];
        trento_P.pMaxTheta = globalparameters[10];
        trento_P.pCelerityFactor = globalparameters[11];
        trento_P.pExponent = globalparameters[12];
        trento_P.pTolerance = globalparameters[13];
        trento_P.pC = globalparameters[14];
        trento_P.pGamma = globalparameters[15];
        trento_P.pEspInflux = globalparameters[16];
        trento_P.inDiameters = HMTestMaps.PIPE;
        trento_P.pOutPipe = 16;
        URL net = this.getClass().getClassLoader().getResource("TestTrentoP1Trap.shp");
        File netFile = new File(net.toURI());
        ShapefileFeatureReader netReader = new ShapefileFeatureReader();
        netReader.file = netFile.getAbsolutePath();
        netReader.readFeatureCollection();
        SimpleFeatureCollection netFC = netReader.geodata;
        // set global parameters
        // verify
        // SimpleFeatureCollection netFC=Utility.readShp(netFile);
        trento_P.inPipes = netFC;
        trento_P.process();
        result = trento_P.getResults();
        checkMatrixEqual(result, HMTestMaps.project1Trapezio, TOLL);

    }

    /**
     * Test project 1 with align set to 1.
     * 
     * Check the result in the project mode, for the file Mulinu.geo.
     * 
     * @throws Exception
     */
    public void testProject1Align1() throws Exception {
        double[][] result = null;
        double[] globalparameters = INTERNAL_PARAMETERS;
        TrentoP trento_P = new TrentoP();
        // set parameters;
        trento_P.pTest = 0; // project
        trento_P.pA = a;
        trento_P.pN = n;
        trento_P.pTau = tau;
        trento_P.pG = g;
        trento_P.pAlign = 1;
        trento_P.pMinimumDepth = globalparameters[0];
        trento_P.pMaxJunction = (int) globalparameters[1];
        trento_P.pJMax = (int) globalparameters[2];
        trento_P.pAccuracy = globalparameters[3];
        trento_P.tDTp = globalparameters[4];
        trento_P.tpMin = globalparameters[5];
        trento_P.tpMax = globalparameters[6];
        trento_P.pEpsilon = globalparameters[7];
        trento_P.pMinG = globalparameters[8];
        trento_P.pMinDischarge = globalparameters[9];
        trento_P.pMaxTheta = globalparameters[10];
        trento_P.pCelerityFactor = globalparameters[11];
        trento_P.pExponent = globalparameters[12];
        trento_P.pTolerance = globalparameters[13];
        trento_P.pC = globalparameters[14];
        trento_P.pGamma = globalparameters[15];
        trento_P.pEspInflux = globalparameters[16];
        trento_P.inDiameters = HMTestMaps.PIPE;
        trento_P.pOutPipe = 16;
        URL net = this.getClass().getClassLoader().getResource("TestTrentoP1.shp");
        File netFile = new File(net.toURI());
        ShapefileFeatureReader netReader = new ShapefileFeatureReader();
        netReader.file = netFile.getAbsolutePath();
        netReader.readFeatureCollection();
        SimpleFeatureCollection netFC = netReader.geodata;
        // set global parameters
        // verify
        // SimpleFeatureCollection netFC=Utility.readShp(netFile);
        trento_P.inPipes = netFC;
        trento_P.process();
        result = trento_P.getResults();
        checkMatrixEqual(result, HMTestMaps.project1align1, TOLL);

    }

    /**
     * 
     * Check if the models work well in verify mode..
     * 
     * @throws Exception
     * 
     * @throws IOException
     */
    public void testVerify1() throws Exception {

        URL rainUrl = this.getClass().getClassLoader().getResource("rain_trentop.csv"); //$NON-NLS-1$
        File rainFile = new File(rainUrl.toURI());

        double[][] result = null;
        double[] globalparameters = INTERNAL_PARAMETERS;

        TrentoP trento_P = new TrentoP();
        // set parameters;
        trento_P.pTest = 1; // verify
        trento_P.pMinimumDepth = globalparameters[0];
        trento_P.pMaxJunction = (int) globalparameters[1];
        trento_P.pJMax = (int) globalparameters[2];
        trento_P.pAccuracy = globalparameters[3];
        trento_P.tDTp = globalparameters[4];
        trento_P.tpMin = globalparameters[5];
        trento_P.tpMax = globalparameters[6];
        trento_P.pEpsilon = globalparameters[7];
        trento_P.pMaxTheta = 6.28;
        trento_P.pCelerityFactor = Constants.DEFAULT_CELERITY_FACTOR;
        trento_P.pExponent = globalparameters[12];
        trento_P.pTolerance = 0.01;
        trento_P.pGamma = globalparameters[15];
        trento_P.pEspInflux = globalparameters[16];
        trento_P.pOutPipe = 16;
        TimeSeriesReader rainReader = new TimeSeriesReader();

        rainReader.fileNovalue = "-9999";
        rainReader.file = rainFile.getAbsolutePath();
        rainReader.read();
        rainReader.close();
        trento_P.inRain = rainReader.outData;
        // set global parameters.
        URL net = this.getClass().getClassLoader().getResource("TestTrentoP1Verifica.shp");
        File netFile = new File(net.toURI());
        ShapefileFeatureReader netReader = new ShapefileFeatureReader();
        netReader.file = netFile.getAbsolutePath();
        netReader.readFeatureCollection();
        SimpleFeatureCollection netFC = netReader.geodata;
        // set global parameters
        // verify
        // SimpleFeatureCollection netFC=Utility.readShp(netFile);
        trento_P.inPipes = netFC;
        trento_P.process();
        result = hashToMatrix(trento_P.outDischarge, trento_P.inRain, trento_P.getResults().length);
        checkMatrixEqual(result, HMTestMaps.verify1, TOLL);

    }

    private double[][] hashToMatrix( HashMap<DateTime, double[]> outDischarge, HashMap<DateTime, double[]> inRain, int nStation ) {
        // create the rains array from the input.
        Set<Entry<DateTime, double[]>> dischargeSet = outDischarge.entrySet();
        DateTime first = null;
        DateTime second = null;
        int l = outDischarge.size();

        double[][] rainData = new double[l][nStation + 1];
        int index = 0;
        int dt = 0;
        for( Entry<DateTime, double[]> dischargeRecord : dischargeSet ) {
            DateTime dateTime = dischargeRecord.getKey();
            double[] values = dischargeRecord.getValue();
            if (first == null) {
                first = dateTime;
                rainData[index][0] = 1;

                for( int i = 0; i < values.length; i++ ) {
                    rainData[index][i + 1] = values[i];
                }

            } else if (second == null) {
                second = dateTime;
                dt = Math.abs(second.getMinuteOfDay() - first.getMinuteOfDay());
                rainData[index][0] = rainData[index-1][0] + dt;
                for( int i = 0; i < values.length; i++ ) {
                    rainData[index][i + 1] = values[i];
                }
            } else {
                rainData[index][0] = rainData[index-1][0] + dt;
                for( int i = 0; i < values.length; i++ ) {
                    rainData[index][i + 1] = values[i];
                }
            }
            index++;
        }
        return rainData;
    }
}