# -*- coding: utf-8 -*-
"""
Created on Sat Mar 28 16:47:26 2020

@author: salahga
"""

# -*- coding: utf-8 -*-
"""
Spyder Editor

This is a temporary script file.
"""

import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from matplotlib.pyplot import *
from matplotlib import dates
import scipy.stats as stats
import datetime
import os

from collections import deque
from io import StringIO


TestResultBase = r'C:\Users\salahga\Desktop\vmsharingspace\ubuntu180404\BMs'
os.chdir(TestResultBase)

TestResultVersion = 1

MbcTestNum      = 10
CoreNums        = 4

RunMode         = 'Directed'
RunMode         = 'Regression'

DirectedTCNum   = 0

TestCaT         = 'mbc' # or 'splash'

MbcDirName   = 'eembc-traces'
SpashDirName = 'Splash'

MbcTestCnt = 9
SplashTestCnt = 11

MbcTests = ['a2time01-trace',
           'aifirf01-trace',
           'basefp01-trace',
           'cacheb01-trace',
           'empty-trace',
           'iirflt01-trace',
           'pntrch01-trace',
           'rspeed01-trace',
           'ttsprk01-trace']

SplashTests =['barnes',
             'cholesky',
             'fft',
             'fmm',
             'lu_non_contig',
             'ocean',
             'radiosity',
             'radix',
             'raytrace',
             'volrend',
             'water_nsquared',
             'water_spatial']


AverageLatency = []
WcLatency = []
WcReplcLatency = []
WcLatencyMinusRepl = []
WcRespLatency = []
WcReqLatency = []
WcReplcLatency = []

if RunMode == 'Directed':
    TCNums = 1
else:
    if TestCaT == 'mbc':
        TCNums = MbcTestCnt
    else:
        TCNums = SplashTestCnt
    
for ii in range (TCNums):
    averageLatency = 0
    wcLatency = 0
    WclatencyMinusRepl = 0
    wcRespLatency = 0
    wcReqLatency = 0
    wcReplcLatency = 0
    for pp in range (CoreNums):
        
        if TestCaT == 'mbc':
            BaseDirName = MbcDirName
            my_xticks = ['a2time01','aifirf01','basefp01','cacheb01','empty','iirflt01','pntrch01','rspeed01','ttsprk01']
            x = np.array([0,1,2,3,4,5,6,7,8])
            
            if RunMode == 'Directed':
                TCName = MbcTests[DirectedTCNum] 
            else:
                TCName = MbcTests[ii] 
        else:
            BaseDirName = SpashDirName
            my_xticks = ['a2time01','aifirf01','basefp01','cacheb01','empty','iirflt01','pntrch01','rspeed01','ttsprk01']
            x = np.array([0,1,2,3,4,5,6,7,8])
            if RunMode == 'Directed':
                TCName = SplashTests[DirectedTCNum] 
            else:
                TCName = SplashTests[ii]

        TCPath = BaseDirName + '/' + TCName + '/' + str(TestResultVersion) 
        TraceFile = TCPath + '/LatencyReport_C' + str(pp) + '.csv'
        with open(TraceFile,'r') as f:
            q = deque(f,2)
            
        df =pd.read_csv(StringIO(''.join(q)), names=["ReqId","ReqAddr","RefTraceCycle","ReqBusLatency","RespBusLatency","ReplcLatency","LatencyMinusRepl","TotLatency","WcReqLatency","WcRespLatency","WcReplcLatency","WcLatency","WcLatencyMinsRepl","AverageLatency"])

        averageLatency = averageLatency + df.iloc[-1].AverageLatency
        if wcLatency < df.iloc[-1].WcLatency:
            wcLatency = df.iloc[-1].WcLatency
        if WclatencyMinusRepl < df.iloc[-1].WcLatencyMinsRepl:
            WclatencyMinusRepl = df.iloc[-1].WcLatencyMinsRepl
        if wcRespLatency < df.iloc[-1].WcRespLatency:
            wcRespLatency = df.iloc[-1].WcRespLatency
        if wcReqLatency < df.iloc[-1].WcReqLatency:
            wcReqLatency = df.iloc[-1].WcReqLatency
        if wcReplcLatency < df.iloc[-1].WcReplcLatency:
            wcReplcLatency = df.iloc[-1].WcReplcLatency
        
    AverageLatency.append(averageLatency/CoreNums)
    WcLatency.append(wcLatency)
    WcLatencyMinusRepl.append(WclatencyMinusRepl)
    WcRespLatency.append(wcRespLatency)
    WcReqLatency.append(wcReqLatency)
    WcReplcLatency.append(wcReplcLatency)

