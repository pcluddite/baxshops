/*
 * Copyright (C) Timothy Baxendale
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.tbax.baxshops.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.BaxEntry;
import org.tbax.baxshops.FormatText;
import org.tbax.baxshops.Permissions;
import org.tbax.baxshops.items.EnchantMap;
import org.tbax.bukkit.CommandHelp;
import org.tbax.bukkit.CommandHelpArgument;
import org.tbax.bukkit.commands.CmdActor;
import org.tbax.bukkit.commands.CommandArgument;
import org.tbax.bukkit.errors.PrematureAbortException;

import java.util.List;
import java.util.Map;

public final class CmdInfo extends ShopCommand
{
    @Override
    public @org.jetbrains.annotations.Nullable String getAction()
    {
        return "info";
    }

    @Override
    public String getPermission()
    {
        return Permissions.SHOP_TRADER_BUY;
    }

    @Override
    public @NotNull CommandHelp getHelp(@NotNull CmdActor actor)
    {
        CommandHelp help = new CommandHelp(this, "show more information about an entry");
        help.setLongDescription("Show extended information about an entry in the selected shop. It is recommended to use this before all major purchases. " +
                ChatColor.ITALIC + "Caveat emptor" + ChatColor.RESET);
        help.setArgs(
                new CommandHelpArgument("item", "the name or shop index of the entry", true)
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull CmdActor actor)
    {
        return actor.getNumArgs() == 2;
    }

    @Override
    public boolean requiresSelection(@NotNull ShopCmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresOwner(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public boolean requiresPlayer(@NotNull CmdActor actor)
    {
        return false;
    }

    @Override
    public boolean requiresItemInHand(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public void onShopCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        BaxEntry entry = actor.getArg(1).asEntry();
        int index = actor.getShop().indexOf(entry) + 1;
        Component info = Component.text("Name: ", NamedTextColor.WHITE)
                .append(Component.text(entry.getName()).hoverEvent(entry.getItemStack().asHoverEvent()));
        info = info.appendNewline()
                .append(Component.text("Material: " + entry.getType().toString()));
        if (entry.getType().getMaxDurability() > 0) {
            info = info.appendNewline()
                    .append(Component.text("Durability: "))
                    .append(Component.text(entry.getDamagePercent() + "%", NamedTextColor.YELLOW));
        }
        if (entry.getItemStack().hasItemMeta()) {
            ItemMeta meta = entry.getItemStack().getItemMeta();
            if (meta.hasDisplayName()) {
                info = info.appendNewline()
                        .append(Component.text("Display Name: "))
                        .append(meta.displayName().color(NamedTextColor.YELLOW));
            }
            if (meta.hasLore()) {
                info = info.appendNewline()
                        .append(Component.text("Description: "));
                for (Component line : meta.lore()) {
                    info = info.appendNewline().append(line.color(NamedTextColor.BLUE));
                }
            }
        }
        Map<Enchantment, Integer> enchmap = EnchantMap.getEnchants(entry.getItemStack());
        if (enchmap != null && !enchmap.isEmpty()) {
            info = info.appendNewline()
                    .append(Component.text("Enchants: "))
                    .append(FormatText.enchantments(EnchantMap.fullListString(enchmap)));
        }
        info = info.appendNewline()
                .append(Component.text("Quantity: "));
        if (entry.getAmount() < 1) {
            info = info.append(Component.text("OUT OF STOCK", NamedTextColor.DARK_RED));
        } else {
            info = info.append(FormatText.number(entry.getAmount()));
        }
        info = info.appendNewline();
        if (entry.canBuy()) {
            info = info.append(Component.text("[BUY]", NamedTextColor.GREEN, TextDecoration.UNDERLINED)
                    .hoverEvent(HoverEvent.showText(Component.text("Buy for " + entry.getFormattedBuyPrice())))
                    .clickEvent(ClickEvent.suggestCommand("/buy " + index + " "))
            );
            info = info.appendSpace();
        }
        if (entry.canSell()) {
            info = info.append(Component.text("[SELL]", NamedTextColor.BLUE, TextDecoration.UNDERLINED)
                    .hoverEvent(HoverEvent.showText(Component.text("Sell for " + entry.getFormattedSellPrice())))
                    .clickEvent(ClickEvent.suggestCommand("/shop sellfrominventory " + index + " "))
            );
        }
        info = FormatText.header("Entry Information")
                .appendNewline()
                .append(info);
        if (actor.getPlayer() == null) {
            actor.getSender().sendMessage(info);
        }
        else {
            actor.getPlayer().sendMessage(info);
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CmdActor actor, @NotNull Command command,
                                      @NotNull String alias, List<? extends CommandArgument> args)
    {
        ShopCmdActor shopActor = (ShopCmdActor)actor;
        if (args.size() == 2 && shopActor.getShop() != null) {
            return shopActor.getShop().getAllItemAliases();
        }
        else {
            return super.onTabComplete(actor, command, alias, args);
        }
    }
}
