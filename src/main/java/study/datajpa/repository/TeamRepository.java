package study.datajpa.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import study.datajpa.entity.Team;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

//    @Override
//    @EntityGraph(attributePaths = "members")
//    @Query("select t from Team t join t.members")
//    List<Team> findAll();

    @Query("select t from Team t join fetch t.members")
    @EntityGraph(attributePaths = "members")
    List<Team> findFetchJoinAll();
}
