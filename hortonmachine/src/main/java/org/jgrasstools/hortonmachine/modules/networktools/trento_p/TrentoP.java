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
package org.jgrasstools.hortonmachine.modules.networktools.trento_p;

import static java.lang.Math.pow;
import static org.jgrasstools.gears.utils.features.FeatureUtilities.findAttributeName;
import static org.jgrasstools.hortonmachine.modules.networktools.trento_p.utils.Constants.DEFAULT_CELERITY_FACTOR;
import static org.jgrasstools.hortonmachine.modules.networktools.trento_p.utils.Constants.DEFAULT_EPSILON;
import static org.jgrasstools.hortonmachine.modules.networktools.trento_p.utils.Constants.DEFAULT_ESP1;
import static org.jgrasstools.hortonmachine.modules.networktools.trento_p.utils.Constants.DEFAULT_EXPONENT;
import static org.jgrasstools.hortonmachine.modules.networktools.trento_p.utils.Constants.DEFAULT_FRANCO;
import static org.jgrasstools.hortonmachine.modules.networktools.trento_p.utils.Constants.DEFAULT_GAMMA;
import static org.jgrasstools.hortonmachine.modules.networktools.trento_p.utils.Constants.DEFAULT_J_MAX;
import static org.jgrasstools.hortonmachine.modules.networktools.trento_p.utils.Constants.DEFAULT_MAX_JUNCTION;
import static org.jgrasstools.hortonmachine.modules.networktools.trento_p.utils.Constants.DEFAULT_MAX_THETA;
import static org.jgrasstools.hortonmachine.modules.networktools.trento_p.utils.Constants.DEFAULT_MING;
import static org.jgrasstools.hortonmachine.modules.networktools.trento_p.utils.Constants.DEFAULT_MINIMUM_DEPTH;
import static org.jgrasstools.hortonmachine.modules.networktools.trento_p.utils.Constants.DEFAULT_MIN_DISCHARGE;
import static org.jgrasstools.hortonmachine.modules.networktools.trento_p.utils.Constants.DEFAULT_TDTP;
import static org.jgrasstools.hortonmachine.modules.networktools.trento_p.utils.Constants.DEFAULT_TMAX;
import static org.jgrasstools.hortonmachine.modules.networktools.trento_p.utils.Constants.DEFAULT_TOLERANCE;
import static org.jgrasstools.hortonmachine.modules.networktools.trento_p.utils.Constants.DEFAULT_TPMIN;

import java.util.HashMap;
import java.util.LinkedHashMap;

import oms3.annotations.Author;
import oms3.annotations.Bibliography;
import oms3.annotations.Description;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Keywords;
import oms3.annotations.Label;
import oms3.annotations.License;
import oms3.annotations.Out;
import oms3.annotations.Range;
import oms3.annotations.Role;
import oms3.annotations.Status;
import oms3.annotations.Unit;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.jgrasstools.gears.libs.monitor.IJGTProgressMonitor;
import org.jgrasstools.gears.libs.monitor.PrintStreamProgressMonitor;
import org.jgrasstools.hortonmachine.i18n.HortonMessageHandler;
import org.jgrasstools.hortonmachine.modules.networktools.trento_p.net.Network;
import org.jgrasstools.hortonmachine.modules.networktools.trento_p.net.NetworkBuilder;
import org.jgrasstools.hortonmachine.modules.networktools.trento_p.net.NetworkCalibration;
import org.jgrasstools.hortonmachine.modules.networktools.trento_p.net.Pipe;
import org.jgrasstools.hortonmachine.modules.networktools.trento_p.utils.TrentoPFeatureType;
import org.jgrasstools.hortonmachine.modules.networktools.trento_p.utils.TrentoPFeatureType.PipesTrentoP;
import org.joda.time.DateTime;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;

