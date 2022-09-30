package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback(false)
public class TeamRepositoryTest {

    @PersistenceContext
    EntityManager em;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    MemberRepository memberRepository;

    @Test
    public void findAll() {
        for(int i=0; i<5; i++) {
            Team team = new Team("team"+i);
            teamRepository.save(team);

            Member memberA = new Member("memberA"+i, i*10, team);
            memberRepository.save(memberA);
            Member memberB = new Member("memberB"+i, i*10, team);
            memberRepository.save(memberB);
        }

        em.flush();
        em.clear();

        List<Team> list = teamRepository.findAll();
        list.forEach(t -> {
            System.out.println("team=" + t);
            System.out.println("========== LAZY LOADING ========");
            for(int i=0; i<t.getMembers().size(); i++) {
                System.out.println("-> team.members =" + t.getMembers().get(i).getUsername());
            }
        });

        assertThat(list.size()).isEqualTo(5);
    }
}
