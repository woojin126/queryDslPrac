package study.querydsl.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;

import javax.persistence.EntityManager;
import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.springframework.util.StringUtils.*;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

@Repository
@Transactional
public class MemberJpaRepository {

    private final EntityManager em; //빈으로쓰던 그냥쓰던 동시성문제는 없다!
    private final JPAQueryFactory queryFactory;


     public MemberJpaRepository(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

  /*  public MemberJpaRepository(EntityManager em, JPAQueryFactory jpaQueryFactory) {
        this.em = em;
        this.queryFactory = jpaQueryFactory;
    }
*/
    public void save(Member member){
        em.persist(member);
    }

    public Optional<Member> findById(Long id){
        Member findMember = em.find(Member.class, id);
        return Optional.ofNullable(findMember);
    }

    public List<Member> findAll(){
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }
    public List<Member> findAll_Querydsl(){
        return queryFactory
                .selectFrom(member)
                .fetch();
    }

    public List<Member> findByUsername(String username){
        return em.createQuery("select m from Member m where m.username = :username ", Member.class)
                .setParameter("username", username)
                .getResultList();
    }

    public List<Member> findByUsername_Querydsl(String username){
        return queryFactory
                .selectFrom(member)
                .where(member.username.eq(username))
                .fetch();
    }

    //동적 빌더로 검색조건 쿼리 짜보기
    public List<MemberTeamDto> searchByBuilder(MemberSearchCondition condition){

        BooleanBuilder builder = new BooleanBuilder();

        if(hasText(condition.getUsername())){ // "" , null 둘다 체크가능 StringUtils 사용하면
            builder.and(member.username.eq(condition.getUsername()));
        }

        if(hasText(condition.getTeamName())){
            builder.and(team.name.eq(condition.getTeamName()));
        }

        if(condition.getAgeGoe() != null){
            builder.and(member.age.goe(condition.getAgeGoe()));
        }
        if(condition.getAgeLoe() != null){
            builder.and(member.age.loe(condition.getAgeLoe()));
        }

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
                .where(builder)
                .fetch();
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
                .fetch();
    }
//메서드 위치변경 컨트롤 +쉬프트 위아래 방향키
    // 타입을 BooleanExpression 으로하면 조합이가능
    private BooleanExpression usernameEq(String username) {
        return hasText(username) ? member.username.eq(username) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }


    public List<MemberTeamDto> searchTeamMemberWhereType(MemberSearchCondition condition){
        BooleanBuilder builder = new BooleanBuilder();

        return queryFactory
                .select(new QMemberTeamDto(
                        member.id,
                        member.username,
                        member.age,
                        team.id,
                        team.name
                ))
                .from(member)
                .leftJoin(member.team,team)
                .where(eqUsername(condition.getUsername()),
                        eqTeamName(condition.getTeamName()),
                        goeAge(condition.getAgeGoe()),
                        loeAge(condition.getAgeLoe())
                        )
                .fetch();
    }

    private BooleanBuilder eqUsername(String username){
        return NullSafeValid(() -> member.username.eq(username));
    }

    private BooleanBuilder eqTeamName(String teamName){
        return NullSafeValid(() -> team.name.eq(teamName));
    }

    private BooleanBuilder goeAge(Integer age){
        return NullSafeValid(() -> member.age.goe(age));
    }

    private BooleanBuilder loeAge(Integer age){
        return NullSafeValid(() -> member.age.loe(age));
    }

    public static BooleanBuilder NullSafeValid(Supplier<BooleanExpression> o) {
     try{
         return new BooleanBuilder(o.get());
     }catch (IllegalArgumentException  | NullPointerException e){
         return new BooleanBuilder();
     }
    }
}