@Description("Calculates the diameters of a sewer net or verify the discharge for each pipe.")
@Author(name = "Daniele Andreis,Rigon Riccardo,David tamanini", contact = "")
@Keywords("Sewer network")
@Bibliography("http://www.ing.unitn.it/dica/hp/?user=rigon")
@Label(" ")
@Status(Status.DRAFT)
@License("http://www.gnu.org/licenses/gpl-3.0.html")
public class TrentoP {

    /**
     * Message handler.
     */
    private final HortonMessageHandler msg = HortonMessageHandler.getInstance();

    @Description("The progress monitor.")
    @In
    public final IJGTProgressMonitor pm = new PrintStreamProgressMonitor(System.out, System.err);

    @Description("Minimum excavation depth")
    @Unit("m")
    @In
    public double pMinimumDepth = DEFAULT_MINIMUM_DEPTH;

    @Description("Max number of pipes that can converge in a junction.")
    @Unit("-")
    @Range(max = 6, min = 0)
    @In
    public int pMaxJunction = DEFAULT_MAX_JUNCTION;

    @Description("Max number of bisection to do (default is 40)to search a solution of a transcendently equation.")
    @Unit("-")
    @Range(max = 1000, min = 3)
    @In
    public int pJMax = DEFAULT_J_MAX;

    @Description("Accuracy to use to calculate a solution with bisection method.")
    @Unit("-")
    @Range(min = 0)
    @In
    public Double pAccuracy;

    @Description("Accuracy to use to calculate the discharge.")
    @Unit("-")
    @Range(max = 1, min = 0)
    @In
    public double pEpsilon = DEFAULT_EPSILON;

    @Description("Minimum Fill degree")
    @Unit("-")
    @Range(max = 0.1, min = 0)
    @In
    public double pMinG = DEFAULT_MING;

    @Description("Minimum discharge in a pipe")
    @Unit("m3/s")
    @Range(min = 0)
    @In
    public double pMinDischarge = DEFAULT_MIN_DISCHARGE;

    @Description("Maximum Fill degree")
    @Unit("-")
    @Range(min = 3.14)
    @In
    public double pMaxTheta = DEFAULT_MAX_THETA;

    @Description("Celerity factor, value used to obtain the celerity of the discharge wave.")
    @Unit("-")
    @Range(min = 1, max = 1.6)
    @In
    public double pCelerityFactor = DEFAULT_CELERITY_FACTOR;

    @Description("Exponent of the basin extension. Used to calculate the average acces time to the network.")
    @Unit("-")
    @Range(min = 0)
    @In
    public double pExponent = DEFAULT_EXPONENT;

    @Description("tollerance, used to obtain the radius")
    @Unit("-")
    @Range(min = 0)
    @In
    public double pTolerance = DEFAULT_TOLERANCE;

    @Description("Division base to height in the rectangular or trapezium section.")
    @Unit("-")
    @Range(min = 0)
    @In
    public double pC = 1;

    @Description("Exponent of the average ponderal slope of a basin to calculate the average access time to the network  for area units.")
    @Unit("-")
    @Range(min = 0)
    @In
    public double pGamma = DEFAULT_GAMMA;

    @Description("Exponent of the influx coefficent to calculate the average residence time in the network .")
    @Unit("-")
    @Range(min = 0)
    @In
    public double pEspInflux = DEFAULT_ESP1;

    @Description("Minimum dig depth, for rectangular or trapezium pipe.")
    @Unit("m")
    @Range(min = 0)
    @In
    public double pFranco = DEFAULT_FRANCO;

    @Description(" Coefficient of the pluviometric curve of possibility.")
    @Unit("-")
    @Range(min = 0)
    @In
    public Double pA;

    @Description("Exponent of the pluviometric curve of possibility. ")
    @Unit("-")
    @Range(min = 0.05, max = 0.95)
    @In
    public Double pN;

    @Description("Tangential bottom stress, which ensure the self-cleaning of the network.")
    @Unit("N/m2")
    @Range(min = 0)
    @In
    public Double pTau;

    @Description("Fill degree to use during the project.")
    @Unit("-")
    @Range(min = 0, max = 0.99)
    @In
    public Double pG;

