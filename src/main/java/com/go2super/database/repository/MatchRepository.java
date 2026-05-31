package com.go2super.database.repository;

import com.go2super.database.entity.Match;
import com.go2super.database.entity.type.MatchType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.io.Serializable;
import java.util.List;

public interface MatchRepository extends MongoRepository<Match, String>, Serializable {

    Match findTopByOrderByIdDesc();

    List<Match> findAll();

    Match findByMatchTypeAndPlayersInvolvedContainsAndEnded(MatchType matchType, int guid, boolean ended);

    List<Match> findAllByMatchType(MatchType matchType);

    List<Match> findAllByEnded(boolean ended);

    Match findByMatchId(int matchId);

    Match findByGalaxyId(int galaxyId);

    long count();

}