# 코드가 Git에 푸시 되면 자동으로 배포해 보자.
#### Travis CI를 이용해 배포 자동화를 해본다.
* master 브랜치가 푸시되면 배포가 자동으로 이루어지는환경을 구축해본다.

---
## 1. CI, CD 란?
* CI(Continuous Integration - 지속적 통합) 란 일반적으로 코드 버전 관리를 하는 VCS시스템 (Git, svn 등)에 푸시가 되면 자동으로 테스트와 빌드가 수행되어 ```안정적인 배포 파일을 만드는 과정```이다.
* CD(Continuous Deployment -지속적인 배포) 란 CI의 빌드 결과를 자동으로 운영 서버에 무중단 배포까지 진행되는 과정이다.
* 일반적으론 CI만 구축되어 있지는 않고, CD도 함께 구축된 경우가 대부분이다.

### 1.1 마틴 파울러(Martin Fowler)가 말하는 CI의 4가지 규칙
> * 모든 소스코드가 살이 있고 누구든 현재의 소스에 접근할 수 있는 단일 지점을 유지할 것
> * 빌드 프로세스를 자동화해서 누구든 소스로부터 시스템을 빌드하는 단일 명령어를 사용할 수 있게 할 것
> * 테스팅을 자동화해서 단일 명령어로 어네든지 시스템에 대한 건전한 테스트 수트를 실행할 수 있게 할것
> * 누구나 현재 실행 파일을 얻으면 지금까지 가장 완전한 실행 파일을 얻었다는 확신을 하게 할 것
* 여기서 특히 중요한 것은 ```테스팅 자동화```이다.
* 지속적으로 통합하기 이해서는 무엇보다 이 프로젝트가 ```완전한 상태임을 보장하기 위해``` 테스트 코드가 구현되어 있어야만 한다.

