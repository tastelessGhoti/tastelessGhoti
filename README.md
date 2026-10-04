<h1 align="center">박한수</h1>
<p align="center"><b>Java/Spring 백엔드 개발자 · 총 6년 4개월</b></p>
<p align="center">총 6년 4개월 경력의 Java/Spring 백엔드 개발자입니다. 금융권 AI 컨택센터와 챗봇, 클라우드 운영 포털, 리테일 업무 시스템에서 API와 배치, 외부 연동을 개발해 왔습니다. 최근에는 여러 서버가 함께 쓰는 상담 세션을 Redis로 관리하며 동시성을 제어했고, OpenSearch에 적재가 멈춘 운영 로그를 복원했습니다.</p>

## Engineering Focus

- Redis 공유 저장소와 분산 락으로 상담 세션의 상태, 동시성, 연결 종료 관리
- 금융 AICC 런타임 검증과 OpenSearch 운영 데이터 복구, 인덱스 정리
- 금융권 채널과 상담 솔루션, 외부 시스템 연동과 보안 요구 반영

## Selected Engineering Evidence

- 상담 세션을 Redis 공유 저장소로 옮기고 세션 키 단위 분산 락을 걸어 여러 서버에서도 한 세션의 요청이 하나씩 처리되게 했고, 연결이 잠깐 끊겨도 바로 닫히지 않도록 종료에 유예를 두었습니다. 상태 전이와 종료, TTL 경로는 mock 기반 단위 테스트 88개로 검증했습니다.
- 일회용 진입 토큰과 Redis 세션 바인딩, 세션 복구를 구현해 은행 웹이나 앱에서 넘어온 고객의 세션이 챗봇과 상담사 채팅까지 이어지게 했고, 배포 후 동작까지 검증했습니다.
- 샤드 한도 때문에 적재가 멈춘 날짜의 로그를 개발계 리허설, 건수 검증, 해시 검증을 거쳐 운영에 복원했습니다. 따로 진행한 인덱스 정리에서는 15개 인덱스를 close해 샤드 수가 3,098에서 3,053으로 줄고 클러스터가 green 상태인 것을 확인했습니다.
- 세션과 URL의 만료 시각을 Redis ZSET에 저장하고 ShedLock을 건 스케줄러가 처리하게 해, 만료 이벤트를 놓쳐도 세션과 URL이 만료되게 했습니다. 권한에 따라 조회 범위가 달라지는 QueryDSL 조회 API도 구현했습니다.
- Redis 5에서 7로 전환하는 절차와 검증, 원복 계획을 정리해 적용을 지원했습니다. MySQL 8.0에서 8.4로 올릴 때의 영향은 Docker로 두 버전을 띄워 재현하고, 업그레이드 전에 확인할 사항 2가지로 정리했습니다.

## Why Hire Me

- **운영까지 책임지는 백엔드 개발자:** 구현에 그치지 않고 원인 분석, 배포, 복구, 검증까지 이어지는 실무 경험이 있습니다.
- **실시간·분산 상태 관리 경험:** Redis 기반 세션·동시성 제어, WebSocket 연결 생명주기와 멀티 인스턴스 동작을 다뤘습니다.
- **금융/AICC 환경의 제약 이해:** 데이터 정합성, 보안, 외부 연동, 운영 안정성을 함께 고려해 구현합니다.
- **근거 중심의 문제 해결:** 재색인, 건수·해시 검증, 원복 절차처럼 결과를 재현하고 확인할 수 있는 방식으로 문제를 해결합니다.


---

## 🛠️ Tech & Tools



<!-- CANONICAL_TECH_INVENTORY:START -->
<!-- inventory_count: 113 -->

### Core Backend Profile

#### Backend

