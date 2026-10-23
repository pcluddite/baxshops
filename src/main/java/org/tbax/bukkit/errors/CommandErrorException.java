/*
 * Copyright (C) 2013-2026 Timothy Baxendale
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
package org.tbax.bukkit.errors;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.Format;
import org.tbax.baxshops.FormatText;

@SuppressWarnings("unused")
public final class CommandErrorException extends PrematureAbortException
{
    public CommandErrorException(@NotNull String message)
    {
        super(message);
    }

    public CommandErrorException(@NotNull String message, Throwable cause)
    {
        super(message, cause);
    }

    @Override
    public @NotNull String getFormattedMessage()
    {
        return Format.error(super.getMessage());
    }

    @Override
    public @NotNull Component getComponent()
    {
        return FormatText.error(super.getMessage());
    }
}
