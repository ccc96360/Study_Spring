# AWS 환경 세팅하기
#### AWS로 서버를 만들어 본다.

### 클라우드 서비스란?
* 쉽게 말해 클라우드(인터넷)를 통해 서버, 스토리지, DB, 네트워크, S/W, 모니터링 등의 컴퓨팅 서비스를 제공하는 것이다.
* 단순히 물리적인 장비를 대여하는 것이 아니라, 로그관리, 모니터링, 하드웨어 교체, 네트워크 관리등을 기본적으로 지원한다.
* 즉, 개발자가 직접해야 할 일을 AWS가 전부 지원 하는것 이다.
---
### 클라우드의 형태
1. Infrastructur as a Service(IaaS)
* 기존 물리 장비를 미들웨어와 함께 묶어둔 추상화 서비스이다.
* 가상머신, 스토리지, 네트워크, OS등의 IT인프라를 대여해 주는 서비스라고 보면 된다.
* 종류: AWS의 EC2, S3등

2. Platform as a Service(PaaS)
* IaaS에서 한 번 더 추상화한 서비스이다.
* 한 번 더 추상화했기 때문에 많은 기능이 자동화되어 있다.
* 종류: AWS의 Beanstalk, Heroku 등

3. Software as a Service(SaaS)
* 소프트웨어 서비스이다.
* 구글 드라이브, 드랍박스, 와탭등
---
## 1. EC2 인스턴스 생성
### 1.1 AMI(Amazon Machin Image)선택
* 아마존 리눅스 2 AMI를 선택한다.

### 1.2 인스턴스 유형 선택
* t2.micro를 선택한다.
* t2는 요금 타입 micro는 사양이다.
* t2이외에도 t3가 있으며 보통 T시리즈라고 한다. 다른 시리즈는 nano,micro같은 저사양이 존재 하지 않는다.
### 1.3 스토리지 추가
* 프리티어는 30GB가 최대이므로 30GB 로 설정한다.
### 1.4 태그 추가
* 인스턴스가 여러개인 경우 태그별로 구분하면 검색이나 그룹 짓기가 편하다.
### 1.5 보안 그룹 구성
* 보안 그룹은 방화벽이다.
* ssh 접속은 내 IP로 한다.
* 이제 설정은 끝났다.
--
## 2. 고정 IP할당
* AWS에선 고정 IP를 EIP(Elastic IP, 탄력적IP)라고 한다.
* EC2 인스턴스에서 탄력적 IP를 할당한다.
* 생성 후 할당 버튼 옆 작업 버튼에서 주소연결 버튼을 누른다.
* 이동한 페이지에서 인스턴스를 선택한다.
* 인스턴스 목록에서 연결이 잘 되었는지 확인한다.

---
## 3. 기본 세팅
* 자바설치
```shell
$ sudo yum install -y java-1.8.0-openjdk-devel.x86_64
```
* 타임존 변경
```shell
$ sudo rm /etc/localtime
$ sudo ln -s /usr/share/zoneinfo/Asia/Seoul /etc/localtime
```
* HostName 변경
```shell
변경 하기
$ sudo hostnamectl set-hostname single-development-service
변경 확인
$ hostname
변경 됐으면 리부트
$ sudo reboot
```
* /etc/hosts에 hostname 등록
