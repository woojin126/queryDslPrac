package study.querydsl.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.repository.support.Querydsl4RepositorySupport;

import java.util.List;
import java.util.Optional;

import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

@Repository
public class MemberTestRepository extends Querydsl4RepositorySupport {
   /* public MemberTestRepository() {
        super(Member.class);
    }

    public List<Member> basicSelect(){
        return select(member)
                .from(member)
                .fetch();
    }

    public List<Member> basicSelectFrom(){
        return selectFrom(member)
                .fetch();
    }

    public Page<Member> searchPageByApplyPage(MemberSearchCondition condition, Pageable pageable){
        JPAQuery<Member> query = selectFrom(member)
                .leftJoin(member.team , team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                );

        List<Member> content = getQuerydsl().applyPagination(pageable, query)
                .fetch();

        return PageableExecutionUtils.getPage(content, pageable, query::fetchCount);
    }

    //직접 커스텀한 기능
    public Page<Member> applyPagination(MemberSearchCondition condition, Pageable pageable){

        return applyPagination(pageable, query ->
                query.selectFrom(member)
                        .leftJoin(member.team , team)
                        .where(usernameEq(condition.getUsername()),
                                teamNameEq(condition.getTeamName()),
                                ageGoe(condition.getAgeGoe()),
                                ageLoe(condition.getAgeLoe())
                        )
        );
    }

    *//**
     *  위와같지만 카운트쿼리 분리
     *//*
    public Page<Member> applyPagination2(MemberSearchCondition condition, Pageable pageable){

        //컨텐츠용쿼리
        return applyPagination(pageable, contentQuery ->
                        contentQuery.selectFrom(member)
                                .leftJoin(member.team, team)
                                .where(usernameEq(condition.getUsername()),
                                        teamNameEq(condition.getTeamName()),
                                        ageGoe(condition.getAgeGoe()),
                                        ageLoe(condition.getAgeLoe())
                                )
                //카운트용 쿼리
                , countQuery -> countQuery
                        .select(member.id)
                        .from(member)
                        .leftJoin(member.team, team)
                        .where(usernameEq(condition.getUsername()),
                                teamNameEq(condition.getTeamName()),
                                ageGoe(condition.getAgeGoe()),
                                ageLoe(condition.getAgeLoe())
                        )
        );
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
    }*/

}
