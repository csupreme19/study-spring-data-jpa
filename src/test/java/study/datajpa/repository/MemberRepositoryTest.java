package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.entity.Member;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
@Rollback(false)
class MemberRepositoryTest {

    @Autowired
    MemberRepository repository;

    @Test
    void testMember() {
        System.out.println(repository.getClass());
        System.out.println(AopUtils.getTargetClass(repository));

        Member member = new Member("member1");
        Member saveMember = repository.save(member);
        Member findMember = repository.findById(saveMember.getId()).get();

        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());

        // 같은 트랜잭션 내 영속성 컨텍스트 동일성 보장
        assertThat(findMember).isEqualTo(member);
    }

    @Test
    void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        repository.save(member1);
        repository.save(member2);

        // 단건 조회 검증
        Member findMember1 = repository.findById(member1.getId()).get();
        Member findMember2 = repository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        // 변경 감지를 통해 변경
        // 커밋 시점에 update 쿼리 실행
        findMember1.changeUsername("newMember1");

        // 리스트 조회 검증
        List<Member> all = repository.findAll();
        assertThat(all.size()).isEqualTo(2);

        // 카운트 검증
        Long count = repository.count();
        assertThat(count).isEqualTo(2);

        // 삭제 검증
        repository.delete(member1);
        repository.delete(member2);
        Long deletedCount = repository.count();
        assertThat(deletedCount).isEqualTo(0);
    }

}