    @Description("Align mode, it can be 0 (so the free surface is aligned through a change in the depth of the pipes) or 1 (aligned with bottom step).")
    @In
    public short pAlign;

    @Description("Matrix which contains the commercial diameters of the pipes.")
    @In
    public double[][] inDiameters;

    @Description("The outlet, the last pipe of the network,.")
    @Unit("-")
    @In
    public Integer pOutPipe = null;

    @Description(" Use mode, 0=project, 1=verify.")
    @In
    public short pTest;

    @Description("Time step to calculate the discharge.")
    @Unit("-")
    @Range(min = 0.015)
    @In
    public double tDTp = DEFAULT_TDTP;

    @Description("Minimum Rain Time step to calculate the discharge.")
    @Unit("-")
    @Range(min = 5)
    @In
    public double tpMin = DEFAULT_TPMIN;

    @Description("Maximum Rain Time step to calculate the discharge.")
    @Unit("-")
    @Range(min = 30)
    @In
    public double tpMax = DEFAULT_TMAX;

    @Description("Max number of time step.")
    @Unit("-")
    @In
    public double tMax = DEFAULT_TMAX;

    @Description("Time step, if pMode=1, in minutes. Is the step used to calculate the discharge. If it's not setted then it's equal to the rain time step.")
    @Unit("minutes")
    @In
    public Integer dt;

    @Description("rain data.")
    @Role(Role.INPUT)
    @In
    public HashMap<DateTime, double[]> inRain = null;

    @Description("The read feature collection.")
    @In
    public SimpleFeatureCollection inPipes = null;

    @Description("The output feature collection which contains the net with all hydraulics value.")
    @Out
    public SimpleFeatureCollection outPipes = null;
    /**
     * The output if pTest=1, contains the discharge for each pipes at several
     * time.
     */
    @Description(" The output if pTest=1, contains the discharge for each pipes at several time.")
    @Role(Role.OUTPUT)
    @Out
    public HashMap<DateTime, double[]> outDischarge;

    /**
     * Is an array with all the pipe of the net.
     */
    private Pipe[] networkPipes;

    /*
     * string which collected all the warnings. the warnings are printed at the
     * end of the processes.
     */
    private String warnings = "warnings";
    private StringBuilder strBuilder = new StringBuilder(warnings);