plt.figure(11)
h = plt.bar(x, AverageLatency, label=my_xticks, width=0.3, color = 'orange')
plt.subplots_adjust(bottom=0.3)
xticks_pos = [0.65*patch.get_width() + patch.get_xy()[0] for patch in h]
plt.xticks(xticks_pos, my_xticks,  ha='right', rotation=45)
plt.title('Mbc Benchmarks Average Performance')
plt.grid(color='black', linestyle='-.', linewidth=0.7)

plt.figure(12)
h = plt.bar(x, WcLatencyMinusRepl, label=my_xticks, width=0.3, color = 'orange')
plt.subplots_adjust(bottom=0.3)
xticks_pos = [0.65*patch.get_width() + patch.get_xy()[0] for patch in h]
plt.xticks(xticks_pos, my_xticks,  ha='right', rotation=45)
plt.title('Mbc Benchmarks WC Latency (Without Replacement)')
plt.grid(color='black', linestyle='-.', linewidth=0.7)

plt.figure(13)
h = plt.bar(x, WcLatency, label=my_xticks, width=0.3, color = 'orange')
plt.subplots_adjust(bottom=0.3)
xticks_pos = [0.65*patch.get_width() + patch.get_xy()[0] for patch in h]
plt.xticks(xticks_pos, my_xticks,  ha='right', rotation=45)
plt.title('Mbc Benchmarks WC Latency')
plt.grid(color='black', linestyle='-.', linewidth=0.7)

plt.figure(14)
h = plt.bar(x, WcRespLatency, label=my_xticks, width=0.3, color = 'orange')
plt.subplots_adjust(bottom=0.3)
xticks_pos = [0.65*patch.get_width() + patch.get_xy()[0] for patch in h]
plt.xticks(xticks_pos, my_xticks,  ha='right', rotation=45)
plt.title('Mbc Benchmarks WC Response Latency')
plt.grid(color='black', linestyle='-.', linewidth=0.7)

plt.figure(15)
h = plt.bar(x, WcReqLatency, label=my_xticks, width=0.3, color = 'orange')
plt.subplots_adjust(bottom=0.3)
xticks_pos = [0.65*patch.get_width() + patch.get_xy()[0] for patch in h]
plt.xticks(xticks_pos, my_xticks,  ha='right', rotation=45)
plt.title('Mbc Benchmarks WC Request Latency')
plt.grid(color='black', linestyle='-.', linewidth=0.7)

plt.figure(16)
h = plt.bar(x, WcReplcLatency, label=my_xticks, width=0.3, color = 'orange')
plt.subplots_adjust(bottom=0.3)
xticks_pos = [0.65*patch.get_width() + patch.get_xy()[0] for patch in h]
plt.xticks(xticks_pos, my_xticks,  ha='right', rotation=45)
plt.title('Mbc Benchmarks WC Replacement Latency')
plt.grid(color='black', linestyle='-.', linewidth=0.7)


#plt.show()
#plt.stem(WcLatency)
#plt.show()

    #    fig = plt.figure()
    #    sub1 = fig.add_subplot(3,1,1)
    #    sub2 = fig.add_subplot(3,1,2)
    #    sub3 = fig.add_subplot(3,1,3)
    #    sub1.hist(df.TotLatency,bins=100,color = 'b',alpha=1)
    #    sub1.set_title('PISCOT CPU ' + str(pp) + ' Total Latency')
    #    sub2.hist(df.ReqBusLatency,bins=100,color = 'k',alpha=1)
    #    sub2.set_title('PISCOT CPU ' + str(pp) + ' ReqBus Latency')
    #    sub3.hist(df.RespBusLatency,bins=100,color = 'k',alpha=1)
    #    sub3.set_title('PISCOT CPU ' + str(pp) + ' RespBus Latency')
    #    sub1.grid()
    #    sub2.grid()
    #    sub3.grid()

