/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands;

import org.jetbrains.annotations.NotNull;
import tbax.baxshops.BaxEntry;
import tbax.baxshops.CommandHelpArgument;
import tbax.baxshops.Resources;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.CommandHelp;

public final class CmdSetIndex extends BaxShopCommand
{
    @Override
    public @NotNull String getName()
    {
        return "setindex";
    }

    @Override
    public @NotNull String[] getAliases()
    {
        return new String[]{"setindex","setorder","reorder"};
    }

    @Override
    public String getPermission()
    {
        return "shops.owner";
    }

    @Override
    public CommandHelp getHelp(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        CommandHelp help = super.getHelp(actor);
        help.setDescription("Change the order of an entry in the shop");
        help.setArgs(
            new CommandHelpArgument("old-index", "the current index of the item", true),
            new CommandHelpArgument("new-index", "the new index of the item", true)
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull ShopCmdActor actor)
    {
        return actor.getNumArgs() == 3;
    }

    @Override
    public boolean requiresSelection(@NotNull ShopCmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresOwner(@NotNull ShopCmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresPlayer(@NotNull ShopCmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresItemInHand(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public void onCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        assert actor.getShop() != null;
        int oldIndex = actor.getShop().indexOf(actor.getArgEntry(1));
        int newIndex = actor.getArgInt(2, String.format(Resources.INVALID_DECIMAL, "new index"));
        if (newIndex > actor.getShop().size()) {
            actor.exitError( "You must choose a new index that is less than the number of items in the shop!");
        }
        if (newIndex < 1) {
            actor.exitError("The new index must be greater than 0.");
        }
        if (newIndex == oldIndex) {
            actor.exitWarning( "The index has not been changed.");
        }
        BaxEntry entry = actor.getShop().removeEntryAt(oldIndex);
        if (actor.getShop().size() < newIndex) {
            actor.getShop().add(entry);
        }
        else {
            actor.getShop().addEntry(newIndex - 1, entry);
        }
        actor.sendMessage("The index for this item was successfully changed.");
    }
}
