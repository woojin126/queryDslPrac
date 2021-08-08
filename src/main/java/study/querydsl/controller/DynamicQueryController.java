package study.querydsl.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.repository.MemberJpaRepository;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DynamicQueryController {

    private final MemberJpaRepository memberJpaRepository;


    @GetMapping("api/v1/search")
    public ResponseEntity<List<MemberTeamDto>> searchType(@RequestBody MemberSearchCondition searchCondition){
       return new ResponseEntity<>(memberJpaRepository.searchTeamMemberWhereType(searchCondition),HttpStatus.OK);
    }

}