<p>
<!-- skill: Java -->
<img src="https://img.shields.io/badge/Java-ED8B00?style=flat-square&logo=openjdk&logoColor=white"/>
<!-- skill: Spring Boot -->
<img src="https://img.shields.io/badge/Spring%20Boot-6DB33F?style=flat-square&logo=springboot&logoColor=white"/>
<!-- skill: Spring Framework -->
<img src="https://img.shields.io/badge/Spring%20Framework-6DB33F?style=flat-square&logo=spring&logoColor=white"/>
<!-- skill: REST API -->
<img src="https://img.shields.io/badge/REST%20API-0052CC?style=flat-square&logo=postman&logoColor=white"/>
<!-- skill: MyBatis -->
<img src="https://img.shields.io/badge/MyBatis-000000?style=flat-square&logoColor=white"/>
<!-- skill: JPA -->
<img alt="JPA" src="https://img.shields.io/badge/JPA-59666C?style=flat-square&logo=hibernate&logoColor=white"/>
<!-- skill: QueryDSL -->
<img src="https://img.shields.io/badge/QueryDSL-0769AD?style=flat-square&logoColor=white"/>
<!-- skill: Spring Batch -->
<img src="https://img.shields.io/badge/Spring%20Batch-6DB33F?style=flat-square&logo=spring&logoColor=white"/>
<!-- skill: Spring WebFlux -->
<img src="https://img.shields.io/badge/Spring%20WebFlux-6DB33F?style=flat-square&logo=spring&logoColor=white"/>
</p>

#### Session & Realtime

<p>
<!-- skill: Redis -->
<img src="https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=redis&logoColor=white"/>
<!-- skill: Spring Data Redis -->
<img alt="Spring Data Redis" src="https://img.shields.io/badge/Spring%20Data%20Redis-6DB33F?style=flat-square&logo=spring&logoColor=white"/>
<!-- skill: ReactiveRedisTemplate -->
<img alt="ReactiveRedisTemplate" src="https://img.shields.io/badge/ReactiveRedisTemplate-DC382D?style=flat-square&logo=redis&logoColor=white"/>
<!-- skill: WebSocket -->
<img alt="WebSocket" src="https://img.shields.io/badge/WebSocket-111827?style=flat-square&logoColor=white"/>
<!-- skill: STOMP -->
<img alt="STOMP" src="https://img.shields.io/badge/STOMP-6D28D9?style=flat-square&logoColor=white"/>
<!-- skill: CometD -->
<img src="https://img.shields.io/badge/CometD-2563EB?style=flat-square&logoColor=white"/>
<!-- skill: Distributed Lock -->
<img alt="Distributed Lock" src="https://img.shields.io/badge/Distributed%20Lock-0F172A?style=flat-square&logoColor=white"/>
<!-- skill: ShedLock -->
<img src="https://img.shields.io/badge/ShedLock-0F172A?style=flat-square&logoColor=white"/>
<!-- skill: Redis ZSET -->
<img alt="Redis ZSET" src="https://img.shields.io/badge/Redis%20ZSET-DC382D?style=flat-square&logo=redis&logoColor=white"/>
</p>

#### Data & Ops

<p>
<!-- skill: SQL -->
<img src="https://img.shields.io/badge/SQL-336791?style=flat-square&logo=postgresql&logoColor=white"/>
<!-- skill: Oracle -->
<img src="https://img.shields.io/badge/Oracle-F80000?style=flat-square&logo=oracle&logoColor=white"/>
<!-- skill: MariaDB -->
<img src="https://img.shields.io/badge/MariaDB-003545?style=flat-square&logo=mariadb&logoColor=white"/>
<!-- skill: PostgreSQL -->
<img src="https://img.shields.io/badge/PostgreSQL-336791?style=flat-square&logo=postgresql&logoColor=white"/>
<!-- skill: OpenSearch -->
<img src="https://img.shields.io/badge/OpenSearch-005EB8?style=flat-square&logo=opensearch&logoColor=white"/>
<!-- skill: Logstash -->
<img src="https://img.shields.io/badge/Logstash-005571?style=flat-square&logo=logstash&logoColor=white"/>
<!-- skill: Nginx -->
<img src="https://img.shields.io/badge/Nginx-009639?style=flat-square&logo=nginx&logoColor=white"/>
<!-- skill: Linux -->
<img src="https://img.shields.io/badge/Linux-FCC624?style=flat-square&logo=linux&logoColor=black"/>
</p>

#### Delivery

