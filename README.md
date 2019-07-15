# 이미지 분석

  ## 1.개요

  Google Cloud Platform 의 Cloud Vision Api 를 사용한 이미지 분석을 할 수 있는 Android 어플 "이미지 분석" 입니다.

  ## 2. 개발 환경

  - OS : Mac Mojave( version : 10.14.5 )

  - 개발 언어 : Kotlin( version : 1.3.31 )

  - Tool : Android Studio ( version : 3.4.1 )

  - 외부 라이브러리 :
   |<center>Tool</center>  |<center>사용목적</center>  |<center>Version</center> |
   |:---------------------:|:-----------------------:|:-----------------------:|
   | **Google Vision API**  | Google Cloud Platform 에서 제공하는 이미지 분석기능을 사용하기위해 추가 | 1.23.0 |
   | **Google Map** | LANDMARK 분석시 해당 LANDMARK의 위치를 보여주기 위해 사용 | 16.0.0 |
   | **Glide** | 사용자의 이미지를 빠르고 좀더 효과적으로 보여주기 위해서 사용 | 4.9.0 |

   ### 어플 시연 영상
   [![IMAGE ALT TEXT](https://img.youtube.com/vi/euhvDLVBtgo/0.jpg)](http://www.youtube.com/watch?v=euhvDLVBtgo "어플 시연 영상")


## 3.이미지 분석
  #### - 이미지 분석 절차
  <img width="700px" height="300px" src="./app/src/main/res/drawable/dataAnalysis.png"></img>

  ####  - LABEL 분석
  <img width="300px" height="300px" src="./app/src/main/res/drawable/labelAnalysis.png"></img>

  LABEL 분석은 이미지에서 카테고리에 속하는 물체를 감지하여 관련 정보를 추출할 수 있다.
  라벨분석을 통해 물체, 장소, 활동, 동물종, 상품 등을 식별하여 막대 그래프로 분석결과를 통계적으로 보여준다.

####  - LANDMARK 분석
  <img width="300px" height="300px" src="./app/src/main/res/drawable/landmarkAnalysis.png"></img>

  LANDMARK(명소) 분석은 이미지에서 유명한 자연 경관과 인공 구조물을 감지해 위치를 분석한뒤 해당 LANDMARK(명소)의 위치를 Google 지도에 표시해준다.
