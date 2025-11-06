package io.cockroachdb.betting.repository;

import io.cockroachdb.betting.model.Bet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BetRepository extends JpaRepository<Bet, UUID> {
    @Query(value = "select b from Bet b "
            + "join fetch b.race r")
    Page<Bet> findAllBets(Pageable pageable);

    @Query(value = "select b from Bet b "
            + "join fetch b.race r "
            + "where b.settled = false and b.placementStatus = 'APPROVED' "
            + "order by b.insertedAt desc")
    Page<Bet> findUnsettledBets(Pageable pageable);

    @Query(value = "select b from Bet b "
            + "join b.race r "
            + "where b.settled = false and b.placementStatus = 'APPROVED' "
            + "and r.id = ?1")
    List<Bet> findUnsettledBetsWithRaceId(UUID raceId);

    @Query(value = "select b from Bet b "
            + "join fetch b.race r "
            + "where b.settled = true and b.settlementStatus = 'APPROVED' "
            + "order by b.insertedAt desc")
    Page<Bet> findSettledBets(Pageable pageable);

    @Override
    @Query(value = "select b from Bet b "
            + "join fetch b.race r "
            + "where b.id=?1")
    Optional<Bet> findById(UUID id);
}
