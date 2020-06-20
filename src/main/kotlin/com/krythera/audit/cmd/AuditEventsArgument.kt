package com.krythera.audit.cmd

import com.krythera.audit.events.AuditEvent
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.command.CommandSource
import net.minecraft.command.ISuggestionProvider
import net.minecraft.util.text.TranslationTextComponent
import java.util.concurrent.CompletableFuture

class AuditEventsArgument : ArgumentType<Set<AuditEvent>> {
    override fun parse(reader: StringReader): Set<AuditEvent> {
        if (!reader.canRead()) {
            return setOf(AuditEvent.AUDIT_EVENT_UNSPECIFIED)
        }

        val events = reader.readString()
        val (valid, invalid) = events.split(',')
            .map { AuditEvent.valueOf(it) }
            .partition { AuditEvent.values().contains(it) }

        if (invalid.isNotEmpty()) {
            throw AUDIT_EVENT_INVALID.createWithContext(StringReader(invalid.joinToString(",")))
        }

        return valid.toSet()
    }

    override fun <S : Any?> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        return ISuggestionProvider.suggest(
            AuditEvent.values()
                .filterNot { it == AuditEvent.AUDIT_EVENT_UNSPECIFIED }
                .map { it.toString() },
            builder
        )
    }

    override fun getExamples(): MutableCollection<String> = mutableListOf(
        AuditEvent.BLOCK_BREAK.toString(),
        "'${AuditEvent.BLOCK_BREAK},${AuditEvent.BLOCK_PLACE}'"
    )

    companion object {
        val AUDIT_EVENT_INVALID =
            SimpleCommandExceptionType(TranslationTextComponent("argument.kryaudit.auditevent.invalid"))

        fun auditEvents() = AuditEventsArgument()

        fun getAuditEvents(ctx: CommandContext<CommandSource>, name: String): Set<AuditEvent> {
            return ctx.getArgument(name, Set::class.java) as Set<AuditEvent>
        }
    }
}