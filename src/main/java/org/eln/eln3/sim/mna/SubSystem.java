package org.eln.eln3.sim.mna;


import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.eln.eln3.misc.Profiler;
import org.eln.eln3.misc.Utils;
import org.eln.eln3.sim.mna.component.Component;
import org.eln.eln3.sim.mna.component.CurrentSource;
import org.eln.eln3.sim.mna.component.Resistor;
import org.eln.eln3.sim.mna.component.VoltageSource;
import org.eln.eln3.sim.mna.misc.IDestructor;
import org.eln.eln3.sim.mna.misc.ISubSystemProcessFlush;
import org.eln.eln3.sim.mna.misc.ISubSystemProcessI;
import org.eln.eln3.sim.MnaConst;
import org.eln.eln3.sim.mna.state.State;
import org.eln.eln3.sim.mna.state.VoltageState;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SubSystem {
    public ArrayList<Component> component = new ArrayList<Component>();
    public List<State> states = new ArrayList<State>();
    public LinkedList<IDestructor> breakDestructor = new LinkedList<IDestructor>();
    public ArrayList<SubSystem> interSystemConnectivity = new ArrayList<SubSystem>();
    ArrayList<ISubSystemProcessI> processI = new ArrayList<ISubSystemProcessI>();
    State[] statesTab;

    RootSystem root;

    double dt;
    boolean matrixValid = false;

    int stateCount;
    RealMatrix A;
    boolean singularMatrix;

    double[][] AInvdata;
    double[] Idata;

    double[] XtempData;

    boolean breaked = false;

    ArrayList<ISubSystemProcessFlush> processF = new ArrayList<ISubSystemProcessFlush>();

    public RootSystem getRoot() {
        return root;
    }

    public SubSystem(RootSystem root, double dt) {
        this.dt = dt;
        this.root = root;
    }

    public void invalidate() {
        matrixValid = false;
    }

    public void addComponent(Component c) {
        component.add(c);
        c.addToSubsystem(this);
        invalidate();
    }

    public void addState(State s) {
        states.add(s);
        s.setSubsystem(this);
        invalidate();
    }

    public void removeComponent(Component c) {
        component.remove(c);
        c.quitSubSystem();
        invalidate();
    }

    public void removeState(State s) {
        states.remove(s);
        s.quitSubSystem();
        invalidate();
    }

    public void removeProcess(ISubSystemProcessI p) {
        processI.remove(p);
        invalidate();
    }

    public void addComponent(Iterable<Component> i) {
        for (Component c : i) {
            addComponent(c);
        }
    }

    public void addState(Iterable<State> i) {
        for (State s : i) {
            addState(s);
        }
    }

    public void addProcess(ISubSystemProcessI p) {
        processI.add(p);
    }

    public void generateMatrix() {
        stateCount = states.size();

        Profiler p = new Profiler();
        p.add("Inversse with " + stateCount + " state : ");

        A = MatrixUtils.createRealMatrix(stateCount, stateCount);
        Idata = new double[stateCount];
        XtempData = new double[stateCount];
        {
            int idx = 0;
            for (State s : states) {
                s.setId(idx++);
            }
        }

        for (Component c : component) {
            c.applyToSubsystem(this);
        }

        //	org.apache.commons.math3.linear.

        try {
            //FieldLUDecomposition QRDecomposition  LUDecomposition RRQRDecomposition
            RealMatrix Ainv = new QRDecomposition(A).getSolver().getInverse();
            AInvdata = Ainv.getData();
            singularMatrix = false;
        } catch (Exception e) {
            singularMatrix = true;
            if (stateCount > 1) {
                int idx = 0;
                idx++;
                Utils.println("//////////SingularMatrix////////////");
            }
        }

        statesTab = new State[stateCount];
        statesTab = states.toArray(statesTab);

        matrixValid = true;

        p.stop();
        //Utils.println(p);
    }

    public void addToA(State a, State b, double v) {
        if (a == null || b == null)
            return;
        A.addToEntry(a.getId(), b.getId(), v);
    }

    public void addToI(State s, double v) {
        if (s == null) return;
        Idata[s.getId()] = v;
    }

    public void step() {
        stepCalc();
        stepFlush();
    }

    public void stepCalc() {
        if (!matrixValid) {
            generateMatrix();
        }

        if (!singularMatrix) {
            for (int y = 0; y < stateCount; y++) {
                Idata[y] = 0;
            }
            for (ISubSystemProcessI p : processI) {
                p.simProcessI(this);
            }

            for (int idx2 = 0; idx2 < stateCount; idx2++) {
                double stack = 0;
                for (int idx = 0; idx < stateCount; idx++) {
                    stack += AInvdata[idx2][idx] * Idata[idx];
                }
                XtempData[idx2] = stack;
            }
        }
    }

    public double solve(State pin) {
        if (!matrixValid) {
            generateMatrix();
        }

        if (!singularMatrix) {
            for (int y = 0; y < stateCount; y++) {
                Idata[y] = 0;
            }
            for (ISubSystemProcessI p : processI) {
                p.simProcessI(this);
            }

            int idx2 = pin.getId();
            double stack = 0;
            for (int idx = 0; idx < stateCount; idx++) {
                stack += AInvdata[idx2][idx] * Idata[idx];
            }
            return stack;
        }
        return 0;
    }

    public void stepFlush() {
        if (!singularMatrix) {
            for (int idx = 0; idx < stateCount; idx++) {
                statesTab[idx].state = XtempData[idx];

            }
        } else {
            for (int idx = 0; idx < stateCount; idx++) {
                statesTab[idx].state = 0;
            }
        }

        for (ISubSystemProcessFlush p : processF) {
            p.simProcessFlush();
        }
    }

    public static void main(String[] args) {
//		SubSystem s = new SubSystem(null, 0.1);
//		VoltageState n1, n2;
//		VoltageSource u1;
//		Resistor r1, r2;
//
//		s.addState(n1 = new VoltageState());
//		s.addState(n2 = new VoltageState());
//
//		//s.addComponent((u1 = new VoltageSource()).setU(1).connectTo(n1, null));
//
//		s.addComponent((r1 = new Resistor()).setR(10).connectTo(n1, n2));
//		s.addComponent((r2 = new Resistor()).setR(20).connectTo(n2, null));
//
//		s.step();
//		s.step();

        SubSystem s = new SubSystem(null, 0.1);
        VoltageState n1, n2;
        CurrentSource cs1;
        Resistor r1;

        s.addState(n1 = new VoltageState());

        s.addComponent((cs1 = new CurrentSource("cs1")).setCurrent(0.01).connectTo(n1, null));
        s.addComponent((r1 = new Resistor()).setResistance(10).connectTo(n1, null));

        s.step();

        System.out.println("R: U = " + r1.getVoltage() + ", I = " + r1.getCurrent());
        System.out.println("CS: U = " + cs1.getVoltage());
    }

    public boolean containe(State state) {
        return states.contains(state);
    }

    public void setX(State s, double value) {
        s.state = value;
    }

    public double getX(State s) {
        return s.state;
    }

    public double getXSafe(State bPin) {
        return bPin == null ? 0 : getX(bPin);
    }

    public boolean breakSystem() {
        if (breaked) return false;
        while (!breakDestructor.isEmpty()) {
            breakDestructor.pop().destruct();
        }

        for (Component c : component) {
            c.quitSubSystem();
        }
        for (State s : states) {
            s.quitSubSystem();
        }

        if (root != null) {
            for (Component c : component) {
                c.returnToRootSystem(root);
            }
            for (State s : states) {
                s.returnToRootSystem(root);
            }
        }
        root.systems.remove(this);

        invalidate();

        breaked = true;
        return true;
    }

    public void addProcess(ISubSystemProcessFlush p) {
        processF.add(p);
    }

    public void removeProcess(ISubSystemProcessFlush p) {
        processF.remove(p);
    }

    public double getDt() {
        return dt;
    }

    static public class Thevenin {
        public double resistance, voltage;

        public boolean isHighImpedance() {
            return resistance > 1e8;
        }
    }

    public Thevenin getTh(State d, VoltageSource voltageSource) {
        Thevenin thevenin = new Thevenin();
        double originalVoltage = d.state;

        double testVoltage = originalVoltage + 5;
        voltageSource.setVoltage(testVoltage);
        double testCurrent = solve(voltageSource.getCurrentState());

        voltageSource.setVoltage(originalVoltage);
        double originalCurrent = solve(voltageSource.getCurrentState());

        double theveninResistance = (testVoltage - originalVoltage) / (originalCurrent - testCurrent);
        double theveninVoltage;
        if (theveninResistance > 10000000000000000000.0 || theveninResistance < 0) {
            theveninVoltage = 0;
            theveninResistance = 10000000000000000000.0;
        } else {
            theveninVoltage = testVoltage + theveninResistance * testCurrent;
        }
        voltageSource.setVoltage(originalVoltage);

        thevenin.resistance = theveninResistance;
        thevenin.voltage = theveninVoltage;

        if(Double.isNaN(thevenin.voltage)) {
            thevenin.voltage = originalVoltage;
            thevenin.resistance = MnaConst.highImpedance;
        }
        if (Double.isNaN(thevenin.resistance)) {
            thevenin.voltage = originalVoltage;
            thevenin.resistance = MnaConst.highImpedance;
        }

        return thevenin;
    }

    public String toString() {
        String str = "";
        for (Component c: component) {
            if (c != null)
                str += c.toString();
        }
        return str;
    }

    public int componentSize() {
        return component.size();
    }
}
