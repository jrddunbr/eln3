package org.eln.eln3.sim;

import org.eln.eln3.misc.Utils;

import java.util.List;
/*
public class MonsterPopFreeProcess implements IProcess {

    private final Coordinate coordinate;
    private final int range;

    double timerCounter = 0;
    final double timerPeriod = 0.212;

    List oldList = null;

    public MonsterPopFreeProcess(Coordinate coordinate, int range) {
        this.coordinate = coordinate;
        this.range = range;
    }

    @Override
    public void process(double time) {
        //Monster killing must be active before continuing :
        if (!Eln.instance.killMonstersAroundLamps)
            return;

        timerCounter += time;
        if (timerCounter > timerPeriod) {
            timerCounter -= Utils.rand(1, 1.5) * timerPeriod;
            List list = coordinate.world().getEntitiesWithinAABB(EntityMob.class, coordinate.getAxisAlignedBB(range + 8));

            for (Object o : list) {
                EntityMob mob = (EntityMob) o;
                if (oldList == null || !oldList.contains(o)) {
                    if (coordinate.distanceTo(mob) < range) {
                        if (!(o instanceof ReplicatorEntity) && !(o instanceof EntityWither) && !(o instanceof EntityEnderman)) {
                            mob.setDead();
                            Utils.println("MonsterPopFreeProcess : Dead");
                        }
                    }
                }
            }
            oldList = list;
        }
    }

}
*/