# 나만의 SNS 웹 애플리케이션: Moira!

현대 사회에서 소셜 네트워크 서비스(SNS)는 사람들이 소통하고 정보를 공유하는 중요한 플랫폼 중 하나입니다. 본 프로젝트에서는 사용자들이 자유롭게 의견을 나누고 정보를 공유할 수 있는 SNS 웹 애플리케이션을 개발했습니다.

해당 프로젝트는 다음과 같은 목표를 가지고 있습니다.

- **빠른 응답 속도와 안정성**

  본 프로젝트는 대규모 사용자 트래픽에도 빠르고 안정적인 서비스를 제공하기 위해 다양한 최적화 과정을 거쳐 고가용성을 갖춘 웹 애플리케이션을 제공하는 것을 목표로 합니다. 이를 통해 사용자들은 끊김 없이 실시간으로 최신 정보를 놓치지 않고 다른 유저와 원활하게 소통할 수 있습니다.

- **멀티미디어 컨텐츠 게시**

  사용자들은 텍스트뿐만 아니라 이미지 형태의 멀티미디어 컨텐츠를 게시할 수 있습니다. 게시된 컨텐츠에는 다른 유저들이 댓글을 달거나 '좋아요'를 눌러 작성자와 적극적으로 소통할 수 있습니다. 또한, 사용자는 자신만의 동영상 채널을 생성하여 구독자들에게 다양한 주제의 최신 영상을 제공할 수 있으며, 구독자들은 관심 있는 채널을 구독하여 손쉽게 최신 컨텐츠를 시청할 수 있습니다.

- **사용자 간 소통 활성화**

  사용자들은 다른 사용자를 팔로우하여 해당 사용자의 최신 게시글을 자신의 피드에서 실시간으로 받아볼 수 있습니다. 팔로우하는 사용자의 수에 따라 피드가 더욱 풍부해져 다양한 의견과 정보를 접할 수 있으며, 이를 통해 보다 넓은 시각에서 소셜 네트워킹을 즐길 수 있습니다. 마지막으로, 사용자 간 1:1 채팅 기능을 통해 직접 소통할 수 있어, 보다 친밀한 관계 형성이 가능합니다.

- **효율적인 어드민 관리 도구 제공**

  어드민은 대시보드를 통해 애플리케이션을 효율적으로 관리할 수 있습니다. 사용자의 활동을 모니터링하고, 필요한 조치를 신속하게 취할 수 있으며, 전반적인 애플리케이션의 상태를 한 눈에 파악할 수 있습니다.

## ✋들어가기 앞서...👇

#### 최종 수정 시간: 2025년 1월 30일 오전 10시

이 문서는 지속적으로 업데이트될 예정입니다. 아래 목차를 참고하시면 보다 원활한 이해가 가능합니다.

1. [개발 정보](#1-개발-정보)

2. [기술 스택](#2-기술-스택)

3. [핵심 기능](#3-핵심-기능)

---

### 1. 개발 정보

#### 👨‍👩‍👧‍👦 개발진 👨‍👩‍👧‍👦
| 허훈 |
|:----------------------------------------------------------:|
| ![](https://avatars.githubusercontent.com/u/152062846?v=4) |
| [@hunzzzzz](https://github.com/hunzzzzz) |

#### ⏰ 개발 일정 ⏰

`25.01.28 ~ 25.01.30` Moira 1.0.0 개발

개발진은 최대한 빠르고 정확하게 프로젝트를 완성하기 위해서 최선을 다하고 있습니다만...

본 프로젝트는 개인 업무<s>(❗취업 준비❗)</s>와 병행하여 진행되고 있기 때문에,

개발 일정이 다소 불규칙할 수 있으며 예상보다 개발이 지연될 수 있음을 미리 공지드립니다. 🙇‍♂️

---

### 2. 기술 스택

#### 백엔드
<img src="https://img.shields.io/badge/Kotlin 2.1.0-7F52FF?style=for-the-badge&logo=Kotlin&logoColor=white">
<img src="https://img.shields.io/badge/springboot 3.4.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
<img src="https://img.shields.io/badge/QueryDSL 5.0-%23007bff?style=for-the-badge">
<img src="https://img.shields.io/badge/JWT 0.12.6-%23f532f2?style=for-the-badge">
<img src="https://img.shields.io/badge/JUnit 5-%236DB33F?style=for-the-badge">
<img src="https://img.shields.io/badge/TestContainers 1.20.4-%233fb3cd?style=for-the-badge">

#### 인프라

<img src="https://img.shields.io/badge/MySQL 8.4-4479A1?style=for-the-badge&logo=MySQL&logoColor=white">
<img src="https://img.shields.io/badge/Redis 7.4-DC382D?style=for-the-badge&logo=Redis&logoColor=white">
<img src="https://img.shields.io/badge/AWS S3-%23FF9900?style=for-the-badge&logo=amazon-web-services&logoColor=white">

---

### 3. 핵심 기능

| 구분 | 기능 | v1 |
| --- | --- | --- |
| User | 회원가입 | **O** |
| User | 로그인 | **O** |
| User | 소셜 로그인 | **X** |
| User | 로그아웃 | **O** |
| User | 프로필 조회 | **O** |
| User | 프로필 사진 | **X** |
| User | 팔로우/언팔로우 | **X** |
| User | 팔로잉/팔로우 목록 조회 | **X** |
| Post | 게시글 등록 | **X** |
| Post | 게시글 수정 | **X** |
| Post | 게시글 삭제 | **X** |
| Post | 게시글 좋아요 | **X** |
| Comment | 댓글 등록 | **X** |
| Comment | 댓글 수정 | **X** |
| Comment | 댓글 삭제 | **X** |
| Comment | 전체 댓글 목록 조회 | **X** |
| Admin | 전체 유저 목록 조회 (이름 검색) | **X** |
| Admin | 가입일 별 유저 수 조회 | **X** |
| Admin | 특정 유저의 계정 정지 | **X** |
| Admin | 계정 정지된 유저 해제 | **X** |
| Infra | 마이크로서비스 아키텍처 구현 | **X** |
| Infra | 서버 배포 | **X** |
| Infra | CI/CD 파이프라인 구축 | **X** |
| Test | Component 별 단위 테스트 | **X** |
| Test | 기능 별 통합 테스트 (HTTP 통신) | **O** |
| Other | 유저의 특정 활동에 대한 알림 제공 | **X** |

- **O** : 기능 구현 / **X** : 기능 미구현 / **◎** : 업그레이드 및 최적화

각 기능 별 세부적인 정책은 [노션 링크](https://hunzz.notion.site/moira-17d8fd4bde4b8013a4e1fd67815d4397?pvs=4)에서 확인하실 수 있습니다.

