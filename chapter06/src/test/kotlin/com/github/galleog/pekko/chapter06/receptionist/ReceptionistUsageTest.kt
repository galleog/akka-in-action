package com.github.galleog.pekko.chapter06.receptionist

import io.kotest.assertions.throwables.shouldThrow
import org.apache.pekko.actor.testkit.typed.annotations.JUnit5TestKit
import org.apache.pekko.actor.testkit.typed.javadsl.*
import org.apache.pekko.actor.typed.ActorRef
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test

@ExtendWith(TestKitJUnit5Extension::class, LogCapturingExtension::class)
class ReceptionistUsageTest {
    @JUnit5TestKit
    private val testKit = ActorTestKit.create()

    @Test
    fun `a concierge should get notified about all the guests each time a guest registers`() {
        val guest = testKit.spawn(VipGuest.create(), "Mr.Wick")
        testKit.spawn(HotelConcierge.create())

        LoggingTestKit.info("Mr.Wick is in")
            .expect(testKit.system()) { guest.tell(VipGuest.EnterHotel) }

        val guest2 = testKit.spawn(VipGuest.create(), "Mr.Ious")
        LoggingTestKit.info("Mr.Ious is in")
            .expect(testKit.system()) {
                LoggingTestKit.info("Mr.Wick is in")
                    .expect(testKit.system()) { guest2.tell(VipGuest.EnterHotel) }
            }

        testKit.stop(guest)
        testKit.stop(guest2)
    }

    @Test
    fun `should find that an actor is registered, with basic Find usage`() {
        val probe = testKit.createTestProbe<ActorRef<VipGuest.Command>>()
        val guest = testKit.spawn(VipGuest.create(), "Mr.Wick")
        val finder = testKit.spawn(GuestSearch.create("Mr.Wick", probe.ref))

        guest.tell(VipGuest.EnterHotel)
        finder.tell(GuestSearch.Find)
        probe.expectMessageType<ActorRef<VipGuest.Command>>()

        testKit.stop(guest)
    }

    @Test
    fun `should find that no actor is registered, with basic Find usage`() {
        val probe = testKit.createTestProbe<ActorRef<VipGuest.Command>>()
        val finder = testKit.spawn(GuestSearch.create("NoOne", probe.ref))

        finder.tell(GuestSearch.Find)
        probe.expectNoMessage()
    }

    @Test
    fun `should find that an actor is registered, with search params in Find`() {
        val probe = testKit.createTestProbe<ActorRef<VipGuest.Command>>()
        val guest = testKit.spawn(VipGuest.create(), "Mr.Wick")
        val finder = testKit.spawn(GuestFinder.create())

        guest.tell(VipGuest.EnterHotel)
        finder.tell(GuestFinder.Find("Mr.Wick", probe.ref))
        probe.expectMessageType<ActorRef<VipGuest.Command>>()

        testKit.stop(guest)
    }

    @Test
    fun `should get notified only alive actors`() {
        shouldThrow<AssertionError> {
            val guest = testKit.spawn(VipGuest.create(), "Mr.X")
            testKit.stop(guest)

            LoggingTestKit.info("Mr.X is in")
                .expect(testKit.system()) { guest.tell(VipGuest.EnterHotel) }
        }
    }
}

private inline fun <reified T> TestProbe<T>.expectMessageType() = expectMessageClass(T::class.java)