<p>
<!-- skill: Docker -->
<img src="https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white"/>
<!-- skill: Kubernetes -->
<img src="https://img.shields.io/badge/Kubernetes-326CE5?style=flat-square&logo=kubernetes&logoColor=white"/>
<!-- skill: Jenkins -->
<img src="https://img.shields.io/badge/Jenkins-D24939?style=flat-square&logo=jenkins&logoColor=white"/>
<!-- skill: Git -->
<img src="https://img.shields.io/badge/Git-F05032?style=flat-square&logo=git&logoColor=white"/>
<!-- skill: Maven -->
<img src="https://img.shields.io/badge/Maven-C71A36?style=flat-square&logo=apachemaven&logoColor=white"/>
<!-- skill: Gradle -->
<img src="https://img.shields.io/badge/Gradle-02303A?style=flat-square&logo=gradle&logoColor=white"/>
<!-- skill: Apache JMeter -->
<img src="https://img.shields.io/badge/Apache%20JMeter-D22128?style=flat-square&logo=apache&logoColor=white"/>
</p>

### Full Experience Inventory - Additional 80

#### Languages

<p>
<!-- skill: JavaScript -->
<img src="https://img.shields.io/badge/JavaScript-F7DF1E?style=flat-square&logo=javascript&logoColor=black"/>
</p>

#### Core Backend & Runtime

<p>
<!-- skill: Spring MVC -->
<img src="https://img.shields.io/badge/Spring%20MVC-6DB33F?style=flat-square&logo=spring&logoColor=white"/>
<!-- skill: Spring Cloud -->
<img src="https://img.shields.io/badge/Spring%20Cloud-6DB33F?style=flat-square&logo=spring&logoColor=white"/>
<!-- skill: Spring WebSocket -->
<img src="https://img.shields.io/badge/Spring%20WebSocket-6DB33F?style=flat-square&logo=spring&logoColor=white"/>
<!-- skill: Spring Messaging -->
<img alt="Spring Messaging" src="https://img.shields.io/badge/Spring%20Messaging-6DB33F?style=flat-square&logo=spring&logoColor=white"/>
<!-- skill: Spring Scheduler -->
<img src="https://img.shields.io/badge/Spring%20Scheduler-6DB33F?style=flat-square&logo=spring&logoColor=white"/>
<!-- skill: Spring Security -->
<img src="https://img.shields.io/badge/Spring%20Security-6DB33F?style=flat-square&logo=springsecurity&logoColor=white"/>
<!-- skill: Spring Data JPA -->
<img src="https://img.shields.io/badge/Spring%20Data%20JPA-6DB33F?style=flat-square&logo=spring&logoColor=white"/>
<!-- skill: Hibernate -->
<img src="https://img.shields.io/badge/Hibernate-59666C?style=flat-square&logo=hibernate&logoColor=white"/>
<!-- skill: iBATIS -->
<img src="https://img.shields.io/badge/iBATIS-4B5563?style=flat-square&logoColor=white"/>
<!-- skill: Reactor -->
<img alt="Reactor" src="https://img.shields.io/badge/Project%20Reactor-6DB33F?style=flat-square&logo=reactivex&logoColor=white"/>
<!-- skill: Apache Tomcat -->
<img src="https://img.shields.io/badge/Apache%20Tomcat-F8DC75?style=flat-square&logo=apachetomcat&logoColor=black"/>
<!-- skill: Undertow -->
<img alt="Undertow" src="https://img.shields.io/badge/Undertow-1F2937?style=flat-square&logoColor=white"/>
<!-- skill: JBoss / LENA WAS -->
<img src="https://img.shields.io/badge/JBoss%20%2F%20LENA%20WAS-A30000?style=flat-square&logo=redhat&logoColor=white"/>
<!-- skill: Netflix Zuul -->
<img src="https://img.shields.io/badge/Netflix%20Zuul-E50914?style=flat-square&logo=netflix&logoColor=white"/>
<!-- skill: ZooKeeper -->
<img src="https://img.shields.io/badge/ZooKeeper-D97706?style=flat-square&logo=apache&logoColor=white"/>
</p>

#### Frontend & Client

