package org.eln.eln3.compat;

import mcjty.theoneprobe.api.ITheOneProbe;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModList;
import org.eln.eln3.Eln3;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * I tried to make this class in Kotlin, and what prevented it was the weird invocation on L20 and Function on L23
 */
public class TopCompat {

    public static void register() {
        if (!ModList.get().isLoaded("theoneprobe")) {
            return;
        }
        InterModComms.sendTo("theoneprobe", "getTheOneProbe", GetTheOneProbe::new);
    }

    public static class GetTheOneProbe implements Function<ITheOneProbe, Void> {

        public static ITheOneProbe probe;

        @Nullable
        @Override
        public Void apply(ITheOneProbe theOneProbe) {
            probe = theOneProbe;
            Eln3.Companion.getLOGGER().info("Enabled support for The One Probe");
            probe.registerProvider(TopCompatibilityKotlin.getProbeInfoProvider());
            return null;
        }
    }
}