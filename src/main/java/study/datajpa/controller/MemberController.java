package study.datajpa.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.repository.MemberRepository;

import javax.annotation.PostConstruct;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;

    @PostConstruct
    public void init() {
        for(int i=0; i<10; i++) {
            Member member = new Member("member" + i, 10+i);
            memberRepository.save(member);
        }
    }

    @GetMapping("/members/{id}")
    public String findMember(@PathVariable("id") Long id) {
        Member member = memberRepository.findById(id).get();
        return member.getUsername();
    }

    @GetMapping("/members2/{id}")
    public String findMember(@PathVariable("id") Member member) {
        return member.getUsername();
    }

    @GetMapping("/members")
    public Page<MemberDto> findMembers(@PageableDefault(size = 5, page = 1,
            sort = "username", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<Member> members = memberRepository.findAll(pageable);
        return members.map(member ->
                new MemberDto(member.getId(), member.getUsername(), member.getUsername()));
    }

    @GetMapping("/members-teams")
    public Page<MemberDto> findMembers(@Qualifier("member") Pageable memberPageable
            , @Qualifier("team") Pageable teamPageable) {

        Page<Member> members = memberRepository.findAll(memberPageable);
        return members.map(member ->
                new MemberDto(member.getId(), member.getUsername(), member.getUsername()));
    }
}