    /**
     * 
     * Elaboration on the net.
     * 
     * <p>
     * 
     * <ol>
     * <li>Verify the net throughout the method verify.
     * <li>Call the geoSewere method which calculate the pipes diameter or the
     * discharge.
     * </ol>
     * </p>
     * 
     * @throws Exception
     * 
     * 
     * @throw {@link IllegalArgumentException} this is throw to the verify
     *        methods, if some parameters isn't correct.
     * @see {@link NetworkBuilder}
     * 
     */
    @Execute
    public void process() throws Exception {
        /*
         * verify the parameter in input (these method, when the OMS annotation
         * work well, can be deleted).
         */
        verifyParameter();

        Pipe.pm = pm;
        // begin the process.
        pm.message(msg.message("trentoP.firstMessage"));
        // create the net as an array of pipes.
        setNetworkPipes();
        /*
         * create an network object. It can be a NetworkCalibration if the mode
         * (pTest==1) verify otherwise is a NetworkBuilder.
         */
        Network network = null;
        if (pTest == 1) {
            // set other common parameters for the verify.

            if (inPipes != null) {
                for( int t = 0; t < networkPipes.length; t++ ) {
                    networkPipes[t].setAccuracy(pAccuracy);
                    networkPipes[t].setMinimumDepth(pMinimumDepth);
                    networkPipes[t].setMinG(pMinG);
                    networkPipes[t].setJMax(pJMax);
                    networkPipes[t].setMaxTheta(pMaxTheta);
                    networkPipes[t].setTolerance(pTolerance);
                    networkPipes[t].setK(pEspInflux, pExponent, pGamma);

                }

            }

            outDischarge = new LinkedHashMap<DateTime, double[]>();
            // initialize the NetworkCalibration.
            network = new NetworkCalibration.Builder(pm, networkPipes, dt, inRain, outDischarge, strBuilder)
                    .celerityFactor(pCelerityFactor).tMax(tMax).build();

        } else {
            // set other common parameters for the project.

            if (inPipes != null) {
                for( int t = 0; t < networkPipes.length; t++ ) {
                    networkPipes[t].setAccuracy(pAccuracy);
                    networkPipes[t].setMinimumDepth(pMinimumDepth);
                    networkPipes[t].setMinG(pMinG);
                    networkPipes[t].setJMax(pJMax);
                    networkPipes[t].setMaxJunction(pMaxJunction);
                    networkPipes[t].setAlign(pAlign);
                    networkPipes[t].setJMax(pJMax);
                    networkPipes[t].setC(pC);
                    networkPipes[t].setG(pG);
                    networkPipes[t].setTau(pTau);
                    networkPipes[t].setMinDischarge(pMinDischarge);

                }

            }

            pA = pA / pow(60, pN); /* [mm/hour^n] -> [mm/min^n] */
            // initialize the NetworkCalibration.

            NetworkBuilder.Builder builder = new NetworkBuilder.Builder(pm, networkPipes, pN, pA, inDiameters, inPipes, outPipes,
                    strBuilder);
            network = builder.celerityFactor(pCelerityFactor).pEpsilon(pEpsilon).pEsp1(pEspInflux).pExponent(pExponent)
                    .pGamma(pGamma).tDTp(tDTp).tpMax(tpMax).tpMin(tpMin).build();
        }

        // elaborate.
        network.geoSewer();

        String w = strBuilder.toString();
        if (!w.equals(warnings)) {
            pm.message(w);
        }

        pm.message(msg.message("trentoP.end"));

    }

