

# Intro



<br><br>

# 혈압값 구하는 과정(PTT구하는 공식 꼭 수정!!)



**내가 구하는 혈압 방식에는 PPG, ECG신호가 필요한데, 이를 구하기 위해선 데이터 필터링이 필요하다.**

* **why??** PPG, ECG신호가 처음 기계에서 올때 너무 많은 쓰레기값들이 있기 때문이다.

**혈압구하는 과정(필터링 완료되었다 가정)**

1. 필터링 된 데이터에서 피크값 추출해보기(peak : 최대값)
2. 피크값 추출한 것에서 혈압, 심박수 표기하기(가능하면 PPG, ECG 변화 그래프까지)





## 1. 데이터 필터링(Matlab)

* **매트랩(MATLAB)**을 통해서 데이터 필터링을 하는 과정을 나타낸다.



### 어떤흐름?

처음 웨어러블에 데이터는 너무 많은 데이터를 보내서 쓰레기 값이 많다. 따라서 spline과 baseline 등의 기법들을 통해서 가공을 해야하는 것이다.



#### spline 보간법이란

* 일부분의 데이터를 날리고 여기서는 5:1의 비율로 데이터를 샘플링해서 데이터를 선별하고
  sinc함수로 보간하는 거를 spline 보간법이라고 한다. (5:1 -> downsample활용)

![image-20211111122525423](C:\Users\KoBongHun\AppData\Roaming\Typora\typora-user-images\image-20211111122525423.png)

위의 파란선 처럼 저런것들을 평탄화 시키기 위해서 spline 보간법이라는것을 이용

데이터가 더러워서 깔끔하게 만드는 용도라고 이해하거나, 곡선(sin그래프처럼)나타낸다고 이해하자.

* 참고 : https://helloworldpark.github.io/jekyll/update/2017/02/04/Spline.html



#### baseline correction이란

![image-20211111122713653](C:\Users\KoBongHun\AppData\Roaming\Typora\typora-user-images\image-20211111122713653.png)

왼쪽것을 보면 그래프가 전체적으로 요동을 친다. 정확한 표현은 DC성분이 섞여서 그렇다. 이것은 광학 측정방식이다 보니까 어쩔수 없이 발생하는것이다.
따라서 데이터를 보고 하나의 기준선을 정해서 그 기준점을 기준으로 데이터를 재정렬 시키는것.
(군에서 총기 영점잡는것처럼 생각)

**아래처럼 생각해 볼 수 있겠다.(원리)**

1. 주어진 스펙트럼은 (N) 범위로 나눈다.
2. 모든 범위에서 가장 낮은 지점을 결정한다.
3. 초기 기준선은 해당 지점으로 결정한다.
4. 이제 스펙트럼의 모든 지점은 가장 낮은 지점(2번에서 구한) 간의 차이로 그려진다.



#### 최종 데이터 필터링 화면

![KakaoTalk_20211106_191633821](C:\Users\KoBongHun\Desktop\MarkDown\images\2022-02-12 Python_Matplot (프로젝트에서 이용한)\KakaoTalk_20211106_191633821.png)

=> 이런식으로 최종적으로 그래프가 그려지면 된다. (여기까지가 데이터 필터링)

* 파란선 : ECG, 주황선 : PPG



#### 데이터 필터링 이후,,

심장이 뛰면 ECG는 전기신호라서 가장 높이 나타나는 피크가 보이고 PPG는 심장이 뛴 이후에 피가 말단(손가락)에 도달하면 피크 값이 발생하는데 결론은 PPG와 ECG에서 피크값을 찾고 두점(빨간,파란점)간의 시간차이를 구하면 혈압을 도출 할 수 있다.



#### 코드 : example.m

