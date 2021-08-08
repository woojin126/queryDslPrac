package study.querydsl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.querydsl.entity.Member;

import java.util.List;

/**
 * 여기서 MemberRepositoryCustom을 상속했는데 이렇게하면 너무 복잡해질수있다.
 * 그러면그냥 @Repository 클래스를 따로만들어서 하는것도 상황에따라 고려하면서 설계(그냥아주 특화된 쿼리일경우!)
 */
public interface MemberRepository extends JpaRepository<Member,Long>,MemberRepositoryCustom {

    //select m from Member m where m.username = ?
    List<Member> findByUsername(String username);
}