    /*
     * Verifica la validità dei dati, OSSERVAZIONE con OMS non necessaria,
     * vedere dichiarazione variabili per il range.
     * 
     * @throw IllegalArgumentException se un parametro non rispetta certe
     * condizioni (in OMS3 fatto dalle annotation)
     */
    private void verifyParameter() {

        if (inPipes == null) {
            pm.errorMessage(msg.message("trentoP.error.inputMatrix") + " geometry file");
            throw new IllegalArgumentException(msg.message("trentoP.error.inputMatrix" + " geometry file"));
        }

        /* Il numero di giunzioni in un nodo non puo' superiore a 7 */
        if (pMaxJunction <= 0 || pMaxJunction > 6) {
            pm.errorMessage(msg.message("trentoP.error.maxJunction"));
            throw new IllegalArgumentException();
        }

        /*
         * Il numero di iterazioni ammesso non puo' essere troppo piccolo ne'
         * eccessivamente grande
         */
        if (pJMax < 3 || pJMax > 1000) {
            pm.errorMessage(msg.message("trentoP.error.jMax"));
            throw new IllegalArgumentException(msg.message("trentoP.error.jMax"));
        }

        /*
         * La precisione con cui si cercano alcune soluzioni non puo' essere
         * negativa
         */
        if (pAccuracy <= 0 || pAccuracy == null) {
            pm.errorMessage(msg.message("trentoP.error.accuracy"));
            throw new IllegalArgumentException();
        }
        /* Intervallo in cui puo variare il riempimento minimo */
        if (pMinG <= 0 || pMinG > 0.1) {
            pm.errorMessage(msg.message("trentoP.error.minG"));
            throw new IllegalArgumentException();
        }
        /* Non sono ammesse portate minime negative nei tubi */
        if (pMinDischarge <= 0) {
            pm.errorMessage(msg.message("trentoP.error.minDischarge"));
            throw new IllegalArgumentException();
        }

        /* Il fattore di celerita' deve essere compreso tra 1 e 1.6 */
        if (pCelerityFactor < 1 || pCelerityFactor > 1.6) {
            pm.errorMessage(msg.message("trentoP.error.celerity"));
            throw new IllegalArgumentException();
        }

        /* EXPONENT non puo' essere negativo */
        if (pExponent <= 0) {
            pm.errorMessage(msg.message("trentoP.error.exponent"));
            throw new IllegalArgumentException();
        }

        /* La tolleranza non puo' essere nulla tantomeno negativa */
        if (pTolerance <= 0) {
            pm.errorMessage(msg.message("trentoP.error.tolerance"));
            throw new IllegalArgumentException();
        }

        if (pGamma <= 0) {
            pm.errorMessage(msg.message("trentoP.error.gamma"));
            throw new IllegalArgumentException();
        }

        if (pEspInflux <= 0) {
            pm.errorMessage(msg.message("trentoP.error.eps1"));
            throw new IllegalArgumentException();
        }

        // verificy if the field exist.
        SimpleFeatureType schema = inPipes.getSchema();
        verifyFeatureKey(findAttributeName(schema, PipesTrentoP.ID.getAttributeName()));
        verifyFeatureKey(findAttributeName(schema, PipesTrentoP.DRAIN_AREA.getAttributeName()));
        verifyFeatureKey(findAttributeName(schema, PipesTrentoP.INITIAL_ELEVATION.getAttributeName()));
        verifyFeatureKey(findAttributeName(schema, PipesTrentoP.FINAL_ELEVATION.getAttributeName()));
        verifyFeatureKey(findAttributeName(schema, PipesTrentoP.RUNOFF_COEFFICIENT.getAttributeName()));
        verifyFeatureKey(findAttributeName(schema, PipesTrentoP.AVERAGE_RESIDENCE_TIME.getAttributeName()));
        verifyFeatureKey(findAttributeName(schema, PipesTrentoP.KS.getAttributeName()));
        verifyFeatureKey(findAttributeName(schema, PipesTrentoP.MINIMUM_PIPE_SLOPE.getAttributeName()));
        verifyFeatureKey(findAttributeName(schema, PipesTrentoP.PIPE_SECTION_TYPE.getAttributeName()));
        verifyFeatureKey(findAttributeName(schema, PipesTrentoP.AVERAGE_SLOPE.getAttributeName()));;
        if (pTest == 0) {

            if (pA <= 0 || pA == null) {
                pm.errorMessage(msg.message("trentoP.error.a"));
                throw new IllegalArgumentException(msg.message("trentoP.error.a"));
            }
            if (pN < 0.05 || pN > 0.95 || pN == null) {
                pm.errorMessage(msg.message("trentoP.error.n"));
                throw new IllegalArgumentException(msg.message("trentoP.error.n"));
            }
            if (pTau <= 0 || pTau == null) {
                pm.errorMessage(msg.message("trentoP.error.tau"));
                throw new IllegalArgumentException(msg.message("trentoP.error.tau"));
            }

            if (pG <= 0 || pG > 0.99 || pG == null) {
                pm.errorMessage(msg.message("trentoP.error.g"));

                throw new IllegalArgumentException(msg.message("trentoP.error.g"));
            }
            if (pAlign != 0 && pAlign != 1) {
                pm.errorMessage(msg.message("trentoP.error.align"));
                throw new IllegalArgumentException(msg.message("trentoP.error.align"));
            }
            /* Lo scavo minimo non puo' essere uguale o inferiore a 0 */
            if (pMinimumDepth <= 0) {
                pm.errorMessage(msg.message("trentoP.error.scavomin"));
                throw new IllegalArgumentException();

            }
            /* Pecisione con cui si ricerca la portata nelle aree non di testa. */
            if (pEpsilon <= 0 || pEpsilon > 1) {
                pm.errorMessage(msg.message("trentoP.error.epsilon"));
                throw new IllegalArgumentException();
            }
            /*
             * L'angolo di riempimento minimo non puo' essere inferiore a 3.14
             * [rad]
             */
            if (pMaxTheta < 3.14) {
                pm.errorMessage(msg.message("trentoP.error.maxtheta"));
                throw new IllegalArgumentException();
            }
            if (pC <= 0) {
                pm.errorMessage(msg.message("trentoP.error.c"));
                throw new IllegalArgumentException();
            }
            if (inDiameters == null) {

                throw new IllegalArgumentException();
            }
        } else {

            if (inRain == null) {
                pm.errorMessage(msg.message("trentoP.error.inputRainMatrix") + " rain file");
                throw new IllegalArgumentException(msg.message("trentoP.error.inputRainMatrix" + " rain file"));
            }

            /*
             * Il passo temporale con cui valutare le portate non puo' essere
             * inferiore a 0.015 [min]
             */
            if (tDTp < 0.015) {
                pm.errorMessage(msg.message("trentoP.error.dtp"));
                throw new IllegalArgumentException();
            }

            /*
             * Tempo di pioggia minimo da considerare nella massimizzazione
             * delle portate non puo' essere superiore a 5 [min]
             */
            if (tpMin > 5) {
                pm.errorMessage(msg.message("trentoP.error.tpmin"));
                throw new IllegalArgumentException();
            }

            /*
             * Tempo di pioggia massimo da adottare nella ricerca della portata
             * massima non puo' essere inferiore a 5 [min]
             */
            if (tpMax < 30) {
                pm.errorMessage(msg.message("trentoP.error.tpmax"));
                throw new IllegalArgumentException();
            }
            // verificy if the field exist.

            verifyFeatureKey(findAttributeName(schema, PipesTrentoP.DIAMETER_TO_VERIFY.getAttributeName()));
            verifyFeatureKey(findAttributeName(schema, PipesTrentoP.VERIFY_PIPE_SLOPE.getAttributeName()));

        }
    }

