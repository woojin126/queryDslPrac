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
*/
        Member fetchFirst = queryFactory
                .selectFrom(QMember.member)
                //.limit(1).fetchOne()
                .fetchFirst();//위와 같은코드  단건조회

        System.out.println("fetchFirst = " + fetchFirst);
  /*
        QueryResults<Member> results = queryFactory
                .selectFrom(member)
                .fetchResults();

        //results.getTotal();
        List<Member> content = results.getResults(); // 데이터가 나옴 이렇게해야
        //results.getLimit();
  */      //results.getOffset();



       //카운트만가져옴
        long total = queryFactory
                .selectFrom(member)
                .fetchCount();

    }
    /*
       회원 정렬 순서
       1. 회원 나이 내림차순 desc
       2. 회원 이름 올림차순 asc
       단 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
     */
    @Test
    public void sort(){
        em.persist(new Member(null,100));
        em.persist(new Member("member5",100));
        em.persist(new Member("member6",100));

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())//nullsFirst 반대
                .fetch();

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);

        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
    }

    @Test
    public void paging1(){
        List<Member> fetch = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();

        assertThat(fetch.size()).isEqualTo(2);
    }


    @Test
    public void paging2(){
        QueryResults<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();//전체 조회수가 필요하면 , 에는 카운트쿼리가 한번더나감
        //where문에 제약조건이 붙거나 join이 많아지면  count 쿼리에도 조건이 더붙어서 성능이 나빠질수있다.
        //그러니 count 문은 직접 작성하는게 좋을수있다.

        System.out.println("result.getResults() = " + result.getResults());

        assertThat(result.getTotal()).isEqualTo(4);
        assertThat(result.getLimit()).isEqualTo(2);
        assertThat(result.getOffset()).isEqualTo(1);
        assertThat(result.getResults().size()).isEqualTo(2);
    }

    @Test
    public void 집합(){
        //튜플은 쿼리dsl이 제공하는튜플
        List<Tuple> fetch = queryFactory 
                .select(member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetch();
            //tuple은 데이터타입이 여러개 들어올떄 반환함
        Tuple tuple = fetch.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }


    /*
    tip) 빨간밑줄요류로 바로가는 방법 F2
    팀의 이름과 각 팀의 평균 연령을 구해보자
     */
    @Test
    public void group(){
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);
    }


    /*
    팀 A에 소속된 모든 회원
     */
    @Test
    public void join(){
        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("member1","member2");
    }

    /*
    세타 조인
    회원의 이름이 팀 이름과 같은 회원 조회
     */
    @Test
    public void 세타조인(){
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Member> result = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("teamA","teamB");
    }

    /*
    회원과 팀을 조인하면서,팀 이름이 teamA인 팀만 조인, 회원은 모두조회회
    */
    @Test
    void join_on_filter(){
        List<Tuple> result = queryFactory
                .select(member,team)
                .from(member)
                .leftJoin(member.team, team)
                .on(team.name.eq("teamA"))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }



    /*
    회원의 이름이 팀 이름과 같은 대상 외부조인
    leftjoin(member.team,team) 이것과 아래의 차이(member.team) 이면 on절에서 id가 매칭이됨
      from
            member member0_
        left outer join
            team team1_
                on member0_.team_id=team1_.id
                and (
                    member0_.username=team1_.name
                )
    leftJoin(team) //member와 team을 세타조인, 막조인 , 카디션조인 할때 이렇게씀 ,아이디 매칭 x
      from
            member member0_
        left outer join
            team team1_
                on (
                    member0_.username=team1_.name
                )
    */
    @Test
    public void 연관관계가없는엔티티외부조인(){
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        //세타조인 에서는 left join 불가능ㄴ
        List<Tuple> result= queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team) //member와 team을 세타조인, 막조인 , 카디션조인 할때 이렇게씀 ,아이디 매칭 x
                .on(member.username.eq(team.name))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }

    }
}
