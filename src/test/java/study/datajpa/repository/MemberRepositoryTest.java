package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
@Rollback(false)
class MemberRepositoryTest {

    @Autowired
    MemberRepository repository;

    @Autowired
    TeamRepository teamRepository;

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

    @Test
    void findByUsernameAndAgeGraterThan() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        repository.save(m1);
        repository.save(m2);

        List<Member> result = repository.findByUsernameAndAgeGreaterThan("AAA", 15);

        assertThat(result).extracting("username").containsExactly("AAA");
        assertThat(result).extracting("age").containsExactly(20);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void testNamedQuery() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        repository.save(m1);
        repository.save(m2);

        List<Member> result = repository.findByUsername("AAA");

        assertThat(result).extracting("username").containsExactly("AAA");
        assertThat(result).extracting("age").containsExactly(10);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void testQuery() {

        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        repository.save(m1);
        repository.save(m2);

        List<Member> result = repository.findUser("AAA", 10);

        assertThat(result).extracting("username").containsExactly("AAA");

    }

    @Test
    public void findUsernameList() {

        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        repository.save(m1);
        repository.save(m2);

        List<String> result = repository.findUsernameList();
        result.forEach(System.out::println);

        assertThat(result).containsExactly("AAA", "BBB");

    }

    @Test
    public void findMemberDto() {

        Team team = new Team("teamA");
        teamRepository.save(team);

        Member m1 = new Member("AAA", 10);
        m1.changeTeam(team);
        Member m2 = new Member("BBB", 20);
        m2.changeTeam(team);
        repository.save(m1);
        repository.save(m2);

        List<MemberDto> result = repository.findMemberDto();
        result.forEach(System.out::println);

    }


    @Test
    public void findByNames() {

        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        repository.save(m1);
        repository.save(m2);

        List<Member> result = repository.findByNames(List.of("AAA", "BBB"));
        result.forEach(System.out::println);

        assertThat(result).extracting("username").containsExactly("AAA", "BBB");

    }

    @Test
    public void returnType() {

        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        repository.save(m1);
        repository.save(m2);

        List<Member> list = repository.findListByUsername("AAA");
        Member member = repository.findMemberByUsername("AAA");
        Optional<Member> optional = repository.findOptionalByUsername("AAA");

        assertThat(list).extracting("username").containsExactly("AAA");
        assertThat(member.getUsername()).isEqualTo("AAA");
        assertThat(optional.get()).isNotNull();

    }

    @Test
    public void paging() {
        repository.save(new Member("member1", 10));
        repository.save(new Member("member2", 10));
        repository.save(new Member("member3", 10));
        repository.save(new Member("member4", 10));
        repository.save(new Member("member5", 10));

        int age = 10;

        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        Slice<Member> page = repository.findByAge(age, pageRequest);

        page.map(member -> new MemberDto(member.getId(), member.getUsername(), member.getTeam().getName()));

        assertThat(page.getContent().size()).isEqualTo(3);
//        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getNumber()).isEqualTo(0);
//        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();
    }
}