    /**
     * Verify if there is a key of a FeatureCollections.
     * 
     * @param key
     * @throws IllegalArgumentException
     *             if the key is null.
     */
    private void verifyFeatureKey( String key ) {
        if (key == null) {
            pm.errorMessage(msg.message("trentoP.error.featureKey") + key);
            throw new IllegalArgumentException(msg.message("trentoP.error.featureKey") + key);
        }

    }

    /**
     * Initializating the array.
     * 
     * <p>
     * The array is the net. If there is a FeatureCollection extract values from
     * it. The Array is order following the ID.
     * </p>
     * oss: if the FeatureCillection is null a IllegalArgumentException is throw
     * in {@link TrentoP#verifyParameter()}.
     * 
     * @throws IllegalArgumentException
     *             if the FeatureCollection hasn't the correct parameters.
     */
    private void setNetworkPipes() throws Exception {

        int length = inPipes.size();
        networkPipes = new Pipe[length];
        SimpleFeatureIterator stationsIter = inPipes.features();
        try {
            int t;
            while( stationsIter.hasNext() ) {
                SimpleFeature feature = stationsIter.next();
                try {
                    /*
                     * extract the value of the ID which is the position (minus
                     * 1) in the array.
                     */

                    Number field = ((Number) feature.getAttribute(TrentoPFeatureType.ID_STR));
                    if (field == null) {
                        pm.errorMessage(msg.message("trentoP.error.number") + TrentoPFeatureType.ID_STR);
                        throw new IllegalArgumentException(msg.message("trentoP.error.number" + TrentoPFeatureType.ID_STR));
                    }
                    t = field.intValue();
                    networkPipes[t - 1] = new Pipe(feature, pTest);

                } catch (NullPointerException e) {
                    pm.errorMessage(msg.message("trentop.illegalNet"));
                    throw new IllegalArgumentException(msg.message("trentop.illegalNet"));

                }
            }

        } finally {
            stationsIter.close();
        }
        // set the id where drain of the outlet.
        networkPipes[pOutPipe - 1].setIdPipeWhereDrain(0);
        // start to construct the net.
        findIdWhereDrain(pOutPipe, networkPipes[pOutPipe - 1].point[0]);
        findIdWhereDrain(pOutPipe, networkPipes[pOutPipe - 1].point[1]);

        verifyNet(networkPipes, pm);

    }