<p>
<!-- skill: HTML5 -->
<img src="https://img.shields.io/badge/HTML5-E34F26?style=flat-square&logo=html5&logoColor=white"/>
<!-- skill: JSP -->
<img src="https://img.shields.io/badge/JSP-007396?style=flat-square&logo=java&logoColor=white"/>
<!-- skill: JSTL -->
<img src="https://img.shields.io/badge/JSTL-0F766E?style=flat-square&logoColor=white"/>
<!-- skill: jQuery -->
<img src="https://img.shields.io/badge/jQuery-0769AD?style=flat-square&logo=jquery&logoColor=white"/>
<!-- skill: Ajax -->
<img src="https://img.shields.io/badge/Ajax-0EA5E9?style=flat-square&logoColor=white"/>
<!-- skill: Thymeleaf -->
<img src="https://img.shields.io/badge/Thymeleaf-005F0F?style=flat-square&logo=thymeleaf&logoColor=white"/>
<!-- skill: PostMessage API -->
<img src="https://img.shields.io/badge/PostMessage%20API-F59E0B?style=flat-square&logo=javascript&logoColor=white"/>
<!-- skill: WebSocket Client -->
<img src="https://img.shields.io/badge/WebSocket%20Client-111827?style=flat-square&logoColor=white"/>
<!-- skill: MiPlatform -->
<img src="https://img.shields.io/badge/MiPlatform-7C2D12?style=flat-square&logoColor=white"/>
</p>

#### Databases, Cache & Search

<p>
<!-- skill: MySQL -->
<img src="https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white"/>
<!-- skill: MongoDB -->
<img src="https://img.shields.io/badge/MongoDB-47A248?style=flat-square&logo=mongodb&logoColor=white"/>
<!-- skill: Amazon Aurora -->
<img src="https://img.shields.io/badge/Amazon%20Aurora-527FFF?style=flat-square&logo=amazonrds&logoColor=white"/>
<!-- skill: Reactive Redis -->
<img alt="Reactive Redis" src="https://img.shields.io/badge/Reactive%20Redis-DC382D?style=flat-square&logo=redis&logoColor=white"/>
<!-- skill: Redis TTL -->
<img alt="Redis TTL" src="https://img.shields.io/badge/Redis%20TTL-DC382D?style=flat-square&logo=redis&logoColor=white"/>
<!-- skill: Redis SETNX -->
<img alt="Redis SETNX" src="https://img.shields.io/badge/Redis%20SETNX-DC382D?style=flat-square&logo=redis&logoColor=white"/>
<!-- skill: Lua script -->
<img alt="Lua script" src="https://img.shields.io/badge/Lua%20Script-2C2D72?style=flat-square&logo=lua&logoColor=white"/>
<!-- skill: Redisson -->
<img src="https://img.shields.io/badge/Redisson-B91C1C?style=flat-square&logo=redis&logoColor=white"/>
<!-- skill: Elasticsearch -->
<img src="https://img.shields.io/badge/Elasticsearch-005571?style=flat-square&logo=elasticsearch&logoColor=white"/>
<!-- skill: Apache Solr -->
<img src="https://img.shields.io/badge/Apache%20Solr-D9411E?style=flat-square&logo=apachesolr&logoColor=white"/>
<!-- skill: SolrJ -->
<img alt="SolrJ" src="https://img.shields.io/badge/SolrJ-D9411E?style=flat-square&logo=apachesolr&logoColor=white"/>
</p>

#### Infra, DevOps & Platform

<p>
<!-- skill: Docker Compose -->
<img src="https://img.shields.io/badge/Docker%20Compose-1D63ED?style=flat-square&logo=docker&logoColor=white"/>
<!-- skill: Amazon EC2 -->
<img src="https://img.shields.io/badge/Amazon%20EC2-FF9900?style=flat-square&logo=amazonec2&logoColor=white"/>
<!-- skill: CloudFront -->
<img alt="Amazon CloudFront" src="https://img.shields.io/badge/Amazon%20CloudFront-8C4FFF?style=flat-square&logo=amazonwebservices&logoColor=white"/>
<!-- skill: GitLab -->
<img src="https://img.shields.io/badge/GitLab-FC6D26?style=flat-square&logo=gitlab&logoColor=white"/>
<!-- skill: SVN -->
<img src="https://img.shields.io/badge/SVN-809CC9?style=flat-square&logo=subversion&logoColor=white"/>
<!-- skill: RHEL 7 -->
<img alt="RHEL 7" src="https://img.shields.io/badge/RHEL%207-EE0000?style=flat-square&logo=redhat&logoColor=white"/>
<!-- skill: CentOS -->
<img src="https://img.shields.io/badge/CentOS-262577?style=flat-square&logo=centos&logoColor=white"/>
<!-- skill: Bash / Shell Script -->
<img alt="Bash / Shell Script" src="https://img.shields.io/badge/Bash%20%2F%20Shell%20Script-4EAA25?style=flat-square&logo=gnubash&logoColor=white"/>
</p>

