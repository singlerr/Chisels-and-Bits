package mod.chiselsandbits.api.item.chisel;

import mod.chiselsandbits.api.chiseling.IChiselMode;
import mod.chiselsandbits.api.item.leftclick.ILeftClickControllingItem;
import mod.chiselsandbits.api.item.withmode.IWithModeItem;

public interface IChiselingItem extends ILeftClickControllingItem, IWithModeItem<IChiselMode>
{
}