    /**
     * Find the pipes that are draining in this pipe.
     * 
     * @param id
     *            the ID of this pipe.
     * @param cord
     *            the Coordinate of the link where drain.
     */
    private void findIdWhereDrain( int id, Coordinate cord ) {
        int t = 0;
        for( int i = 0; i < networkPipes.length; i++ ) {
            // if it is this pipe then go haead.
            if (id - 1 == i) {
                continue;
            }
            // there isn-t other pipe that can drain in this.
            else if (t == pMaxJunction) {
                break;
            }
            // the id is already set.
            else if (networkPipes[i].getIdPipeWhereDrain() != null) {
                continue;
            }
            // extract the coordinate of the point of the linee of the new pipe.
            Coordinate[] coords = networkPipes[i].point;
            // if one of the coordinates are near of coord then the 2 pipe are
            // linked.
            if (cord.distance(coords[0]) < 0.5) {
                networkPipes[i].setIdPipeWhereDrain(id);
                findIdWhereDrain(networkPipes[i].getId(), coords[1]);
                t++;
            } else if (cord.distance(coords[1]) < 0.5) {
                networkPipes[i].setIdPipeWhereDrain(id);
                findIdWhereDrain(networkPipes[i].getId(), coords[0]);
                t++;
            }

        }

    }

    /**
     * Verify if the network is consistent.
     * 
     * <p>
     * <ol>
     * <li>Verify that the <i>ID</i> of a pipe is a value less than the number
     * of pipe.
     * <li>Verify that the pipe where, the current pipe drain, have an <i>ID</i>
     * less than the number of pipes.
     * <li>Verify that there is an <b>outlet<b> in the net.
     * </ol>
     * </p>
     * 
     * @param networkPipes
     *            the array which rappresent the net.
     * @param pm
     *            the progerss monitor.
     * @throws IllegalArgumentException
     *             if the net is unconsistent.
     */
    public void verifyNet( Pipe[] networkPipes, IJGTProgressMonitor pm ) {
        /*
         * serve per verificare che ci sia almeno un'uscita. True= esiste
         * un'uscita
         */
        boolean isOut = false;
        if (networkPipes != null) {
            /* VERIFICA DATI GEOMETRICI DELLA RETE */
            // Per ogni stato
            int length = networkPipes.length;

            int kj;

            for( int i = 0; i < length; i++ )

            {
                // verifica che la rete abbia almeno un-uscita.
                if (networkPipes[i].getIdPipeWhereDrain() == 0) {
                    isOut = true;
                }
                /*
                 * Controlla che non ci siano errori nei dati geometrici della
                 * rete, numero ID pipe in cui drena i >del numero consentito
                 * (la numerazione va da 1 a length
                 */
                if (networkPipes[i].getIdPipeWhereDrain() > length) {
                    pm.errorMessage(msg.message("trentoP.error.pipe"));
                    throw new IllegalArgumentException(msg.message("trentoP.error.pipe"));
                }
                /*
                 * Da quanto si puo leggere nel file di input fossolo.geo in
                 * Fluide Turtle, ogni stato o sottobacino e contraddistinto da
                 * un numero crescente che va da 1 a n=numero di stati; n e
                 * anche pari a data->nrh. Inoltre si apprende che la prima
                 * colonna della matrice in fossolo.geo riporta l'elenco degli
                 * stati, mentre la seconda colonna ci dice dove ciascun stato
                 * va a drenare.(NON E AMMESSO CHE LO STESSO STATO DRENI SU PIU
                 * DI UNO!!) Questa if serve per verificare che non siano
                 * presenti condotte non dichiarate, ovvero piu realisticamente
                 * che non ci sia un'errore di numerazione o battitura. In altri
                 * termini lo stato analizzato non puo drenare in uno stato al
                 * di fuori di quelli esplicitamente dichiarati o dell'uscita,
                 * contradistinta con ID 0
                 */
                kj = i;
                /*
                 * Terra conto degli stati attraversati dall'acqua che inizia a
                 * scorrere a partire dallo stato analizzato
                 */

                int count = 0;
                /*
                 * Seguo il percorso dell'acqua a partire dallo stato corrente
                 */
                while( networkPipes[kj].getIdPipeWhereDrain() != 0 ) {
                    kj = networkPipes[kj].getIdPipeWhereDrain() - 1;
                    /*
                     * L'acqua non puo finire in uno stato che con sia tra
                     * quelli esplicitamente definiti, in altre parole il
                     * percorso dell'acqua non puo essere al di fuori
                     * dell'inseme dei dercorsi possibili
                     */
                    if (kj > length) {
                        pm.errorMessage(msg.message("trentoP.error.drainPipe") + kj);
                        throw new IllegalArgumentException(msg.message("trentoP.error.drainPipe") + kj);
                    }

                    count++;
                    if (count > length) {
                        pm.errorMessage(msg.message("trentoP.error.pipe"));
                        throw new IllegalArgumentException(msg.message("trentoP.error.pipe"));
                    }
                    /*
                     * La variabile count mi consente di uscire dal ciclo while,
                     * nel caso non ci fosse [kj][2]=0, ossia un'uscita. Infatti
                     * partendo da uno stato qualsiasi il numero degli stati
                     * attraversati prima di raggiungere l'uscita non puo essere
                     * superiore al numero degli stati effettivamente presenti.
                     * Quando questo accade vuol dire che l'acqua e in un loop
                     * chiuso
                     */
                }

            }
            /*
             * Non si e trovato neanche un uscita, quindi Trento_p da errore di
             * esecuzione, perchè almeno una colonna deve essere l'uscita
             */
            if (isOut == false) {
                pm.errorMessage(msg.message("trentoP.error.noout"));
                throw new IllegalArgumentException(msg.message("trentoP.error.noout"));
            }

        } else {
            throw new IllegalArgumentException(msg.message("trentoP.error.incorrectmatrix"));
        }

    }

