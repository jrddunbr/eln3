package org.eln.eln3.sim.mna.state;

import org.eln.eln3.sim.mna.RootSystem;
import org.eln.eln3.sim.mna.SubSystem;
import org.eln.eln3.sim.mna.component.Component;
import org.eln.eln3.sim.mna.component.IAbstractor;

import java.util.ArrayList;

public class State {

    private int id = -1;

    public double state;
    SubSystem subSystem;

    ArrayList<Component> components = new ArrayList<Component>();

    boolean isPrivateSubSystem = false;
    boolean mustBeFarFromInterSystem = false;

    public IAbstractor abstractedBy;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setSubsystem(SubSystem s) {
        this.subSystem = s;
    }

    public SubSystem getSubSystem() {
        if (isAbstracted()) return abstractedBy.getAbstractorSubSystem();
        return subSystem;
    }

    public void quitSubSystem() {
        subSystem = null;
    }

    public ArrayList<Component> getConnectedComponents() {
        return components;
    }

    public ArrayList<Component> getConnectedComponentsNotAbstracted() {
        ArrayList<Component> list = new ArrayList<Component>();
        for (Component c : components) {
            if (c.isAbstracted()) continue;
            list.add(c);
        }
        return list;
    }

    public void addComponent(Component c) {
        components.add(c);
    }

    public void removeComponent(Component c) {
        components.remove(c);
    }

    public boolean canBeSimplifiedByLine() {
        return false;
    }

    public State setAsPrivate() {
        isPrivateSubSystem = true;
        return this;
    }

    public State setAsMustBeFarFromInterSystem() {
        mustBeFarFromInterSystem = true;
        return this;
    }

    public boolean mustBeFarFromInterSystem() {
        return mustBeFarFromInterSystem;
    }

    public boolean isPrivateSubSystem() {
        return isPrivateSubSystem;
    }

    public void returnToRootSystem(RootSystem root) {
        root.addStates.add(this);
    }

    public boolean isAbstracted() {
        return abstractedBy != null;
    }

    public boolean isNotSimulated() {
        return subSystem == null && abstractedBy == null;
    }

    @Override
    public String toString() {
        return "(" + this.getId() + "," + this.getClass().getSimpleName() + ")";
    }
}
