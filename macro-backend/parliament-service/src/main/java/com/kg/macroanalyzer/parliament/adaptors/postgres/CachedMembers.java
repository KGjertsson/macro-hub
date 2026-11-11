package com.kg.macroanalyzer.parliament.adaptors.postgres;

import com.kg.macroanalyzer.parliament.domain.MemberOfParliament;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

// Parsing every member's JSON takes about half a second per request, and only a members scrape
// changes them.
// load and write exclude each other, so a load that read the old rows can't store them after a
// write has cleared them.
@Component
public class CachedMembers {

    private final SweParMemberRepository repository;
    private final AtomicReference<List<MemberOfParliament>> members = new AtomicReference<>();

    public CachedMembers(SweParMemberRepository repository) {
        this.repository = repository;
    }

    public List<MemberOfParliament> read() {
        final var cached = members.get();
        return cached != null ? cached : load();
    }

    public synchronized Integer write(List<MemberOfParliament> written) {
        try {
            return repository.upsertMembers(written);
        } finally {
            members.set(null);
        }
    }

    private synchronized List<MemberOfParliament> load() {
        final var cached = members.get();
        if (cached != null) {
            return cached;
        }
        final var loaded = Collections.unmodifiableList(repository.readMembers());
        members.set(loaded);

        return loaded;
    }

}
