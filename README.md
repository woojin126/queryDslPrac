
#####목표

# queryDslPrac
## 쿼리DSL 개념, 코드 최적화 시켜보기

### 기본 사양
- spring boot 2.4.5
- Spring Data JPA
- Oracle 11g XE 
- h2Database

### Utils dependencies
- lombok
- spring-boot-starter-web
- spring-boot-starter-data-jpa
- querydsl-jpa
- devtools
- junit-jupiter-api:5.7.0
- unit-jupiter-engine:5.7.2


### IDEL
- IntelliJ로 구축

### Version
## 1.0 구현
- 1.1.0 순수 JPA로 CRUD , 페이징 처리해보기
- 1.1.1 Spring data JPA + QueryDsl 를 이용해서 페이징처리(검색조건,정렬,페이징,기본조인,on절, 페치 조인, case문 상수,문자 더하기
- 프로젝션 결과 반환: 기본조회, DTO 반환조회 , @QueryProjection
- 동적 쿼리 BooleanBuilder , where 다중 파라미터 사용
- 수정, 삭제 벌크 연산
- SQL FUNCTION 호출
- QueryDsl을 이용하여 페이징을 극한으로 최적화해보기
- 커스텀 jpaRepository 상속해보기

### IntelliJ 콘솔 로그 한글 깨짐 해결 방법
- IntelliJ File Encodings 변경

1. 컨트롤 + 알트 + S
2. Editor > File Encodings 선택
3. 셋팅

- Global Encoding:UTF-8
- Project Encoding:UTF-8
- Default encoding for properties files:UTF-8

### lombok 설정
1. Setting
2. Annotation Processors 
3. Enable annotaion processing 체크

### gradle 동작 방식 설정
1. setting
2. 검색창에 gradle
3. Build and run using , Run tests using : 모두 IntelliJ로 변경

