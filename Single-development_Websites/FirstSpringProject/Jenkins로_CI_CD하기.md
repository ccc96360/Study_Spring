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
* 기본 Timezone이 UTC로 설정되어 있어 불편하므로 변경한다.
* /etc/default/jenkins 파일을 열어 Timezone 설정을 한다.
> JENKINS_JAVA_OPTIONS="-Dorg.apache.commons.jelly.tags.fmt.timeZone=Asia/Seoul"
* 위와 같이 한 줄 추가한다.
* 포트를 변경하고 싶으면 ```HTTP_PORT```항목을 수정하면 젠킨스 실행 포트가 변경된다.
* 스프링 부트가 8080을 사용하므로 조심하기위해 8081로 변경했다.
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
* 위 명령입력시 나오는것은 그냥 엔터엔터로 넘긴다.(비밀 번호 설정을 패스하는것 이다.)
* ```인증키이름.pub```, ```인증키이름``` 파일이 생성되는데 각각 퍼블릭 키, 프라이빗 키 이다.
* 젠킨스 페이지에서 Jenkins관리 -> Manage Credentials -> Domain의 glolbal 에서  Add Credential 로 이동해 위에서 생성한 Private Key를 추가해 준다.
* Pub키는 CI를 하고자하는 깃헙 레포지토리의 Settings => Deploy Key에 등록한다.
> 즉 Private Key는 젠킨스에 Public Key는 깃헙에 둔다.
* 또, Settings에서 WebHook을 등록한다.
* Payload Url을 ```http://젠킨스Url:포트/github-webhook/```로 설정하고 Just the push event를 체크해 푸시이벤트에만 웹훅을 트리거 시킨다.

### 2.3 Job 생성
  
![이미지](https://raw.githubusercontent.com/ccc96360/ccc96360/main/images/studyspring/jenkins/%EC%9E%A1_%EC%B4%88%EA%B8%B0_%EC%84%A4%EC%A0%95.PNG)
  
1. 새로운 Item 텝을 눌러 새로운 Job을 생성한다.
2. Freestyle Project로 생성한다.
3. 소스코드 관리 탭에서 Repository URL은 위에서 CI를 하고자하는 깃헙의 SSH주소를 입력하고 Credential은 위에서 만들어준 Credential을 사용한다.
4. Branches to build는 원하는 브랜치를 입력한다.
5. 빌드 유발에서는 GitHub hook trigger for GITScm polling을 체크해 웹훅을 통해 빌드를 시작할 수 있게 한다.
6. 이제 깃헙에 코드를 푸시하면 자동으로 빌드가 되며 깃에서 풀 받은 파일들은 ```/var/lib/jenkins/workspace```에 위치한다.

## 3. 라즈베리파이 서버에 배포하기
* EC2 젠킨스 서버에서 빌드된 결과물을 라즈베리파이 서버로 배포한다.  
* [이 블로그](https://goddaehee.tistory.com/259) 를 참고 했다. 
### 3.1 Publish Over SSH 플러그인 설정 
1. 젠킨스에서 Jenkins 관리 => 플러그인 관리 에서 ```Publish Over SSH``` 플러그인을 다운 받는다.
2. 다운완료후 Jenkins를 제시작 한다.
3. Jenkins 관리 => 시스템 설정 => Publish Over SSH 항목을 작성한다.
  
![이미지](https://raw.githubusercontent.com/ccc96360/ccc96360/main/images/studyspring/jenkins/Publish_over_ssh%EC%84%A4%EC%A0%95%ED%98%B8%EB%A7%88%E3%85%95%E3%84%B4.PNG)
> 1. Passphrase, Path to Key는 건들지 않는다.
> 2. Key는 스프링 어플리케이션을 실행 시킬 서버(라즈베리파이)의 SSH rsa Private Key를 입력한다. (없다면 새로 만들어 주자)
> 3. Name은 본인이 식별 가능한 것으로 적절히 입력한다.
> 4. Hostname은 서버 IP주소이고 Username은 사용자명이다 즉 Username@Hostname 과 같다.
> 5. RemoteDirectory는 파일을 옮길 디렉토리이다.
> 6. 고급 버튼을 눌러서 Use password authentication, ~~ 을 체크해 User의 비밀번호를 입력해 준다.

### 3.2 Job 설정
1. ```빌드 후 조치```에서 Send build artifacts over SSH를 선택한다. 
2. 다음 그림과 같이 작성한다. 
  
![이미지](https://raw.githubusercontent.com/ccc96360/ccc96360/main/images/studyspring/jenkins/%EB%B9%8C%EB%93%9C%ED%9B%84Deploy%EC%9E%A1%EC%84%A4%EC%A0%95.PNG)
> 1. Sourcefile은 옮길 파일의 위치이다.
> 2. Remove prefix는 파일만 옮기기위해 파일 앞 디렉토리를 적어준다.
> 3. Exec Command는 원격지 에서 실행 시킬 명령을 입력한다. (deploy.sh처럼 스크립트를 만들어 배포받은 서비스를 실행 까지 할 수 있다.)
> 4. 명령이 실행되는 위치를 확인한 결과 user의 home이 초기값이다.
## 4. Deploy.sh 작성
* 배포한 스프링 부트 어플리케이션을 실행시키기 위해 Deploy.sh를 작성한다.
```shell
SCRIPT_DIR=/root/single-development-webservice/jenkins/
PROJECT_NAME=FirstSpringProject

echo "> 현재 구동중인 어플리케이션 pid 확인"
CURRENT_PID=$(pgrep -f ${PROJECT_NAME}.*.jar)

echo "> 현재 구동중인 어플리케이션 pid: $CURRENT_PID"
if [ -z "$CURRENT_PID" ]; then
    echo "> 현재 구동 중인 어플리케이션이 없으므로 종료하지 않습니다."
else
    echo "> kill -15 $CURRENT_PID"
    kill -15 $CURRENT_PID
    sleep5
fi

echo "> 새 어플리케이션 배포"
JAR_NAME=$(ls -tr $SCRIPT_DIR/ | grep jar | tail -n 1)

echo "> JAR Name: $JAR_NAME"

nohup java -jar \
-Dspring.config.location=classpath:/application.properties,\
/root/single-development-webservice/properties/application-oauth.properties,\
/root/single-development-webservice/properties/application-real-db.properties,\
classpath:/application-real.properties \
-Dspring.profiles.active=real \
$SCRIPT_DIR/$JAR_NAME>$SCRIPT_DIR/nohup.log 2>&1 &
```
* ```SCRIPT_DIR```은 jar파일과 deploy.sh파일의 위치이다.
* 현재 실행중인 어플리케이션을 종료시키고 새 어플리케이션을 실행 시킨다.