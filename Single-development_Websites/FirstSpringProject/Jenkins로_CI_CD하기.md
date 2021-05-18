# Jenkins를 이용해 CI/CD하기
#### 젠킨스 전용 서버가 필요 하며 우분투 18.04가 설치된 EC2 인스턴스를 이용했다.
#### 어플리케이션은 라즈베리파이에서 실행한다.

---

## 1. 젠킨스 설치
* [공식홈페이지](https://www.jenkins.io/download/) 의 설명에 따라 설치한다.
* 보통 도커를 이용해 젠킨스를 구동하지만, 도커도 젠킨스도 익숙하지 않으므로 도커를 이용하지 않고 로컬에 설치 했다.
## 2. 기본 적인 설정
* [여기](https://yuddomack.tistory.com/entry/%EB%B0%B0%ED%8F%AC-%ED%94%84%EB%A1%9C%EC%84%B8%EC%8A%A4-%EA%B5%AC%EC%84%B1%ED%95%98%EA%B8%B03-jenkins%EB%A1%9C-%EB%B0%B0%ED%8F%AC-%EC%9E%90%EB%8F%99%ED%99%94) 를 참고 했다.
### 2.1 Timezone 설정
* Jenkins 설치 후 /etc/default/jenkins 파일을 열어 Timezone 설정을 한다.
> JENKINS_JAVA_OPTIONS="-Dorg.apache.commons.jelly.tags.fmt.timeZone=Asia/Seoul"
* 위와같이 한 줄 추가한다.
* 포트를 변경하고 싶으면 ```HTTP_PORT```항목을 수정하면 젠킨스 실행 포트가 변경된다.
* ```젠킨스url```:```port(Default:8080)```/systemInfo 에서 변경된 설정을 확인한다

### 2.2 깃헙과 연동
* 깃헙 웹훅을 이용해 특정 브랜치에 푸시가 되면 젠킨스 서버에서 변경된 코드를 빌드한다.
* SSH 인증 Key생성을 위해 jenkins 계정으로 변경한다.
```shell
sudo -u jenkins /bin/bash
```
* /var/lib/jenkins/ 디렉토리에 .ssh 디렉토리를 만든다.
```shell
$ mkidr /var/lib/jenkins/.ssh
```
* 방금 생성한 디렉토리에 ssh Key를 생성한다.
```shell
ssh-keygen -t rsa -f <인증키 이릅>
```
* 위 명령입력시 나오는것은 그냥 엔터엔터로 넘긴다.
* ```인증키이름.pub```, ```인증키이름``` 파일이 생성되는데 각각 퍼블릭 키, 프라이빗 키 이다.
* 젠킨스 페이지에서 Jenkins관리 -> Manage Credentials -> Domain의 glolbal 에서  Add Credential 로 이동해 위에서 생성한 Private Key를 추가해 준다.
* Pub키는 CI를 하고자하는 깃헙 레포지토리의 Settings => Deploy Key에 등록한다.
> 즉 Private Key는 젠킨스에 Public Key는 깃헙에
* 또, Settings에서 WebHook을 등록한다.
* Payload Url을 ```http://젠킨스Url:포트/github-webhook/```로 설정하고 Just the push event를 체크해 푸시이벤트에만 웹훅을 트리거 시킨다.

### 2.3 Job 생성
* 새로운 Item 텝을 눌러 새로운 Job을 생성한다.
* Freestyle Project로 생성한다.
* 소스코드 관리 탭에서 Repository URL은 위에서 CI를 하고자하는 깃헙의 SSH주소를 입력하고 Credential은 위에서 만들어준 Credential을 사용한다.
* Branches to build는 원하는 브랜치를 입력한다.
* 빌드 유발에서는 GitHub hook trigger for GITScm polling을 체크해 웹훅을 통해 빌드를 시작할 수 있게 한다.
* 이제 깃헙에 코드를 푸시하면 자동으로 빌드가 되며 빌드 결과물은 ```/var/lib/jenkins/workspace```에 위치한다.

## 3. 라즈베리파이 서버에 배포하기
* [이 블로그](https://goddaehee.tistory.com/259) 를 참고 했다. 
### 3.1 Publish Over SSH 플러그인 설정 
* 1. 젠킨스에서 Jenkins 관리 => 플러그인 관리 에서 ```Publish Over SSH``` 플러그인을 다운 받는다.
* 2. 다운완료후 Jenkins를 제시작 한다.
* 3. Jenkins 관리 => 시스템 설정 => Publish Over SSH 항목을 작성한다.