```matlab
x=[0:1:2000]; %raw data sample 숫자 : 0 1 2 3 4 ... 2000 벡터공간 생성
x1=[0:5:2000]; %sampling 할 만큼으로 간격 조절 : 0 5 10 15 ... 2000 벡터공간 생성

file = 'ECPPG_2021-08-28_14-28-17.xlsx'
sample2 = readmatrix(file,'Range','G9600:G11600') %raw data sample 수
sample3 = downsample(sample2, 5) %down sampling 할 간격 : x1처럼 5단위로 데이터 저장

x_int=[0:1:2000] %보정해서 만들 데이터 샘플
y_int=spline(x1,sample3,x_int) %스플라인 보간법
y_int = reshape(y_int,[2001 1]) %baseline 함수가 [x; y;]형태라서 데이터형태를 종형으로 바꿔줌

Spectrum = [y_int] %없어도 됨

[Base, Corrected_Spectrum]=baseline(y_int);

x=[0:1:2000]; %raw data sample 숫자
x1=[0:5:2000]; %sampling 할 만큼으로 간격 조절

sample2 = readmatrix(file,'Range','C9600:C11600') %raw data sample 수
sample3 = downsample(sample2, 5) %down sampling 할 간격

x_int=[0:1:2000] %보정해서 만들 데이터 샘플
y_int2 = spline(x1,sample3,x_int) %스플라인 보간법
y_int2 = reshape(y_int2,[2001 1]) %baseline 함수가 [x; y;]형태라서 데이터형태를 종형으로 바꿔줌

[Base2, Corrected_Spectrum2]=baseline(y_int2.*-1);

figure(1)
clf
[pks,locs]=findpeaks(Corrected_Spectrum,'MinPeakDistance',200)
[pks2,locs2]=findpeaks(Corrected_Spectrum2,'MinPeakDistance',200)
plot(x,Corrected_Spectrum);
hold on
plot(x,Corrected_Spectrum2);
hold on
plot(x(locs),pks,'ob');
hold on
plot(x(locs2),pks2,'or');
hold on
%writematrix(Corrected_Spectrum,file,'Sheet',2,'Range','A1');
%writematrix(Corrected_Spectrum2,file,'Sheet',2,'Range','B1');
%writematrix(locs,file,'Sheet',2,'Range','C1');
%writematrix(locs2,file,'Sheet',2,'Range','D1');
```





## 1-1. 데이터 필터링(Python_Matplotlib)

* 기존에 매트랩으로 만들었는데, 파이썬의 Matplotlib 라이브러리를 활용해서 다시 만들어 보고싶어서 만들어 보았다.



#### Scipy패키지

* 그래프 다루기에 유용한 패키지라고 생각

**spline 기법 - 데이터 곡선**

* 참고 : https://www.delftstack.com/ko/howto/matplotlib/matplotlib-plot-smooth-curve/

**그래프 피크(peak)찾기 - 인덱스 반환**

* distance, prominence, width, threshold 속성중 prominence가 좋다고함.
  * 그러나 이 프로젝트에선 distance=200정도로 사용(이것도 좋다고 생각함)

* 참고 : https://stackovergo.com/ko/q/333081/peak-finding-algorithm-for-pythonscipy
             https://www.delftstack.com/ko/howto/python/find-peaks-in-python/



#### BaselineRemoval

* 그래프 다루기에 유용한 패키지라고 생각
* IModPoly, ModPoly, ZhangFit 메소드중 괜찮은걸로 사용. (각각 특징이 있음. 맘에드는걸로 사용)
* degree는 2가 제일 적당한데, 바꿔가면서 맘에드는걸로 사용해도 좋음.(ZhangFit은 사용안함)

```python
# BaselineRemoval 라이브러리 이용
polynomial_degree=2 #only needed for Modpoly and IModPoly algorithm
baseObj=BaselineRemoval(data)

Imodpoly_output=baseObj.IModPoly(polynomial_degree)
Modpoly_output=baseObj.ModPoly(polynomial_degree)
Zhangfit_output=baseObj.ZhangFit()
```



#### 최종 데이터 필터링 화면

* MATLAB과 거희 똑같지만 아주 약간 다른 부분들이 보이기는 한다. 아마도 baseline이나 spline보간법 활용에서 서로다른 라이브러리를 활용함으로써 약간의 오차가 발생하지 않았나 싶다.

![image-20220213225729477](C:\Users\KoBongHun\Desktop\MarkDown\images\2022-02-12 Python_Matplot (프로젝트에서 이용한)\image-20220213225729477-16447607342921.png)



#### 코드 : example.py

