package mod.chiselsandbits.logic;

import mod.chiselsandbits.measures.MeasuringManager;

public class MeasuringSynchronisationHandler
{

    public static void syncToAll() {
        MeasuringManager.getInstance().syncToAll();
    }
}
