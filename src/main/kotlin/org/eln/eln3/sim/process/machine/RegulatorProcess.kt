package org.eln.eln3.sim.process.machine

import net.minecraft.nbt.CompoundTag
import org.eln.eln3.sim.IProcess
import org.eln.eln3.sim.nbt.TagSerializable

abstract class RegulatorProcess(var name: String?) : IProcess, TagSerializable {
    var type: RegulatorType = RegulatorType.None
    var target: Double = 0.0
    var OnOffHysteresisDiv2: Double = 0.0
    var P: Double = 0.0
    var I: Double = 0.0
    var D: Double = 0.0
    var hitLast: Double = 0.0
    var errorIntegrated: Double = 0.0
    var boot: Boolean = true

    fun setManual() {
        type = RegulatorType.Manual
    }

    fun setNone() {
        type = RegulatorType.None
    }

    fun setOnOff(OnOffHysteresisFactor: Double, workingPoint: Double) {
        type = RegulatorType.OnOff
        this.OnOffHysteresisDiv2 = OnOffHysteresisFactor * workingPoint / 2
        boot = false
        setCmd(0.0)
    }

    fun setAnalog(P: Double, I: Double, D: Double, workingPoint: Double) {
        var P = P
        var I = I
        var D = D
        P /= workingPoint
        I /= workingPoint
        D /= workingPoint

        if (!boot && (this.P != P || this.I != I || this.D != D || type != RegulatorType.Analog)) {
            errorIntegrated = 0.0
            hitLast = this.hit
        }

        this.P = P
        this.I = I
        this.D = D

        type = RegulatorType.Analog
        boot = false
    }

    protected abstract val hit: Double

    protected abstract fun setCmd(cmd: Double)

    override fun process(time: Double) {
        val hit = this.hit

        when (type) {
            RegulatorType.Manual -> {}
            RegulatorType.None -> setCmd(1.0)
            RegulatorType.Analog -> {
                val error = target - hit
                val fP = error * P
                var cmd = fP - (hit - hitLast) * D * time

                errorIntegrated += error * time * I

                if (errorIntegrated > 1.0 - fP) {
                    errorIntegrated = 1.0 - fP
                    if (errorIntegrated < 0.0) errorIntegrated = 0.0
                } else if (errorIntegrated < (-1.0 + fP)) {
                    errorIntegrated = (-1.0 + fP)
                    if (errorIntegrated > 0.0) errorIntegrated = 0.0
                }

                cmd += errorIntegrated

                if (cmd > 1.0) setCmd(1.0)
                else if (cmd < -1.0) setCmd(-1.0)
                else setCmd(cmd)

                hitLast = hit
            }

            RegulatorType.OnOff -> {
                if (hit > target + OnOffHysteresisDiv2) setCmd(0.0)
                if (hit < target - OnOffHysteresisDiv2) setCmd(1.0)
            }

            else -> {}
        }
    }

    override fun loadAdditionalData(nbt: CompoundTag, str: String) {
        errorIntegrated = nbt.getDouble(str + name + "errorIntegrated")
        if (java.lang.Double.isNaN(errorIntegrated)) errorIntegrated = 0.0
        this.target = nbt.getDouble(str + name + "target")
    }

    override fun saveAdditionalData(nbt: CompoundTag, str: String) {
        nbt.putDouble(str + name + "errorIntegrated", errorIntegrated)
        nbt.putDouble(str + name + "target", target)
    }
}