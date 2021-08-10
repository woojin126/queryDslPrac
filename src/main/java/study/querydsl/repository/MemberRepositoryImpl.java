package study.querydsl.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.support.PageableExecutionUtils;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

//클래명이 중요 MemberRepository + Impl 규칙임
public class MemberRepositoryImpl implements MemberRepositoryCustom{

    private final JPAQueryFactory queryFactory;
    public MemberRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    public List<MemberTeamDto> search(MemberSearchCondition condition){
        return queryFactory
                .select(new QMemberTeamDto(
                        member.id,
                        member.username,
                        member.age,
                        team.id,
                        team.name
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()))
                //ageBetween(condition))
                .fetch();
    }

    /**
     메서드 위치변경 컨트롤 +쉬프트 위아래 방향키
     타입을 BooleanExpression 으로하면 조합이가능
     재사용이 가능하다는것이 미친듯한 장점
     */

    private BooleanExpression ageBetween(MemberSearchCondition condition){ //이런식으로 조립도가능
        return ageGoe(condition.getAgeGoe()).and(ageLoe(condition.getAgeLoe()));
    }
    private BooleanExpression usernameEq(String username) {
        //return hasText(username) ? member.username.eq(username) : null;
        return Optional.ofNullable(username).map(member.username::eq).orElse(null);
    }

    private BooleanExpression teamNameEq(String teamName) {
        //return hasText(teamName) ? team.name.eq(teamName) : null;
        return Optional.ofNullable(teamName).map(team.name::eq).orElse(null);
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        //return ageGoe != null ? member.age.goe(ageGoe) : null;
        return Optional.ofNullable(ageGoe).map(member.age::goe).orElse(null);
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        //return ageLoe != null ? member.age.loe(ageLoe) : null;
        return Optional.ofNullable(ageLoe).map(member.age::loe).orElse(null);
    }

    @Override
    public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {
        QueryResults<MemberTeamDto> result = queryFactory
                .select(new QMemberTeamDto(
                        member.id,
                        member.username,
                        member.age,
                        team.id,
                        team.name
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                ).offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                //ageBetween(condition))
                .fetchResults();//컨텐츠용쿼리, count용 쿼리 2번날림,데이터가 얼마없으면 그냥쓰꼬 아니면 아래 최적화

        List<MemberTeamDto> content = result.getResults();//실제 데이터
        long total = result.getTotal();//count 숫자

        return new PageImpl<>(content,pageable,total);
    }


        @Override
    public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {
        List<MemberTeamDto> content = queryFactory
                .select(new QMemberTeamDto(
                        member.id,
                        member.username,
                        member.age,
                        team.id,
                        team.name
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                ).offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                //ageBetween(condition))
                .fetch();//컨텐츠용쿼리, count용 쿼리 2번날림
/**
 * 스프링 데이터 라이브러리 제공
 * count 쿼리가 생략 가능한 경우 생략해서 처리 (최적화 를위해)
 * 1.페이지 시작이면서 컨텐츠 사이즈가 페이지 사이즈보다 작을때
 * 2.마지막 페이지 일 때(offset + 컨텐츠 사이즈를 더해서 사이즈를 구함)
 */
        JPAQuery<Member> countQuery = queryFactory
                .select(member)
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                );
        /*long count = queryFactory
                .select(member)
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                ).fetchCount();*/

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount); // 카운트가 필요없음 최적화이걸쓰짜,이구문이 실행될떄 카운트쿼리를날릴지 말지 결정함 알아서
        //return new PageImpl<>(content,pageable,count);
    }

}