#### Logging & Observability

<p>
<!-- skill: Logback -->
<img src="https://img.shields.io/badge/Logback-111827?style=flat-square&logoColor=white"/>
<!-- skill: Log4j -->
<img src="https://img.shields.io/badge/Log4j-CB0000?style=flat-square&logo=apache&logoColor=white"/>
<!-- skill: Kibana -->
<img src="https://img.shields.io/badge/Kibana-005571?style=flat-square&logo=kibana&logoColor=white"/>
<!-- skill: Zipkin -->
<img src="https://img.shields.io/badge/Zipkin-111827?style=flat-square&logoColor=white"/>
<!-- skill: Prometheus -->
<img src="https://img.shields.io/badge/Prometheus-E6522C?style=flat-square&logo=prometheus&logoColor=white"/>
<!-- skill: Grafana -->
<img src="https://img.shields.io/badge/Grafana-F46800?style=flat-square&logo=grafana&logoColor=white"/>
<!-- skill: Datadog -->
<img alt="Datadog" src="https://img.shields.io/badge/Datadog-632CA6?style=flat-square&logo=datadog&logoColor=white"/>
</p>

#### Security & External Integration

<p>
<!-- skill: Lucy XSS -->
<img src="https://img.shields.io/badge/Lucy%20XSS-1D4ED8?style=flat-square&logoColor=white"/>
<!-- skill: Jasypt -->
<img src="https://img.shields.io/badge/Jasypt-15803D?style=flat-square&logoColor=white"/>
<!-- skill: JWT -->
<img alt="JWT" src="https://img.shields.io/badge/JWT-000000?style=flat-square&logo=jsonwebtokens&logoColor=white"/>
<!-- skill: RSA -->
<img alt="RSA" src="https://img.shields.io/badge/RSA-334155?style=flat-square&logoColor=white"/>
<!-- skill: Apache HttpClient -->
<img src="https://img.shields.io/badge/Apache%20HttpClient-D22128?style=flat-square&logo=apache&logoColor=white"/>
<!-- skill: Kakao API -->
<img alt="Kakao API" src="https://img.shields.io/badge/Kakao%20API-FFCD00?style=flat-square&logo=kakaotalk&logoColor=3C1E1E"/>
<!-- skill: Kakao Maps API -->
<img src="https://img.shields.io/badge/Kakao%20Maps%20API-FFCD00?style=flat-square&logo=kakaotalk&logoColor=3C1E1E"/>
<!-- skill: Spectra -->
<img alt="Spectra" src="https://img.shields.io/badge/Spectra-475569?style=flat-square&logoColor=white"/>
<!-- skill: REST 콜백 -->
<img alt="REST 콜백" src="https://img.shields.io/badge/REST%20Callback-0052CC?style=flat-square&logo=postman&logoColor=white"/>
<!-- skill: WebSocket 푸시 -->
<img alt="WebSocket 푸시" src="https://img.shields.io/badge/WebSocket%20Push-111827?style=flat-square&logoColor=white"/>
<!-- skill: Polling -->
<img alt="Polling" src="https://img.shields.io/badge/Polling-2563EB?style=flat-square&logoColor=white"/>
<!-- skill: TMS API -->
<img alt="TMS API" src="https://img.shields.io/badge/TMS%20API-0F766E?style=flat-square&logoColor=white"/>
<!-- skill: GetSmart API -->
<img alt="GetSmart API" src="https://img.shields.io/badge/GetSmart%20API-4338CA?style=flat-square&logoColor=white"/>
<!-- skill: U.STRA TALK -->
<img alt="U.STRA TALK" src="https://img.shields.io/badge/U.STRA%20TALK-7C3AED?style=flat-square&logoColor=white"/>
<!-- skill: Multi-instance Session Management -->
<img alt="Multi-instance Session Management" src="https://img.shields.io/badge/Multi--instance%20Session%20Management-1E40AF?style=flat-square&logoColor=white"/>
<!-- skill: Session Lifecycle Management -->
<img alt="Session Lifecycle Management" src="https://img.shields.io/badge/Session%20Lifecycle%20Management-0F766E?style=flat-square&logoColor=white"/>
</p>

