package mod.chiselsandbits.api.chiseling;

/**
 * Represents the mode of operandus of the current chiseling context.
 */
public enum ChiselingOperation
{
    CHISELING(false),
    PLACING(true);

    private final boolean processesAir;

    ChiselingOperation(final boolean processesAir) {this.processesAir = processesAir;}

    public boolean processesAir()
    {
        return processesAir;
    }
}