---
## 2. Travis CI 연동하기
* Travis CI는 깃허브에서 제공하는 무료 CI 서비스이다.
### 2.1 설정
* [Travis CI 홈페이지](https://travis-ci.com) 로 이동한다.
* Github으로 로그인 후 Settings에서 현재 프로젝트가 올라간 리포지토리를 선택한다.

### 2.2 프로젝트 설정  
* Travis CI의 상세한 설정은 프로젝트에 존재하는 ```.travis.yml``` 파일로 할 수 있다.
* 깃헙 리포지토리 루트에 다음과 같이 .travis.yml 파일을 생성한다.
```yml
language: java
jdk:
  - openjdk8

branches:
  only:
    - main
before_install:
  - cd ./Single-development_Websites/FirstSpringProject/
  - chmod +x gradlew
# Travis CI 서버의 Home
cache:
  directories:
    - '$HOME/.m2/repository'
    - '$HOME/.gradle'

script: "./gradlew clean build"

# CI 실행 완료 시 메일로 알람
notifications:
  email:
    recipients:
      - 이메일 주소
```
* Travis CI가 가상 환경에서 돌아가는데 ```pwd```가 깃헙 레포의 루트라고 생각해야한다.
* 현재 이 프로젝트는 루트에서 /Single-deve...../FirstSpring..../ 이므로 ```before_install:```에서 디렉토리 이동을 해야 한다.
* 또한 gradlew가 권한 부족으로 에러가 나므로 권한도 추가 해주어야한다.
* 이제 프로젝트를 Push하면  자동으로 빌드가 된다.

## 3. Travis CI 와 AWS S3 연동하기
* Travis CI에서 빌드된 결과물을 S3로 업로드 한다.
### 3.1 IAM 사용자 생성
* 액세스 유형: 프로그래밍 방식 액세스
* 권한 설정: ```AWSCodeDeployFullAccess```, ```AmazonS3FullAccess``` 선택
* 태그 키:값 아무거나 본인이 인지 가능한 정도로 만든다.
* 사용자 추가 성공시 ```액세스 키 ID```, ```비밀 액세스 키```가 생성된다. 이는 Travis CI에서 사용될 키 이다.

### 3.2 키등록
* Travis CI의 설정 화면으로 이동해 환경 변수에 등록한다.
* Environment Variables에 ```AWS_ACCESS_KEY```와 ```AWS_SECRET_KEY```를 변수로해 키값들을 등록한다.
* 이 환경 변수에 등록된 값들은 ```$변수명```으로 ```.travis.yml```에서 접근 가능하다.

### 3.3 S3 버킷 생성
* 모든 퍼블릭 액세스가 차단된 버킷을 만든다.

### 3.4 .travis.yml 수정
* 아래와 같은 코드를 추가한다.
```yaml
before_deploy:
  - pwd
  - zip -r single-development-webservice *
  - mkdir -p deploy
  - mv single-development-webservice.zip deploy/single-development-webservice.zip

deploy:
  - provider: s3
    access_key_id: $AWS_ACCESS_KEY
    secret_access_key: $AWS_SECRET_KEY
    bucket: single-development-webservice
    region: ap-northeast-2
    skip_cleanup: true
    acl: private
    local_dir: deploy
    wait-unitl-deployed: true
    on:
      all_branches: true
```
> beforels_deploy에서 ```pwd```로 찍어본 위치
> * /home/travis/build/ccc96360/Study_Spring/Single-development_Websites/FirstSpringProject

* 위 코드를 추가 후 푸시하면 S3에 ```single-development-webservice.zip``` 이 추가 된것을 확인 할 수있다.
* 추가적으로, ```Skipping a deployment with the s3 provider because this branch is not permitted``` 오류가 발생 했을 경우 아래와 같은 옵션을 추가하니 해결 되었다.
```yaml
    on:
      all_branches: true
```

## 4. CodeDeploy 연동하기
* [여기](https://aws.amazon.com/ko/blogs/devops/automating-deployments-to-raspberry-pi-devices-using-aws-codepipeline/) 를 참고 하며 진행했다.
* [여기2](https://docs.aws.amazon.com/ko_kr/codedeploy/latest/userguide/getting-started-provision-user.html)
### 4.1 온프레미스 인스턴스 생성
* 다음과 같이 awscli를 다운받고 설정을 한다.
```shell
apt install awscli
aws configure
```
* configure에 Travis CI에 등록한 IAM Key값들을 입력해 주었다.
```shell
aws deploy register --instance-name rpi4UbuntuServer --iam-user-arn arn:aws:iam::<IAM ARN값>:user/Rpi --tags Key=Name,Value=Rpi4 --region ap-northeast-2
```
* <IAM ARN값>은 IAM 사용자의 ```사용자 ARN값```에 있다.
* arn:aws:iam::112345678901:user/~~~~~~~~~이런식으로 있으면  ```112345678901```만 사용했다.
* 위의 명령어가 정상적으로 실행 되면 다음과 같이 인스턴스가 생성 됐음을 확인 할 수 있다.
```shell
aws deploy list-on-premises-instances
{
    "instanceNames": [
        "rpi4UbuntuServer"
    ]
}
```
* 생성된 인스턴스는 웹에서도 확인 가능하다
* 코드디플로이 에이전트는 아래와 같이 다운 받는다.
```shell
wget https://aws-codedeploy-ap-northeast-2.s3.ap-northeast-2.amazonaws.com/latest/install
sudo ./install auto 
```
* 다음과 같은 명령어로 제대로 설치 되었는지 확인한다.
```shell
service codedeploy-agent status
```

### 4.2 앱 생성
* CodeDeploy 서비스를 선택해 IAM 역할을 생성한다. (역할 이름: CodeDeployRoleforRpi4)
* CodeDeploy에서 애플리케이션을 생성한다. (애플리케이션 이름: single-development-webservice)
* 생성이 완료되면 배포그룹을 생헝한다. 이때 서비스 역할의 ARN은 방금 만들어준 역할을 입력한다. 
