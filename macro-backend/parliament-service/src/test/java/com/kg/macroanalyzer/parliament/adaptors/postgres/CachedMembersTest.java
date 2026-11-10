package com.kg.macroanalyzer.parliament.adaptors.postgres;

import com.kg.macroanalyzer.parliament.domain.MemberOfParliament;
import com.kg.macroanalyzer.parliament.domain.TestMemberFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CachedMembersTest {

    private static final MemberOfParliament OLD = TestMemberFactory.member("1", "S", "Tidigare riksdagsledamot", List.of());
    private static final MemberOfParliament NEW = TestMemberFactory.member("1", "M", "Tidigare riksdagsledamot", List.of());

    @Mock
    SweParMemberRepository repository;

    CachedMembers cachedMembers;

    @BeforeEach
    void setUp() {
        cachedMembers = new CachedMembers(repository);
    }

    @Test
    void shouldReadTheDatabaseOnce_forRepeatedReads() {
        // given
        when(repository.readMembers()).thenReturn(List.of(OLD));

        // when
        final var first = cachedMembers.read();
        final var second = cachedMembers.read();

        // then
        assertSame(first, second);
        verify(repository, times(1)).readMembers();
    }

    @Test
    void shouldCacheAnEmptyList() {
        // given
        when(repository.readMembers()).thenReturn(List.of());

        // when
        cachedMembers.read();
        final var result = cachedMembers.read();

        // then
        assertTrue(result.isEmpty());
        verify(repository, times(1)).readMembers();
    }

    @Test
    void shouldReadTheNewMembers_afterAWrite() {
        // given
        when(repository.readMembers()).thenReturn(List.of(OLD)).thenReturn(List.of(NEW));
        cachedMembers.read();

        // when
        cachedMembers.write(List.of(NEW));
        final var result = cachedMembers.read();

        // then
        assertEquals(List.of(NEW), result);
    }

    @Test
    void shouldReadTheDatabaseAgain_afterAFailedWrite() {
        // given
        when(repository.readMembers()).thenReturn(List.of(OLD));
        when(repository.upsertMembers(List.of(NEW))).thenThrow(new IllegalStateException("database gone"));
        cachedMembers.read();

        // when
        assertThrows(IllegalStateException.class, () -> cachedMembers.write(List.of(NEW)));
        cachedMembers.read();

        // then
        verify(repository, times(2)).readMembers();
    }

    @Test
    void shouldNotLetCallersChangeTheCachedList() {
        // given
        when(repository.readMembers()).thenReturn(new ArrayList<>(List.of(OLD)));

        // when
        final var result = cachedMembers.read();

        // then
        assertThrows(UnsupportedOperationException.class, result::clear);
    }

    @Test
    void shouldMakeALoadWait_whileAWriteIsRunning() throws Exception {
        // given
        final var writeStarted = new CountDownLatch(1);
        final var finishWrite = new CountDownLatch(1);
        when(repository.upsertMembers(List.of(NEW))).thenAnswer(invocation -> {
            writeStarted.countDown();
            finishWrite.await(5, TimeUnit.SECONDS);
            return 1;
        });
        when(repository.readMembers()).thenReturn(List.of(NEW));

        try (final var executor = Executors.newFixedThreadPool(2)) {
            // when
            final var write = executor.submit(() -> cachedMembers.write(List.of(NEW)));
            assertTrue(writeStarted.await(5, TimeUnit.SECONDS));
            final var read = executor.submit(cachedMembers::read);
            Thread.sleep(200);

            // then
            assertFalse(read.isDone());
            finishWrite.countDown();
            write.get(5, TimeUnit.SECONDS);
            assertEquals(List.of(NEW), read.get(5, TimeUnit.SECONDS));
        }
    }

}