```python
from re import X
import matplotlib.pyplot as plt
import pandas as pd
from openpyxl import load_workbook # 데이터 읽기에 사용
import numpy as np
from scipy.interpolate import make_interp_spline # scipy패키지 활용해 spline기법 이용
from BaselineRemoval import BaselineRemoval # 해당 패키지 활용해 baseline correction
from scipy.signal import find_peaks # scipy를 통해 피크 또한 찾을 수 있음

# x축 먼저 선언
x = list(np.arange(2001)) # 0~2000
x1 = [i for i in range(0, 2001) if i%5==0] # 0~2000 (5의배수)

# 데이터 불러온것 df형식은 index는 0부터,, columns는 A열,B열,,구분위해 0,1,2,,로 설정함
# 1. 데이터 불러오기
file = 'ECPPG_2021-08-28_14-28-17.xlsx' # 원 데이터
df = pd.read_excel(file) # file 불러오기

# 2. 데이터 불러오기 - cols변수를 df에 첫번째 데이터로 삽입을 위해
cols = list(df.columns) # 기존 columns를 리스트로 담기(coulmns가 데이터로 구성되어서,,)
df.columns = ['0','1','2','3','4','5','6','7'] # columns를 변경해주기
new_data = { # 꼭 [] 넣어줘야 2차원 이상인걸로 알기때문에 적용해주기(1차원은 df안됨. Series임.)
    '0' : [cols[0]], '1' : [cols[1]], '2' : [cols[2]], '3' : [cols[3]], '4' : [cols[4]], '5' : [cols[5]], '6' : [cols[6]], '7' : [cols[7]]
}
temp_df = df[0:-1] # 복제
new_df = pd.DataFrame(new_data) # dict -> df
df = pd.concat([new_df, temp_df]) # 합치기
df.reset_index(drop=True, inplace=True) # 기존 index 제거후 새로운 index로 초기화


# 데이터 sample 만들기 - sample2는 ECG(아마도) : 'G9600:G11600', PPG(아마도) : 'C9600:C11600'
sample2_ECG = pd.DataFrame(data=df.iloc[9600:11601,6]) # df형식으로 초기화(ECG)
sample2_PPG = pd.DataFrame(data=df.iloc[9600:11601,2]) # df형식으로 초기화(PPG)
sample2_ECG.reset_index(drop=True, inplace=True) # 기존 index 제거후 새로운 index로 초기화
sample2_PPG.reset_index(drop=True, inplace=True) # 기존 index 제거후 새로운 index로 초기화

# 데이터 sample 만들기 - sample3는 downsample(sample2, 5) : index가 5의 배수마다 추출
sample3_ECG = pd.DataFrame() # df형식으로 초기화
sample3_PPG = pd.DataFrame() # df형식으로 초기화
for i in range(0, int(2005/5)): # 400개의 데이터가 추출 될것임
    sample3_ECG = pd.concat([sample3_ECG, sample2_ECG.iloc[i*5]]) # df 합치기
    sample3_PPG = pd.concat([sample3_PPG, sample2_PPG.iloc[i*5]]) # df 합치기
sample3_ECG.columns = ['6'] # '6'열 = G열로 생각하기 위해 열이름 변경
sample3_PPG.columns = ['6'] # '6'열 = G열로 생각하기 위해 열이름 변경
sample3_ECG.reset_index(drop=True, inplace=True) # 기존 index 제거후 새로운 index로 초기화
sample3_PPG.reset_index(drop=True, inplace=True) # 기존 index 제거후 새로운 index로 초기화


# spline 기법 - 데이터 곡선
# https://www.delftstack.com/ko/howto/matplotlib/matplotlib-plot-smooth-curve/
y_int = make_interp_spline(x1, sample3_ECG) # 스플라인 -> sample3데이터 spline
y_int = y_int(x) # 스플라인 -> 스플라인한 데이터를 x축 데이터 크기와 맞춰주기
y_int2 = make_interp_spline(x1, sample3_PPG) # 스플라인 -> sample3데이터 spline
y_int2 = y_int2(x) # 스플라인 -> 스플라인한 데이터를 x축 데이터 크기와 맞춰주기


# Baseline correction - 기준선에 맞춰주는 기법
index_y = len(list(y_int)) # y_int의 길이 구하기
temp1 = y_int[0].tolist() # y_int데이터가 array로 요소마다 구성되어있어서 하나하나 tolist()
temp2 = (y_int2[0] *-1).tolist() # 데이터에 꼭 -1 곱해줘야함!! PPG 신호 반전해서 봐야해서!

for i in range(1, index_y):
    castType1 = y_int[i].tolist()
    castType2 = (y_int2[i]*-1).tolist()
    temp1.append(castType1[0]) # 리스트로 타입 변환(BaselineRemoval은 list형태 타입을 원함)
    temp2.append(castType2[0])

# BaselineRemoval 라이브러리 이용
polynomial_degree=2 #only needed for Modpoly and IModPoly algorithm

baseObj1=BaselineRemoval(temp1)
baseObj2=BaselineRemoval(temp2)
# Imodpoly_output=baseObj2.IModPoly(polynomial_degree)
Zhangfit_output=baseObj1.ZhangFit() # ECG
Modpoly_output=baseObj2.ModPoly(polynomial_degree) # PPG


# 그래프 피크찾기 (변수 peaks2:피크인 인덱스를 반환)
# https://stackovergo.com/ko/q/333081/peak-finding-algorithm-for-pythonscipy
# https://www.delftstack.com/ko/howto/python/find-peaks-in-python/
peaks1, _ = find_peaks(Zhangfit_output, distance=200) # , _ : 뒤에 받는 변수들 생략
peaks2, _ = find_peaks(Modpoly_output, distance=200) 
plt.figure(figsize=(10,5)) # 크기 설정 (너비 10, 높이 5)
plt.plot(x,Zhangfit_output)
plt.plot(x,Modpoly_output)
plt.plot(peaks1, Zhangfit_output[peaks1], "xr") # 피크!!
plt.plot(peaks2, Modpoly_output[peaks2], "xb") 

plt.show()

# 데이터 필터링 끝
```





