# rss_reader_challenge
프로그래머스 마이리얼트립 Kotlin 챌린지

## 개발 정보
- 언어 : Kotlin
- 개발툴 : Android Studio
- 사용 라이브러리 : 
  1) Picasso (빠른 이미지 로딩을 위해) -> 링크 : https://github.com/square/picasso 
  2) Jsoup (Meta Property를 쉽게 파싱하기 위해) -> 링크 : https://jsoup.org/download

## 앱 화면 스크린샷
<div>
<p>뉴스 본문 로딩 화면</p>
<img width="200" src="https://user-images.githubusercontent.com/36183001/77622429-9afda580-6f81-11ea-897b-a96cdb88880d.jpg">
  &nbsp;&nbsp;
  
<p>뉴스 본문 화면</p>
<img width="200" src="https://user-images.githubusercontent.com/36183001/77622617-f2037a80-6f81-11ea-937b-043035ee7580.jpg">
  &nbsp;&nbsp;
  
<p>메인 뉴스 로딩 화면</p>
<img width="200" src="https://user-images.githubusercontent.com/36183001/77622637-f891f200-6f81-11ea-9663-4ccd80d11f64.jpg">
  &nbsp;&nbsp;
  
<p>메인 뉴스 화면</p>
<img width="200" src="https://user-images.githubusercontent.com/36183001/77622645-faf44c00-6f81-11ea-9291-ffe156989763.jpg">
  &nbsp;&nbsp;
  
<p>스플래시 화면</p>
<img width="200" src="https://user-images.githubusercontent.com/36183001/77622647-fb8ce280-6f81-11ea-9e2d-f81731dafaa0.jpg">
  &nbsp;&nbsp;
  
<p>이미지 로딩 중 화면</p>
<img width="200" src="https://user-images.githubusercontent.com/36183001/77622649-fc257900-6f81-11ea-85aa-d6453537f3e0.jpg">
  &nbsp;&nbsp;
</div>
<br>

## 기능 설명
### 스플래시 화면
- 앱 실행시 스플래시 화면이 보여짐
- 1.3초 후에 뉴스 리스트 화면으로 이동

### 메인 화면 (구글 한글 뉴스 링크 소스를 사용함 : https://news.google.com/rss?hl=ko&gl=KR&ceid=KR:ko)
- 뉴스 항목(item) 클릭 시 뉴스 상세보기 화면으로 이동
- 리스트를 당겨서 새로고침 가능
- 키워드 추출 방법 :
0) RSS에서 제공하는 뉴스 link의 og:description의 본문 내용을 기반으로 키워드 추출
1) 띄어쓰기 단위로 단어 추출
2) 영어 소문자, 대문자, 숫자 0~9, 한글을 제외한 모든 문자 제거
3) 추출한 단어에 대해서 빈도수 측정
4) 정렬 (1 -> 빈도순, 2 -> 문자열 오름차순)

### 뉴스 본문 화면
- 뉴스의 제목, 키워드 3개가 화면 상단에 나타남
- WebView를 사용해 뉴스의 본문을 보여줌
- WebView 내에서 링크 이동 후 뒤로가기 버튼 클릭 시 WebView의 이전 링크로 이동 (이전 링크가 없다면 메인 Activity로 전환)
