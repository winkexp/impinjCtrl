# ImpinjCtrl


使用Impinj Octane SDK，控制 Impinj Speedway R420 的開始與結束讀卡。

執行
===================
java -DreaderHost=IMPINJ_IP -jar out/impinjCtrl.jar

其他選填選項：
-DtxPowerDbm=1.0  設定天線功率
-DdebugMode=0   開/關debug模式（report包含天線強度等資訊）

指令
===================
連上Reader之後，直接輸入下列指令：
START   开始读取
STOP    結束連線
STATUS  检查Reader狀態

結束
===================
於Shell輸入STOP，按enter。
