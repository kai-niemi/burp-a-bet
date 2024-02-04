package io.burpabet.betting.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.burpabet.betting.model.Race;

@Repository
public interface RaceRepository extends JpaRepository<Race, UUID> {
//    @Query(value = "select b.race.id "
//            + "from Bet b "
//            + "group by b.race.id "
//            + "order by "
//            + "sum(b.stake.amount) desc, "
//            + "sum(b.payout.amount) desc")
    @Query(value = "select race_id from bet "
            + "group by race_id "
            + "order by sum(stake) desc, sum(payout) desc",
            nativeQuery = true)
    List<UUID> findTopRaceIdsWithBets();

    @Query(value = "select r.id from Race r")
    Page<UUID> findRaceIds(Pageable pageable);

    @Query(value = "select r from Race r "
            + "left join fetch r.bets b "
            + "where r.id in (?1)")
    List<Race> findRaces(List<UUID> ids);

    @Query(value = "select r from Race r "
            + "where r.track like ?1 "
            + "order by random()")
    Page<Race> findAllTracksStartingWith(String prefix, Pageable page);

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

    @Override
    @Query(value = "select r from Race r "
            + "left join fetch r.bets b "
            + "where r.id=?1")
    Optional<Race> findById(UUID id);
}

