from re import X
import matplotlib.pyplot as plt # graph
import pandas as pd
from openpyxl import load_workbook # read 엑셀
import numpy as np
from scipy.interpolate import make_interp_spline # scipy - spline기법
from scipy.signal import find_peaks # scipy - peak 구함
from BaselineRemoval import BaselineRemoval # BaselineRemoval - baseline correction기법

# x축 선언
x = list(np.arange(2001)) # 0~2000
x1 = [i for i in range(0, 2001) if i%5==0] # 0~2000 (5의배수) => downsample 위해

# 1. 데이터 불러오기
file = './datas/ECPPG_2021-08-28_14-28-17.xlsx' # 원 데이터
df = pd.read_excel(file)

# 2. 데이터 가공하기(1)
# df.columns 를 ['0','1','2','3','4','5','6','7'] 로 변경
cols = list(df.columns) # 기존 columns 따로 기억
df.columns = ['0','1','2','3','4','5','6','7'] # columns 변경
new_data = { # 꼭 [] 넣어줘야 2차원 이상인걸로 알기때문에 적용해주기(1차원은 df안됨. Series임)
    '0' : [cols[0]], '1' : [cols[1]], '2' : [cols[2]], '3' : [cols[3]], '4' : [cols[4]], '5' : [cols[5]], '6' : [cols[6]], '7' : [cols[7]]
}
temp_df = df[0:-1] # 복제(마지막 값 그냥 제외했음)
new_df = pd.DataFrame(new_data) # 자료형 변환 : dict -> df
df = pd.concat([new_df, temp_df]) # 합치기 (아까 기록해둔 columns 합침)
df.reset_index(drop=True, inplace=True) # 기존 index 제거후 새로운 index로 초기화

# 2. 데이터 가공하기(2) => 5초간 데이터량 : 2000개
# 데이터 sample2 만들기 - ECG : 'G9600:G11600', PPG : 'C9600:C11600'
sample2_ECG = pd.DataFrame(data=df.iloc[9600:11601,6]) # df형식으로 초기화(ECG)
sample2_PPG = pd.DataFrame(data=df.iloc[9600:11601,2]) # df형식으로 초기화(PPG)
sample2_ECG.reset_index(drop=True, inplace=True) # 기존 index 제거후 새로운 index로 초기화
sample2_PPG.reset_index(drop=True, inplace=True) # 기존 index 제거후 새로운 index로 초기화

# 2. 데이터 가공하기(3) => 데이터 2000개 -> 400개로 downsample
# 데이터 sample3 만들기 - sample3는 downsample(sample2, 5) : index가 5의 배수인 값들 사용
sample3_ECG = pd.DataFrame() # df형식으로 초기화
sample3_PPG = pd.DataFrame() # df형식으로 초기화
for i in range(0, int(2005/5)): # 400개의 데이터가 추출 될것임
    sample3_ECG = pd.concat([sample3_ECG, sample2_ECG.iloc[i*5]]) # df 합치기
    sample3_PPG = pd.concat([sample3_PPG, sample2_PPG.iloc[i*5]]) # df 합치기
sample3_ECG.columns = ['6'] # [0]으로 열이름 바껴서 다시 원래 열이름인 '6'으로 바꾼거임
sample3_PPG.columns = ['6'] 
sample3_ECG.reset_index(drop=True, inplace=True) # 기존 index 제거후 새로운 index로 초기화
sample3_PPG.reset_index(drop=True, inplace=True) # 기존 index 제거후 새로운 index로 초기화

#################################### 데이터 불러오기 끝 ####################################

# 3. 데이터 필터링(1)
# spline 기법 - 데이터 곡선 (보간법)
y_int = make_interp_spline(x1, sample3_ECG) # 스플라인 -> sample3데이터 spline
y_int = y_int(x) # 스플라인 -> 스플라인한 데이터를 x축 데이터 크기와 맞춰주기
y_int2 = make_interp_spline(x1, sample3_PPG) # 스플라인 -> sample3데이터 spline
y_int2 = y_int2(x) # 스플라인 -> 스플라인한 데이터를 x축 데이터 크기와 맞춰주기

# 3. 데이터 필터링(2)
# Baseline correction - 기준선에 맞춰주는 기법
index_y = len(list(y_int)) # y_int의 길이 구하기
temp1 = y_int[0].tolist() # y_int데이터가 array로 요소마다 구성되어있어서 하나하나 tolist()
temp2 = (y_int2[0] *-1).tolist() # 데이터에 꼭 -1 곱해줘야함!! PPG 신호는 반전해서 봐야해서!

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
peaks1, _ = find_peaks(Zhangfit_output, distance=200) # , _ : 뒤에 받는 변수들 생략
peaks2, _ = find_peaks(Modpoly_output, distance=200) 
plt.figure(figsize=(10,5)) # 크기 설정 (너비 10, 높이 5)
plt.plot(x,Zhangfit_output)
plt.plot(x,Modpoly_output)
plt.plot(peaks1, Zhangfit_output[peaks1], "xr") # 피크!!
plt.plot(peaks2, Modpoly_output[peaks2], "xb") 

print(peaks1)
print(peaks2)

plt.show()

#################################### 데이터 필터링 끝 ####################################

# 4. 혈압 도출하기
peakSize = min(len(peaks1), len(peaks2))
count = 0
for i in range(peakSize):
    count += abs(peaks1[i] - peaks2[i])
count = count / peakSize

PTT = (1/400) * count * 1000
k1=-0.044
k2=-0.05522
b1 = 133.592 # 보정값
b2 = 99.07
SystolicBP = k1*PTT+b1 # 수축기 혈압(SP)
DiastolicBP = k2*PTT+b2 # 이완기 혈압(TP)
print(SystolicBP, DiastolicBP)