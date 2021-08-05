package study.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MemberDto {

    private String username;
    private int age;
    private String name;


    @QueryProjection
    public MemberDto(String username, int age) {
        this.username = username;
        this.age = age;
    }

    @QueryProjection
    public MemberDto(String username, int age, String name) {
        this.username = username; //맴버
        this.age = age; // 맴버 
        this.name = name; //팀의 이름
    }
}
