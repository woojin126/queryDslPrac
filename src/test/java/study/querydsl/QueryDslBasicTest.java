package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.hibernate.query.criteria.internal.expression.function.AggregationFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.data.jdbc.AutoConfigureDataJdbc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.*;

@SpringBootTest
@Transactional
@Commit
public class QueryDslBasicTest {

    @PersistenceContext
    EntityManager em;

    JPAQueryFactory queryFactory; //엔티매니저를 생성자에 넘겨주면 이걸가지고 데이터를 찾던가함
                                  //동시성 문제도 x
    @BeforeEach //테스트 실행전 데이터 셋팅을위해
    public void before() {
        queryFactory = new JPAQueryFactory(em);
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamD");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1",10,teamA);
        Member member2 = new Member("member2",20,teamA);
        Member member3 = new Member("member3",30,teamB);
        Member member4 = new Member("member4",40,teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    void querydsl(){
   /*     List<Member> fetch = queryFactory
                .selectFrom(member)
                .where(member.age.between(20, 40).or(member.username.eq("member1")))
                .orderBy(member.age.desc())
                .fetch();

        for (Member fetch1 : fetch) {
            System.out.println("fetch1.getUsername() = " + fetch1.getUsername());
        }
    */

/*
       queryFactory
                .select(member.age.avg(), team.id)
                .from(member)
                .innerJoin(team)
                .on(member.team.id.eq(team.id))
                .where(member.id.gt(10))
                .fetch();*/

        List<Tuple> fetch = queryFactory
                .select(member.age.avg(), member.username)
                .from(member)
                .where(member.id.goe(3))
                .groupBy(member.username)
                .orderBy(member.username.asc())
                .fetch();


    }

    @Test
    public void startJQPL(){
        //member1을 찾자.  컨트롤 + 알트 + v  쿼리 추출
        String qlString = "select m from Member m where m.username = :username";
        Member findMember = em.createQuery(qlString, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQueryDsl(){
        //QMember m = new QMember("m"); //변수명에다 별칭을 줘야함 중요하진 x
        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1")) //파라미터 바인딩 자동으로해버림
                .fetchOne();


        assertThat(findMember.getUsername()).isEqualTo("member1");

    }

    @Test
    public void search(){
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.between(10,30)))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void searchAndParam() {
        Member findMember = queryFactory
                .selectFrom(member)
                .where(
                        member.username.eq("member1") // ,  == and
                        , (member.age.eq(10))
                )
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void resultFetch(){
  /*      List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch();  //맴버의 목록을 List로 조회

        Member fetchOne = queryFactory
                .selectFrom(QMember.member)
                .fetchOne(); //단건조회

        Member fetchFirst = queryFactory
                .selectFrom(QMember.member)
                //.limit(1).fetchOne()
                .fetchFirst();//위와 같은코드  단건조회
    */
        QueryResults<Member> results = queryFactory
                .selectFrom(member)
                .fetchResults();

        //results.getTotal();
        List<Member> content = results.getResults(); // 데이터가 나옴 이렇게해야
        //results.getLimit();
        //results.getOffset();



       //카운트만가져옴
        long total = queryFactory
                .selectFrom(member)
                .fetchCount();

    }
}
