package com.landmaster.landsutilities.command;

import com.landmaster.landsutilities.LandsUtilities;
import com.landmaster.landsutilities.util.RemoteControlLink;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;

import java.util.ArrayList;

public class RemoteDeleteLinkCommand implements Command<CommandSourceStack>  {
    public static final RemoteDeleteLinkCommand INSTANCE = new RemoteDeleteLinkCommand();

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("delete_link")
                .executes(INSTANCE);
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        var source = context.getSource();
        var player = source.getPlayer();

        if (player != null) {
            for (var hand : InteractionHand.values()) {
                var stack = player.getItemInHand(hand);
                var links = stack.get(LandsUtilities.LINKED_MENU_BLOCKS);
                if (links != null) {
                    if (links.isEmpty()) {
                        source.sendFailure(Component.translatable("message.landsutilities.empty_remote_control"));
                    } else {
                        var newLinks = new ArrayList<>(links);
                        int index = stack.getOrDefault(LandsUtilities.LINKED_MENU_INDEX, 0);
                        if (0 <= index && index < links.size()) {
                            var deletedLink = newLinks.remove(index);
                            stack.set(LandsUtilities.LINKED_MENU_BLOCKS, newLinks);
                            stack.set(LandsUtilities.LINKED_MENU_INDEX, Math.clamp(index, 0, links.size() - 1));
                            source.sendSuccess(() -> Component.translatable("message.landsutilities.removed_link", deletedLink.name()), true);
                        }
                    }
                    return 0;
                }
            }

            source.sendFailure(Component.translatable("message.landsutilities.not_remote_control"));
        }

        return 0;
    }
}
