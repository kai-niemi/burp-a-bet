package io.burpabet.betting.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.burpabet.betting.model.Race;
import io.burpabet.common.domain.Outcome;

@Repository
public interface RaceRepository extends JpaRepository<Race, UUID> {
    @Override
    @Query(value = "select r from Race r "
            + "left join fetch r.bets b "
            + "where r.id=?1")
    Optional<Race> findById(UUID id);

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

    @Query(value = "update Race r set r.outcome=:outcome where r.id=:id")
    @Modifying
    int updateRaceOutcome(@Param("id") UUID id, @Param("outcome") Outcome outcome);
}

