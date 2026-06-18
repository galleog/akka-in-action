package com.github.galleog.pekko.chapter10b

import org.apache.pekko.NotUsed
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.javadsl.Behaviors
import org.apache.pekko.persistence.jdbc.query.javadsl.JdbcReadJournal
import org.apache.pekko.persistence.query.PersistenceQuery
import org.apache.pekko.stream.javadsl.Source

fun main() {
    val system: ActorSystem<Void> = ActorSystem.create(Behaviors.ignore(), "persistence-query")

    val readJournal: JdbcReadJournal = PersistenceQuery.get(system).getReadJournalFor(
        JdbcReadJournal::class.java,
        JdbcReadJournal.Identifier()
    )
    val source: Source<String, NotUsed> = readJournal.persistenceIds()
    source.runForeach(::println, system)
}