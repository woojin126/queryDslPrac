package study.querydsl.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id","username","age"}) //team은 들어가면안됨 무한 루프탐
public class Member {

    @Id
    @GeneratedValue
    @Column(name="member_id")
    private Long id;
    private String username;
    private int age;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;



    public Member(String username){
        this(username,0);
    }

    public Member(String username, int age){
        this(username,age,null);
    }

    public Member(String username, int age, Team team){
        this.username=username;
        this.age=age;
        if(team !=null){
            changeTeam(team);
        }
    }



    public void changeTeam(Team team) {
        this.team = team;
        team.getMembers().add(this);
    }
}
