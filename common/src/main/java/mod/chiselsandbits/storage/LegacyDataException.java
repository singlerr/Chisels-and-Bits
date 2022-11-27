package mod.chiselsandbits.storage;

public class LegacyDataException extends RuntimeException {

    public LegacyDataException() {
        super("Legacy data is not supported anymore. Please use an older version to convert your world to the new format.");
    }
}
