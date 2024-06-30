package org.eln.eln3.sim.mna.component;

import org.eln.eln3.sim.mna.SubSystem;

public interface IAbstractor {

    void dirty(Component component);

    SubSystem getAbstractorSubSystem();
}
