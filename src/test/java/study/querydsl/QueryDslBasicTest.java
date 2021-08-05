package study.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.hibernate.query.criteria.internal.expression.function.AggregationFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.data.jdbc.AutoConfigureDataJdbc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.dto.UserDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;
import study.querydsl.entity.Team;

import javax.persistence.*;

import java.util.List;

import static com.querydsl.jpa.JPAExpressions.*;
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
        Team teamB = new Team("teamB");
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

        for (Tuple tuple : result) {
            System.out.println("tuple.get(team.name) = " + tuple.get(team.name));
            System.out.println("tuple.get(member.age.avg()) = " + tuple.get(member.age.avg()));
        }
        
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

        for (Member member1 : result) {
            System.out.println("member1 = " + member1);
        }

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
                .select(member, team)
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

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    public void fetchJoinNo(){
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());//로딩된 엔티티인지 초기화가 안된엔티티인지 알려주는기능
        assertThat(loaded).isFalse();
    }

    @Test
    public void fetchJoinUse(){
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .leftJoin(member.team,team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();


        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());//로딩된 엔티티인지 초기화가 안된엔티티인지 알려주는기능
        assertThat(loaded).isTrue();
    }
    
    /*
    나이가 가장 많은 회원을 조회
     */
    @Test
    public void subQuery(){

        //alias가 중복되면 안되기때문에 직접 큐맴버 생성
        //메인쿼리 member와 서브쿼리 member의 alias가 같으면 안되기때문에 맴버큐 하나더생성
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        select(memberSub.age.max()) // 여기부터 나이 가장많은 40 추출
                                .from(memberSub)
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(40);
    }


    /*
 나이가 평균 이상인 회원
  */
    @Test
    public void subQueryGoe(){

        //alias가 중복되면 안되기때문에 직접 큐맴버 생성
        //메인쿼리 member와 서브쿼리 member의 alias가 같으면 안되기때문에 맴버큐 하나더생성
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .select(member)
                .from(member)
                .where(member.age.goe(
                        select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(30,40);
    }

    /*


*/
    @Test
    public void subQueryInWhere절(){

        //alias가 중복되면 안되기때문에 직접 큐맴버 생성
        //메인쿼리 member와 서브쿼리 member의 alias가 같으면 안되기때문에 맴버큐 하나더생성
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        select(memberSub.age)
                                .from(memberSub)
                                 .where(memberSub.age.gt(10))
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(20,30,40);
    }


    

    @Test
    public void subQueryInSelect절(){

        //alias가 중복되면 안되기때문에 직접 큐맴버 생성
        //메인쿼리 member와 서브쿼리 member의 alias가 같으면 안되기때문에 맴버큐 하나더생성
        QMember memberSub = new QMember("memberSub");

        List<Tuple> result = queryFactory
                .select(member.username,
                        select(memberSub.age.avg())
                                .from(memberSub))
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }


    }
    
    @Test
    void basicCase문(){
        List<String> result = queryFactory
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }


    @Test
    void basicCase복잡조건(){

        List<String> result = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0, 20)).then("0~20살")
                        .when(member.age.between(21, 30)).then("21~30살")
                        .otherwise("기타")
                )
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }
    
    @Test
    void 케이스빌더(){
        NumberExpression<Integer> rankPath = new CaseBuilder()
                .when(member.age.between(0, 20)).then(2)
                .when(member.age.between(21, 30)).then(1)
                .otherwise(3);
        List<Tuple> result = queryFactory
                .select(member.username, member.age, rankPath)
                .from(member)
                .orderBy(rankPath.desc())
                .fetch();
        for (Tuple tuple : result) {
            String username = tuple.get(member.username);
            Integer age = tuple.get(member.age);
            Integer rank = tuple.get(rankPath);
            System.out.println("username = " + username + " age = " + age + " rank = "
                    + rank);
        }


    }


    @Test
    public void 상수(){
        List<Tuple> result = queryFactory
                .select(member.username, Expressions.constant("A")) //단순히 쿼리결과에 A를 추가해줌
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    /*
    tip)
      ENUM타입 같은것들은 값이 안나오는데 이런경우 stringValue()를 사용하면된다!
     */
    @Test
    public void concat() {
        List<String> result = queryFactory
                .select(member.username.concat("_").concat(member.age.stringValue())) //문자 + 숫자 concat은안됨 age를 string으로 변환해야함
                .from(member)
                .where(member.username.eq("member1"))
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    //대상이 둘이상일때 튜플로반환
    //하나일땐 타입으로 반환
    @Test
    public void simpleProjection(){
        List<String> result = queryFactory
                .select(member.username)
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    /*
    tuple 같은 반환타입은 repository 에서만 사용하는게 좋다 설계상
    하부기술을 쿼리dsl에서 다른걸로 바꾸더라도 front 단에서는 건드릴필요 없도록.
    (다른곳으로 값을 밖으로 던질때는 dto로 변환해서 넘기기)
     */
    @Test
    public void tupleProjection(){
        List<Tuple> result = queryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple.get(member.username) = " + tuple.get(member.username));
            System.out.println("tuple.get(member.age) = " + tuple.get(member.age));

        }
    }

    //생성자 필수!
    @Test
    public void findDtoByJPQL(){
        List<MemberDto> result = em.createQuery("select new study.querydsl.dto.MemberDto" +
                " (m.username, m.age )" +  //new 오퍼레이션은 생성자의 매개변수가 넘어온다
                " from Member m ", MemberDto.class)
                .getResultList();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    //queryDsl은 위의 방식을 모두극복 3가지
    //프로퍼티 접근, 필드 접근 , 생성자 사용
    
    @Test //기본생성자가필요 @Data를 상용해서 기본생성자가없음 ,세터를 통해서 값이 들어감
    public void findDtoBySetter(){

        List<MemberDto> result = queryFactory
                .select(Projections.bean(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test //필드에 값을 바로꽂아버리기떄문에 getter setter 필요없음
    public void findDtoByField(){

        List<MemberDto> result = queryFactory
                .select(Projections.fields(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    //각 이름과 ,나이의 평균을 나타내고싶을때(서브쿼리이용)
    //ExpressionUtils: 서브쿼리는 dto age 필드와 이름 매칭이 필요한데 ,  이를 위해서 ExpressionUtils.as 사용 (alias를 age로 하기위해)
    // , 또 써브쿼리는 별칭이 필요하기도함 (윗줄의 이유와 지금 여기줄의 이유)
    @Test //UserDto 의필드이름은 name 으로되있어서 매칭이안되있어서 null이뜸
    public void findUserDtoField(){

        QMember memberSub = new QMember("memberSub");

        List<UserDto> result = queryFactory
                .select(Projections.fields(UserDto.class,
                        member.username.as("name"),//이렇게해줘야 null이 안뜸

                        ExpressionUtils.as(JPAExpressions
                        .select(memberSub.age.max())
                                .from(memberSub),"age" )
                ))
                .from(member)
                .fetch();


        for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto);

        }
    }


    //생성자방식
    //문제점 select안에 dto에없는 필드가 있다고해도 컴파일오류로 못잡음 런타임 오류가난다 그게문제
    //이문제를 아래 쿼리방식프로젝션이 해결 (컴파일시점에 잡아줌)
    @Test //생성자에 타입이 딱 맞아야함 애는
    public void findDtoByConstructor(){

        List<UserDto> result = queryFactory
                .select(Projections.constructor(UserDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (UserDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    //컨트롤 + p 매개변수 정보가나옴
    /*ㅇ
    장점: 컴파일시점에 컬럼 오류 체크 (dto와 필드매칭, 개수 )
    단점
    1.Q파일을 생성해야함 dto에 대한
    2.dto가 queryDsl에 의존성을 가지게됨 (@QueryProjection 때문에)
    3.그리고 dto가 여러 계층을통해 반환이되는데 (서비스 ,컨트롤, api반환 등) 이dto안에 쿼리 프로젝션이있으니..

    단점하나더!
    DTO에 @QueryProjection을 사용하게 되면 만약에 Querydsl 라이브러리를 제거하고, 다른 라이브러리로 변경해서 사용한다고 했을 때 해당 DTO까지 함께 손을 봐야 합니다.
    @QueryProjection를 사용하지 않으면, 같은 상황에서 해당 DTO를 손대지 않고 그대로 유지할 수 있습니다.

    */
    @Test
    public void findDtoByQueryProejctionAnnotation(){
        List<MemberDto> result = queryFactory
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void findDtoByQueryProejctionAnnotation2(){
        List<MemberDto> result = queryFactory
                .select(new QMemberDto(member.username, member.age,team.name))
                .from(member)
                .join(member.team,team)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }
    
    
    //동적쿼리 해결방법중 하나
  /*  @Test
    public void dynamicQuery_BooleanBuilder(){
        String usernameParam = "member1";
        Integer ageParam = null;

        List<Member> result = searchMember1(usernameParam,ageParam);
        assertThat(result.size()).isEqualTo(1);
    }
*//*
    *매개변수 들어오는값이 null이냐 아니냐에따라 동적으로 바뀌도록설계
     1.usernameParam 값은있고 ageParam이 null이라면  member1인 조건만 찾고
     2.둘다 널이라면 아예 조를 안하도록
*//*
    private List<Member> searchMember1(String usernameCond, Integer ageCond) {
        //방어코드용으로 usernameCond가 null이면안된다 하면
        // BooleanBuilder builder = new BooleanBuilder(member.username.eq(usernameCond));
        BooleanBuilder builder = new BooleanBuilder();
        if(usernameCond != null){
            builder.and(member.username.eq(usernameCond));
        }

        if( ageCond != null){
            builder.and(member.age.eq(ageCond));
        }

        return queryFactory
                .selectFrom(member)
                .where(builder)
                .fetch();
    }*/



    //코드가 직관적으로 이해할수있어 더좋다.
    @Test
    public void dynamicQuery_Whereparam(){
        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember2(usernameParam, ageParam);
    }

    private List<Member> searchMember2(String usernameCond, Integer ageCond) {
        return queryFactory
                .selectFrom(member)
                .where(usernameEq(usernameCond), ageEq(ageCond)) //where에 NULL이들어가면 무시함
                .fetch();

    }
    private Predicate usernameEq(String usernameCond) {
        return usernameCond != null ? member.username.eq(usernameCond) :  null;

    }
    private Predicate ageEq(Integer ageCond) {
        return ageCond != null ? member.age.eq(ageCond) : null;
    }


}
