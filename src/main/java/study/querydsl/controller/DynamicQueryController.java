package study.querydsl.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.repository.MemberJpaRepository;
import study.querydsl.repository.MemberRepository;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DynamicQueryController {

    private final MemberJpaRepository memberJpaRepository;
    private final MemberRepository memberRepository;

    @GetMapping("api/v1/members")
    public ResponseEntity<List<MemberTeamDto>> searchType(@RequestBody MemberSearchCondition searchCondition){
       return new ResponseEntity<>(memberJpaRepository.search(searchCondition),HttpStatus.OK);
    }

    @GetMapping("api/v2/members")
    public ResponseEntity<Page<MemberTeamDto>> searchPageSimple(@RequestBody MemberSearchCondition condition, Pageable pageable){
        return new ResponseEntity<>(memberRepository.searchPageSimple(condition,pageable),HttpStatus.OK);
    }

    @GetMapping("api/v3/members")
    public ResponseEntity<Page<MemberTeamDto>> searchPageComplex(@RequestBody MemberSearchCondition condition, Pageable pageable){
        return new ResponseEntity<>(memberRepository.searchPageComplex(condition,pageable),HttpStatus.OK);
    }
}
