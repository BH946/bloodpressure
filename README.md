# Intro

**혈압 측정 웨어러블 및 앱 제작 프로젝트이다.**

**본인이 맡은 `소프트웨어` 부분과 `혈압 측정` 을 위한 논문 분석 및 혈압값 도출 과정을 중점으로 정리하겠다.**

* 앱 코드는 나중에 올리겠음@@@@@@@@@

<br><br>

# 혈압 측정 방법(bloodpressure.py)

* **용어 정리**
  1. BP(Blood Pressure) : 혈압
     1. SP(Systolic) : 수축기 혈압
     2. TP(Diastolic) : 이완기 혈압
     3. HR : 심장 박동수
  2. ECG(Electrocardiogram) : 심전도, 심장의 전기적 신호를 분석하여 심장의 수축 이완을 추론
  3. PPG(Photoplethysmogram) : 맥파 측정, 일반적으로 LED를 통해 빛을 조사하여, 빛의 양을 측정하여, 혈관의 수축 이완을 감지하여 심장의 수축이완을 추론
  4. PTT(Pulse Transmit Time) : 맥파 전달 시간, 두 동맥 박동처 사이를 맥파가 이동하는 데 걸리는 시간

* **[논문 분석 노션](https://www.notion.so/CheckMate-6c6baa1416c94617b1e541b9b0d05f46)**
  * **필수 논문**
    * 자세한 공식 내용 -> PPG 및 ECG 센서를 이용한 혈압추정 기법 개발.pdf
    * 수축기 혈압 공식 -> ECG와PPG를이용한실시간연속혈압측정시스템.pdf

<br>

## 혈압 도출 흐름

**MAX86150EFF+ 센서 -> PPG, ECG 도출 -> 데이터 필터링(spline, baseline correction) -> PTT 도출 -> SP, TP 도출(수축기, 이완기 혈압)**

* 혈압을 구하는 방식은 여러가지가 있지만, 우리가 구하는 방식은 PPG, ECG 신호가 필요한 방식이다.
* 단, 해당 신호 값들은 쓰레기값들이 있기 때문에 **데이터 필터링 과정**이 필요하다.

<br><br>

## 데이터 필터링

**참고로 일부분의 데이터를 날리고 여기서는 5:1의 비율로 데이터를 샘플링해서 데이터를 선별해서 사용한다. `(5:1 -> downsample활용)`**

**또한, 5초간의 데이터를 사용한다.**

* PTT 측정시 입력하는 신호의 양이 증가 할수록 PTT 값의 표준편차가 증가하게 되는 데 5초 이후부터 표준편차가 급격하게 증가하기 때문(논문속 내용입니다)
* 해당 프로젝트에서 전처리하는 데이터는 2000개(행) = 약 5초동안 측정된 데이터

<br>

**사용 라이브러리는??**

* **Scipy**
  * spline(보간법), peak(인덱스 반환) 사용
* **BaselineRemoval**
  * baseline correction 사용
* **Matplotlib**
  * 그래프로 시각화

<br>

### 1. spline 보간법

**spline 적용전(PPG 데이터)**

<img src=".\images\PPG.png" alt="image-20211111122525423" style="zoom:60%;" />

<br>

**위의 파란선 처럼 저런것들을 평탄화 시키기 위해서 spline 보간법 사용**

* 데이터가 더러워서 깔끔하게 만드는 용도

<br>

**spline 적용후**

<img src=".\images\PPG_Spline.png" alt="image-20211111122525423" style="zoom:60%;" />

<br>

### 2. BaselineRemoval 기법

**ECG 그래프(기법 적용전)**

<img src=".\images\ECG_Spline.png" alt="image-20211111122525423" style="zoom:60%;" />

<br>

위 그래프를 보면 굉장히 그래프가 위아래로 요동을 친다. 즉, 영점이 안맞다.

**이를 해결하기 위해 Baseline 기법을 사용한다는 것이다.**

* 데이터를 보고 하나의 기준선을 정해서 그 기준점을 기준으로 데이터를 재정렬 시키는것 (군에서 총기 영점잡는것과 같은 효과)
* **자세한 원리**
  * 주어진 스펙트럼은 (N) 범위로 나눈다.
  * 모든 범위에서 가장 낮은 지점을 결정한다.
  * 초기 기준선은 해당 지점으로 결정한다.
  * 이제 스펙트럼의 모든 지점은 가장 낮은 지점(2번에서 구한) 간의 차이로 그려진다.

<br>

**이를 적용한 최종 화면으로 바로 넘어가겠다.**

<br>

### 최종 데이터 필터링

<img src=".\images\최종_필터링.png" alt="image-20211111122525423" style="zoom:60%;" />

* 파란선 : ECG, 주황선 : PPG
  * 참고로 PPG의 경우 -1을 곱해서 반전시킨 모양을 가져야 한다.

<br>

**BaselineRemoval의 IModPoly, ModPoly, ZhangFit 메소드 중 ZhangFit 사용**

<br><br>

## 혈압 값 도출(PTT, SP, TP)

**심장이 뛰면 ECG는 전기신호라서 가장 높이 나타나는 피크가 보이고,  
PPG는 심장이 뛴 이후에 피가 말단(손가락)에 도달하면 피크 값이 발생!** 

**이때, PPG와 ECG에서 피크값을 찾고 두점(위 사진속 빨간,파란점)간의 시간차이를 구하면 혈압을 도출 할 수 있다.**

<br>

### PTT 도출 과정

**논문에서 확인한 결과 PTT를 구하려면 ECG, PPG의 피크값들이 필요하다.**

**각 그래프(ECG, PPG) peak(피크) 데이터들을 scipy의 peak함수를 통해서 바로 구해주고,  
서로간의 피크를 각각 비교해서 빼준다(PTT=큰값-작은값)** 

**이후 빼준 값들의 전체 평균을 구한다(=count)**

* EX) count=152.625
  * **PTT = 0.0025 * count * 1000** 

* 왜 0.0025와 1000을 곱하는가??
  * 처음에 downsample을 했던것을 기억해보면, 2000개 데이터에서 총 400개 데이터를 사용하게 되었기 때문이다(0.0025 == 1/400)
  * 1000을 곱하는 이유는 현제 데이터가 `00:28:22.200000` 마이크로 초 단위이기 때문에 1000을 곱해서 ms 단위로 바꿔준것이다.


<br>

### SP, TP 도출 과정

**이제 최종 혈압을 구한다.**

**논문에서 PTT와 혈압간의 상관성에 대해 분석한 결과로 `수축기 혈압= -0.044 · PTT + 133.592` 의 상관식을 도출해줬으므로 논문의 공식을 사용한다.**

* 수축기 혈압 : k(=-0.044) * PTT + 보정(=133.592)
* 이완기 혈압 : k(=-0.05522) * PTT + 보정(=99.07)

<br><br>

# 혈압 측정 앱

**아직 정리X**

<br><br>

# 혈압 측정 웨어러블

* **MAX86150 모듈 사용**

<img src=".\images\회로사진_실사.png" alt="회로사진_실사"  />   <img src=".\images\회로사진_도면.jpg" alt="회로사진_도면" style="zoom: 33%;" />      <img src=".\images\완성본.jpg" alt="완성본" style="zoom: 25%;" /> 

<br><br>

# Outro

**프로젝트 진행 기간 : `2021.3 ~ 2021.10`**

**코로나 이슈로 급하게 마무리**
