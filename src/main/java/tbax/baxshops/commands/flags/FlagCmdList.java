/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
 **/

package tbax.baxshops.commands.flags;

import org.jetbrains.annotations.NotNull;
import tbax.baxshops.BaxShop;
import tbax.baxshops.Format;
import tbax.baxshops.commands.ShopCmdActor;
import tbax.baxshops.errors.PrematureAbortException;

public final class FlagCmdList extends FlagCmd
{
    @Override
    public @NotNull String[] getAliases()
    {
        return new String[]{"list"};
    }

    @Override
    public boolean hasValidArgCount(@NotNull ShopCmdActor actor)
    {
        return actor.getNumArgs() == 2;
    }

    @Override
    public void onCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        BaxShop shop = actor.getShop();
        assert shop != null;
        actor.sendMessage("\nFlags currently applied to this shop:");
        actor.sendMessage("%s: %s", Format.flag("Infinite"), Format.keyword(shop.hasFlagInfinite() ? "Yes" : "No"));
        actor.sendMessage("%s: %s", Format.flag("Notify"), Format.keyword(shop.hasFlagNotify() ? "Yes" : "No"));
        actor.sendMessage("%s: %s", Format.flag("Sell to Shop"), Format.keyword(shop.hasFlagSellToShop() ? "Yes" : "No"));
        actor.sendMessage("%s: %s", Format.flag("Sell Requests"), Format.keyword(shop.hasFlagSellRequests() ? "Yes" : "No"));
        actor.sendMessage("%s: %s", Format.flag("Buy Requests"), Format.keyword(shop.hasFlagBuyRequests() ? "Yes" : "No"));
    }
}