## 2. 혈압값 구하기(최종)

* 조금,, 주저리 주저리 쓰겠다.



#### 여기까지의 과정

**5초의 데이터를 사용하는 이유?**
PTT 측정시 입력하는 신호의 양이 증가 할수록 PTT 값의 표준편차가 증가하게 되는 데 5초 이후부터 표준편차가 급격하게 증가 하는 것이 분석 되어 졌기 때문이다.즉, 논문에서는 5초동안의 데이터가 가장 정확.
해당 프로젝트에서 측정한 데이터는 2000개(행) = 약 5초동안 측정된 데이터 임. 따라서 2000개 Data사용.

**이 데이터는 필터링 과정을 거쳐줘야 함.**
데이터 필터링을 위해서 downsample을 통해 1:5로 만들고, spline보간법 사용 및 baseline까지.



#### 혈압값 도출하기

**논문에서 확인한 결과 PTT구하려면 ECG, PPG 피크값들이 필요함.**
먼저, 피크값을 구한다.(위의 .py 나 .m 에서 한줄 추가하면 되겠죠)

**PTT를 구한다.**
각 그래프(ECG, PPG) peak(피크) 데이터들 5개정도씩 나타났으며, 이 피크들을 비교해서 빼준다(PTT=큰값-작은값) 그리고나서 빼준 값들의 전체 평균을 구한다 = count(변수이름)

* EX) count=152.625
* PTT = 0.0025 * count * 1000 
* 왜 위의 count에서 끝나지 않고 400을 나눴나??(0.0025 == 1/400) 그것은 나도 기억이안나서 의문임. 이거 아마 sample3 데이터 400개라서 그럴걸? 그래서 곱해서 적용해준겨.
  1000은 아마 ms으로 만들기 위함인듯. 왜? 데이터가 00:28:22.200000 이런식으로 마이크로초 단위로 있기 때문.

**혈압을 구한다.**
논문에서 PTT와 혈압간의 상관성에 대해 분석 한 결과로 수축기 혈압= -0.044 · PTT + 133.592의 상관식을 도출. 이를 통해서 아래 식으로 구했다.

* 수축기 혈압은 k(=-0.044) * PTT + 보정(=133.592)
* 이완기 혈압도 k(=-0.05522) * PTT + 보정(=99.07)



# 마치며,,

https://www.notion.so/CheckMate-6c6baa1416c94617b1e541b9b0d05f46
=> 선배들과 정리한 노션.(논문들 확인할 수 있음)

**참고**

공식들이 자세히,, -> PPG 및 ECG 센서를 이용한 혈압추정 기법 개발.pdf
수축기 혈압 공식 -> ECG와PPG를이용한실시간연속혈압측정시스템.pdf