    /**
     * Temporaneo per i test, ritorna i dati sotto forma di matrice.
     * 
     * @return
     */
    public double[][] getResults() {
        double[][] results = new double[networkPipes.length][28];

        for( int i = 0; i < networkPipes.length; i++ ) {
            results[i][0] = networkPipes[i].getId();
            results[i][1] = networkPipes[i].getIdPipeWhereDrain();
            results[i][2] = networkPipes[i].getDrainArea();
            results[i][3] = networkPipes[i].getLenght();
            results[i][4] = networkPipes[i].getInitialElevation();
            results[i][5] = networkPipes[i].getFinalElevation();
            results[i][6] = networkPipes[i].getRunoffCoefficient();
            results[i][7] = networkPipes[i].getAverageResidenceTime();
            results[i][8] = networkPipes[i].getKs();
            results[i][9] = networkPipes[i].getMinimumPipeSlope();
            results[i][10] = networkPipes[i].getPipeSectionType();
            results[i][11] = networkPipes[i].getAverageSlope();
            results[i][12] = networkPipes[i].discharge;
            results[i][13] = networkPipes[i].coeffUdometrico;
            results[i][14] = networkPipes[i].residenceTime;
            results[i][15] = networkPipes[i].tP;
            results[i][16] = networkPipes[i].tQmax;
            results[i][17] = networkPipes[i].meanSpeed;
            results[i][18] = networkPipes[i].pipeSlope;
            results[i][19] = networkPipes[i].diameter;
            results[i][20] = networkPipes[i].emptyDegree;
            results[i][21] = networkPipes[i].depthInitialPipe;
            results[i][22] = networkPipes[i].depthFinalPipe;
            results[i][23] = networkPipes[i].initialFreesurface;
            results[i][24] = networkPipes[i].finalFreesurface;
            results[i][25] = networkPipes[i].totalSubNetArea;
            results[i][26] = networkPipes[i].meanLengthSubNet;
            results[i][27] = networkPipes[i].varianceLengthSubNet;

        }

        return results;
    }
}