#### Enterprise & Legacy Experience

<p>
<!-- skill: DevOn Framework -->
<img src="https://img.shields.io/badge/DevOn%20Framework-1E3A8A?style=flat-square&logoColor=white"/>
<!-- skill: vCenter -->
<img src="https://img.shields.io/badge/vCenter-607078?style=flat-square&logo=vmware&logoColor=white"/>
<!-- skill: vRealize Orchestrator -->
<img src="https://img.shields.io/badge/vRealize%20Orchestrator-4B5563?style=flat-square&logo=vmware&logoColor=white"/>
<!-- skill: vRealize Operations -->
<img src="https://img.shields.io/badge/vRealize%20Operations-4B5563?style=flat-square&logo=vmware&logoColor=white"/>
<!-- skill: NSX-T -->
<img src="https://img.shields.io/badge/NSX--T-607078?style=flat-square&logo=vmware&logoColor=white"/>
<!-- skill: PowerFlex API -->
<img src="https://img.shields.io/badge/PowerFlex%20API-006BB6?style=flat-square&logo=dell&logoColor=white"/>
<!-- skill: VMware API -->
<img alt="VMware API" src="https://img.shields.io/badge/VMware%20API-607078?style=flat-square&logo=vmware&logoColor=white"/>
<!-- skill: Apache POI -->
<img src="https://img.shields.io/badge/Apache%20POI-D22128?style=flat-square&logo=apache&logoColor=white"/>
<!-- skill: JXLS -->
<img src="https://img.shields.io/badge/JXLS-4B5563?style=flat-square&logoColor=white"/>
</p>

#### Monitoring & Testing

<p>
<!-- skill: JUnit -->
<img src="https://img.shields.io/badge/JUnit-25A162?style=flat-square&logo=junit5&logoColor=white"/>
</p>

#### Build Tools & IDEs

<p>
<!-- skill: IntelliJ IDEA -->
<img src="https://img.shields.io/badge/IntelliJ%20IDEA-000000?style=flat-square&logo=intellijidea&logoColor=white"/>
<!-- skill: Eclipse -->
<img src="https://img.shields.io/badge/Eclipse-2C2255?style=flat-square&logo=eclipse&logoColor=white"/>
<!-- skill: VS Code -->
<img src="https://img.shields.io/badge/VS%20Code-007ACC?style=flat-square&logo=visualstudiocode&logoColor=white"/>
</p>

<!-- CANONICAL_TECH_INVENTORY:END -->

### Architecture & Applied Patterns

- 멀티 인스턴스 상태 관리와 동시성 제어
- 세션과 연결 종료 생명주기 관리
- 콜백과 실시간 푸시를 함께 사용하는 연동 흐름

### Enterprise / External Integration

- 금융 상담·채널과 외부 시스템 연동
- 프라이빗 클라우드 운영 포털과 엔터프라이즈 연동


---

## Awards & Certifications

### 수상

- **KOTRA 사장상** | 2017.06
  - 2017 대한민국 소비재수출대전 4차 산업혁명 선도 소비재 융합제품 경진대회

### 📜 자격증
| 자격 | 일자 | 발급기관 |
|---|---|---|
| 정보기기운용기능사 | 2016.07 | 한국산업인력공단 |
| 워드프로세서 | 2014.08 | 대한상공회의소 |
| 리눅스마스터 2급 | 2014.07 | 한국정보통신인력개발센터 |
| 인터넷정보관리사 2급 | 2014.07 | 한국정보통신인력개발센터 |
| ITQ 한글파워포인트 A등급 | 2012.12 | 한국생산성본부 |
| ITQ 아래한글 A등급 | 2012.09 | 한국생산성본부 |

---

## Links & Contact

<p align="center">
  <a href="https://velog.io/@peobae/posts">Velog 기술 블로그</a>
  &nbsp;|&nbsp;
  <a href="https://github.com/tastelessGhoti/tastelessGhoti">GitHub 기술 프로필</a>
  &nbsp;|&nbsp;
  <a href="https://gamulgamulgamulchi.tistory.com/">Tistory 기술 기록</a>
</p>

<p align="center"><a href="mailto:ghoti.park@gmail.com">ghoti.park@gmail.com</a></p>
