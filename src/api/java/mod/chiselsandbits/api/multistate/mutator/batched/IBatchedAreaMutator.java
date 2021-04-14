package mod.chiselsandbits.api.multistate.mutator.batched;

import mod.chiselsandbits.api.multistate.mutator.IAreaMutator;

/**
 * A mutator which supports making mutations in batches.
 */
public interface IBatchedAreaMutator extends IAreaMutator
{
    /**
     * Trigger a batch mutation start.
     *
     * As long as at least one batch mutation is still running
     * no changes are transmitted to the client.
     *
     * @return The batch mutation lock.
     */
    IBatchMutation batch();
}
