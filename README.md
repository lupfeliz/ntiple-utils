# NTIPLE UTILS

## 개요

- 본 라이브러리는 개발시 코드 분량이 늘어나는것을 막고자 만들어진 유틸 모음집 입니다.

- 다년간 프로젝트 수행하면서 매번 새로 만들어쓰던 동일 코드들 내용들을 사용하기 쉽게 모아봤습니다.

- 오로지 **코딩 분량을 줄이기 위한 꼼수** 만으로 만들어졌기에 퍼포먼스는 보장하지 않습니다.

- 프로젝트 개발시 **빠르게 프로트타입을 내기 위한 용도**로, 최적화 시에는 제거를 권장합니다.

- 정식 릴리즈 하기 전까지는 지속적인 변경이 예상되어 버젼간 호환성이 보장되지 않을수 있습니다.

- 본 라이브러리는 **가능한 경량화** (사이즈 경량화)를 추진할 것이며

- 되도록이면 다른 **라이브러리에 의존하지 않도록** 개발할 예정입니다.

- gradle 빌드 사용시 `build.gradle` 에 다음과 같이 추가한다.

```java
--- 중략 ---
repositories {
  maven {
    url 'https://nexus.ntiple.com/repository/maven-public'
    allowInsecureProtocol = true
  }
}
--- 중략 ---
implementation 'com.ntiple:ntiple-utils:0.0.2-6'
--- 중략 ---
```

- maven 빌드 사용시 `pom.xml` 에 다음과 같이 추가한다.

```xml
--- 중략 ---
  <repositories>
    <repository>
      <id>nexus.ntiple.com</id>
      <name>nexus.ntiple.com</name>
      <url>https://nexus.ntiple.com/repository/maven-public</url>
    </repository>
  </repositories>
  --- 중략 ---
  <dependencies>
    --- 중략 ---
    <dependency>
      <groupId>com.ntiple</groupId>
      <artifactId>ntiple-utils</artifactId>
      <version>0.0.2-6</version>
    </dependency>
    --- 중략 ---
  <dependencies>
--- 중략 ---
```

- 본 라이브러리는 자유로운 사용 및 수정, 재배포가 가능하며, 단. **사용시 원본출처 정도는 기억해 주세요**.