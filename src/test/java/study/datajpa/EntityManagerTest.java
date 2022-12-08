package study.datajpa;

import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 호기심 해결 테스트
 */
//@DataJpaTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest
@Transactional
public class EntityManagerTest {

    private static final Logger log = LoggerFactory.getLogger(EntityManagerTest.class);

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("연관관계 엔티티를 영속 했을 때")
    public void mappingEntityPersistTest() throws Exception {
        // given
        Member member1 = new Member("member1", 10);
        Member member2 = new Member("member2", 20);
        Team teamA = new Team("teamA");
        member1.changeTeam(teamA);
        member2.changeTeam(teamA);

        // when
        em.persist(member1);
        em.persist(member2);
        em.persist(teamA);

        em.flush();
        em.clear();

        TypedQuery<Member> query = em.createQuery("select m from Member m where m.username = :username", Member.class);
        Member findMember1 = query.setParameter("username", "member1").getSingleResult();
        Member findMember2 = query.setParameter("username", "member2").getSingleResult();

        // then
        assertThat(findMember1).isNotNull();
        assertThat(findMember1.getTeam()).isNotNull();
        assertThat(findMember2).isNotNull();
        assertThat(findMember2.getTeam()).isNotNull();
    }

    @Test
    @DisplayName("연관관계 엔티티를 영속 안했을 때")
    public void mappingEntityTransientTest() throws Exception {
        // given
        Member member1 = new Member("member1", 10);
        Member member2 = new Member("member2", 20);
        Team teamA = new Team("teamA");
        member1.changeTeam(teamA);
        member2.changeTeam(teamA);

        // when
//        em.persist(member1);
//        em.persist(member2);
        em.persist(teamA);
        em.flush();
        em.clear();

        TypedQuery<Member> query = em.createQuery("select m from Member m where m.username = :username", Member.class);
        ThrowableAssert.ThrowingCallable actual = () -> query.setParameter("username", "member1").getSingleResult();

        // then
        assertThatThrownBy(actual).isInstanceOf(NoResultException.class);
    }

    @Test
    @Commit
    @DisplayName("영속성 컨텍스트 활성화 상태에서 DB 엔티티를 직접 삭제 - 배타적 락 설정")
    public void persistenceContextRemoveTest() throws Exception {
        Member findMember = em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", "member0")
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getSingleResult();

        findMember.setUsername("update member");

        // 이 시점에서 삭제
        log.info("delete entity from other session");
    }

}
