package com.landmaster.landsutilities.command;

import com.landmaster.landsutilities.LandsUtilities;
import com.landmaster.landsutilities.util.RemoteControlLink;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;

import java.util.ArrayList;

public class RemoteRenameLinkCommand implements Command<CommandSourceStack> {
    public static final RemoteRenameLinkCommand INSTANCE = new RemoteRenameLinkCommand();

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("rename_link")
                .then(
                        Commands.argument("name", StringArgumentType.string())
                                .executes(INSTANCE)
                );
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        var source = context.getSource();
        var player = source.getPlayer();
        var name = StringArgumentType.getString(context, "name");
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
                            var link = links.get(index);
                            newLinks.set(index, new RemoteControlLink(Component.literal(name), link.pos(), link.dimension(), link.face()));
                        }
                        stack.set(LandsUtilities.LINKED_MENU_BLOCKS, newLinks);
                        source.sendSuccess(() -> Component.translatable("message.landsutilities.renamed_link", name), true);
                    }
                    return 0;
                }
            }

            source.sendFailure(Component.translatable("message.landsutilities.not_remote_control"));
        }
        return 0;
    }
}
