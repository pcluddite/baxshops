/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands;

import org.jetbrains.annotations.NotNull;
import tbax.baxshops.CommandHelpArgument;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.CommandHelp;
import tbax.baxshops.notification.LollipopNotification;
import tbax.baxshops.serialization.StoredData;

public final class CmdLollipop extends BaxShopCommand
{
    @Override
    public @NotNull String getName()
    {
        return "lollipop";
    }

    @Override
    public @NotNull String[] getAliases()
    {
        return new String[]{"lollipop","lol","lolly"};
    }

    @Override
    public String getPermission()
    {
        return null;
    }

    @Override
    public CommandHelp getHelp(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        CommandHelp help = super.getHelp(actor);
        help.setDescription("Hand out lollipop");
        help.setArgs(
            new CommandHelpArgument("player", "player to send lollipop", true),
            new CommandHelpArgument("tastiness", "the tastiness (0-100)", true)
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull ShopCmdActor actor)
    {
        return actor.getNumArgs() == 3 || actor.getNumArgs() == 2;
    }

    @Override
    public boolean requiresSelection(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public boolean requiresOwner(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public boolean requiresPlayer(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public boolean requiresItemInHand(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public void onCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        double tastiness = LollipopNotification.DEFAULT_TASTINESS;
        if (actor.getNumArgs() == 3) {
            tastiness = actor.getArgDouble(2, "Invalid tastiness");
        }
        StoredData.sendNotification(actor.getArgPlayer(1), new LollipopNotification(actor.getPlayer(), tastiness));
    }
}
