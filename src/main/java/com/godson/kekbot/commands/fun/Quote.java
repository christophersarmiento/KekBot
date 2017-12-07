package com.godson.kekbot.commands.fun;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.darichey.discord.api.CommandRegistry;
import com.godson.kekbot.GSONUtils;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Responses.Action;
import com.godson.kekbot.Settings.Quotes;
import com.jagrosh.jdautilities.menu.pagination.PaginatorBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.utils.PermissionUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Quote {
    public static Command quote = new Command("quote")
            .withAliases("q")
            .withCategory(CommandCategory.FUN)
            .withDescription("Grabs a random command from a list of quotes made in a server.")
            .withUsage("{p}quote {add/remove/list} (Note: Adding and removing quotes requires the \"Manage Messages\" permission.)")
            .botRequiredPermissions(Permission.MESSAGE_WRITE)
            .onExecuted(context -> {
                TextChannel channel = context.getTextChannel();
                Guild guild = context.getGuild();
                String rawSplit[] = context.getMessage().getContent().split(" ", 3);
                Quotes quotes = GSONUtils.getQuotes(guild);
                String prefix = CommandRegistry.getForClient(context.getJDA()).getPrefixForGuild(context.getGuild()) == null
                        ? CommandRegistry.getForClient(context.getJDA()).getPrefix()
                        : CommandRegistry.getForClient(context.getJDA()).getPrefixForGuild(context.getGuild());
                if (rawSplit.length == 1) {
                    List<String> quotesList = quotes.getQuotes();
                    if (quotesList.isEmpty()) {
                        channel.sendMessage("You have no quotes!").queue();
                    } else {
                        channel.sendMessage(quotes.getQuote()).queue();
                    }
                } else {
                    switch (rawSplit[1]) {
                        case "add":
                            if (context.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
                                if (rawSplit.length == 3) {
                                    quotes.addQuote(rawSplit[2]);
                                    quotes.save(guild);
                                    channel.sendMessage("Successfully added quote! :thumbsup:").queue();
                                } else {
                                    channel.sendMessage("```md\n[Subcommand](quote add)" +
                                            "\n\n[Description](Adds a quote.)" +
                                            "\n\n# Paramaters (<> Required, {} Optional)" +
                                            "\n[Usage](" + prefix + "quote add <quote>)```").queue();
                                }
                            } else {
                                channel.sendMessage(KekBot.respond(context, Action.NOPERM_USER, "`Manage Messages`")).queue();
                            }
                            break;
                        case "remove":
                            if (context.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
                                if (rawSplit.length == 3) {
                                    try {
                                        int quoteNumber = Integer.valueOf(rawSplit[2]);
                                        if (quotes.getQuotes().size() >= quoteNumber) {
                                            String quote = quotes.getQuote(quoteNumber-1);
                                            quotes.removeQuote(quoteNumber-1);
                                            quotes.save(guild);
                                            channel.sendMessage("Successfully removed quote: **" + quote + "**.").queue();
                                        }
                                    } catch (NumberFormatException e) {
                                        channel.sendMessage("\"" + rawSplit[2] + "\" is not a number!").queue();
                                    }
                                } else {
                                    channel.sendMessage("```md\n[Subcommand](quote remove)" +
                                            "\n\n[Description](Removes a specific quote. Use " + prefix + "quote list to getResponder the quote's number.)" +
                                            "\n\n# Paramaters (<> Required, {} Optional)" +
                                            "\n[Usage](" + prefix + "quote remove <quote number>)```").queue();
                                }
                            } else {
                                channel.sendMessage(KekBot.respond(context, Action.NOPERM_USER, "`Manage Messages`")).queue();
                            }
                            break;
                        case "list":
                            int size = quotes.getQuotes().size();

                            if (size != 0) {
                                PaginatorBuilder builder = new PaginatorBuilder();
                                for (int i = 0; i < size; i++) {
                                    String quote = quotes.getQuotes().get(i);
                                    builder.addItems(quote.length() > 100 ? quote.substring(0, 100) + "..." : quote);
                                }

                                builder.setText("Here are your quotes:")
                                        .setEventWaiter(KekBot.waiter)
                                        .setColor(context.getGuild().getSelfMember().getColor())
                                        .setItemsPerPage(10)
                                        .waitOnSinglePage(true)
                                        .showPageNumbers(true)
                                        .useNumberedItems(true)
                                        .setTimeout(5, TimeUnit.MINUTES)
                                        .setUsers(context.getAuthor());

                                builder.build().display(context.getTextChannel());
                            } else {
                                channel.sendMessage("There are no quotes to list!").queue();
                            }
                            break;
                    }
                }
            });
}
