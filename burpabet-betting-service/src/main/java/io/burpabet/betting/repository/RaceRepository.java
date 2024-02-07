package io.burpabet.betting.repository;

import io.burpabet.betting.model.Race;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RaceRepository extends JpaRepository<Race, UUID> {
    @Query(value = "select r.id from Race r "
            + "left join r.bets b "
            + "order by b.stake.amount desc, b.payout.amount desc")
    Page<UUID> findRaceIds(Pageable pageable);

    @Query(value = "select r from Race r "
            + "left join fetch r.bets b "
            + "where r.id in (?1) order by b.stake.amount desc, b.payout.amount desc")
    List<Race> findRaces(List<UUID> ids);

    @Query(value = "select r from Race r")
    Page<Race> findAllByPage(Pageable page);

    @Query(value = "select r.id from Race r "
            + "join r.bets b "
            + "where b.settled = true and b.placementStatus = 'APPROVED'")
    Page<UUID> findRaceIdsWithSettledBets(Pageable pageable);

    @Query(value = "select r from Race r "
            + "join fetch r.bets b "
            + "where r.id in (?1)")
    List<Race> findRacesWithSettledBets(List<UUID> ids);

    @Query(value = "select r.id from Race r "
            + "join r.bets b "
            + "where b.settled = false and b.placementStatus = 'APPROVED'")
    Page<UUID> findRaceIdsWithUnsettledBets(Pageable pageable);

    @Query(value = "select r from Race r "
            + "join fetch r.bets b "
            + "where r.id in (?1)")
    List<Race> findRacesWithUnsettledBets(List<UUID> ids);

    @Query(value = "select r from Race r "
            + "order by random() "
            + "limit 1")
    Optional<Race> getRandomRace();

    @Query(value = "select r from Race r "
            + "where r.id=?1")
    @Lock(LockModeType.PESSIMISTIC_READ) // SFS/SFU to reduce contention
    @QueryHints(value = {
            @QueryHint(name = "hibernate.query.followOnLocking", value = "false")}, forCounting = false)
    Optional<Race> findByIdForShare(UUID id);
}

