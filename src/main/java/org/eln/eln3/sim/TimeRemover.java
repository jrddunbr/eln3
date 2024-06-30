package org.eln.eln3.sim;

import org.eln.eln3.Eln3;

public class TimeRemover implements IProcess {

    ITimeRemoverObserver observer;
    double timeout = 0;

    public TimeRemover(ITimeRemoverObserver observer) {
        this.observer = observer;
    }

    public void setTimeout(double timeout) {
        if (this.timeout <= 0) {
            observer.timeRemoverAdd();
            Eln3.getSimulator().addSlowProcess(this);
        }
        this.timeout = timeout;
    }

    @Override
    public void process(double time) {
        if (isArmed()) {
            timeout -= time;
            if (timeout <= 0) {
                shot();
            }
        }
    }

    public boolean isArmed() {
        return timeout > 0;
    }

    public void shot() {
        timeout = 0;
        observer.timeRemoverRemove();
        Eln3.getSimulator().removeSlowProcess(this);
    }
}
